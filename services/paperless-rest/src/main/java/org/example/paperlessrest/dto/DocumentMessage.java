package org.example.paperlessrest.dto;

import java.util.UUID;

/**
 * Nachricht für die RabbitMQ Queue 'ocr.queue'.
 * Triggered den Worker-Service zur Verarbeitung.
 */
public record DocumentMessage(
        UUID documentId,
        String objectKey
) {
}