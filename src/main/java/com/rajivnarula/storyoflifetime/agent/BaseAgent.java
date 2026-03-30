package com.rajivnarula.storyoflifetime.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rajivnarula.storyoflifetime.config.AppConfig;
import okhttp3.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Shared infrastructure for all agents.
 * Provides: HTTP client, JSON mapper, prompt loading, Claude API call, cost calculation.
 * Each agent subclass supplies its own prompt file and public method.
 */
public abstract class BaseAgent {

    protected static final String API_URL = "https://api.anthropic.com/v1/messages";
    protected static final String API_KEY =
            System.getenv("STORY_OF_LIFETIME_ANTHROPIC_API_KEY") != null
            ? System.getenv("STORY_OF_LIFETIME_ANTHROPIC_API_KEY")
            : System.getenv("ANTHROPIC_API_KEY");

    protected final AppConfig      config;
    protected final OkHttpClient   httpClient;
    protected final ObjectMapper   mapper;

    protected BaseAgent(AppConfig config) {
        this.config = config;
        this.mapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    /** Load a prompt template from the classpath. */
    protected String loadPromptTemplate(String promptFile) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(promptFile)) {
            if (is == null) {
                throw new IOException("Prompt file not found on classpath: " + promptFile);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** POST a single-turn message to Claude and return the parsed JSON response. */
    protected JsonNode callClaude(String model, double temperature, int maxTokens,
                                  String userMessage) throws Exception {
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new RuntimeException(
                "API key not set. Set STORY_OF_LIFETIME_ANTHROPIC_API_KEY or ANTHROPIC_API_KEY."
            );
        }

        ObjectNode body = mapper.createObjectNode();
        body.put("model",       model);
        body.put("max_tokens",  maxTokens);
        body.put("temperature", temperature);

        ArrayNode  messages = mapper.createArrayNode();
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
            return mapper.readTree(responseBody);
        }
    }

    /** Calculate cost in USD based on model tier and token counts. */
    protected double calculateCost(String model, int inputTokens, int outputTokens) {
        double inputPricePerM;
        double outputPricePerM;
        if (model.contains("opus")) {
            inputPricePerM = 15.00; outputPricePerM = 75.00;
        } else if (model.contains("haiku")) {
            inputPricePerM = 0.25;  outputPricePerM = 1.25;
        } else {
            inputPricePerM = 3.00;  outputPricePerM = 15.00;
        }
        return (inputTokens  / 1_000_000.0 * inputPricePerM)
             + (outputTokens / 1_000_000.0 * outputPricePerM);
    }

    /** Format a numbered fact list for prompt injection. */
    protected String formatFacts(java.util.List<String> facts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < facts.size(); i++) {
            sb.append((i + 1)).append(". ").append(facts.get(i)).append("\n");
        }
        return sb.toString().trim();
    }
}
