package org.example.paperlessservices.it;
import io.minio.MakeBucketArgs;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;

import io.minio.MinioClient;
import org.awaitility.Awaitility;
import org.example.paperlessservices.PaperlessServiceApplication;
import org.example.paperlessservices.entity.Document;
import org.example.paperlessservices.entity.DocumentStatus;
import org.example.paperlessservices.repository.DocumentRepository;
import org.junit.jupiter.api.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(
        classes = PaperlessServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class OcrConsumerIntegrationIT {

    private static final Network NET = Network.newNetwork();

    @Container
    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3.13-management")
            .withNetwork(NET)
            .withExposedPorts(5672, 15672);

    @Container
    static final GenericContainer<?> MINIO = new GenericContainer<>("minio/minio:RELEASE.2024-08-17T01-24-54Z")
            .withNetwork(NET)
            .withEnv("MINIO_ACCESS_KEY", "testkey")
            .withEnv("MINIO_SECRET_KEY", "testsecret")
            .withCommand("server /data --console-address :9001")
            .withExposedPorts(9000, 9001)
            .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forLogMessage(".*API: http://.*:9000.*\\n", 1));

    static MinioClient minio;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        // Rabbit
        r.add("spring.rabbitmq.host", RABBIT::getHost);
        r.add("spring.rabbitmq.port", () -> RABBIT.getMappedPort(5672));
        r.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        r.add("spring.rabbitmq.password", RABBIT::getAdminPassword);

        // MinIO
        r.add("paperless.minio.endpoint", () -> "http://" + MINIO.getHost() + ":" + MINIO.getMappedPort(9000));
        r.add("paperless.minio.accessKey", () -> "testkey");
        r.add("paperless.minio.secretKey", () -> "testsecret");
        r.add("paperless.minio.secure", () -> "false");
        r.add("paperless.minio.bucket", () -> "documents");
    }

    @Autowired
    RabbitTemplate rabbit;

    @Autowired
    DocumentRepository repo;

    @BeforeAll
    static void initMinio() throws Exception {
        minio = MinioClient.builder()
                .endpoint("http://" + MINIO.getHost() + ":" + MINIO.getMappedPort(9000))
                .credentials("testkey", "testsecret")
                .build();

        var buckets = minio.listBuckets();
        var exists = buckets.stream().anyMatch(b -> b.name().equals("documents"));
        if (!exists) {
            minio.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket("documents")
                            .build()
            );
        }

    }

    @Test
    void consumer_processes_message_and_updates_db() throws Exception {
        // arrange: Dokument in DB + Dummy-Datei in MinIO
        UUID id = UUID.randomUUID();
        String objectKey = id + "_test.pdf";

        // 1) DB
        var doc = new Document();
        doc.setId(id);
        doc.setFilename("test.pdf");
        doc.setContentType("application/pdf");
        doc.setSize(123L);
        doc.setObjectKey(objectKey);
        doc.setStatus(DocumentStatus.PENDING);
        repo.save(doc);

        // 2) MinIO: lege Datei ab (damit „OCR“ etwas lesen könnte)
        var content = "fake pdf bytes";
        var bytes = content.getBytes(StandardCharsets.UTF_8);
        minio.putObject(
                PutObjectArgs.builder()
                        .bucket("documents")
                        .object(objectKey)
                        .stream(new java.io.ByteArrayInputStream(bytes), bytes.length, -1)
                        .contentType("application/pdf")
                        .build()
        );


        // act: Nachricht in Upload-Queue werfen (so wie Producer)
        rabbit.convertAndSend("paperless.upload", id.toString());

        // assert: Worker setzt auf PROCESSING → COMPLETED + ocrText gefüllt
        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            var reloaded = repo.findById(id).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(DocumentStatus.COMPLETED);
            assertThat(reloaded.getOcrText()).isNotBlank();
        });
    }
}
