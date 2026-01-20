package org.example.paperlessservices.service.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paperlessservices.dto.DocumentResultMessage;
import org.example.paperlessservices.service.port.ResultProducerPort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * RabbitMQ-Implementierung des {@link ResultProducerPort}.
 * Sendet Status-Updates (COMPLETED/FAILED) an die definierte Ergebnis-Queue.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AmqpResultProducerAdapter implements ResultProducerPort {

    private final RabbitTemplate tpl;

    @Value("${RESULT_QUEUE:result.queue}")
    private String resultQueue;

    @Override
    public void publishCompleted(UUID documentId, String ocrText) {
        log.info("Sende SUCCESS Nachricht an Queue '{}' für DocID: {}", resultQueue, documentId);
        tpl.convertAndSend(resultQueue, new DocumentResultMessage(documentId, "COMPLETED", ocrText, null));
    }

    @Override
    public void publishFailed(UUID documentId, String error) {
        log.error("Sende FAILED Nachricht an Queue '{}' für DocID: {}", resultQueue, documentId);
        tpl.convertAndSend(resultQueue, new DocumentResultMessage(documentId, "FAILED", null, error));
    }
}