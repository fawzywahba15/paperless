package org.example.paperlessrest.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.example.paperlessrest.entity.AccessLog;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TinyUrl
    @Column(unique = true, nullable = false)
    private String token;

    // expire
    private LocalDateTime expiresAt;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    // Statistik-Verbindung
    @OneToMany(mappedBy = "shareLink", cascade = CascadeType.ALL)
    private List<AccessLog> accessLogs;
}