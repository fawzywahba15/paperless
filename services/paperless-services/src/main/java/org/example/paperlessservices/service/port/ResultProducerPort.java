package org.example.paperlessservices.service.port;

import java.util.UUID;

public interface ResultProducerPort {
    void publishCompleted(UUID documentId, String ocrText);
    void publishFailed(UUID documentId, String error);
}
