package org.example.paperlessrest.controller;

import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Document> upload(@RequestPart("file") MultipartFile file) throws Exception {
        Document saved = service.uploadAndDispatch(file);
        return ResponseEntity.accepted().body(saved);
    }
}
