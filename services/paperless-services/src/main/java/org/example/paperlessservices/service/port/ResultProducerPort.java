package org.example.paperlessservices.service.port;

import java.util.UUID;

/**
 * Port-Interface für das Versenden von Verarbeitungsergebnissen (Hexagonal Architecture).
 * Definiert, wie der Worker den Status (Erfolg/Fehler) zurückmeldet, unabhängig vom Übertragungsweg.
 */
public interface ResultProducerPort {

    /**
     * Meldet eine erfolgreiche Verarbeitung zurück.
     * @param documentId Die ID des Dokuments.
     * @param ocrText Der extrahierte Text.
     */
    void publishCompleted(UUID documentId, String ocrText);

    /**
     * Meldet einen Fehler bei der Verarbeitung.
     * @param documentId Die ID des Dokuments.
     * @param error Die Fehlermeldung.
     */
    void publishFailed(UUID documentId, String error);
}