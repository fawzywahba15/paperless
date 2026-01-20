package org.example.paperlessrest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paperlessrest.dto.DocumentResponseDto;
import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.entity.DocumentStatus;
import org.example.paperlessrest.mapper.DocumentMapper;
import org.example.paperlessrest.repository.DocumentRepository;
import org.example.paperlessrest.repository.ElasticSearchRepository;
import org.example.paperlessrest.search.ElasticDocument;
import org.example.paperlessrest.service.port.DocumentStoragePort;
import org.example.paperlessrest.service.port.OcrProducerPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service-Komponente für die zentrale Geschäftslogik der Dokumentenverarbeitung.
 * Koordiniert Datenbank, Object-Storage (MinIO), Suchindex (Elastic) und Messaging (RabbitMQ).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ElasticSearchRepository elasticSearchRepository;
    private final DocumentMapper documentMapper;
    private final DocumentStoragePort storagePort;
    private final OcrProducerPort ocrProducerPort;

    /**
     * Verarbeitet den Upload einer Datei: Speicherung in MinIO, Persistierung in DB
     * und Versand an die OCR-Queue.
     *
     * @param file  Die hochgeladene Datei
     * @param title Optionaler Titel (sonst Dateiname)
     * @throws Exception bei Fehlern im Storage oder I/O
     */
    @Transactional
    public void uploadAndDispatch(MultipartFile file, String title) throws Exception {
        String originalFilename = file.getOriginalFilename();

        // 1. Speichern im Object Storage (MinIO)
        String objectKey = storagePort.store(
                originalFilename,
                file.getContentType(),
                file.getSize(),
                file.getInputStream()
        );
        log.debug("Datei in MinIO gespeichert. Key: {}", objectKey);

        // 2. Metadaten in Datenbank speichern
        Document doc = new Document();
        doc.setFilename(originalFilename);
        doc.setTitle(title != null && !title.isEmpty() ? title : originalFilename);
        doc.setContentType(file.getContentType());
        doc.setSize(file.getSize());
        doc.setCategory("Uncategorized");
        doc.setObjectKey(objectKey);
        doc.setStatus(String.valueOf(DocumentStatus.PENDING));

        doc = documentRepository.saveAndFlush(doc);
        log.info("Dokument in DB angelegt. ID: {}", doc.getId());

        // 3. Trigger für OCR Worker (RabbitMQ)
        ocrProducerPort.sendForOcr(doc.getId(), objectKey);
        log.debug("Nachricht an OCR-Queue gesendet.");
    }

    /**
     * Sucht ein Dokument anhand der ID und wandelt es in ein DTO um.
     */
    public Optional<DocumentResponseDto> findById(UUID id) {
        return documentRepository.findById(id)
                .map(documentMapper::entityToDto);
    }

    /**
     * Liefert alle Dokumente als DTOs zurück.
     */
    public List<DocumentResponseDto> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(documentMapper::entityToDto)
                .toList();
    }

    /**
     * Führt eine Fuzzy-Suche über ElasticSearch durch.
     */
    public List<ElasticDocument> searchDocuments(String query) {
        return elasticSearchRepository.fuzzySearch(query);
    }
}