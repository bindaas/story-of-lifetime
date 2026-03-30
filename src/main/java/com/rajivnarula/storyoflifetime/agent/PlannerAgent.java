package com.rajivnarula.storyoflifetime.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.rajivnarula.storyoflifetime.config.AppConfig;
import com.rajivnarula.storyoflifetime.model.WorldModel;
import com.rajivnarula.storyoflifetime.result.PlannerResult;

/**
 * Produces a structured story outline from the world model.
 * On retry attempts, critic feedback is injected into the prompt
 * so the Planner knows exactly what to fix.
 */
public class PlannerAgent extends BaseAgent {

    private static final String PROMPT_FILE = "prompts/planner_prompt.txt";

    public PlannerAgent(AppConfig config) {
        super(config);
    }

    /**
     * criticFeedback is empty on the first attempt.
     * On retries it contains the Critic's rejection reason.
     */
    public PlannerResult plan(WorldModel worldModel, String criticFeedback) throws Exception {
        String   prompt    = buildPrompt(worldModel, criticFeedback);
        long     startTime = System.currentTimeMillis();
        JsonNode root      = callClaude(config.getPlannerModel(),
                                        config.getPlannerTemperature(),
                                        config.getMaxTokens(), prompt);
        long     elapsedMs = System.currentTimeMillis() - startTime;

        String outline      = root.path("content").get(0).path("text").asText();
        int    inputTokens  = root.path("usage").path("input_tokens").asInt(0);
        int    outputTokens = root.path("usage").path("output_tokens").asInt(0);
        double costUsd      = calculateCost(config.getPlannerModel(), inputTokens, outputTokens);

        System.out.printf("[PlannerAgent] model=%s input=%d output=%d cost=$%.5f elapsed=%dms%n",
                config.getPlannerModel(), inputTokens, outputTokens, costUsd, elapsedMs);

        return new PlannerResult(outline, inputTokens, outputTokens, costUsd, elapsedMs);
    }

    private String buildPrompt(WorldModel worldModel, String criticFeedback) throws Exception {
        String feedbackSection = (criticFeedback == null || criticFeedback.isBlank())
                ? ""
                : "CRITIC FEEDBACK FROM PREVIOUS ATTEMPT:\n" + criticFeedback
                  + "\n\nYour revised outline must specifically address this feedback.";

        return loadPromptTemplate(PROMPT_FILE)
                .replace("{{START_STATE}}",      worldModel.getStartState())
                .replace("{{END_STATE}}",        worldModel.getEndState())
                .replace("{{FACTS}}",            formatFacts(worldModel.getFacts()))
                .replace("{{FEEDBACK_SECTION}}", feedbackSection);
    }
}
