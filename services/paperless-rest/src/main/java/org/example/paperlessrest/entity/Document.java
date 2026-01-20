package org.example.paperlessrest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Die zentrale Entität für ein gespeichertes Dokument.
 * Speichert Metadaten, Referenzen zum Object-Storage (MinIO) und Ergebnisse der Worker (OCR/KI).
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String title;
    private String category;

    private String filename;
    private String contentType;
    private long size;

    // Referenz auf die Datei im MinIO Object Storage
    private String objectKey;

    private String status;

    // Ergebnis des OCR-Prozesses (großer Textblob)
    @Column(columnDefinition = "TEXT")
    private String ocrText;

    // Ergebnis der Generative AI (Zusammenfassung)
    @Column(length = 4096)
    private String summary;
}