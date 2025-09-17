package org.example.paperlessrest.controller;

import org.example.paperlessrest.TestDoublesConfig;
import org.example.paperlessrest.dto.DocumentResponseDto;
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
    MockMvc mvc;

    @MockBean
    DocumentService service;

    @Test
    void getReturns200WhenFound() throws Exception {
        UUID id = UUID.randomUUID();
        var dto = new DocumentResponseDto(id, "test.pdf", "application/pdf", 1234L, "OCR_QUEUED");
        given(service.find(id)).willReturn(Optional.of(dto));

        mvc.perform(get("/api/documents/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.filename").value("test.pdf"))
                .andExpect(jsonPath("$.status").value("OCR_QUEUED"));
    }

    @Test
    void getReturns404WhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        given(service.find(id)).willReturn(Optional.empty());

        mvc.perform(get("/api/documents/{id}", id))
                .andExpect(status().isNotFound());
    }
}
