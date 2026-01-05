package org.example.paperlessrest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Data // automatisch Getter, Setter, ToString, HashCode
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

    private String objectKey;
    private String status;

    // Felder für die Worker-Ergebnisse
    @Column(length = 10000)
    private String ocrText;

    @Column(length = 4096)
    private String summary;
}