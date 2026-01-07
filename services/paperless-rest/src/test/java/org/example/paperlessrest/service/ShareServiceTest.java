package org.example.paperlessrest.service;

import org.example.paperlessrest.entity.AccessLog;
import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.entity.ShareLink;
import org.example.paperlessrest.repository.AccessLogRepository;
import org.example.paperlessrest.repository.DocumentRepository;
import org.example.paperlessrest.repository.ShareLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareServiceTest {

    @Mock private ShareLinkRepository shareLinkRepository;
    @Mock private AccessLogRepository accessLogRepository;
    @Mock private DocumentRepository documentRepository;

    private ShareService shareService;

    @BeforeEach
    void setUp() {
        shareService = new ShareService(shareLinkRepository, accessLogRepository, documentRepository);
    }

    @Test
    void createShareLink_ShouldGenerateToken() {
        // Arrange
        UUID docId = UUID.randomUUID();
        Document doc = Document.builder().id(docId).filename("test.pdf").build();
        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        // Act
        String token = shareService.createShareLink(docId);

        // Assert
        assertNotNull(token);
        assertEquals(8, token.length()); // Wir nutzen ja substring(0, 8)

        // Wurde gespeichert?
        verify(shareLinkRepository).save(any(ShareLink.class));
    }

    @Test
    void getDocumentByToken_ShouldReturnDoc_WhenTokenValid() {
        // Arrange
        String token = "valid123";
        Document doc = Document.builder().id(UUID.randomUUID()).build();
        ShareLink link = ShareLink.builder()
                .token(token)
                .document(doc)
                .expiresAt(LocalDateTime.now().plusDays(1)) // Gültig
                .build();

        when(shareLinkRepository.findByToken(token)).thenReturn(Optional.of(link));

        // Act
        Document result = shareService.getDocumentByToken(token);

        // Assert
        assertEquals(doc, result);
        // Access Log muss geschrieben werden
        verify(accessLogRepository).save(any(AccessLog.class));
    }

    @Test
    void getDocumentByToken_ShouldThrow_WhenExpired() {
        // Arrange
        String token = "expired";
        ShareLink link = ShareLink.builder()
                .token(token)
                .expiresAt(LocalDateTime.now().minusDays(1)) // Abgelaufen
                .build();

        when(shareLinkRepository.findByToken(token)).thenReturn(Optional.of(link));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> shareService.getDocumentByToken(token));

        // Trotzdem Loggen (Failure Log)
        verify(accessLogRepository).save(any(AccessLog.class));
    }
}