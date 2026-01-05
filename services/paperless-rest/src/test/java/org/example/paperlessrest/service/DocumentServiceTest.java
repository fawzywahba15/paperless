package org.example.paperlessrest.service;

import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.repository.DocumentRepository;
import org.example.paperlessrest.service.port.DocumentStoragePort;
import org.example.paperlessrest.service.port.OcrProducerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock private DocumentRepository repo;
    @Mock private DocumentStoragePort storagePort;
    @Mock private OcrProducerPort ocrProducerPort;
    @Mock private MultipartFile file;

    private DocumentService service;

    @BeforeEach
    void setUp() {
        // Wir injizieren die Mocks
        service = new DocumentService(repo, storagePort, ocrProducerPort);
    }

    @Test
    void uploadAndDispatch_ShouldSaveAndNotifyQueue() throws Exception {
        // --- ARRANGE ---
        when(file.getOriginalFilename()).thenReturn("invoice.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(100L);
        when(file.getInputStream()).thenReturn(InputStream.nullInputStream());

        // MinIO Mock: gibt key zurück
        when(storagePort.store(any(), any(), anyLong(), any())).thenReturn("invoice.pdf");

        // DB Mock: gibt gespeichertes Doc mit ID zurück
        when(repo.saveAndFlush(any(Document.class))).thenAnswer(i -> {
            Document d = i.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        // --- ACT ---
        service.uploadAndDispatch(file);

        // --- ASSERT ---

        // 1. MinIO aufgerufen?
        verify(storagePort).store(any(), any(), anyLong(), any());

        // 2. DB aufgerufen?
        verify(repo).saveAndFlush(any(Document.class));

        // 3. Wurde RabbitMQ benachrichtigt?
        verify(ocrProducerPort).sendForOcr(any(UUID.class), eq("invoice.pdf"));
    }
}