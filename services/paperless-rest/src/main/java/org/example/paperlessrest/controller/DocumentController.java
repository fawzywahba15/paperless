package org.example.paperlessrest.controller;

import org.example.paperlessrest.dto.DocumentRequestDto;
import org.example.paperlessrest.repository.ElasticSearchRepository;
import org.example.paperlessrest.search.ElasticDocument;
import org.example.paperlessrest.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentController {

    private final DocumentService documentService;
    private final ElasticSearchRepository elasticRepository;


    public DocumentController(DocumentService documentService, ElasticSearchRepository elasticRepository) {
        this.documentService = documentService;
        this.elasticRepository = elasticRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title
    ) {
        try {
            DocumentRequestDto dto = new DocumentRequestDto();
            dto.setTitle(title != null ? title : file.getOriginalFilename());
            dto.setCategory("Uncategorized");

            documentService.uploadAndDispatch(file);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // --- search endpunkt ---
    @GetMapping("/search")
    public ResponseEntity<List<ElasticDocument>> searchDocuments(@RequestParam("query") String query) {
        // Sucht nach Dokumenten, die den Query-String im Content enthalten
        List<ElasticDocument> results = elasticRepository.fuzzySearch(query);
        return ResponseEntity.ok(results);
    }
}