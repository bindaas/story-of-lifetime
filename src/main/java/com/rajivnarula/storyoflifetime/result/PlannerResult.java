package com.rajivnarula.storyoflifetime.result;

/**
 * Holds the output of PlannerAgent — the story outline.
 */
public class PlannerResult extends AgentResult {

    private final String outline;

    public PlannerResult(String outline, int inputTokens, int outputTokens,
                         double costUsd, long elapsedMs) {
        super(inputTokens, outputTokens, costUsd, elapsedMs);
        this.outline = outline;
    }

    public String getOutline() { return outline; }
}
