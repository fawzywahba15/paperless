package org.example.paperlessservices.messaging;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import org.example.paperlessservices.dto.DocumentMessage;
import org.example.paperlessservices.entity.Document;
import org.example.paperlessservices.entity.DocumentStatus;
import org.example.paperlessservices.repository.DocumentRepository;
import org.example.paperlessservices.repository.ElasticSearchRepository;
import org.example.paperlessservices.search.ElasticDocument;
import org.example.paperlessservices.service.port.ResultProducerPort;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Konsumiert Nachrichten aus der 'ocr.queue'.
 * Lädt die Datei aus MinIO, führt OCR durch (Tesseract) und speichert das Ergebnis.
 * Triggered danach die 'genai.queue'.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OcrConsumer {

    private final DocumentRepository repo;
    private final ResultProducerPort resultProducer;
    private final MinioClient minioClient;
    private final RabbitTemplate rabbitTemplate;
    private final ITesseract tesseract;
    private final ElasticSearchRepository elasticRepository;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${GENAI_QUEUE:genai.queue}")
    private String genAiQueue;

    @RabbitListener(queues = "${OCR_QUEUE:ocr.queue}")
    public void handle(DocumentMessage msg) {
        log.info("📩 OCR Auftrag erhalten für ID: {}", msg.documentId());
        UUID docId = msg.documentId();

        try {
            // 1. Dokument aus DB laden (mit Retry, falls DB langsamer als Queue ist)
            Document doc = fetchDocumentWithRetry(docId);
            if (doc == null) return;

            // Status update
            updateStatus(doc, DocumentStatus.PROCESSING);

            // 2. Datei aus MinIO laden
            log.info("Lade Datei aus MinIO: {}", doc.getObjectKey());
            File tempFile = downloadFile(doc.getObjectKey());

            // 3. OCR durchführen
            log.info("Starte Tesseract OCR...");
            String ocrText = tesseract.doOCR(tempFile);
            tempFile.delete(); // Aufräumen
            log.info("OCR fertig. Extrahierte Zeichen: {}", ocrText.length());

            // 4. Speichern & Indexieren
            doc.setOcrText(ocrText);
            updateStatus(doc, DocumentStatus.COMPLETED);

            indexInElasticSearch(doc);

            // 5. Erfolgsmeldung & Trigger GenAI
            resultProducer.publishCompleted(docId, ocrText);

            log.info("Leite weiter an GenAI Queue...");
            rabbitTemplate.convertAndSend(genAiQueue, msg);

        } catch (Exception e) {
            log.error("❌ Fehler bei OCR Verarbeitung für ID {}: {}", docId, e.getMessage());
            handleFailure(docId, e.getMessage());
        }
    }

    private Document fetchDocumentWithRetry(UUID id) throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            var opt = repo.findById(id);
            if (opt.isPresent()) return opt.get();
            Thread.sleep(500);
        }
        log.error("Dokument {} nicht in DB gefunden (trotz Retries).", id);
        return null;
    }

    private File downloadFile(String objectKey) throws Exception {
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(objectKey).build()
        );
        File tempFile = File.createTempFile("ocr_", ".pdf");
        Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        stream.close();
        return tempFile;
    }

    private void indexInElasticSearch(Document doc) {
        try {
            ElasticDocument elasticDoc = ElasticDocument.builder()
                    .id(doc.getId().toString())
                    .title(doc.getTitle())
                    .content(doc.getOcrText())
                    .build();
            elasticRepository.save(elasticDoc);
            log.info("Dokument in ElasticSearch indexiert.");
        } catch (Exception e) {
            log.error("ElasticSearch Indexing fehlgeschlagen (nicht kritisch): {}", e.getMessage());
        }
    }

    private void updateStatus(Document doc, DocumentStatus status) {
        doc.setStatus(String.valueOf(status));
        repo.save(doc);
    }

    private void handleFailure(UUID docId, String error) {
        repo.findById(docId).ifPresent(d -> updateStatus(d, DocumentStatus.FAILED));
        resultProducer.publishFailed(docId, error);
    }
}