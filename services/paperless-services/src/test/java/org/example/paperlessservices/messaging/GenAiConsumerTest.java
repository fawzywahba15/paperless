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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-Test für den GenAiConsumer.
 * Prüft, ob die Google Gemini API korrekt aufgerufen und das Ergebnis gespeichert wird.
 */
@ExtendWith(MockitoExtension.class)
class GenAiConsumerTest {

    @Mock private DocumentRepository repo;
    @Mock private RestTemplate restTemplate;

    private GenAiConsumer consumer;

    @BeforeEach
    void setUp() {
        // Manuelle Injection, um volle Kontrolle zu haben
        consumer = new GenAiConsumer(repo, restTemplate);

        // Simuliert den API-Key aus application.properties
        ReflectionTestUtils.setField(consumer, "apiKey", "dummy-test-key");
    }

    @Test
    void handle_ShouldSummarize_WhenTextExists() {
        // --- ARRANGE ---
        UUID docId = UUID.randomUUID();
        DocumentMessage msg = new DocumentMessage(docId, "file.pdf");

        Document doc = new Document();
        doc.setId(docId);
        doc.setOcrText("Das ist ein sehr langer Text, der zusammengefasst werden soll.");

        when(repo.findById(docId)).thenReturn(Optional.of(doc));

        // Wir mocken die Antwort von Google (JSON Struktur)
        String fakeJson = """
            {
                "candidates": [
                    { "content": { "parts": [ { "text": "Zusammenfassung OK" } ] } }
                ]
            }
            """;
        // Wenn RestTemplate aufgerufen wird, gib unser Fake-JSON zurück
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn(fakeJson);

        // --- ACT ---
        consumer.handle(msg);

        // --- ASSERT ---
        // Wurde die Zusammenfassung im Dokument gesetzt?
        assertEquals("Zusammenfassung OK", doc.getSummary());

        // Wurde gespeichert?
        verify(repo).save(doc);
    }
}