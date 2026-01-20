package org.example.paperlessrest.service.adapter;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.example.paperlessrest.service.port.DocumentStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.UUID;

/**
 * MinIO-Implementierung des Storage-Ports.
 * Speichert Dateien in einem S3-kompatiblen Bucket.
 */
@Component
public class MinioStorageAdapter implements DocumentStoragePort {

    private final MinioClient client;
    private final String bucket;

    public MinioStorageAdapter(MinioClient client,
                               @Value("${minio.bucket}") String bucket) {
        this.client = client;
        this.bucket = bucket;
    }

    @Override
    public String store(String filename, String contentType, long size, InputStream data) throws Exception {
        // Generiere eindeutigen Key, um Namenskollisionen zu vermeiden
        String objectKey = UUID.randomUUID() + "_" + filename;

        client.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .contentType(contentType)
                        .stream(data, size, -1)
                        .build()
        );
        return objectKey;
    }
}