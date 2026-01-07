package org.example.paperlessrest.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}") private String endpoint;
    @Value("${minio.access}")   private String access;
    @Value("${minio.secret}")   private String secret;
    @Value("${minio.bucket}")   private String bucket;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder().endpoint(endpoint).credentials(access, secret).build();
    }

    @Bean
    @ConditionalOnProperty(name = "minio.autocreate", havingValue = "true", matchIfMissing = true)
    public CommandLineRunner createBucketIfMissing(MinioClient client) {
        return args -> {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        };
    }
}

