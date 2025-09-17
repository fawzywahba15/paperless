package org.example.paperlessservices.messaging;

import org.example.paperlessservices.dto.DocumentMessage;
import org.example.paperlessservices.entity.Document;
import org.example.paperlessservices.entity.DocumentStatus;
import org.example.paperlessservices.repository.DocumentRepository;
import org.example.paperlessservices.service.port.ResultProducerPort;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        repo.findById(msg.documentId()).ifPresent(doc -> {
            doc.setStatus(DocumentStatus.PROCESSING);
            repo.save(doc);
        });

        try {
            // Dummy-OCR
            String ocrText = "Dummy OCR for objectKey=" + msg.objectKey();
            // DB update
            repo.findById(msg.documentId()).ifPresent(doc -> {
                doc.setOcrText(ocrText);
                doc.setStatus(DocumentStatus.COMPLETED);
                repo.save(doc);
            });
            // Ergebnis auf result.queue
            resultProducer.publishCompleted(msg.documentId(), ocrText);
        } catch (Exception e) {
            repo.findById(msg.documentId()).ifPresent(doc -> {
                doc.setStatus(DocumentStatus.FAILED);
                repo.save(doc);
            });
            resultProducer.publishFailed(msg.documentId(), e.getMessage());
        }
    }
}
