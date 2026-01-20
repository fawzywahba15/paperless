package org.example.paperlessrest;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;

/**
 * System-Integrationstest (End-to-End).
 * Überprüft den gesamten Workflow der Anwendung gegen eine laufende Umgebung (Docker).
 *
 * <p>Test-Szenario:
 * <ol>
 * <li><b>Upload:</b> Ein PDF wird an die REST-API gesendet.</li>
 * <li><b>Verarbeitung:</b> Das System verarbeitet die Datei asynchron (OCR, GenAI).
 * Der Test wartet (Polling), bis der Status 'COMPLETED' erreicht ist.</li>
 * <li><b>Suche:</b> Überprüfung, ob das Dokument im ElasticSearch-Index gefunden wird.</li>
 * </ol>
 * </p>
 *
 * <p><b>Voraussetzung:</b> Die Docker-Container (REST, DB, RabbitMQ, etc.) müssen laufen.</p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EndToEndIntegrationTest {

    private static final String BASE_URI = "http://localhost:8081/api/documents";
    private static final String TEST_FILE_PATH = "src/test/resources/integration-test.pdf";

    // Suchbegriff, der im Test-PDF vorkommt (muss zur PDF-Datei passen!)
    private static final String SEARCH_TERM = "Test";

    // Wir speichern die ID des hochgeladenen Dokuments für nachfolgende Testschritte
    private static String uploadedDocumentId;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URI;
    }

    /**
     * Schritt 1: Dokument hochladen.
     * Sendet ein PDF per Multipart-Request an die REST-API.
     * Erwartet HTTP 201 (Created) oder 200 (OK).
     */
    @Test
    @Order(1)
    @DisplayName("1. Upload Document")
    void testUploadDocument() {
        File uploadFile = new File(TEST_FILE_PATH);

        // Fail-Fast: Wenn die Datei fehlt, sofort abbrechen
        if (!uploadFile.exists()) {
            Assertions.fail("Test-Datei nicht gefunden: " + uploadFile.getAbsolutePath());
        }

        System.out.println("🚀 [Step 1] Starte Upload...");

        given()
                .multiPart("file", uploadFile)
                .when()
                .post()
                .then()
                .statusCode(201);

        System.out.println("✅ Upload erfolgreich initiiert.");
    }

    /**
     * Schritt 2: Auf asynchrone Verarbeitung warten.
     * Nutzt Awaitility für Polling. Prüft alle 2 Sekunden, ob:
     * 1. Der Status 'COMPLETED' ist.
     * 2. OCR-Text vorhanden ist.
     * 3. Eine KI-Zusammenfassung vorhanden ist.
     */
    @Test
    @Order(2)
    @DisplayName("2. Wait for Async Processing (OCR & AI)")
    void testWaitForProcessingAndValidation() {
        System.out.println("⏳ [Step 2] Warte auf Verarbeitung (OCR, Elastic, GenAI)...");

        // Maximal 120 Sekunden warten (GenAI kann dauern)
        await().atMost(120, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> {
                    // API Abfrage: Liste aller Dokumente holen
                    String response = given()
                            .when()
                            .get()
                            .then()
                            .statusCode(200)
                            .extract().asString();

                    // Wir suchen unser Dokument in der Liste (anhand des Dateinamens)
                    return io.restassured.path.json.JsonPath.from(response)
                            .getList("findAll { it.filename == 'integration-test.pdf' }")
                            .stream()
                            .anyMatch(docObj -> {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> doc = (Map<String, Object>) docObj;

                                // ID speichern für Schritt 3
                                uploadedDocumentId = (String) doc.get("id");

                                String status = (String) doc.get("status");
                                String content = (String) doc.get("content");
                                String summary = (String) doc.get("summary");

                                if (uploadedDocumentId != null) {
                                    System.out.println("   ... Status: " + status +
                                            " | OCR-Length: " + (content != null ? content.length() : 0));
                                }

                                // Erfolgsbedingungen
                                return "COMPLETED".equals(status)
                                        && content != null && !content.isEmpty()
                                        && summary != null && !summary.isEmpty();
                            });
                });

        System.out.println("✅ Dokument wurde vollständig verarbeitet! ID: " + uploadedDocumentId);
    }

    /**
     * Schritt 3: ElasticSearch Index prüfen.
     * Führt eine Suchanfrage durch und erwartet, dass das Dokument im Ergebnis enthalten ist.
     */
    @Test
    @Order(3)
    @DisplayName("3. Verify ElasticSearch Indexing")
    void testElasticSearch() {
        System.out.println("🔍 [Step 3] Teste ElasticSearch Indexing...");

        // Sicherheitscheck: Wurde Schritt 2 erfolgreich durchlaufen?
        Assertions.assertNotNull(uploadedDocumentId, "Dokument-ID fehlt (Schritt 2 fehlgeschlagen?)");

        given()
                .queryParam("query", SEARCH_TERM)
                .when()
                .get("/search")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))          // Mindestens 1 Treffer
                .body("id", hasItem(uploadedDocumentId)); // Unsere ID muss dabei sein

        System.out.println("✅ Dokument wurde über die Suche gefunden.");
    }
}