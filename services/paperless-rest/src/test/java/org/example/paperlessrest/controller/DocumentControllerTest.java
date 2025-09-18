package org.example.paperlessrest.controller;

import org.example.paperlessrest.TestDoublesConfig;
import org.example.paperlessrest.dto.DocumentResponseDto;
import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.entity.DocumentStatus;
import org.example.paperlessrest.repository.DocumentRepository;
import org.example.paperlessrest.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.given;

@WebMvcTest(DocumentController.class)
@ActiveProfiles("test")
@Import(TestDoublesConfig.class)
class DocumentControllerTest {


    @Autowired
    DocumentRepository repo;
    @Autowired
    MockMvc mvc;

    @MockBean
    DocumentService service;

    @Test
    void getReturns200WhenFound() throws Exception {
        var id = UUID.randomUUID();
        var doc = new Document();
        doc.setId(id);
        doc.setFilename("x.pdf");
        doc.setContentType("application/pdf");
        doc.setSize(1L);
        doc.setObjectKey("k");
        doc.setStatus(String.valueOf(DocumentStatus.PENDING));
        repo.save(doc);

        mvc.perform(get("/api/documents/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void getReturns404WhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        given(service.find(id)).willReturn(Optional.empty());

        mvc.perform(get("/api/documents/{id}", id))
                .andExpect(status().isNotFound());
    }
}
