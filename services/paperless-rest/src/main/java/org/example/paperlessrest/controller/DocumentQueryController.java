package org.example.paperlessrest.controller;

import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.repository.DocumentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentQueryController {

    private final DocumentRepository repo;

    public DocumentQueryController(DocumentRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> get(@PathVariable UUID id){
        return repo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
