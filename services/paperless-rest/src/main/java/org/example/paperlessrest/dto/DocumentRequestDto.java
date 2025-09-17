package org.example.paperlessrest.dto;

import org.springframework.web.multipart.MultipartFile;

public record DocumentRequestDto(
        MultipartFile file
) {}
