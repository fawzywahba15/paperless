package org.example.paperlessrest.dto;

import java.util.UUID;

/**
 * Nachricht für die RabbitMQ Queue 'result.queue'.
 * Enthält das Ergebnis der OCR-Verarbeitung vom Worker.
 */
public record DocumentResultMessage(
        UUID documentId,
        String status,
        String ocrText,
        String error
) {
}