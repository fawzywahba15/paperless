package org.example.paperlessrest.repository;

import org.example.paperlessrest.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository für die Zugriffsprotokolle (Sprint 7 Batch-Import).
 */
@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
}