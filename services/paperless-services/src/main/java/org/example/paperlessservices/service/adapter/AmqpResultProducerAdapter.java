package org.example.paperlessservices.service.adapter;

import org.example.paperlessservices.dto.DocumentResultMessage;
import org.example.paperlessservices.service.port.ResultProducerPort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AmqpResultProducerAdapter implements ResultProducerPort {

    private final RabbitTemplate tpl;
    private final String resultQueue;

    public AmqpResultProducerAdapter(RabbitTemplate tpl,
                                     @Value("${RESULT_QUEUE:result.queue}") String resultQueue) {
        this.tpl = tpl;
        this.resultQueue = resultQueue;
    }

    @Override
    public void publishCompleted(UUID documentId, String ocrText) {
        tpl.convertAndSend(resultQueue, new DocumentResultMessage(documentId, "COMPLETED", ocrText, null));
    }

    @Override
    public void publishFailed(UUID documentId, String error) {
        tpl.convertAndSend(resultQueue, new DocumentResultMessage(documentId, "FAILED", null, error));
    }
}
