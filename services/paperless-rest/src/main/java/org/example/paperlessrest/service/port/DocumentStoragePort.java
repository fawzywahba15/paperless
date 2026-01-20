package org.example.paperlessrest.service.port;

import java.io.InputStream;

/**
 * Port-Interface für die Dateispeicherung (Hexagonal Architecture).
 * Entkoppelt die Business-Logik von der konkreten Speichertechnologie (z.B. MinIO, S3, Filesystem).
 */
public interface DocumentStoragePort {

    /** * Speichert einen Datenstrom und gibt einen eindeutigen Schlüssel zurück.
     * @return Der Object-Key (Pfad) im Storage.
     */
    String store(String filename, String contentType, long size, InputStream data) throws Exception;
}