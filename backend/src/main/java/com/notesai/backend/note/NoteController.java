package com.notesai.backend.note;

import com.notesai.backend.ai.AiService;
import com.notesai.backend.ai.QuizQuestion;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;
    private final AiService aiService;

    public NoteController(NoteService noteService, AiService aiService) {
        this.noteService = noteService;
        this.aiService = aiService;
    }

    @GetMapping
    public List<Note> getAllNotes() {
        return noteService.getAllNotes();
    }

    @GetMapping("/{id}")
    public Note getNoteById(@PathVariable Long id) {
        return noteService.getNoteById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Note createNote(@Valid @RequestBody Note note) {
        return noteService.createNote(note);
    }

    @PutMapping("/{id}")
    public Note updateNote(@PathVariable Long id, @Valid @RequestBody Note updatedNote) {
        return noteService.updateNote(id, updatedNote);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
    }

    @PostMapping("/{id}/summarize")
    public java.util.Map<String, String> summarizeNote(@PathVariable Long id) {
        Note note = noteService.getNoteById(id);
        String summary = aiService.summarize(note.getContent());
        return java.util.Map.of("summary", summary);
    }

    @PostMapping("/{id}/quiz")
    public List<QuizQuestion> generateQuiz(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int count) {
        Note note = noteService.getNoteById(id);
        int safeCount = Math.max(1, Math.min(count, 10));
        return aiService.generateQuiz(note.getContent(), safeCount);
    }
}