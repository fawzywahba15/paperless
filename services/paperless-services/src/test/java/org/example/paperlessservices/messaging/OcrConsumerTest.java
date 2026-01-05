package org.example.paperlessservices.messaging;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import net.sourceforge.tess4j.ITesseract;
import org.example.paperlessservices.dto.DocumentMessage;
import org.example.paperlessservices.entity.Document;
import org.example.paperlessservices.entity.DocumentStatus;
import org.example.paperlessservices.repository.DocumentRepository;
import org.example.paperlessservices.repository.ElasticSearchRepository;
import org.example.paperlessservices.service.port.ResultProducerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OcrConsumerTest {

    @Mock private DocumentRepository repo;
    @Mock private ResultProducerPort resultProducer;
    @Mock private MinioClient minioClient;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private ITesseract tesseract;
    @Mock private ElasticSearchRepository elasticRepository;

    private OcrConsumer ocrConsumer;

    @BeforeEach
    void setUp() {
        ocrConsumer = new OcrConsumer(repo, resultProducer, minioClient, rabbitTemplate, tesseract, elasticRepository);

        ReflectionTestUtils.setField(ocrConsumer, "genAiQueue", "genai.queue");
        ReflectionTestUtils.setField(ocrConsumer, "bucketName", "paperless-bucket");
    }

    @Test
    void handle_ShouldProcessOcrAndSendToGenAi() throws Exception {
        // Arrange
        UUID docId = UUID.randomUUID();
        DocumentMessage msg = new DocumentMessage(docId, "test.pdf");

        Document doc = new Document();
        doc.setId(docId);
        doc.setObjectKey("test.pdf");
        doc.setStatus(String.valueOf(DocumentStatus.PENDING));

        when(repo.findById(docId)).thenReturn(Optional.of(doc));
        when(tesseract.doOCR(any(File.class))).thenReturn("Mocked OCR Text");

        // MinIO Mocking
        InputStream fakeStream = new ByteArrayInputStream("PDF".getBytes());
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(new GetObjectResponse(null, "bucket", null, "obj", fakeStream));

        // Act
        ocrConsumer.handle(msg);

        // Assert
        assertEquals(String.valueOf(DocumentStatus.COMPLETED), doc.getStatus());
        assertEquals("Mocked OCR Text", doc.getOcrText());

        // check, ob RabbitMQ benachrichtigt wurde
        verify(rabbitTemplate).convertAndSend(eq("genai.queue"), eq(msg));

        // check, ob in ElasticSearch gespeichert wurde
        verify(elasticRepository, times(1)).save(any());
    }
}