package org.example.paperlessrest.it;
import io.minio.MakeBucketArgs;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.awaitility.Awaitility;
import org.example.paperlessrest.PaperlessRestApplication;
import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.repository.DocumentRepository;
import org.junit.jupiter.api.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(
        classes = PaperlessRestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class UploadIntegrationIT {

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

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        // RabbitMQ
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

    @LocalServerPort
    int port;

    @Autowired
    DocumentRepository repository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    MinioClient minio;

    @BeforeEach
    void setupMinio() throws Exception {

        minio = MinioClient.builder()
                .endpoint("http://" + MINIO.getHost() + ":" + MINIO.getMappedPort(9000))
                .credentials("testkey", "testsecret")
                .build();

        // Bucket anlegen, falls nicht vorhanden
        try {
            var buckets = minio.listBuckets();
            var exists = buckets.stream().anyMatch(b -> b.name().equals("documents"));
            if (!exists) {
                minio.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket("documents")
                                .build()
                );
            }
        } catch (MinioException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void upload_persists_minio_and_publishes_message() throws Exception {
        // arrange: Dummy-PDF
        File tmp = File.createTempFile("cv-", ".pdf");
        Files.writeString(tmp.toPath(), "dummy-pdf-content");

        // act: POST multipart
        var rt = new RestTemplate();
        var url = "http://localhost:" + port + "/api/documents";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        var resource = new org.springframework.core.io.FileSystemResource(tmp);
        body.add("file", resource);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var req = new HttpEntity<>(body, headers);
        var response = rt.postForEntity(url, req, org.example.paperlessrest.dto.DocumentMessage.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var dto = response.getBody();
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isNotNull();

        UUID id = dto.getId();

        // assert: DB-Eintrag PENDING
        Optional<Document> fromDb = repository.findById(id);
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getStatus()).isEqualTo(org.example.paperlessrest.entity.DocumentStatus.PENDING);

        // assert: Objekt in MinIO existiert
        var obj = minio.statObject(
                StatObjectArgs.builder()
                        .bucket("documents")
                        .object(dto.getObjectKey())
                        .build()
        );

        assertThat(obj.size()).isGreaterThan(0);

        // assert: Message wurde in Upload-Queue gelegt (wir lesen 1x raus und legen sie zurück)
        var queueName = "paperless.upload";
        var msg = rabbitTemplate.receive(queueName, 1000);
        assertThat(msg).as("Upload-Queue enthält Message").isNotNull();

        // (Optional) wieder zurücklegen, damit nachfolgende Tests nicht „leerlaufen“
        if (msg != null) {
            rabbitTemplate.send(queueName, msg);
        }

        // clean
        Files.deleteIfExists(tmp.toPath());
    }
}
