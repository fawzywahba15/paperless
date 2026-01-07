package org.example.paperlessservices.repository; // <--- Package beachten

import org.example.paperlessservices.search.ElasticDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticSearchRepository extends ElasticsearchRepository<ElasticDocument, String> {
}