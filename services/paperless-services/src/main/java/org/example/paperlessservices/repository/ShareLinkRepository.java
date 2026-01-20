package org.example.paperlessservices.repository;

import org.example.paperlessservices.entity.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository für Freigabe-Links.
 * Wird benötigt, um Referenzen in AccessLogs korrekt aufzulösen (falls vorhanden).
 */
@Repository
public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {
    Optional<ShareLink> findByToken(String token);
}