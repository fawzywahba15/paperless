package org.example.paperlessservices.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

/**
 * Entität für Dokumente (Spiegelbild zur REST-Service Entität).
 * Enthält Metadaten und die Ergebnisse der Worker (OCR, Summary).
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "document")
public class Document {

    @Id
    private UUID id;

    private String title;
    private String category;
    private String filename;
    private String contentType;
    private long size;
    private String objectKey;

    private String status;

    @Column(columnDefinition = "TEXT")
    @ToString.Exclude // Verhindert riesige Logs
    private String ocrText;

    @Column(length = 4096)
    @ToString.Exclude
    private String summary;
}