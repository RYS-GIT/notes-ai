package com.notesai.backend.ai;

import com.pgvector.PGvector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

@Repository
public class EmbeddingRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmbeddingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Long noteId, float[] embedding) {
        jdbcTemplate.update(
                """
                INSERT INTO note_embeddings (note_id, embedding)
                VALUES (?, ?)
                ON CONFLICT (note_id) DO UPDATE SET embedding = EXCLUDED.embedding
                """,
                (PreparedStatement ps) -> {
                    ps.setLong(1, noteId);
                    ps.setObject(2, new PGvector(embedding));
                }
        );
    }

    public List<Long> findSimilarNoteIds(float[] queryEmbedding, int limit) {
        return jdbcTemplate.query(
                """
                SELECT note_id FROM note_embeddings
                ORDER BY embedding <-> ?
                LIMIT ?
                """,
                (PreparedStatement ps) -> {
                    ps.setObject(1, new PGvector(queryEmbedding));
                    ps.setInt(2, limit);
                },
                (ResultSet rs, int rowNum) -> rs.getLong("note_id")
        );
    }
}