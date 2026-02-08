package com.example.instructions.kafka;

import com.example.instructions.service.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;

@Service
public class KafkaListener {
    private static final Logger log = LoggerFactory.getLogger(KafkaListener.class);
    private final TradeService tradeService;
    private final int maxPayloadBytes;

    public KafkaListener(
            TradeService tradeService,
            @Value("${app.kafka.maxPayloadBytes:1000000}") int maxPayloadBytes
    ) {
        this.tradeService = tradeService;
        this.maxPayloadBytes = maxPayloadBytes;
    }

    @org.springframework.kafka.annotation.KafkaListener(
            topics = "${app.kafka.topics.inbound:instructions.inbound}",
            groupId = "${spring.kafka.consumer.group-id:instructions-capture}",
            concurrency = "${app.kafka.listener.concurrency:1}"
    )
    public void onMessage(String message,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                          @Header(KafkaHeaders.OFFSET) long offset) {

        if (message == null || message.isBlank()) return;

        int bytes = message.getBytes(StandardCharsets.UTF_8).length;
        if (bytes > maxPayloadBytes) {
            throw new IllegalArgumentException("Inbound payload too large: " + bytes);
        }
        log.info("RECEIVED topic={} partition={} offset={} bytes={}, message={}", topic, partition, offset, bytes,message);
        tradeService.handleKafkaMessage(message);
    }
}
