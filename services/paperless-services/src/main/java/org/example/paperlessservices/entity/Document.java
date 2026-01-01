package org.example.paperlessservices.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue
    private UUID id;

    @Column
    private String objectKey;

    @Column
    private String filename;

    @Column
    private String contentType;

    @Column
    private long size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status = DocumentStatus.PENDING;

    @Lob
    @Column
    private String ocrText;

    @Column(length = 4096)
    private String summary;

    // --- GETTER / SETTER ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }

    public String getOcrText() { return ocrText; }
    public void setOcrText(String ocrText) { this.ocrText = ocrText; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
