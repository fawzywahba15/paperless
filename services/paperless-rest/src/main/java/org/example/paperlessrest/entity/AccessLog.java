package org.example.paperlessrest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entität zur Protokollierung von Zugriffen.
 * Diese Klasse wird dual genutzt:
 * 1. Für Zugriffe auf Share-Links (Sprint 6).
 * 2. Für importierte Logs aus dem XML-Batch-Import (Sprint 7).
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "access_log")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime accessedAt;

    private boolean successful;

    // Optional: Nur gesetzt bei Zugriffen über Share-Links.
    // Beim Batch-Import bleibt dies null.
    @ManyToOne
    @JoinColumn(name = "share_link_id", nullable = true)
    private ShareLink shareLink;

    // Zusatzfelder für Sprint 7 (Batch Import aus XML)
    // Speichert die externe ID aus der XML-Datei.
    @Column(name = "document_id_ref")
    private Long documentId;

    @Column(name = "log_message")
    private String logMessage;
}