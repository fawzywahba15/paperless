package org.example.paperlessrest.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class EntityStructureTest {

    @Test
    void testDocumentEntity() {
        // Test für @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
        UUID id = UUID.randomUUID();

        // 1. Test Builder
        Document doc = Document.builder()
                .id(id)
                .title("Title")
                .filename("file.pdf")
                .contentType("application/pdf")
                .size(100L) // long
                .category("Invoice")
                .objectKey("minio-key")
                .status("COMPLETED")
                .ocrText("OCR Content")
                .summary("AI Summary")
                .build();

        // 2. Test Getters
        assertEquals(id, doc.getId());
        assertEquals("Title", doc.getTitle());
        assertEquals("file.pdf", doc.getFilename());
        assertEquals("application/pdf", doc.getContentType());
        assertEquals(100L, doc.getSize());
        assertEquals("Invoice", doc.getCategory());
        assertEquals("minio-key", doc.getObjectKey());
        assertEquals("COMPLETED", doc.getStatus());
        assertEquals("OCR Content", doc.getOcrText());
        assertEquals("AI Summary", doc.getSummary());

        // 3. Test Setters & NoArgs Constructor
        Document doc2 = new Document();
        doc2.setTitle("New Title");
        assertEquals("New Title", doc2.getTitle());

        // 4. Test toString / equals / hashCode (Lombok Coverage)
        assertNotNull(doc.toString());
        assertNotEquals(doc, doc2);
        assertNotEquals(doc.hashCode(), doc2.hashCode());
    }

    @Test
    void testShareLinkEntity() {
        // 1. Setup
        ShareLink link = new ShareLink();
        Long id = 1L;
        LocalDateTime now = LocalDateTime.now();
        Document doc = Document.builder().id(UUID.randomUUID()).build();

        // 2. Setters
        link.setId(id);
        link.setToken("token_123");
        link.setExpiresAt(now);
        link.setDocument(doc);
        link.setAccessLogs(new ArrayList<>());

        // 3. Getters
        assertEquals(id, link.getId());
        assertEquals("token_123", link.getToken());
        assertEquals(now, link.getExpiresAt());
        assertEquals(doc, link.getDocument());
        assertNotNull(link.getAccessLogs());

        // 4. Builder Test
        ShareLink link2 = ShareLink.builder().token("builder").build();
        assertEquals("builder", link2.getToken());

        // 5. toString (mit @ToString.Exclude check)
        assertNotNull(link.toString());
    }

    @Test
    void testAccessLogEntity() {
        AccessLog log = new AccessLog();
        log.setId(1L);
        log.setLogMessage("Test");
        log.setSuccessful(true);

        assertEquals(1L, log.getId());
        assertEquals("Test", log.getLogMessage());
        assertTrue(log.isSuccessful());
    }
}