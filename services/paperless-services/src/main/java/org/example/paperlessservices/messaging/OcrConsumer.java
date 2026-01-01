package org.example.paperlessservices.messaging;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.example.paperlessservices.dto.DocumentMessage;
import org.example.paperlessservices.entity.Document;
import org.example.paperlessservices.entity.DocumentStatus;
import org.example.paperlessservices.repository.DocumentRepository;
import org.example.paperlessservices.service.port.ResultProducerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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

    @Value("${MINIO_BUCKET_NAME:paperless-bucket}")
    private String bucketName;

    public OcrConsumer(DocumentRepository repo, ResultProducerPort resultProducer, MinioClient minioClient) {
        this.repo = repo;
        this.resultProducer = resultProducer;
        this.minioClient = minioClient;
    }

    @RabbitListener(queues = "${OCR_QUEUE:ocr.queue}")
    public void handle(DocumentMessage msg) {
        log.info("Received OCR message for ID: {}", msg.documentId());
        UUID docId = msg.documentId();

        try {
            // --- RETRY LOGIK ---
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

            // 1. Status auf PROCESSING
            doc.setStatus(DocumentStatus.PROCESSING);
            repo.save(doc);

            log.info("Starting OCR for ObjectKey: {}", doc.getObjectKey());

            // 2. MinIO Laden
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(doc.getObjectKey())
                            .build()
            );

            File tempFile = File.createTempFile("ocr_", ".pdf");
            Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            stream.close();

            // 3. Tesseract Setup (MIT KORREKTEM PFAD!)
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
            tesseract.setLanguage("eng");

            String ocrText = tesseract.doOCR(tempFile);
            tempFile.delete();

            log.info("OCR finished. Text length: {}", ocrText.length());

            // 4. Speichern
            doc.setOcrText(ocrText);
            doc.setStatus(DocumentStatus.COMPLETED);
            repo.save(doc);

            // 5. Result
            resultProducer.publishCompleted(docId, ocrText);
            log.info("Processing finished successfully for {}", docId);

        } catch (Exception e) {
            log.error("OCR processing failed for {}", docId, e);
            repo.findById(docId).ifPresent(d -> {
                d.setStatus(DocumentStatus.FAILED);
                repo.save(d);
            });
            resultProducer.publishFailed(docId, e.getMessage());
        }
    }
}