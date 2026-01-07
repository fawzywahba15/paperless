package org.example.paperlessservices.dto;

import java.util.UUID;

public record DocumentResultMessage(
        UUID documentId,
        String status,
        String ocrText,
        String error
) {}
