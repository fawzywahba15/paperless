package org.example.paperlessrest.dto;

import java.util.UUID;

/**
 * DTO für die Antwort an das Frontend.
 * Enthält alle relevanten Metadaten sowie den extrahierten Inhalt.
 *
 * @param content Enthält den OCR-Text (gemappt vom Backend 'ocrText')
 */
public record DocumentResponseDto(
        UUID id,
        String title,
        String category,
        String summary,
        String filename,
        String contentType,
        long size,
        String status,
        String content
) {
}