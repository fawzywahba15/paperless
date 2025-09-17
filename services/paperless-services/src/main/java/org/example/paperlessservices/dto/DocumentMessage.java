package org.example.paperlessservices.dto;

import java.util.UUID;
public record DocumentMessage(UUID documentId, String objectKey) {}
