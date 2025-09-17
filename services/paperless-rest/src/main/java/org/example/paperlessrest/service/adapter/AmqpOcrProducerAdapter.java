package org.example.paperlessrest.service.adapter;

import org.example.paperlessrest.dto.DocumentMessage;
import org.example.paperlessrest.service.port.OcrProducerPort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
        rabbitTemplate.convertAndSend(ocrQueue, new DocumentMessage(documentId, objectKey));
    }
}
