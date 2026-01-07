package org.example.paperlessrest.repository;

import org.example.paperlessrest.search.ElasticDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElasticSearchRepository extends ElasticsearchRepository<ElasticDocument, String> {

    // "fuzziness": "AUTO" erlaubt Tippfehler
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"content\", \"title\"], \"fuzziness\": \"AUTO\"}}")
    List<ElasticDocument> fuzzySearch(String query);
}