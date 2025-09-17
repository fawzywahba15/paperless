package org.example.paperlessrest.messaging;

import org.example.paperlessrest.dto.DocumentResultMessage;
import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.entity.DocumentStatus;
import org.example.paperlessrest.repository.DocumentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ResultConsumer {

    private final DocumentRepository repo;

    public ResultConsumer(DocumentRepository repo) {
        this.repo = repo;
    }

    @RabbitListener(queues = "${RESULT_QUEUE:result.queue}")
    @Transactional
    public void handle(DocumentResultMessage msg) {
        repo.findById(msg.documentId()).ifPresent(doc -> {
            if ("COMPLETED".equalsIgnoreCase(msg.status())) {
                doc.setStatus(String.valueOf(DocumentStatus.COMPLETED));
                doc.setOcrText(msg.ocrText());
            } else {
                doc.setStatus(String.valueOf(DocumentStatus.FAILED));
            }
            repo.save(doc);
        });
    }
}
