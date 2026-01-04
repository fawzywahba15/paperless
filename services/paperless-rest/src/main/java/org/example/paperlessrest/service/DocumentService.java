package org.example.paperlessrest.service;

import org.example.paperlessrest.dto.DocumentResponseDto;
import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.entity.DocumentStatus;
import org.example.paperlessrest.repository.DocumentRepository;
import org.example.paperlessrest.service.port.DocumentStoragePort;
import org.example.paperlessrest.service.port.OcrProducerPort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository repo;
    private final DocumentStoragePort storagePort;
    private final OcrProducerPort ocrProducerPort;

    public DocumentService(DocumentRepository repo,
                           DocumentStoragePort storagePort,
                           OcrProducerPort ocrProducerPort) {
        this.repo = repo;
        this.storagePort = storagePort;
        this.ocrProducerPort = ocrProducerPort;
    }

    public Document uploadAndDispatch(MultipartFile file) throws Exception {
        // 1) Speichern (MinIO)
        String objectKey = storagePort.store(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream()
        );

        // 2) DB-Eintrag
        Document doc = new Document();
        doc.setFilename(file.getOriginalFilename());
        doc.setContentType(file.getContentType());
        doc.setSize(file.getSize());
        doc.setObjectKey(objectKey);
        doc.setStatus(String.valueOf(DocumentStatus.PENDING));

        // Speichern
        doc = repo.saveAndFlush(doc);

        // 3) Nachricht an RabbitMQ senden
        ocrProducerPort.sendForOcr(doc.getId(), objectKey);

        return doc;
    }

    public Optional<DocumentResponseDto> find(UUID id) {
        return repo.findById(id).map(doc -> new DocumentResponseDto(
                doc.getId(),
                doc.getFilename(),
                doc.getContentType(),
                doc.getSize(),
                doc.getStatus()
        ));
    }
}