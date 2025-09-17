package org.example.paperlessservice.amqp;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class AmqpConfig {

    @Value("${OCR_QUEUE:ocr.queue}")
    private String ocrQueueName;

    @Value("${RESULT_QUEUE:result.queue}")
    private String resultQueueName;

    @Value("${RABBITMQ_HOST:rabbitmq}")
    private String rabbitHost;

    @Value("${RABBITMQ_USER:guest}")
    private String rabbitUser;

    @Value("${RABBITMQ_PASS:guest}")
    private String rabbitPass;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory f = new CachingConnectionFactory(rabbitHost);
        f.setUsername(rabbitUser);
        f.setPassword(rabbitPass);
        return f;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        return new RabbitTemplate(cf);
    }

    @Bean
    public Queue ocrQueue() {
        return new Queue(ocrQueueName, true);
    }

    @Bean
    public Queue resultQueue() {
        return new Queue(resultQueueName, true);
    }
}
