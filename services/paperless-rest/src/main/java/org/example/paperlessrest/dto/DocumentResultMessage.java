package org.example.paperlessrest.dto;

import java.util.UUID;

public record DocumentResultMessage(
        UUID documentId,
        String status,
        String ocrText,
        String error
) {}
