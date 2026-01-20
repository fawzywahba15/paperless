package org.example.paperlessservices.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repräsentiert einen temporären Freigabe-Link für ein Dokument.
 * Dient auch als Parent-Entity für Zugriffslogs (AccessLog).
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Eindeutiges Token für den URL-Zugriff
    @Column(unique = true, nullable = false)
    private String token;

    private LocalDateTime expiresAt;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    // Statistik über Zugriffe auf diesen Link
    @OneToMany(mappedBy = "shareLink", cascade = CascadeType.ALL)
    @ToString.Exclude // WICHTIG: Verhindert StackOverflowError durch Zirkelbezug
    private List<AccessLog> accessLogs;
}