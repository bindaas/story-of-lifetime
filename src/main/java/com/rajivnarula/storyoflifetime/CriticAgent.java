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
 * CriticAgent evaluates a Planner outline and either approves or rejects it.
 * If rejected, the reason is fed back into the Planner for a revised attempt.
 * Uses a low temperature — we want analytical, deterministic judgements.
 */
public class CriticAgent {

    private static final String API_URL     = "https://api.anthropic.com/v1/messages";
    private static final String API_KEY     =
            System.getenv("STORY_OF_LIFETIME_ANTHROPIC_API_KEY") != null
            ? System.getenv("STORY_OF_LIFETIME_ANTHROPIC_API_KEY")
            : System.getenv("ANTHROPIC_API_KEY");
    private static final String PROMPT_FILE = "prompts/critic_prompt.txt";

    private final AppConfig config;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public CriticAgent(AppConfig config) {
        this.config = config;
    }

    /**
     * Evaluates the outline. previousFeedback is empty on the first attempt,
     * and contains the prior rejection reason on subsequent attempts.
     */
    public CriticResult evaluate(WorldModel worldModel, String outline,
                                 String previousFeedback) throws Exception {

        String   prompt    = buildPrompt(worldModel, outline, previousFeedback);
        long     startTime = System.currentTimeMillis();
        JsonNode root      = callClaude(prompt);
        long     elapsedMs = System.currentTimeMillis() - startTime;

        String rawResponse  = root.path("content").get(0).path("text").asText().trim();
        int    inputTokens  = root.path("usage").path("input_tokens").asInt(0);
        int    outputTokens = root.path("usage").path("output_tokens").asInt(0);
        double costUsd      = calculateCost(config.getCriticModel(), inputTokens, outputTokens);

        CriticResult.Decision decision = parseDecision(rawResponse);
        String                reason   = parseReason(rawResponse);

        System.out.printf("[CriticAgent] decision=%s model=%s input=%d output=%d cost=$%.5f elapsed=%dms%n",
                decision, config.getCriticModel(), inputTokens, outputTokens, costUsd, elapsedMs);
        System.out.printf("[CriticAgent] reason=%s%n", reason);

        return new CriticResult(decision, reason, inputTokens, outputTokens, costUsd, elapsedMs);
    }

    private CriticResult.Decision parseDecision(String response) {
        for (String line : response.split("\n")) {
            if (line.startsWith("DECISION:")) {
                return line.contains("APPROVED")
                        ? CriticResult.Decision.APPROVED
                        : CriticResult.Decision.REJECTED;
            }
        }
        // Default to approved if we can't parse — don't block indefinitely
        return CriticResult.Decision.APPROVED;
    }

    private String parseReason(String response) {
        for (String line : response.split("\n")) {
            if (line.startsWith("REASON:")) {
                return line.substring("REASON:".length()).trim();
            }
        }
        return response;
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

    private String buildPrompt(WorldModel worldModel, String outline,
                                String previousFeedback) throws IOException {
        String template = loadPromptTemplate();

        List<String> facts = worldModel.getFacts();
        StringBuilder factList = new StringBuilder();
        for (int i = 0; i < facts.size(); i++) {
            factList.append((i + 1)).append(". ").append(facts.get(i)).append("\n");
        }

        String feedbackSection = previousFeedback == null || previousFeedback.isBlank()
                ? ""
                : "PREVIOUS REJECTION REASON:\n" + previousFeedback + "\n\nNote: the outline has been revised in response to this feedback. Evaluate the revised version.";

        return template
                .replace("{{START_STATE}}",      worldModel.getStartState())
                .replace("{{END_STATE}}",        worldModel.getEndState())
                .replace("{{FACTS}}",            factList.toString().trim())
                .replace("{{OUTLINE}}",          outline)
                .replace("{{FEEDBACK_SECTION}}", feedbackSection);
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
        body.put("model",       config.getCriticModel());
        body.put("max_tokens",  512);
        body.put("temperature", config.getCriticTemperature());

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
