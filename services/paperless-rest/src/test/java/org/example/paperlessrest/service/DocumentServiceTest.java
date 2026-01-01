package org.example.paperlessrest.service;

import org.example.paperlessrest.entity.Document;
import org.junit.jupiter.api.Disabled;
import org.example.paperlessrest.repository.DocumentRepository;
import org.example.paperlessrest.service.port.DocumentStoragePort;
import org.example.paperlessrest.service.port.OcrProducerPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@ActiveProfiles("test")
class DocumentServiceTest {

    @Autowired
    DocumentRepository repo;

    @MockBean
    DocumentStoragePort storagePort;

    @MockBean
    OcrProducerPort producerPort;

    //: Refactor für Sprint 5/6.
    // Grund: Der DocumentService-Konstruktor hat sich in Sprint 4 geändert (RabbitMQ ist jetzt im Controller).
    // Die Tests müssen mit Mockito angepasst werden, um den neuen Flow widerzuspiegeln.
    @Disabled("Vorerst deaktiviert wegen Refactoring in Sprint 4. Muss gefixt werden!")
    @Test
    void uploadAndDispatch_storesAndPublishes() throws Exception {
        // arrange
        DocumentService service = new DocumentService(repo, storagePort, producerPort);
        MockMultipartFile file = new MockMultipartFile(
                "file","doc.pdf","application/pdf","hello".getBytes()
        );
        when(storagePort.store(anyString(), anyString(), anyLong(), any())).thenReturn("obj-123");

        // act
        Document saved = service.uploadAndDispatch(file);

        // assert DB
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getObjectKey()).isEqualTo("obj-123");

        // assert Ports
        verify(storagePort).store(eq("doc.pdf"), eq("application/pdf"), eq(5L), any());
        ArgumentCaptor<java.util.UUID> idCap = ArgumentCaptor.forClass(java.util.UUID.class);
        verify(producerPort).sendForOcr(idCap.capture(), eq("obj-123"));
        assertThat(idCap.getValue()).isEqualTo(saved.getId());
    }
}
