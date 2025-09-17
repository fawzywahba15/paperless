package org.example.paperlessrest.service.port;

import java.io.InputStream;

public interface DocumentStoragePort {
    /** Speichert den Stream und gibt einen objectKey zurück (z.B. MinIO key). */
    String store(String filename, String contentType, long size, InputStream data) throws Exception;
}
