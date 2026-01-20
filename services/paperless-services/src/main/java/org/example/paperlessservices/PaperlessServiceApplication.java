package org.example.paperlessservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Hauptklasse für den 'paperless-services' Worker.
 * <p>
 * Dieser Microservice kümmert sich um rechenintensive Aufgaben im Hintergrund:
 * <ul>
 * <li>OCR-Verarbeitung (Tesseract)</li>
 * <li>KI-Zusammenfassungen (Google Gemini)</li>
 * <li>Nächtliche Batch-Importe (XML)</li>
 * </ul>
 */
@SpringBootApplication
@EnableScheduling // Aktiviert den Scheduler für Sprint 7 (Batch Jobs)
public class PaperlessServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperlessServiceApplication.class, args);
    }
}