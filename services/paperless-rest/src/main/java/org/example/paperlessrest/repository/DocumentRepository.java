package org.example.paperlessrest.repository;

import org.example.paperlessrest.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA Repository für den Zugriff auf die 'document' Tabelle in PostgreSQL.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
}