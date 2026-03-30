package com.rajivnarula.storyoflifetime.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.rajivnarula.storyoflifetime.config.AppConfig;
import com.rajivnarula.storyoflifetime.model.WorldModel;
import com.rajivnarula.storyoflifetime.result.CriticResult;

/**
 * Evaluates a Planner outline and either approves or rejects it.
 * If rejected, the reason is fed back into the Planner for a revised attempt.
 * Uses a low temperature — we want analytical, deterministic judgements.
 */
public class CriticAgent extends BaseAgent {

    private static final String PROMPT_FILE      = "prompts/critic_prompt.txt";
    private static final int    CRITIC_MAX_TOKENS = 512;

    public CriticAgent(AppConfig config) {
        super(config);
    }

    /**
     * previousFeedback is empty on the first attempt;
     * on subsequent attempts it contains the prior rejection reason.
     */
    public CriticResult evaluate(WorldModel worldModel, String outline,
                                 String previousFeedback) throws Exception {

        String   prompt    = buildPrompt(worldModel, outline, previousFeedback);
        long     startTime = System.currentTimeMillis();
        JsonNode root      = callClaude(config.getCriticModel(),
                                        config.getCriticTemperature(),
                                        CRITIC_MAX_TOKENS, prompt);
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

    private String buildPrompt(WorldModel worldModel, String outline,
                                String previousFeedback) throws Exception {
        String feedbackSection = (previousFeedback == null || previousFeedback.isBlank())
                ? ""
                : "PREVIOUS REJECTION REASON:\n" + previousFeedback
                  + "\n\nNote: the outline has been revised in response to this feedback. Evaluate the revised version.";

        return loadPromptTemplate(PROMPT_FILE)
                .replace("{{START_STATE}}",      worldModel.getStartState())
                .replace("{{END_STATE}}",        worldModel.getEndState())
                .replace("{{FACTS}}",            formatFacts(worldModel.getFacts()))
                .replace("{{OUTLINE}}",          outline)
                .replace("{{FEEDBACK_SECTION}}", feedbackSection);
    }
}
