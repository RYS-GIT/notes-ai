package com.notesai.backend.ai;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmbeddingRequest(Content content, Integer outputDimensionality) {
    public record Content(List<Part> parts) {}
    public record Part(String text) {}

    public static EmbeddingRequest of(String text) {
        return new EmbeddingRequest(new Content(List.of(new Part(text))), 768);
    }
}