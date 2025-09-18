package org.example.paperlessrest.dto;

import java.util.UUID;

public record DocumentResponseDto(
        UUID id,
        String filename,
        String contentType,
        long size,
        String status
) {
    public UUID getId() {
        return id;
    }
}
