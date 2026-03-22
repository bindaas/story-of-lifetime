package com.rajivnarula.storyoflifetime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * WriterAgent generates full prose from the WorldModel.
 * Reads its prompt template from the classpath.
 * All config (model, temperature, length) comes from AppConfig.
 */
public class WriterAgent {

    private static final String API_URL     = "https://api.anthropic.com/v1/messages";
    private static final String API_KEY     = System.getenv("ANTHROPIC_API_KEY");
    private static final String PROMPT_FILE = "prompts/writer_prompt.txt";

    private final AppConfig config;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public WriterAgent(AppConfig config) {
        this.config = config;
    }

    public String write(WorldModel worldModel) throws Exception {
        String prompt = buildPrompt(worldModel);
        return callClaude(prompt);
    }

    private String buildPrompt(WorldModel worldModel) throws IOException {
        String template = loadPromptTemplate();

        List<String> facts = worldModel.getFacts();
        StringBuilder factList = new StringBuilder();
        for (int i = 0; i < facts.size(); i++) {
            factList.append((i + 1)).append(". ").append(facts.get(i)).append("\n");
        }

        return template
                .replace("{{START_STATE}}",  worldModel.getStartState())
                .replace("{{END_STATE}}",    worldModel.getEndState())
                .replace("{{FACTS}}",        factList.toString().trim())
                .replace("{{STORY_LENGTH}}", config.getStoryLength());
    }

    private String loadPromptTemplate() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(PROMPT_FILE)) {
            if (is == null) {
                throw new IOException("Prompt file not found on classpath: " + PROMPT_FILE);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String callClaude(String userMessage) throws Exception {

        if (API_KEY == null || API_KEY.isBlank()) {
            throw new RuntimeException(
                "ANTHROPIC_API_KEY environment variable is not set."
            );
        }

        ObjectNode body = mapper.createObjectNode();
        body.put("model",       config.getModel());
        body.put("max_tokens",  config.getMaxTokens());
        body.put("temperature", config.getTemperature());

        ArrayNode messages = mapper.createArrayNode();
        ObjectNode message  = mapper.createObjectNode();
        message.put("role",    "user");
        message.put("content", userMessage);
        messages.add(message);
        body.set("messages", messages);

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(
                        mapper.writeValueAsString(body),
                        MediaType.get("application/json")))
                .addHeader("x-api-key",         API_KEY)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type",      "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                throw new RuntimeException(
                    "API call failed [HTTP " + response.code() + "]: " + responseBody
                );
            }
            JsonNode root = mapper.readTree(responseBody);
            return root.path("content").get(0).path("text").asText();
        }
    }
}
