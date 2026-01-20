package org.example.paperlessrest.service.adapter;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MinioStorageAdapterTest {

    @Mock
    private MinioClient minioClient;

    @Test
    void store_ShouldUploadFileAndReturnKey() throws Exception {
        // ARRANGE
        // Wir injizieren den Mock und einen Fake-Bucket-Namen direkt über den Konstruktor
        MinioStorageAdapter adapter = new MinioStorageAdapter(minioClient, "test-bucket");

        String filename = "testfile.pdf";
        InputStream stream = new ByteArrayInputStream("Dummy Content".getBytes());

        // ACT
        String resultKey = adapter.store(filename, "application/pdf", 100L, stream);

        // ASSERT
        // 1. Der Key darf nicht null sein
        assertNotNull(resultKey);

        // 2. Der Key muss den Dateinamen enthalten (UUID_filename)
        assertTrue(resultKey.endsWith("_" + filename));

        // 3. Wurde minioClient.putObject() aufgerufen?
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }
}