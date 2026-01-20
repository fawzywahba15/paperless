package org.example.paperlessrest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO für eingehende Upload-Requests.
 */
@Data
public class DocumentRequestDto {

    @NotBlank(message = "Titel darf nicht leer sein")
    private String title;

    private String category;
}