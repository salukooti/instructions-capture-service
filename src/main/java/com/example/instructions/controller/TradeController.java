package com.example.instructions.controller;

import com.example.instructions.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/trades")
@Tag(name = "Trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Upload trade instruction file (.csv or .json)",
            description = "Accepts a CSV (header required) or JSON (single object, array, or newline JSON) and publish trades to Kafka."
    )
    @ApiResponse(responseCode = "200", description = "Accepted")
    public ResponseEntity<UploadResponse> upload(@RequestPart("file") @NotNull MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new UploadResponse(0, List.of(), "Empty file"));
        }
        log.info("Controller message has been received!");
        List<String> ids = tradeService.process(file);
        return ResponseEntity.ok(new UploadResponse(ids.size(), ids, null));
    }
    public record UploadResponse(int processed, List<String> ids, String error) {
    }
}
