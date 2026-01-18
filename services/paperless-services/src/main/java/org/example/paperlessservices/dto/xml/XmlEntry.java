package org.example.paperlessservices.dto.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class XmlEntry {
    @JacksonXmlProperty(localName = "DocumentId")
    private Long documentId;

    @JacksonXmlProperty(localName = "User")
    private String user;

    @JacksonXmlProperty(localName = "AccessType")
    private String accessType;
}