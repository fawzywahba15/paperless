package org.example.paperlessservices.repository;

import org.example.paperlessservices.search.ElasticDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Schnittstelle zum ElasticSearch-Cluster.
 * Ermöglicht das Indizieren von Dokumenten (Speichern für die Suche).
 */
@Repository
public interface ElasticSearchRepository extends ElasticsearchRepository<ElasticDocument, String> {
}