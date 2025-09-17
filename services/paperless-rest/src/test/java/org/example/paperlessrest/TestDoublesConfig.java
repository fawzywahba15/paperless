package org.example.paperlessrest;

import io.minio.MinioClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestDoublesConfig {

    @Bean
    @Primary
    public MinioClient minioClientMock() {
        return mock(MinioClient.class);
    }

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplateMock() {
        return mock(RabbitTemplate.class);
    }
}
