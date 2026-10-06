package com.notesai.backend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.ObjectMapper;
import java.util.List;

@Service
public class AiService {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;

    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 1000;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AiService.class);

    public AiService(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model}") String model,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    public String summarize(String noteContent) {
        String prompt = "Summarize the following note in 2-3 concise sentences:\n\n" + noteContent;
        return callGemini(GeminiRequest.of(prompt));
    }

    private String callGemini(GeminiRequest request) {
        long backoff = INITIAL_BACKOFF_MS;

        for (int attempt = 1; ; attempt++) {
            try {
                GeminiResponse response = restClient.post()
                        .uri("/models/{model}:generateContent?key={key}", model, apiKey)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(GeminiResponse.class);

                return response != null ? response.extractText() : "";

            } catch (org.springframework.web.client.HttpServerErrorException e) {
                log.warn("Gemini call failed (attempt {}/{}): {}", attempt, MAX_ATTEMPTS, e.getStatusCode());
                if (attempt == MAX_ATTEMPTS) {
                    throw e; // out of attempts, let GlobalExceptionHandler return the 503
                }
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
                backoff *= 2; // 1s, then 2s
            }
        }
    }

    public List<QuizQuestion> generateQuiz(String noteContent, int count) {
        String prompt = """
            Create %d multiple-choice questions that test understanding of the note below.
            Respond with ONLY a JSON array. Each element must have exactly these fields:
            "question" (string), "options" (array of exactly 4 strings),
            "correctIndex" (integer from 0 to 3), "explanation" (one short sentence).
            Base every question strictly on the note's content.

            NOTE:
            %s
            """.formatted(count, noteContent);

        String raw = callGemini(GeminiRequest.ofJson(prompt));
        String json = raw.replaceAll("(?s)^\\s*```(?:json)?\\s*|\\s*```\\s*$", "").trim();

        try {
            return List.of(objectMapper.readValue(json, QuizQuestion[].class));
        } catch (Exception e) {
            throw new AiResponseException("Could not parse quiz from AI response", e);
        }
    }

    public float[] embed(String text) {
        EmbeddingResponse response = restClient.post()
                .uri("/models/gemini-embedding-001:embedContent?key={key}", apiKey)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(EmbeddingRequest.of(text))
                .retrieve()
                .body(EmbeddingResponse.class);

        if (response == null || response.embedding() == null) {
            throw new AiResponseException("Empty embedding response", null);
        }

        List<Float> values = response.embedding().values();
        float[] result = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    public String answerFromContext(String question, List<String> contextTexts, List<ChatRequest.ChatTurn> history) {
        String context = String.join("\n\n---\n\n", contextTexts);

        String historyBlock = "";
        if (history != null && !history.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ChatRequest.ChatTurn turn : history) {
                sb.append(turn.role().equals("user") ? "User: " : "Assistant: ")
                        .append(turn.text())
                        .append("\n");
            }
            historyBlock = "\nPREVIOUS CONVERSATION:\n" + sb + "\n";
        }

        String prompt = """
        You are a helpful assistant answering questions in an ongoing conversation
        about the user's notes.

        There are two kinds of questions you might get:
        1. Questions about the CONTENT of the notes (e.g. "what is X", "tell me about Y").
           For these, answer using ONLY the information in the NOTES section below.
           If the notes don't contain enough information, say so clearly rather than guessing.
        2. Questions about the CONVERSATION ITSELF (e.g. "what did I ask first",
           "can you repeat that", "what did you just say"). For these, answer directly
           using the PREVIOUS CONVERSATION section below — you do not need the notes
           to answer these.
        %s
        NOTES:
        %s

        QUESTION:
        %s
        """.formatted(historyBlock, context, question);

        return callGemini(GeminiRequest.of(prompt));
    }
}