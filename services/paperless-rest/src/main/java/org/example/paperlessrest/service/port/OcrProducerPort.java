package org.example.paperlessrest.service.port;

import java.util.UUID;

/**
 * Port-Interface für das Messaging.
 * Entkoppelt die Business-Logik vom Message-Broker (RabbitMQ).
 */
public interface OcrProducerPort {

    /** Sendet einen Auftrag zur OCR-Verarbeitung. */
    void sendForOcr(UUID documentId, String objectKey);
}