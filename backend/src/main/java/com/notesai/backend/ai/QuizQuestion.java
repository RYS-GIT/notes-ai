package com.notesai.backend.ai;

import java.util.List;

public record QuizQuestion(
        String question,
        List<String> options,
        int correctIndex,
        String explanation
) {}