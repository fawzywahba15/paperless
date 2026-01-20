package org.example.paperlessservices.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paperlessservices.dto.DocumentMessage;
import org.example.paperlessservices.entity.Document;
import org.example.paperlessservices.repository.DocumentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Konsumiert Nachrichten aus der 'genai.queue'.
 * Sendet den OCR-Text an Google Gemini Flash und speichert die Zusammenfassung.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GenAiConsumer {

    private final DocumentRepository repo;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=";

    @RabbitListener(queues = "${GENAI_QUEUE:genai.queue}")
    public void handle(DocumentMessage msg) {
        UUID docId = msg.documentId();
        log.info("🤖 GenAI Auftrag erhalten für ID: {}", docId);

        try {
            Document doc = repo.findById(docId).orElseThrow(() -> new RuntimeException("Doc not found"));

            if (doc.getOcrText() == null || doc.getOcrText().isBlank()) {
                log.warn("Kein OCR Text vorhanden. Überspringe KI-Zusammenfassung.");
                return;
            }

            String summary = callGeminiApi(doc.getOcrText());

            doc.setSummary(summary);
            repo.save(doc);
            log.info("✅ KI-Zusammenfassung gespeichert (Länge: {}).", summary.length());

        } catch (Exception e) {
            log.error("❌ GenAI Fehler: {}", e.getMessage());
            // Wir markieren das Dokument nicht als FAILED, da OCR ja erfolgreich war.
            // Die Zusammenfassung fehlt dann halt.
        }
    }

    private String callGeminiApi(String text) throws Exception {
        // JSON Body bauen
        ObjectNode rootNode = objectMapper.createObjectNode();
        ArrayNode contentsArray = rootNode.putArray("contents");
        ObjectNode contentNode = contentsArray.addObject();
        ArrayNode partsArray = contentNode.putArray("parts");
        ObjectNode textNode = partsArray.addObject();

        String prompt = "Fasse das folgende Dokument kurz auf Deutsch zusammen:\n\n" + text;
        textNode.put("text", prompt);

        // Request senden
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(rootNode.toString(), headers);

        String responseUrl = GEMINI_URL + apiKey;
        String jsonResponse = restTemplate.postForObject(responseUrl, request, String.class);

        // Antwort parsen
        JsonNode responseRoot = objectMapper.readTree(jsonResponse);
        return responseRoot.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();
    }
}