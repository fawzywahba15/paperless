package org.example.paperlessservices.dto;

import java.util.UUID;

/**
 * RabbitMQ-Nachricht: Ergebnis einer Verarbeitung.
 * Meldet Erfolg oder Misserfolg zurück an das System (optional für Monitoring).
 */
public record DocumentResultMessage(
        UUID documentId,
        String status,
        String ocrText,
        String error
) {}