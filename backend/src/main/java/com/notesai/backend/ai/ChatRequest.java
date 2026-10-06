package com.notesai.backend.ai;

import java.util.List;

public record ChatRequest(String question, List<ChatTurn> history) {
    public record ChatTurn(String role, String text) {}
}