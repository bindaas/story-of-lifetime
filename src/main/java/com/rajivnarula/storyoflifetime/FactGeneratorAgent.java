package com.rajivnarula.storyoflifetime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * FactGeneratorAgent generates a set of life facts connecting start to end state.
 * Creativity and contradiction levels control the nature of the generated facts.
 * Output is reviewed and optionally edited by a human before the pipeline proceeds.
 */
public class FactGeneratorAgent {

    private static final String API_URL     = "https://api.anthropic.com/v1/messages";
    private static final String API_KEY     =
            System.getenv("STORY_OF_LIFETIME_ANTHROPIC_API_KEY") != null
            ? System.getenv("STORY_OF_LIFETIME_ANTHROPIC_API_KEY")
            : System.getenv("ANTHROPIC_API_KEY");
    private static final String PROMPT_FILE = "prompts/factgenerator_prompt.txt";

    private final AppConfig config;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public FactGeneratorAgent(AppConfig config) {
        this.config = config;
    }

    public FactGeneratorResult generate(String startState, String endState,
                                        String creativity, String contradiction,
                                        String worldType, int factCount) throws Exception {

        // 0 facts is a valid creative choice — skip the API call entirely
        if (factCount == 0) {
            System.out.println("[FactGeneratorAgent] factCount=0, returning empty list");
            return new FactGeneratorResult(Collections.emptyList(), 0, 0, 0.0, 0L);
        }

        String   prompt    = buildPrompt(startState, endState, creativity, contradiction, worldType, factCount);
        long     startTime = System.currentTimeMillis();
        JsonNode root      = callClaude(prompt);
        long     elapsedMs = System.currentTimeMillis() - startTime;

        String rawText      = root.path("content").get(0).path("text").asText();
        int    inputTokens  = root.path("usage").path("input_tokens").asInt(0);
        int    outputTokens = root.path("usage").path("output_tokens").asInt(0);
        double costUsd      = calculateCost(config.getFactGeneratorModel(), inputTokens, outputTokens);

        List<String> facts = Arrays.stream(rawText.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());

        System.out.printf("[FactGeneratorAgent] model=%s facts=%d input=%d output=%d cost=$%.5f elapsed=%dms%n",
                config.getFactGeneratorModel(), facts.size(), inputTokens, outputTokens, costUsd, elapsedMs);

        return new FactGeneratorResult(facts, inputTokens, outputTokens, costUsd, elapsedMs);
    }

    private double calculateCost(String model, int inputTokens, int outputTokens) {
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

    private String buildPrompt(String startState, String endState,
                                String creativity, String contradiction,
                                String worldType, int factCount) throws IOException {
        String template = loadPromptTemplate();
        return template
                .replace("{{START_STATE}}",   startState)
                .replace("{{END_STATE}}",     endState)
                .replace("{{CREATIVITY}}",    creativity)
                .replace("{{CONTRADICTION}}", contradiction)
                .replace("{{WORLD_TYPE}}",    worldType)
                .replace("{{FACT_COUNT}}",    String.valueOf(factCount));
    }

    private String loadPromptTemplate() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(PROMPT_FILE)) {
            if (is == null) {
                throw new IOException("Prompt file not found: " + PROMPT_FILE);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private JsonNode callClaude(String userMessage) throws Exception {

        if (API_KEY == null || API_KEY.isBlank()) {
            throw new RuntimeException(
                "API key not set. Set STORY_OF_LIFETIME_ANTHROPIC_API_KEY or ANTHROPIC_API_KEY."
            );
        }

        ObjectNode body = mapper.createObjectNode();
        body.put("model",       config.getFactGeneratorModel());
        body.put("max_tokens",  config.getMaxTokens());
        body.put("temperature", config.getFactGeneratorTemperature());

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
            return mapper.readTree(responseBody);
        }
    }
}
