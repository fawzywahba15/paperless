package org.example.paperlessservices.messaging;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import net.sourceforge.tess4j.ITesseract;
import org.example.paperlessservices.dto.DocumentMessage;
import org.example.paperlessservices.entity.Document;
import org.example.paperlessservices.entity.DocumentStatus;
import org.example.paperlessservices.repository.DocumentRepository;
import org.example.paperlessservices.service.port.ResultProducerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class OcrConsumer {

    private static final Logger log = LoggerFactory.getLogger(OcrConsumer.class);

    private final DocumentRepository repo;
    private final ResultProducerPort resultProducer;
    private final MinioClient minioClient;
    private final RabbitTemplate rabbitTemplate;
    private final ITesseract tesseract;

    @Value("${MINIO_BUCKET_NAME:paperless-bucket}")
    private String bucketName;

    @Value("${GENAI_QUEUE:genai.queue}")
    private String genAiQueue;

    public OcrConsumer(DocumentRepository repo,
                       ResultProducerPort resultProducer,
                       MinioClient minioClient,
                       RabbitTemplate rabbitTemplate,
                       ITesseract tesseract) {
        this.repo = repo;
        this.resultProducer = resultProducer;
        this.minioClient = minioClient;
        this.rabbitTemplate = rabbitTemplate;
        this.tesseract = tesseract;
    }

    @RabbitListener(queues = "${OCR_QUEUE:ocr.queue}")
    public void handle(DocumentMessage msg) {
        log.info("Received OCR message for ID: {}", msg.documentId());
        UUID docId = msg.documentId();

        try {
            Document doc = null;
            int maxRetries = 10;
            for (int i = 0; i < maxRetries; i++) {
                var documentOptional = repo.findById(docId);
                if (documentOptional.isPresent()) {
                    doc = documentOptional.get();
                    break;
                }
                log.warn("Document {} not found in DB yet. Waiting 1s... (Attempt {}/{})", docId, i + 1, maxRetries);
                Thread.sleep(1000);
            }

            if (doc == null) {
                log.error("FATAL: Document not found in DB after {} retries: {}", maxRetries, docId);
                return;
            }

            doc.setStatus(String.valueOf(DocumentStatus.PROCESSING));
            repo.save(doc);

            log.info("Starting OCR for ObjectKey: {}", doc.getObjectKey());

            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(doc.getObjectKey())
                            .build()
            );

            File tempFile = File.createTempFile("ocr_", ".pdf");
            Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            stream.close();

            //Nutzen das injizierte Tesseract Objekt
            String ocrText = tesseract.doOCR(tempFile);

            tempFile.delete();

            log.info("OCR finished. Text length: {}", ocrText.length());

            doc.setOcrText(ocrText);
            doc.setStatus(String.valueOf(DocumentStatus.COMPLETED));
            repo.save(doc);

            resultProducer.publishCompleted(docId, ocrText);

            log.info("Sending document {} to GenAI queue: {}", docId, genAiQueue);
            rabbitTemplate.convertAndSend(genAiQueue, msg);

        } catch (Exception e) {
            log.error("OCR processing failed for {}", docId, e);
            if (repo.existsById(docId)) { // Safety check
                repo.findById(docId).ifPresent(d -> {
                    d.setStatus(String.valueOf(DocumentStatus.FAILED));
                    repo.save(d);
                });
            }
            resultProducer.publishFailed(docId, e.getMessage());
        }
    }
}