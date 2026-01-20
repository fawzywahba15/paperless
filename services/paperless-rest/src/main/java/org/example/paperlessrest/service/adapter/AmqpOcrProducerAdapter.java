package org.example.paperlessrest.service.adapter;

import lombok.extern.slf4j.Slf4j;
import org.example.paperlessrest.dto.DocumentMessage;
import org.example.paperlessrest.service.port.OcrProducerPort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * RabbitMQ-Implementierung des Producer-Ports.
 * Sendet Nachrichten an den OCR-Service.
 */
@Slf4j
@Component
public class AmqpOcrProducerAdapter implements OcrProducerPort {

    private final RabbitTemplate rabbitTemplate;
    private final String ocrQueue;

    public AmqpOcrProducerAdapter(RabbitTemplate rabbitTemplate,
                                  @Value("${OCR_QUEUE:ocr.queue}") String ocrQueue) {
        this.rabbitTemplate = rabbitTemplate;
        this.ocrQueue = ocrQueue;
    }

    @Override
    public void sendForOcr(UUID documentId, String objectKey) {
        try {
            DocumentMessage message = new DocumentMessage(documentId, objectKey);
            rabbitTemplate.convertAndSend(ocrQueue, message);
            log.info("Nachricht an OCR-Queue '{}' gesendet: {}", ocrQueue, message);
        } catch (Exception e) {
            log.error("Fehler beim Senden an OCR-Queue '{}'", ocrQueue, e);
            throw new RuntimeException("Konnte OCR-Nachricht nicht senden", e);
        }
    }
}