package com.example.instructions.handler;

import com.example.instructions.model.InputTrade;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class JsonTradeFileHandlerImpl implements TradeFileHandlerI {
    private final ObjectMapper objectMapper;

    public JsonTradeFileHandlerImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Override public String format() { return "json"; }

    @Override
    public boolean supports(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        return name.endsWith(".json");
    }

    @Override
    public List<InputTrade> parse(InputStream in) throws IOException {
        List<InputTrade> out = new ArrayList<>();

        try (var parser = objectMapper.getFactory().createParser(in)) {
            var first = parser.nextToken();
            if (first == null) return out;

            /** JSON array  [ {...}, {...} ] */
            if (first == com.fasterxml.jackson.core.JsonToken.START_ARRAY) {
                while (parser.nextToken() != com.fasterxml.jackson.core.JsonToken.END_ARRAY) {
                    out.add(objectMapper.readValue(parser, InputTrade.class));
                }
                return out;
            }

            // Case 2: single JSON object { ... }
            if (first == com.fasterxml.jackson.core.JsonToken.START_OBJECT) {
                out.add(objectMapper.readValue(parser, InputTrade.class));
                return out;
            }

            throw new IllegalArgumentException("Invalid JSON payload: expected object or array");
        }
    }
}