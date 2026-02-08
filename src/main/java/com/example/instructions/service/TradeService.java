package com.example.instructions.service;

import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.model.InputTrade;
import com.example.instructions.model.PlatformTrade;
import com.example.instructions.util.TradeTransformer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TradeService {

    private final Validator validator;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TradeFileHandlerRegistry registry;
    private final ObjectMapper objectMapper;
    private final String outboundTopic;
    private final ConcurrentHashMap<String, CanonicalTrade> processedTrades = new ConcurrentHashMap<>();

    public TradeService(
            Validator validator,
            KafkaTemplate<String, String> kafkaTemplate,
            TradeFileHandlerRegistry registry,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.outbound:instructions.outbound}") String outboundTopic
    ) {
        this.validator = validator;
        this.kafkaTemplate = kafkaTemplate;
        this.registry = registry;
        this.objectMapper = objectMapper;
        this.outboundTopic = outboundTopic;
    }

    /**
     * REST flow using file
     * input file -> InputTrade list -> canonical -> platform-specific -> publish outbound
     */
    public List<String> process(MultipartFile file) throws IOException {
        List<InputTrade> trades = registry.process(file);
        return publishPlatformTrades(trades);
    }

    /**
     * Kafka flow:
     * inbound payload -> parse (object/array/json) -> canonical -> platform-specific -> publish outbound
     */
    public void handleKafkaMessage(String rawPayload) {
        try {
            List<InputTrade> trades = parseInputTrades(rawPayload);
            publishPlatformTrades(trades);
        } catch (Exception e) {
            throw new RuntimeException("Failed processing inbound kafka payload", e);
        }
    }

    /**
     * Converts each InputTrade -> CanonicalTrade -> PlatformTrade, publish to outbound, and waits for completion.
     */
    private List<String> publishPlatformTrades(List<InputTrade> trades) {
        if (trades == null || trades.isEmpty()) return List.of();

        List<String> ids = new ArrayList<>(trades.size());
        List<CompletableFuture<Void>> publishes = new ArrayList<>(trades.size());

        for (InputTrade instruction : trades) {
            validate(instruction);
            CanonicalTrade canonical = TradeTransformer.toCanonical(instruction);
            String tradeId = canonical.getTradeId();
            processedTrades.put(tradeId, canonical); //you can use for retry however failed one needs to capture.
            PlatformTrade platformTrade = TradeTransformer.toPlatformTrade(canonical);

            ids.add(tradeId);
            publishes.add(publishToOutbound(tradeId, platformTrade));
        }
        // waits for completing and ensures Kafka offset commit happens only after sending successfully.
        CompletableFuture.allOf(publishes.toArray(new CompletableFuture[0])).join();

        log.info("Published {} platform trade(s) to outboundTopic={}", ids.size(), outboundTopic);
        return ids;
    }

    private CompletableFuture<Void> publishToOutbound(String key, PlatformTrade trade) {
        final String payload;
        try {
            payload = objectMapper.writeValueAsString(trade);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize platform trade key=" + key, e);
        }

        return kafkaTemplate.send(outboundTopic, key, payload)
                .thenAccept(result -> log.info(
                        "PUBLISHED TO OUTBOUND topic={} key={} partition={} offset={},payload={}",
                        outboundTopic,
                        key,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        payload
                ))
                .exceptionally(ex -> {
                    log.error("Failed to publish outbound topic={} key={}", outboundTopic, key, ex);
                    throw new RuntimeException(ex);
                });
    }

    /**
     * It Supports the below formats only
     * JSON array: [ {...}, {...} ]
     * JSON object: { ... }
     * JSON: { ... }\n{ ... }\n...
     */
    private List<InputTrade> parseInputTrades(String rawPayload) throws IOException {
        if (rawPayload == null || rawPayload.isBlank()) return List.of();

        try (var parser = objectMapper.getFactory().createParser(new StringReader(rawPayload.trim()))) {
            JsonToken first = parser.nextToken();
            if (first == null) return List.of();

            if (first == JsonToken.START_ARRAY) {
                List<InputTrade> out = new ArrayList<>();
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                    out.add(objectMapper.readValue(parser, InputTrade.class));
                }
                return out;
            }

            ObjectReader reader = objectMapper.readerFor(InputTrade.class);
            MappingIterator<InputTrade> it = reader.readValues(parser);
            return it.readAll();
        }
    }

    private void validate(InputTrade instruction) {
        Set<ConstraintViolation<InputTrade>> violations = validator.validate(instruction);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted()
                    .findFirst()
                    .orElse("Validation failed");
            throw new IllegalArgumentException(msg);
        }
    }

    public ConcurrentHashMap<String, CanonicalTrade> getProcessedTrades() {
        return processedTrades;
    }
}