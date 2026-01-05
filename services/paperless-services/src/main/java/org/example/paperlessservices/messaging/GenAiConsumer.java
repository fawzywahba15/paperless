package org.example.paperlessservices.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.paperlessservices.dto.DocumentMessage;
import org.example.paperlessservices.entity.Document;
import org.example.paperlessservices.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.UUID;

@Component
public class GenAiConsumer {

    private static final Logger log = LoggerFactory.getLogger(GenAiConsumer.class);

    private final DocumentRepository repo;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=";

    public GenAiConsumer(DocumentRepository repo, RestTemplate restTemplate) {
        this.repo = repo;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @RabbitListener(queues = "${GENAI_QUEUE:genai.queue}")
    public void handle(DocumentMessage msg) {
        log.info("GenAI Worker received message for ID: {}", msg.documentId());
        UUID docId = msg.documentId();

        try {
            // 1. Dokument aus DB laden
            Document doc = repo.findById(docId).orElseThrow(() -> new RuntimeException("Doc not found"));

            // Check auf null UND leer
            if (doc.getOcrText() == null || doc.getOcrText().isBlank()) {
                log.warn("No OCR text found for document {}. Skipping GenAI summary.", docId);
                return;
            }

            log.info("Sending text (len={}) to Google Gemini...", doc.getOcrText().length());

            // 2. JSON Request bauen
            ObjectNode rootNode = objectMapper.createObjectNode();
            ArrayNode contentsArray = rootNode.putArray("contents");
            ObjectNode contentNode = contentsArray.addObject();
            ArrayNode partsArray = contentNode.putArray("parts");
            ObjectNode textNode = partsArray.addObject();

            String prompt = "Fasse bitte das folgende Dokument kurz und knapp auf Deutsch zusammen:\n\n" + doc.getOcrText();
            textNode.put("text", prompt);

            // 3. Request senden
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(rootNode.toString(), headers);

            String responseUrl = GEMINI_URL + apiKey;
            String jsonResponse = restTemplate.postForObject(responseUrl, request, String.class);

            // 4. Antwort parsen
            JsonNode responseRoot = objectMapper.readTree(jsonResponse);
            String summary = responseRoot.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            log.info("Gemini summary received: {}", summary);

            // 5. Speichern
            doc.setSummary(summary);
            repo.save(doc);
            log.info("Summary saved to DB for document {}", docId);

        } catch (Exception e) {
            log.error("GenAI processing failed for {}", docId, e);
        }
    }
}