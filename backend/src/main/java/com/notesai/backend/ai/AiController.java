package com.notesai.backend.ai;

import com.notesai.backend.note.Note;
import com.notesai.backend.note.NoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class AiController {

    private final AiService aiService;
    private final EmbeddingRepository embeddingRepository;
    private final NoteService noteService;

    public AiController(AiService aiService, EmbeddingRepository embeddingRepository, NoteService noteService) {
        this.aiService = aiService;
        this.embeddingRepository = embeddingRepository;
        this.noteService = noteService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        float[] questionEmbedding = aiService.embed(request.question());

        List<Long> matchingIds = embeddingRepository.findSimilarNoteIds(questionEmbedding, 3);
        if (matchingIds.isEmpty()) {
            return new ChatResponse("I don't have any notes to search yet.", List.of());
        }

        List<Note> matchingNotes = noteService.getNotesByIds(matchingIds);
        List<String> contextTexts = matchingNotes.stream()
                .map(n -> n.getTitle() + "\n" + n.getContent())
                .toList();

        String answer = aiService.answerFromContext(request.question(), contextTexts);

        return new ChatResponse(answer, matchingIds);
    }
}