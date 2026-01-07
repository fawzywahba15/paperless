package org.example.paperlessrest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.paperlessrest.dto.DocumentRequestDto;
import org.example.paperlessrest.dto.DocumentResponseDto;
import org.example.paperlessrest.mapper.DocumentMapper;
import org.example.paperlessrest.repository.DocumentRepository;
import org.example.paperlessrest.repository.ElasticSearchRepository;
import org.example.paperlessrest.search.ElasticDocument;
import org.example.paperlessrest.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
@Tag(name = "Document Controller", description = "Endpoints for managing documents")
public class DocumentController {

    private final DocumentService documentService;
    private final ElasticSearchRepository elasticRepository;
    private final DocumentMapper documentMapper;
    private final DocumentRepository documentRepository;

    public DocumentController(DocumentService documentService,
                              ElasticSearchRepository elasticRepository,
                              DocumentMapper documentMapper,
                              DocumentRepository documentRepository) {
        this.documentService = documentService;
        this.elasticRepository = elasticRepository;
        this.documentMapper = documentMapper;
        this.documentRepository = documentRepository;
    }

    @Operation(summary = "Upload a document", description = "Uploads a PDF file and triggers OCR processing.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title
    ) {
        try {
            DocumentRequestDto dto = new DocumentRequestDto();
            dto.setTitle(title != null ? title : file.getOriginalFilename());
            dto.setCategory("Uncategorized");

            log.info("Uploading document: {}", dto.getTitle());

            documentService.uploadAndDispatch(file);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            log.error("Upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Operation(summary = "Search documents", description = "Performs a fuzzy search on content and title via ElasticSearch.")
    @GetMapping("/search")
    public ResponseEntity<List<ElasticDocument>> searchDocuments(@RequestParam("query") String query) {
        log.info("Searching for: {}", query);
        List<ElasticDocument> results = elasticRepository.fuzzySearch(query);
        return ResponseEntity.ok(results);
    }

    //Mapstruct
    @Operation(summary = "Get document by ID", description = "Returns document details as DTO using MapStruct conversion.")
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponseDto> getDocument(@PathVariable UUID id) {
        log.info("Fetching document with ID: {}", id);
        return documentRepository.findById(id)
                //Entity -> DTO via MapStruct
                .map(documentMapper::entityToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}