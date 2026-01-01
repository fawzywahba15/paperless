package org.example.paperlessrest.controller;

import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.service.DocumentService;
import org.example.paperlessrest.service.port.OcrProducerPort; // Neu hier!
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService service;
    private final OcrProducerPort producerPort; // Wir holen den Producer hier rein

    public DocumentController(DocumentService service, OcrProducerPort producerPort) {
        this.service = service;
        this.producerPort = producerPort;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Document> upload(@RequestPart("file") MultipartFile file) throws Exception {
        // 1. Schritt: Speichern in DB & MinIO (Transaktion wird hier abgeschlossen)
        Document savedDoc = service.uploadAndDispatch(file);

        // 2. Schritt: Nachricht senden (erst JETZT, wo die DB sicher fertig ist)
        producerPort.sendForOcr(savedDoc.getId(), savedDoc.getObjectKey());

        return ResponseEntity.accepted().body(savedDoc);
    }
}