package org.example.paperlessrest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paperlessrest.dto.DocumentResponseDto;
import org.example.paperlessrest.search.ElasticDocument;
import org.example.paperlessrest.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * REST-Controller für die Dokumentenverwaltung.
 * Bildet die Schnittstelle zum Frontend (Angular) und delegiert
 * die Geschäftslogik an den {@link DocumentService}.
 */
@RestController
@RequestMapping("/api/documents")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Document Controller", description = "API für Upload, Suche und Verwaltung von Dokumenten")
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "Dokument hochladen", description = "Nimmt ein PDF entgegen, speichert es im Object-Storage und startet den asynchronen OCR-Prozess.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponse(responseCode = "201", description = "Dokument erfolgreich hochgeladen.")
    public ResponseEntity<Void> uploadDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title
    ) {
        try {
            log.info("REST Request: Upload Dokument '{}'", file.getOriginalFilename());
            documentService.uploadAndDispatch(file, title);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Fehler beim Upload des Dokuments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Alle Dokumente laden", description = "Liefert eine Liste aller persistierten Dokumente.")
    @GetMapping
    public ResponseEntity<List<DocumentResponseDto>> getAllDocuments() {
        log.info("REST Request: Alle Dokumente abrufen");
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @Operation(summary = "Dokument suchen", description = "Volltextsuche über ElasticSearch (Titel & Inhalt).")
    @GetMapping("/search")
    public ResponseEntity<List<ElasticDocument>> searchDocuments(@RequestParam("query") String query) {
        log.info("REST Request: Suche nach '{}'", query);
        return ResponseEntity.ok(documentService.searchDocuments(query));
    }

    @Operation(summary = "Dokumentdetails laden", description = "Liefert Metadaten und OCR-Text eines spezifischen Dokuments.")
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDto> getDocument(@PathVariable UUID id) {
        log.info("REST Request: Dokument Details für ID {}", id);
        return documentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}