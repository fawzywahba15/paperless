package org.example.paperlessrest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DocumentRequestDto {
    // Validation Annotation
    @NotBlank(message = "Title must not be empty")
    private String title;

    private String category;
}