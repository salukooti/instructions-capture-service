package com.example.instructions.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalExceptionHandler.ErrorResponse> onBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GlobalExceptionHandler.ErrorResponse("BAD_REQUEST", safeMsg(e.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalExceptionHandler.ErrorResponse> onServerError(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GlobalExceptionHandler.ErrorResponse("INTERNAL_ERROR", "Unexpected error"));
    }

    private static String safeMsg(String msg) {
        if (msg == null) return "Validation failed";
        // Avoid reflecting user input; keep short.
        String trimmed = msg.replaceAll("[\r\n\t]", " ").trim();
        return trimmed.length() > 200 ? trimmed.substring(0, 200) : trimmed;
    }

    public record ErrorResponse(String code, String message) {
    }
}
