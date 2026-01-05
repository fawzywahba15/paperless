package org.example.paperlessrest.controller;

import org.example.paperlessrest.repository.ElasticSearchRepository;
import org.example.paperlessrest.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private ElasticSearchRepository elasticRepository;


    @Test
    void upload_ShouldReturn201_WhenUploadSucceeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "Dummy Content".getBytes()
        );

        // check dass der Service-Mock nichts tut (Success Case)
        when(documentService.uploadAndDispatch(any())).thenReturn(null);

        mockMvc.perform(multipart("/api/documents")
                        .file(file)
                        .param("title", "Mein Dokument"))
                .andExpect(status().isCreated());
    }
}