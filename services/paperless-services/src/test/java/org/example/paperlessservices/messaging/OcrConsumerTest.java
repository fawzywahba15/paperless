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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit-Test für den OcrConsumer.
 * Simuliert den Workflow: RabbitMQ Msg -> MinIO Download -> Tesseract OCR -> DB Save -> Next Queue.
 */
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

        // Properties setzen (normalerweise via @Value)
        ReflectionTestUtils.setField(ocrConsumer, "genAiQueue", "genai.queue");
        ReflectionTestUtils.setField(ocrConsumer, "bucketName", "paperless-bucket");
    }

    @Test
    void handle_ShouldProcessOcrAndSendToGenAi() throws Exception {
        // --- ARRANGE ---
        UUID docId = UUID.randomUUID();
        DocumentMessage msg = new DocumentMessage(docId, "test.pdf");

        Document doc = new Document();
        doc.setId(docId);
        doc.setObjectKey("test.pdf");
        doc.setStatus(String.valueOf(DocumentStatus.PENDING));

        when(repo.findById(docId)).thenReturn(Optional.of(doc));

        // Tesseract Mock: Wir führen keine echte OCR durch, sondern geben Text zurück
        when(tesseract.doOCR(any(File.class))).thenReturn("Mocked OCR Text Result");

        // MinIO Mock: Wir simulieren einen Dateistream (PDF Inhalt)
        InputStream fakeStream = new ByteArrayInputStream("PDF DUMMY CONTENT".getBytes());

        GetObjectResponse mockResponse = new GetObjectResponse(null, "bucket", null, "obj", fakeStream);

        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        // --- ACT ---
        ocrConsumer.handle(msg);

        // --- ASSERT ---
        // 1. Status muss COMPLETED sein
        assertEquals(String.valueOf(DocumentStatus.COMPLETED), doc.getStatus());

        // 2. OCR Text muss gesetzt sein
        assertEquals("Mocked OCR Text Result", doc.getOcrText());

        // 3. RabbitMQ: Nachricht an GenAI Queue gesendet?
        verify(rabbitTemplate).convertAndSend(eq("genai.queue"), eq(msg));

        // 4. ElasticSearch: Dokument indexiert?
        verify(elasticRepository, times(1)).save(any());

        // 5. Result Producer (Success) aufgerufen?
        verify(resultProducer).publishCompleted(eq(docId), anyString());
    }
}