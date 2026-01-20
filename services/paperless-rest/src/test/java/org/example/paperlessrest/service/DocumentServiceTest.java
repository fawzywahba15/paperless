package org.example.paperlessrest.service;

import org.example.paperlessrest.dto.DocumentResponseDto;
import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.entity.DocumentStatus;
import org.example.paperlessrest.mapper.DocumentMapper;
import org.example.paperlessrest.repository.DocumentRepository;
import org.example.paperlessrest.repository.ElasticSearchRepository;
import org.example.paperlessrest.service.port.DocumentStoragePort;
import org.example.paperlessrest.service.port.OcrProducerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock private DocumentRepository repo;
    @Mock private ElasticSearchRepository elasticRepository;
    @Mock private DocumentMapper mapper;
    @Mock private DocumentStoragePort storagePort;
    @Mock private OcrProducerPort ocrProducerPort;
    @Mock private MultipartFile file;

    @InjectMocks
    private DocumentService service;

    @Test
    void uploadAndDispatch_ShouldSaveAndNotifyQueue() throws Exception {
        // --- ARRANGE ---
        when(file.getOriginalFilename()).thenReturn("invoice.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(100L);
        when(file.getInputStream()).thenReturn(InputStream.nullInputStream());

        // MinIO Mock: gibt key zurück
        when(storagePort.store(any(), any(), anyLong(), any())).thenReturn("minio-key-123");

        // DB Mock: Wir simulieren das Speichern
        when(repo.saveAndFlush(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            doc.setId(UUID.randomUUID()); // Simuliere generierte ID
            return doc;
        });

        // --- ACT ---
        service.uploadAndDispatch(file, "Mein Titel");

        // --- ASSERT ---
        // 1. MinIO Store aufgerufen
        verify(storagePort).store(eq("invoice.pdf"), any(), anyLong(), any());

        // 2. DB Save aufgerufen
        verify(repo).saveAndFlush(argThat(doc ->
                doc.getTitle().equals("Mein Titel") &&
                        doc.getStatus().equals("PENDING")
        ));

        // 3. RabbitMQ Nachricht gesendet
        verify(ocrProducerPort).sendForOcr(any(UUID.class), eq("minio-key-123"));
    }

    @Test
    void findById_ShouldReturnMappedDto() {
        UUID id = UUID.randomUUID();
        Document doc = new Document();
        doc.setId(id);

        when(repo.findById(id)).thenReturn(Optional.of(doc));

        service.findById(id);

        // Verifiziere, dass der Mapper benutzt wurde
        verify(mapper).entityToDto(doc);
    }
}