package org.example.paperlessrest.repository;

import org.example.paperlessrest.TestDoublesConfig;
import org.example.paperlessrest.entity.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(TestDoublesConfig.class)
class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository repo;

    @Test
    void saveAndFindById() {
        Document d = new Document();
        d.setFilename("test.pdf");
        d.setContentType("application/pdf");
        d.setSize(1234);
        d.setObjectKey("docs/abc");
        d.setStatus("NEW");
        // UUID wird bei GenerationType.UUID von Hibernate gesetzt; wir lassen id null
        Document saved = repo.save(d);

        assertThat(saved.getId()).isNotNull();

        var found = repo.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFilename()).isEqualTo("test.pdf");
    }
}
