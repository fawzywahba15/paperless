package org.example.paperlessservices.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${MINIO_ENDPOINT:http://minio:9000}")
    private String endpoint;

    @Value("${MINIO_ROOT_USER:minioadmin}")
    private String accessKey;

    @Value("${MINIO_ROOT_PASSWORD:minioadmin}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}