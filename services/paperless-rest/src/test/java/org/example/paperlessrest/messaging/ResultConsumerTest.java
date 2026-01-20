package org.example.paperlessrest.messaging;

import org.example.paperlessrest.dto.DocumentResultMessage;
import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.entity.DocumentStatus;
import org.example.paperlessrest.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultConsumerTest {

    @Mock
    private DocumentRepository repo;

    @InjectMocks
    private ResultConsumer consumer;

    @Test
    void handle_ShouldSetCompleted_WhenStatusIsCompleted() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        DocumentResultMessage msg = new DocumentResultMessage(id, "COMPLETED", "Mein OCR Text", null);

        Document doc = new Document();
        doc.setId(id);

        when(repo.findById(id)).thenReturn(Optional.of(doc));

        // ACT
        consumer.handle(msg);

        // ASSERT
        // Prüfen, ob save aufgerufen wurde mit korrektem Status und Text
        verify(repo).save(argThat(savedDoc ->
                savedDoc.getStatus().equals(String.valueOf(DocumentStatus.COMPLETED)) &&
                        savedDoc.getOcrText().equals("Mein OCR Text")
        ));
    }

    @Test
    void handle_ShouldSetFailed_WhenStatusIsNotCompleted() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        DocumentResultMessage msg = new DocumentResultMessage(id, "ERROR", null, "Fehler");

        Document doc = new Document();
        doc.setId(id);

        when(repo.findById(id)).thenReturn(Optional.of(doc));

        // ACT
        consumer.handle(msg);

        // ASSERT
        verify(repo).save(argThat(savedDoc ->
                savedDoc.getStatus().equals(String.valueOf(DocumentStatus.FAILED))
        ));
    }
}