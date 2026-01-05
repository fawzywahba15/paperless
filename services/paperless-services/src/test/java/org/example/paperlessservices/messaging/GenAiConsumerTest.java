package org.example.paperlessservices.messaging;

import org.example.paperlessservices.dto.DocumentMessage;
import org.example.paperlessservices.entity.Document;
import org.example.paperlessservices.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenAiConsumerTest {

    @Mock private DocumentRepository repo;
    @Mock private RestTemplate restTemplate;

    private GenAiConsumer consumer;

    @BeforeEach
    void setUp() {
        // Injection
        consumer = new GenAiConsumer(repo, restTemplate);
        // API Key via Reflection setzen
        ReflectionTestUtils.setField(consumer, "apiKey", "dummy-key");
    }

    @Test
    void handle_ShouldSummarize_WhenTextExists() {
        UUID docId = UUID.randomUUID();
        DocumentMessage msg = new DocumentMessage(docId, "file.pdf");

        Document doc = new Document();
        doc.setId(docId);
        doc.setOcrText("Das ist ein sehr langer Text.");

        when(repo.findById(docId)).thenReturn(Optional.of(doc));

        // Mock Google Antwort
        String fakeJson = """
            {
                "candidates": [
                    { "content": { "parts": [ { "text": "Zusammenfassung OK" } ] } }
                ]
            }
            """;
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn(fakeJson);

        consumer.handle(msg);

        assertEquals("Zusammenfassung OK", doc.getSummary());
        verify(repo).save(doc);
    }
}