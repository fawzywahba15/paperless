package org.example.paperlessrest.mapper;

import org.example.paperlessrest.dto.DocumentResponseDto;
import org.example.paperlessrest.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct Mapper für die Konvertierung zwischen Entity und DTOs.
 */
@Mapper(componentModel = "spring")
public interface DocumentMapper {

    /**
     * Konvertiert Document Entity in Response DTO.
     * Mappt das Datenbank-Feld 'ocrText' auf das Frontend-Feld 'content'.
     */
    @Mapping(target = "content", source = "ocrText")
    DocumentResponseDto entityToDto(Document document);
}