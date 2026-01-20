package org.example.paperlessrest.mapper;

import org.example.paperlessrest.dto.DocumentResponseDto;
import org.example.paperlessrest.entity.Document;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaperlessMapperTest {

    // Holt die Instanz des generierten Mappers
    private final DocumentMapper mapper = Mappers.getMapper(DocumentMapper.class);

    @Test
    void shouldMapEntityToDto() {
        // ARRANGE
        UUID id = UUID.randomUUID();
        Document doc = new Document();
        doc.setId(id);
        doc.setTitle("Test Title");
        doc.setCategory("Rechnung");
        doc.setFilename("test.pdf");
        doc.setContentType("application/pdf");
        doc.setSize(1234L);
        doc.setStatus("COMPLETED");
        doc.setOcrText("Extrahierter Text");
        doc.setSummary("KI Zusammenfassung");

        // ACT
        DocumentResponseDto dto = mapper.entityToDto(doc);

        // ASSERT
        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals("Test Title", dto.title());
        assertEquals("Rechnung", dto.category());
        assertEquals("Extrahierter Text", dto.content());
        assertEquals("KI Zusammenfassung", dto.summary());
    }
}