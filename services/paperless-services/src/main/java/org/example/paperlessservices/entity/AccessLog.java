package org.example.paperlessservices.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    // nullable = true, weil der Batch-Import keinen Link hat
    @ManyToOne
    @JoinColumn(name = "share_link_id", nullable = true)
    private ShareLink shareLink;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "log_message")
    private String logMessage;
}