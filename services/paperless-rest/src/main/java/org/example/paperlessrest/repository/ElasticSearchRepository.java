package org.example.paperlessrest.repository;

import org.example.paperlessrest.search.ElasticDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ElasticSearch Repository für die Volltextsuche.
 */
@Repository
public interface ElasticSearchRepository extends ElasticsearchRepository<ElasticDocument, String> {

    /**
     * Führt eine Fuzzy-Suche über Titel und Inhalt durch.
     * "fuzziness": "AUTO" erlaubt leichte Tippfehler (z.B. "Rchnug" findet "Rechnung").
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"content\", \"title\"], \"fuzziness\": \"AUTO\"}}")
    List<ElasticDocument> fuzzySearch(String query);
}