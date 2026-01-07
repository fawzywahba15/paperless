package org.example.paperlessrest.mapper;

import org.example.paperlessrest.dto.DocumentResponseDto;
import org.example.paperlessrest.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// "componentModel = spring" macht es zur @Component -> Autowired möglich
@Mapper(componentModel = "spring")
public interface DocumentMapper {

    // MapStruct generiert die Implementierung automatisch beim Kompilieren
    @Mapping(target = "filename", source = "filename")
    DocumentResponseDto entityToDto(Document document);
}