package org.example.paperlessrest.service;

import org.example.paperlessrest.dto.DocumentResponseDto;
import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.entity.DocumentStatus;
import org.example.paperlessrest.repository.DocumentRepository;
import org.example.paperlessrest.service.port.DocumentStoragePort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository repo;
    private final DocumentStoragePort storagePort;

    public DocumentService(DocumentRepository repo,
                           DocumentStoragePort storagePort) {
        this.repo = repo;
        this.storagePort = storagePort;
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

        // saveAndFlush, um sicherzustellen, dass die Transaktion committed ist, bevor die Nachricht an RabbitMQ geht (verhindert Race Conditions).
        doc = repo.saveAndFlush(doc);

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