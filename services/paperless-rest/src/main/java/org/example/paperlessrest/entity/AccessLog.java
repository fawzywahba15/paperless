package org.example.paperlessrest.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime accessedAt;

    // Zugriff erfolgreich?
    private boolean successful;

    @ManyToOne
    @JoinColumn(name = "share_link_id", nullable = false)
    private ShareLink shareLink;
}