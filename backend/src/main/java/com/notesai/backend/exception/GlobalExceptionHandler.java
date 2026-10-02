package com.notesai.backend.exception;

import com.notesai.backend.note.NoteNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoteNotFoundException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.NOT_FOUND.value(),
                "error", "Not Found",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(org.springframework.web.client.HttpServerErrorException.class)
    public ResponseEntity<Map<String, Object>> handleAiUnavailable(
            org.springframework.web.client.HttpServerErrorException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", "AI Service Unavailable",
                "message", "The AI provider is busy right now. Please try again in a moment."
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(org.springframework.web.client.HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleAiClientError(
            org.springframework.web.client.HttpClientErrorException ex) {
        log.warn("Gemini rejected request: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());

        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.BAD_GATEWAY.value(),
                "error", "AI Request Failed",
                "message", "The AI provider rejected the request. Check the API key and model configuration."
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @ExceptionHandler(com.notesai.backend.ai.AiResponseException.class)
    public ResponseEntity<Map<String, Object>> handleBadAiResponse(
            com.notesai.backend.ai.AiResponseException ex) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.BAD_GATEWAY.value(),
                "error", "Invalid AI Response",
                "message", "The AI returned an unexpected response. Please try again."
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }
}