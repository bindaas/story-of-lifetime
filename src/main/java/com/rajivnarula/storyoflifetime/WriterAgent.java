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
 * Returns a StoryResponse including token usage and cost.
 *
 * Pricing per million tokens (as of 2025):
 *   claude-opus-4-6    input $15.00  output $75.00
 *   claude-sonnet-4-6  input  $3.00  output $15.00
 *   claude-haiku-4-5   input  $0.25  output  $1.25
 */
public class WriterAgent {

    private static final String API_URL     = "https://api.anthropic.com/v1/messages";
    private static final String API_KEY     =
            System.getenv("STORY_OF_LIFETIME_ANTHROPIC_API_KEY") != null
            ? System.getenv("STORY_OF_LIFETIME_ANTHROPIC_API_KEY")
            : System.getenv("ANTHROPIC_API_KEY");
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

    public StoryResponse write(WorldModel worldModel, String storyLength, int factCount) throws Exception {
        String prompt    = buildPrompt(worldModel);
        long   startTime = System.currentTimeMillis();
        JsonNode root    = callClaude(prompt);
        long   elapsedMs = System.currentTimeMillis() - startTime;

        // Extract story text
        String story = root.path("content").get(0).path("text").asText();

        // Extract token usage
        int inputTokens  = root.path("usage").path("input_tokens").asInt(0);
        int outputTokens = root.path("usage").path("output_tokens").asInt(0);

        // Calculate cost
        double costUsd = calculateCost(config.getModel(), inputTokens, outputTokens);

        // Log to console (visible in Cloud Run logs)
        System.out.printf("[WriterAgent] model=%s input=%d output=%d cost=$%.5f elapsed=%dms%n",
                config.getModel(), inputTokens, outputTokens, costUsd, elapsedMs);

        return new StoryResponse(
                story,
                config.getModel(),
                storyLength,
                factCount,
                inputTokens,
                outputTokens,
                costUsd,
                elapsedMs
        );
    }

    private double calculateCost(String model, int inputTokens, int outputTokens) {
        double inputPricePerM;
        double outputPricePerM;

        if (model.contains("opus")) {
            inputPricePerM  = 15.00;
            outputPricePerM = 75.00;
        } else if (model.contains("haiku")) {
            inputPricePerM  = 0.25;
            outputPricePerM = 1.25;
        } else {
            // sonnet — default
            inputPricePerM  = 3.00;
            outputPricePerM = 15.00;
        }

        return (inputTokens  / 1_000_000.0 * inputPricePerM)
             + (outputTokens / 1_000_000.0 * outputPricePerM);
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

    private JsonNode callClaude(String userMessage) throws Exception {

        if (API_KEY == null || API_KEY.isBlank()) {
            throw new RuntimeException(
                "API key not set. Set STORY_OF_LIFETIME_ANTHROPIC_API_KEY or ANTHROPIC_API_KEY."
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
            return mapper.readTree(responseBody);
        }
    }
}
