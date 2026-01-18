package org.example.paperlessservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaperlessServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(PaperlessServiceApplication.class, args);
    }
}
