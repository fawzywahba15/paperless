package org.example.paperlessservices.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

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

    @Column(length = 10000)
    private String ocrText;

    @Column(length = 4096)
    private String summary;
}