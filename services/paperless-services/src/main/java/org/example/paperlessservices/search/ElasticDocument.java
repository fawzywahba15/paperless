package org.example.paperlessservices.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * Repräsentation eines Dokuments im Suchindex (ElasticSearch).
 * Enthält nur die Felder, die für die Volltextsuche relevant sind.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "documents")
public class ElasticDocument {

    @Id
    private String id;

    private String title;

    private String content;
}