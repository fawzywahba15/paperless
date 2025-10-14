package org.example.paperlessrest.service.adapter;

import lombok.extern.slf4j.Slf4j;
import org.example.paperlessrest.dto.DocumentMessage;
import org.example.paperlessrest.service.port.OcrProducerPort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
            log.info(" Sent message to OCR queue '{}': {}", ocrQueue, message);
        } catch (Exception e) {
            log.error("Failed to send message to OCR queue '{}'", ocrQueue, e);
            throw new RuntimeException("Failed to send OCR message", e);
        }
    }
}
