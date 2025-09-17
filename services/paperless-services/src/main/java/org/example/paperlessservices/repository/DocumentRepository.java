package org.example.paperlessservices.repository;

import org.example.paperlessservices.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {}
