package org.example.paperlessrest.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.repository.DocumentRepository;
import org.example.paperlessrest.dto.DocumentResponseDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository repo;
    private final MinioClient minio;
    private final RabbitTemplate rabbit;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${OCR_QUEUE:ocr.queue}")
    private String ocrQueue;

    public DocumentService(DocumentRepository repo, MinioClient minio, RabbitTemplate rabbit) {
        this.repo = repo;
        this.minio = minio;
        this.rabbit = rabbit;
    }

    public DocumentResponseDto upload(MultipartFile file) {
        try {
            UUID id = UUID.randomUUID();
            String key = "docs/" + id;

            try (InputStream in = file.getInputStream()) {
                minio.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(key)
                                .contentType(file.getContentType())
                                .stream(in, file.getSize(), -1)
                                .build()
                );
            }

            Document doc = new Document();
            doc.setId(id);
            doc.setFilename(file.getOriginalFilename());
            doc.setContentType(file.getContentType());
            doc.setSize(file.getSize());
            doc.setObjectKey(key);
            doc.setStatus("OCR_QUEUED");

            repo.save(doc);

            rabbit.convertAndSend(
                    ocrQueue,
                    Map.of("id", id.toString(), "bucket", bucket, "key", key)
            );

            return new DocumentResponseDto(
                    doc.getId(),
                    doc.getFilename(),
                    doc.getContentType(),
                    doc.getSize(),
                    doc.getStatus()
            );
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
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
