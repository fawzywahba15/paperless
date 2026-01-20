package org.example.paperlessservices.amqp;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Zentrale Konfiguration für Messaging (RabbitMQ) und externe Tools.
 * Definiert Queues, Message-Converter sowie Beans für Tesseract OCR und RestTemplate.
 */
@EnableRabbit
@Configuration
public class AmqpConfig {

    @Value("${OCR_QUEUE:ocr.queue}")
    private String ocrQueueName;

    @Value("${RESULT_QUEUE:result.queue}")
    private String resultQueueName;

    @Value("${GENAI_QUEUE:genai.queue}")
    private String genAiQueueName;

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

    // --- Queue Definitionen ---

    @Bean
    public Queue ocrQueue() {
        return new Queue(ocrQueueName, true);
    }

    @Bean
    public Queue resultQueue() {
        return new Queue(resultQueueName, true);
    }

    @Bean
    public Queue genAiQueue() {
        return new Queue(genAiQueueName, true);
    }

    // --- Converter & Template ---

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        // Konvertiert Java-Objekte automatisch in JSON für die Queue
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter conv) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(conv);
        return tpl;
    }

    // --- Externe Tool Beans ---

    /**
     * Konfiguriert die Tesseract OCR Engine.
     * Der Datapath zeigt auf das Verzeichnis im Docker-Container (/usr/share/tesseract-ocr/...).
     */
    @Bean
    public ITesseract tesseract() {
        ITesseract instance = new Tesseract();
        instance.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
        instance.setLanguage("eng");
        return instance;
    }

    /**
     * RestTemplate für HTTP-Aufrufe an externe APIs (z.b. google gemini).
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}