package org.example.paperlessrest.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * Repräsentiert ein Dokument im ElasticSearch-Index.
 * Enthält nur die für die Suche relevanten Daten.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "documents") // Der Index-Name in ES
public class ElasticDocument {

    @Id
    private String id;

    private String title;

    private String content;
}