package org.example.paperlessservices.repository; // <--- Package beachten

import org.example.paperlessservices.search.ElasticDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticSearchRepository extends ElasticsearchRepository<ElasticDocument, String> {
    // Der Worker muss meistens nur speichern (save), daher brauchen wir hier noch keine speziellen Suchmethoden.
    // Die Standard-Methoden (save, findById, delete) sind automatisch da.
}