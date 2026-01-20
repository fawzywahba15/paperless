package org.example.paperlessrest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.service.ShareService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.UUID;

/**
 * Controller für das Teilen von Dokumenten.
 * Erlaubt das Erstellen von temporären Links und den Download darüber.
 */
@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Share Controller", description = "Verwaltung von temporären Freigabe-Links")
public class ShareController {

    private final ShareService shareService;

    @Operation(summary = "Share-Link erstellen", description = "Erstellt einen zeitlich begrenzten Link für ein Dokument.")
    @PostMapping("/{id}")
    public ResponseEntity<String> createShareLink(@PathVariable UUID id) {
        log.info("Erstelle Share-Link für Dokument ID: {}", id);
        String token = shareService.createShareLink(id);

        // Wir geben die URL zurück, die der User aufrufen kann
        // HINWEIS: In Production sollte hier die echte Domain stehen, nicht localhost
        String shareUrl = "http://localhost:8081/api/share/download/" + token;
        return ResponseEntity.ok(shareUrl);
    }

    @Operation(summary = "Dokument herunterladen", description = "Lädt ein Dokument über einen gültigen Share-Token herunter (öffentlicher Zugriff).")
    @GetMapping("/download/{token}")
    public ResponseEntity<InputStreamResource> downloadViaToken(@PathVariable String token) {
        try {
            log.info("Download-Anfrage mit Token: {}", token);

            // 1. Dokument validieren & Loggen
            Document doc = shareService.getDocumentByToken(token);

            // 2. Stream holen (Logik jetzt im Service)
            InputStream stream = shareService.getFileStream(doc);

            // 3. Dateinamen bereinigen (verhindert "file.pdf.pdf")
            String downloadName = doc.getFilename();
            if (doc.getTitle() != null && !doc.getTitle().isEmpty()) {
                downloadName = doc.getTitle();
                if (!downloadName.toLowerCase().endsWith(".pdf")) {
                    downloadName += ".pdf";
                }
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(stream));

        } catch (Exception e) {
            log.error("Fehler beim Download via Token: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}