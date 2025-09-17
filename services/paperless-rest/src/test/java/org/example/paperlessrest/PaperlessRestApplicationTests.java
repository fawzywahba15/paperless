package org.example.paperlessrest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDoublesConfig.class)
class PaperlessRestApplicationTests {

    @Test
    void contextLoads() {
    }

}
