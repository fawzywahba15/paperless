package org.example.paperlessrest.dto;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DtoStructureTest {

    @Test
    void testDocumentRequestDto() {
        // Class mit @Data
        DocumentRequestDto dto = new DocumentRequestDto();
        dto.setTitle("Test Title");
        dto.setCategory("Test Category");

        assertEquals("Test Title", dto.getTitle());
        assertEquals("Test Category", dto.getCategory());

        // Coverage für Lombok Methoden
        DocumentRequestDto dto2 = new DocumentRequestDto();
        dto2.setTitle("Test Title");
        dto2.setCategory("Test Category");

        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());
        assertNotNull(dto.toString());
    }

    @Test
    void testDocumentResponseDto() {
        // Record
        UUID id = UUID.randomUUID();
        DocumentResponseDto dto = new DocumentResponseDto(
                id,
                "Title",
                "Category",
                "Summary",
                "file.pdf",
                "application/pdf",
                500L,
                "PENDING",
                "Content"
        );

        // Record Accessors
        assertEquals(id, dto.id());
        assertEquals("Title", dto.title());
        assertEquals("Category", dto.category());
        assertEquals("Summary", dto.summary());
        assertEquals("file.pdf", dto.filename());
        assertEquals("application/pdf", dto.contentType());
        assertEquals(500L, dto.size());
        assertEquals("PENDING", dto.status());
        assertEquals("Content", dto.content());

        // Records haben automatische toString/equals
        assertNotNull(dto.toString());
        assertEquals(dto, dto);
    }

    @Test
    void testDocumentMessage() {
        // Record
        UUID id = UUID.randomUUID();
        DocumentMessage msg = new DocumentMessage(id, "obj-key");

        assertEquals(id, msg.documentId());
        assertEquals("obj-key", msg.objectKey());
        assertNotNull(msg.toString());
    }

    @Test
    void testDocumentResultMessage() {
        // Record
        UUID id = UUID.randomUUID();
        DocumentResultMessage res = new DocumentResultMessage(id, "FAILED", null, "Error Msg");

        assertEquals(id, res.documentId());
        assertEquals("FAILED", res.status());
        assertNull(res.ocrText());
        assertEquals("Error Msg", res.error());
        assertNotNull(res.toString());
    }
}