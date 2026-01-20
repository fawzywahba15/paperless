package org.example.paperlessrest.controller;

import org.example.paperlessrest.dto.DocumentResponseDto;
import org.example.paperlessrest.search.ElasticDocument;
import org.example.paperlessrest.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.doThrow;


@WebMvcTest(DocumentController.class)
@ActiveProfiles("test")
public class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Test
    void uploadDocument_ShouldReturn201() throws Exception {
        // GIVEN
        MockMultipartFile file = new MockMultipartFile(
                "file", "invoice.pdf", "application/pdf", "Dummy Content".getBytes());

        // WHEN & THEN
        mockMvc.perform(multipart("/api/documents")
                        .file(file)
                        .param("title", "Rechnung"))
                .andExpect(status().isCreated()); // Wichtig: Controller liefert 201

        verify(documentService).uploadAndDispatch(any(), eq("Rechnung"));
    }

    @Test
    void getAllDocuments_ShouldReturn200() throws Exception {
        // GIVEN
        given(documentService.getAllDocuments()).willReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk());
    }

    @Test
    void searchDocuments_ShouldReturn200() throws Exception {
        // GIVEN
        given(documentService.searchDocuments("test")).willReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/api/documents/search")
                        .param("query", "test"))
                .andExpect(status().isOk());
    }

    @Test
    void getDocument_ShouldReturn200_WhenFound() throws Exception {
        // GIVEN
        UUID id = UUID.randomUUID();
        DocumentResponseDto dto = new DocumentResponseDto(
                id, "Title", "Cat", "Sum", "file.pdf", "application/pdf", 100L, "COMPLETED", "OCR"
        );
        given(documentService.findById(id)).willReturn(Optional.of(dto));

        // WHEN & THEN
        mockMvc.perform(get("/api/documents/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void getDocument_ShouldReturn404_WhenNotFound() throws Exception {
        // GIVEN
        UUID id = UUID.randomUUID();
        given(documentService.findById(id)).willReturn(Optional.empty());

        // WHEN & THEN
        mockMvc.perform(get("/api/documents/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadDocument_ShouldReturn500_WhenServiceFails() throws Exception {
        // GIVEN
        MockMultipartFile file = new MockMultipartFile(
                "file", "error.pdf", "application/pdf", "Content".getBytes());

        // Wir simulieren eine Exception im Service
        doThrow(new RuntimeException("MinIO Error"))
                .when(documentService).uploadAndDispatch(any(), any());

        // WHEN & THEN
        mockMvc.perform(multipart("/api/documents")
                        .file(file)
                        .param("title", "Error Title"))
                .andExpect(status().isInternalServerError()); // Erwartet 500
    }
}