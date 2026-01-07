package org.example.paperlessservices.repository;

import org.example.paperlessservices.entity.ShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShareLinkRepository extends JpaRepository<ShareLink, Long> {
    Optional<ShareLink> findByToken(String token);
}