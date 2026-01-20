package org.example.paperlessrest.controller;

import org.example.paperlessrest.entity.Document;
import org.example.paperlessrest.service.ShareService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShareController.class)
@ActiveProfiles("test")
class ShareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShareService shareService;

    @Test
    void createShareLink_ShouldReturnUrl() throws Exception {
        UUID id = UUID.randomUUID();
        given(shareService.createShareLink(id)).willReturn("token123");

        mockMvc.perform(post("/api/share/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("token123")));
    }

    @Test
    void downloadViaToken_ShouldReturnFile_WhenValid() throws Exception {
        // ARRANGE
        String token = "validToken";
        Document doc = new Document();
        doc.setFilename("test.pdf");
        doc.setTitle("Mein Dokument"); // Testet die Umbenennungs-Logik im Controller

        given(shareService.getDocumentByToken(token)).willReturn(doc);
        given(shareService.getFileStream(doc)).willReturn(new ByteArrayInputStream("PDF Content".getBytes()));

        // ACT & ASSERT
        mockMvc.perform(get("/api/share/download/" + token))
                .andExpect(status().isOk())
                .andExpect(content().bytes("PDF Content".getBytes()));
    }

    @Test
    void downloadViaToken_ShouldReturnBadRequest_WhenExceptionOccurs() throws Exception {
        // ARRANGE - Wir simulieren einen Fehler im Service
        String token = "invalidToken";
        given(shareService.getDocumentByToken(token)).willThrow(new RuntimeException("Token expired"));

        // ACT & ASSERT (Testet den Catch-Block im Controller)
        mockMvc.perform(get("/api/share/download/" + token))
                .andExpect(status().isBadRequest());
    }
}