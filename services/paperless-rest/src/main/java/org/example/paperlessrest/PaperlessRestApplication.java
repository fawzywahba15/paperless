package org.example.paperlessrest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hauptklasse für die 'paperless-rest' Anwendung.
 * <p>
 * Dieser Microservice dient als zentrale Schnittstelle (API) für das Frontend:
 * <ul>
 * <li>REST-Endpunkte für Dokumentenverwaltung</li>
 * <li>Upload & Download (MinIO)</li>
 * <li>Suchfunktionalität (ElasticSearch)</li>
 * </ul>
 */
@SpringBootApplication
public class PaperlessRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperlessRestApplication.class, args);
    }
}