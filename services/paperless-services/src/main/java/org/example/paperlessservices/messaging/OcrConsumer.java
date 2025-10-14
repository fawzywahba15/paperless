package org.example.paperlessservices.messaging;

import lombok.extern.slf4j.Slf4j;
import org.example.paperlessservices.dto.DocumentMessage;
import org.example.paperlessservices.entity.DocumentStatus;
import org.example.paperlessservices.repository.DocumentRepository;
import org.example.paperlessservices.service.port.ResultProducerPort;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OcrConsumer {

    private final DocumentRepository repo;
    private final ResultProducerPort resultProducer;

    public OcrConsumer(DocumentRepository repo, ResultProducerPort resultProducer) {
        this.repo = repo;
        this.resultProducer = resultProducer;
    }

    @RabbitListener(queues = "${OCR_QUEUE:ocr.queue}")
    @Transactional
    public void handle(DocumentMessage msg) {
        log.info(" Received OCR message: {}", msg);
        try {
            repo.findById(msg.documentId()).ifPresent(doc -> {
                doc.setStatus(DocumentStatus.PROCESSING);
                repo.save(doc);
                log.info("Document {} status set to PROCESSING", doc.getId());
            });

            // Dummy OCR
            String ocrText = "Dummy OCR for objectKey=" + msg.objectKey();

            repo.findById(msg.documentId()).ifPresent(doc -> {
                doc.setOcrText(ocrText);
                doc.setStatus(DocumentStatus.COMPLETED);
                repo.save(doc);
                log.info(" Document {} OCR completed with text='{}'", doc.getId(), ocrText);
            });

            resultProducer.publishCompleted(msg.documentId(), ocrText);
            log.info(" Sent result message for {}", msg.documentId());

        } catch (Exception e) {
            log.error(" OCR processing failed for {}", msg.documentId(), e);
            repo.findById(msg.documentId()).ifPresent(doc -> {
                doc.setStatus(DocumentStatus.FAILED);
                repo.save(doc);
            });
            resultProducer.publishFailed(msg.documentId(), e.getMessage());
        }
    }
}
