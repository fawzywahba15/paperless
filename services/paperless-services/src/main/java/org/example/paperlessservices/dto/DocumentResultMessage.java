package org.example.paperlessservices.dto;

import java.util.UUID;

public record DocumentResultMessage(
        UUID documentId,
        String status,   // COMPLETED / FAILED
        String ocrText,  // kann null sein bei FAILED
        String error     // optionaler Fehlertext
) {}
