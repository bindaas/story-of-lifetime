package com.rajivnarula.storyoflifetime.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.rajivnarula.storyoflifetime.config.AppConfig;
import com.rajivnarula.storyoflifetime.model.WorldModel;
import com.rajivnarula.storyoflifetime.result.WriterResult;

/**
 * Generates full prose from the WorldModel and Planner outline.
 * Writing style is shaped by world type. Used for both original and revised stories.
 */
public class WriterAgent extends BaseAgent {

    private static final String PROMPT_FILE = "prompts/writer_prompt.txt";

    public WriterAgent(AppConfig config) {
        super(config);
    }

    public WriterResult write(WorldModel worldModel, String outline) throws Exception {
        String   prompt    = buildPrompt(worldModel, outline);
        long     startTime = System.currentTimeMillis();
        JsonNode root      = callClaude(config.getWriterModel(),
                                        config.getWriterTemperature(),
                                        config.getMaxTokens(), prompt);
        long     elapsedMs = System.currentTimeMillis() - startTime;

        String story        = root.path("content").get(0).path("text").asText();
        int    inputTokens  = root.path("usage").path("input_tokens").asInt(0);
        int    outputTokens = root.path("usage").path("output_tokens").asInt(0);
        double costUsd      = calculateCost(config.getWriterModel(), inputTokens, outputTokens);

        System.out.printf("[WriterAgent] model=%s input=%d output=%d cost=$%.5f elapsed=%dms%n",
                config.getWriterModel(), inputTokens, outputTokens, costUsd, elapsedMs);

        return new WriterResult(story, inputTokens, outputTokens, costUsd, elapsedMs);
    }

    private String buildPrompt(WorldModel worldModel, String outline) throws Exception {
        return loadPromptTemplate(PROMPT_FILE)
                .replace("{{START_STATE}}",  worldModel.getStartState())
                .replace("{{END_STATE}}",    worldModel.getEndState())
                .replace("{{FACTS}}",        formatFacts(worldModel.getFacts()))
                .replace("{{OUTLINE}}",      outline)
                .replace("{{WORLD_TYPE}}",   worldModel.getWorldType())
                .replace("{{STORY_LENGTH}}", config.getStoryLength());
    }
}
