package com.notesai.backend.ai;

import java.util.List;

public record EmbeddingResponse(Embedding embedding) {
    public record Embedding(List<Float> values) {}
}