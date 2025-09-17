package org.example.paperlessrest.controller;

import org.example.paperlessrest.service.DocumentService;
import org.example.paperlessrest.dto.DocumentResponseDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponseDto> upload(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(service.upload(file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDto> get(@PathVariable UUID id) {
        Optional<DocumentResponseDto> doc = service.find(id);
        return doc.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
