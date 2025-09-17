package org.example.paperlessrest.service.port;

import java.util.UUID;

public interface OcrProducerPort {
    void sendForOcr(UUID documentId, String objectKey);
}
