package org.example.paperlessrest.repository;

import org.example.paperlessrest.entity.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository für die Verwaltung von temporären Share-Links.
 */
@Repository
public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {

    /** Findet einen Link anhand des eindeutigen Tokens. */
    Optional<ShareLink> findByToken(String token);
}