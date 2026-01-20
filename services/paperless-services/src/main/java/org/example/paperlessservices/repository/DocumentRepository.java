package org.example.paperlessservices.repository;

import org.example.paperlessservices.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA Repository für den Zugriff auf die Dokumenten-Tabelle.
 * Dient zum Lesen von Metadaten und Speichern von OCR/KI-Ergebnissen.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
}