package org.example.paperlessservices.repository;

import org.example.paperlessservices.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository für die Persistierung von Zugriffsprotokollen.
 * Wird hauptsächlich vom {@link org.example.paperlessservices.worker.BatchImportWorker} genutzt (Sprint 7).
 */
@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
}