package com.notesai.backend.ai;

public class AiResponseException extends RuntimeException {
    public AiResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}