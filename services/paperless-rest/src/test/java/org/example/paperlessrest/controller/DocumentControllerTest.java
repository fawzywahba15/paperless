package org.example.paperlessrest.controller;

import org.example.paperlessrest.dto.DocumentResponseDto;
import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.mapper.DocumentMapper;
import org.example.paperlessrest.repository.DocumentRepository;
import org.example.paperlessrest.repository.ElasticSearchRepository;
import org.example.paperlessrest.search.ElasticDocument;
import org.example.paperlessrest.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private ElasticSearchRepository elasticRepository;

    @MockBean
    private DocumentMapper documentMapper;

    @MockBean
    private DocumentRepository documentRepository;

    @Test
    void upload_ShouldReturn201_WhenUploadSucceeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "Content".getBytes()
        );

        when(documentService.uploadAndDispatch(any())).thenReturn(null);

        mockMvc.perform(multipart("/api/documents")
                        .file(file))
                .andExpect(status().isCreated());
    }

    @Test
    void search_ShouldReturnList() throws Exception {
        // Mock Elastic
        ElasticDocument hit = new ElasticDocument("1", "Title", "Content");
        when(elasticRepository.fuzzySearch("query")).thenReturn(List.of(hit));

        mockMvc.perform(get("/api/documents/search").param("query", "query"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    void getDocument_ShouldReturnDto_WhenFound() throws Exception {
        UUID id = UUID.randomUUID();
        Document doc = new Document();
        doc.setId(id);
        doc.setFilename("test.pdf");

        DocumentResponseDto dto = new DocumentResponseDto(id, "test.pdf", "application/pdf", 100L, "COMPLETED");

        // Mock Repository & Mapper
        when(documentRepository.findById(id)).thenReturn(Optional.of(doc));
        when(documentMapper.entityToDto(doc)).thenReturn(dto);

        mockMvc.perform(get("/api/documents/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("test.pdf"));
    }
}