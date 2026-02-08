package com.example.instructions.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.boot.ssl.SslBundles;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${app.kafka.retry.maxAttempts:1}")
    private long maxAttempts;

    @Value("${app.kafka.retry.backoffMs:10}")
    private long backoffMs;

    @Value("${spring.kafka.listener.concurrency:1}")
    private int concurrency;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            KafkaProperties kafkaProperties,
            SslBundles sslBundles ) {
        var consumerProps = kafkaProperties.buildConsumerProperties(sslBundles);
        var consumerFactory = new DefaultKafkaConsumerFactory<String, String>(consumerProps);
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(concurrency);

        var errorHandler = new DefaultErrorHandler(new FixedBackOff(backoffMs, maxAttempts));
        errorHandler.setCommitRecovered(true);
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}