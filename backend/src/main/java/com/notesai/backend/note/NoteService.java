package com.notesai.backend.note;

import com.notesai.backend.ai.AiService;
import com.notesai.backend.ai.EmbeddingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final AiService aiService;
    private final EmbeddingRepository embeddingRepository;

    public NoteService(NoteRepository noteRepository, AiService aiService, EmbeddingRepository embeddingRepository) {
        this.noteRepository = noteRepository;
        this.aiService = aiService;
        this.embeddingRepository = embeddingRepository;
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Note getNoteById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
    }

    public Note createNote(Note note) {
        Note saved = noteRepository.save(note);
        storeEmbedding(saved);
        return saved;
    }

    public Note updateNote(Long id, Note updatedNote) {
        Note existing = getNoteById(id);
        existing.setTitle(updatedNote.getTitle());
        existing.setContent(updatedNote.getContent());
        Note saved = noteRepository.save(existing);
        storeEmbedding(saved);
        return saved;
    }

    public void deleteNote(Long id) {
        if (!noteRepository.existsById(id)) {
            throw new NoteNotFoundException(id);
        }
        noteRepository.deleteById(id);
    }

    private void storeEmbedding(Note note) {
        String text = note.getTitle() + "\n\n" + note.getContent();
        float[] embedding = aiService.embed(text);
        embeddingRepository.save(note.getId(), embedding);
    }

    public List<Note> getNotesByIds(List<Long> ids) {
        return noteRepository.findAllById(ids);
    }
}