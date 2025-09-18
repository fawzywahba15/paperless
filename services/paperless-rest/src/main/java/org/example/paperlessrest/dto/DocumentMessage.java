package org.example.paperlessrest.dto;

import java.util.UUID;

public record DocumentMessage(UUID documentId, String objectKey) {
    public String getObjectKey() {
        return objectKey;
    }

    public UUID getId() {
        return documentId;
    }
}
