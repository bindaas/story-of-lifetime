package com.rajivnarula.storyoflifetime.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.rajivnarula.storyoflifetime.config.AppConfig;
import com.rajivnarula.storyoflifetime.result.ExplainerResult;

import java.util.List;

/**
 * Compares original and revised stories and explains what changed and why.
 * Runs automatically after the revised story is generated.
 */
public class ExplainerAgent extends BaseAgent {

    private static final String PROMPT_FILE = "prompts/explainer_prompt.txt";

    public ExplainerAgent(AppConfig config) {
        super(config);
    }

    public ExplainerResult explain(String startState, String endState,
                                   List<String> originalFacts, List<String> additionalFacts,
                                   String storyV1, String storyV2) throws Exception {

        String   prompt    = buildPrompt(startState, endState, originalFacts, additionalFacts, storyV1, storyV2);
        long     startTime = System.currentTimeMillis();
        JsonNode root      = callClaude(config.getExplainerModel(),
                                        config.getExplainerTemperature(),
                                        config.getMaxTokens(), prompt);
        long     elapsedMs = System.currentTimeMillis() - startTime;

        String explanation  = root.path("content").get(0).path("text").asText();
        int    inputTokens  = root.path("usage").path("input_tokens").asInt(0);
        int    outputTokens = root.path("usage").path("output_tokens").asInt(0);
        double costUsd      = calculateCost(config.getExplainerModel(), inputTokens, outputTokens);

        System.out.printf("[ExplainerAgent] model=%s input=%d output=%d cost=$%.5f elapsed=%dms%n",
                config.getExplainerModel(), inputTokens, outputTokens, costUsd, elapsedMs);

        return new ExplainerResult(explanation, inputTokens, outputTokens, costUsd, elapsedMs);
    }

    private String buildPrompt(String startState, String endState,
                                List<String> originalFacts, List<String> additionalFacts,
                                String storyV1, String storyV2) throws Exception {
        return loadPromptTemplate(PROMPT_FILE)
                .replace("{{START_STATE}}",     startState)
                .replace("{{END_STATE}}",        endState)
                .replace("{{ORIGINAL_FACTS}}",   formatFacts(originalFacts))
                .replace("{{ADDITIONAL_FACTS}}", formatFacts(additionalFacts))
                .replace("{{STORY_V1}}",         storyV1)
                .replace("{{STORY_V2}}",         storyV2);
    }
}
