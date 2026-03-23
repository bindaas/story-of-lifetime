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
 * ExplainerAgent compares v1 and v2 stories and explains what changed and why.
 * Runs automatically after v2 is generated.
 * Uses a moderate temperature — analytical but readable prose.
 */
public class ExplainerAgent {

    private static final String API_URL     = "https://api.anthropic.com/v1/messages";
    private static final String API_KEY     =
            System.getenv("STORY_OF_LIFETIME_ANTHROPIC_API_KEY") != null
            ? System.getenv("STORY_OF_LIFETIME_ANTHROPIC_API_KEY")
            : System.getenv("ANTHROPIC_API_KEY");
    private static final String PROMPT_FILE = "prompts/explainer_prompt.txt";

    private final AppConfig config;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public ExplainerAgent(AppConfig config) {
        this.config = config;
    }

    public ExplainerResult explain(String startState, String endState,
                                   List<String> originalFacts, List<String> additionalFacts,
                                   String storyV1, String storyV2) throws Exception {

        String   prompt    = buildPrompt(startState, endState, originalFacts, additionalFacts, storyV1, storyV2);
        long     startTime = System.currentTimeMillis();
        JsonNode root      = callClaude(prompt);
        long     elapsedMs = System.currentTimeMillis() - startTime;

        String explanation  = root.path("content").get(0).path("text").asText();
        int    inputTokens  = root.path("usage").path("input_tokens").asInt(0);
        int    outputTokens = root.path("usage").path("output_tokens").asInt(0);
        double costUsd      = calculateCost(config.getExplainerModel(), inputTokens, outputTokens);

        System.out.printf("[ExplainerAgent] model=%s input=%d output=%d cost=$%.5f elapsed=%dms%n",
                config.getExplainerModel(), inputTokens, outputTokens, costUsd, elapsedMs);

        return new ExplainerResult(explanation, inputTokens, outputTokens, costUsd, elapsedMs);
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
                                List<String> originalFacts, List<String> additionalFacts,
                                String storyV1, String storyV2) throws IOException {
        String template = loadPromptTemplate();

        String origFactList = formatFacts(originalFacts);
        String addlFactList = formatFacts(additionalFacts);

        return template
                .replace("{{START_STATE}}",      startState)
                .replace("{{END_STATE}}",         endState)
                .replace("{{ORIGINAL_FACTS}}",    origFactList)
                .replace("{{ADDITIONAL_FACTS}}",  addlFactList)
                .replace("{{STORY_V1}}",          storyV1)
                .replace("{{STORY_V2}}",          storyV2);
    }

    private String formatFacts(List<String> facts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < facts.size(); i++) {
            sb.append((i + 1)).append(". ").append(facts.get(i)).append("\n");
        }
        return sb.toString().trim();
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
        body.put("model",       config.getExplainerModel());
        body.put("max_tokens",  config.getMaxTokens());
        body.put("temperature", config.getExplainerTemperature());

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
