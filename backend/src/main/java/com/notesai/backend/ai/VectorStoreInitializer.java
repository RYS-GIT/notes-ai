package com.notesai.backend.ai;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class VectorStoreInitializer {

    private final JdbcTemplate jdbcTemplate;

    public VectorStoreInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS note_embeddings (
                    note_id BIGINT PRIMARY KEY REFERENCES notes(id) ON DELETE CASCADE,
                    embedding vector(768)
                )
                """);
    }
}