package com.example.instructions.handler;

import com.example.instructions.model.InputTrade;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface TradeFileHandlerI {
    /** e.g., "csv", "json" (for logging/metrics) */
    String format();

    /** Decide if this handler can process the uploaded file */
    boolean supports(org.springframework.web.multipart.MultipartFile file);

    /** Parse + hand off to service layer; return canonical ids */
    //java.util.List<String> handle(java.io.InputStream in) throws java.io.IOException;
    List<InputTrade> parse(InputStream in) throws IOException;
}
