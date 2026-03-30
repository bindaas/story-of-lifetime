package com.rajivnarula.storyoflifetime.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.rajivnarula.storyoflifetime.config.AppConfig;
import com.rajivnarula.storyoflifetime.result.FactGeneratorResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates life facts connecting start to end state.
 * Creativity and contradiction levels shape the nature of the facts.
 * Output is reviewed and optionally edited by a human before the pipeline proceeds.
 */
public class FactGeneratorAgent extends BaseAgent {

    private static final String PROMPT_FILE = "prompts/factgenerator_prompt.txt";

    public FactGeneratorAgent(AppConfig config) {
        super(config);
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
        JsonNode root      = callClaude(config.getFactGeneratorModel(),
                                        config.getFactGeneratorTemperature(),
                                        config.getMaxTokens(), prompt);
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

    private String buildPrompt(String startState, String endState,
                                String creativity, String contradiction,
                                String worldType, int factCount) throws Exception {
        return loadPromptTemplate(PROMPT_FILE)
                .replace("{{START_STATE}}",   startState)
                .replace("{{END_STATE}}",     endState)
                .replace("{{CREATIVITY}}",    creativity)
                .replace("{{CONTRADICTION}}", contradiction)
                .replace("{{WORLD_TYPE}}",    worldType)
                .replace("{{FACT_COUNT}}",    String.valueOf(factCount));
    }
}
