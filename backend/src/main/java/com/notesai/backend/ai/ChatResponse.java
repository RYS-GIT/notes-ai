package com.notesai.backend.ai;

import java.util.List;

public record ChatResponse(String answer, List<Long> sourceNoteIds) {}