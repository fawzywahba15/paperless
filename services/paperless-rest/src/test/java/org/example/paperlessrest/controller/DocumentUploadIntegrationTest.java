package org.example.paperlessrest.controller;

import org.example.paperlessrest.mapper.DocumentMapper;
import org.example.paperlessrest.repository.DocumentRepository;
import org.example.paperlessrest.repository.ElasticSearchRepository;
import org.example.paperlessrest.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@WebMvcTest(DocumentController.class)
@ActiveProfiles("test")
public class DocumentUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private ElasticSearchRepository elasticSearchRepository;

    @MockBean
    private DocumentRepository documentRepository;

    @MockBean
    private DocumentMapper documentMapper;

    @Test
    void whenUploadValidPdf_thenReturns201() throws Exception {
        // 1. Mock PDF
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "integration-test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Dummy Content".getBytes()
        );

        // 2. Perform Request
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/documents")
                        .file(pdfFile))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        // 3. Verifiziere Interaktion
        verify(documentService, times(1)).uploadAndDispatch(any());
    }
}