package com.example.instructions.service;

import com.example.instructions.exception.UnsupportedFileException;
import com.example.instructions.handler.TradeFileHandlerI;
import com.example.instructions.model.InputTrade;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class TradeFileHandlerRegistry {

    private final List<TradeFileHandlerI> handlers;

    public TradeFileHandlerRegistry(List<TradeFileHandlerI> handlers) {
        this.handlers = handlers;
    }

    public List<InputTrade> process(MultipartFile file) throws IOException {
        TradeFileHandlerI handler = handlers.stream()
                .filter(h -> h.supports(file))
                .findFirst()
                .orElseThrow(() -> new UnsupportedFileException(
                        "Only " + supportedFormats() + " files are supported"
                ));

        try (InputStream in = file.getInputStream()) {
            return handler.parse(in);
        }
    }

    private String supportedFormats() {
        return handlers.stream().map(TradeFileHandlerI::format).distinct().sorted().toList().toString();
    }
}
