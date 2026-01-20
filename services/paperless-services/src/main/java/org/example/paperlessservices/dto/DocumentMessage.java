package org.example.paperlessservices.dto;

import java.util.UUID;

/**
 * RabbitMQ-Nachricht: Auftrag zur Verarbeitung (OCR/KI).
 * Wird vom REST-Service gesendet und vom Worker konsumiert.
 */
public record DocumentMessage(
        UUID documentId,
        String objectKey
) {}