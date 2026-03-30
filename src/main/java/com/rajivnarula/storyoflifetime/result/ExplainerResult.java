package com.rajivnarula.storyoflifetime.result;

/**
 * Holds the output of ExplainerAgent — the diff analysis explanation.
 */
public class ExplainerResult extends AgentResult {

    private final String explanation;

    public ExplainerResult(String explanation, int inputTokens, int outputTokens,
                           double costUsd, long elapsedMs) {
        super(inputTokens, outputTokens, costUsd, elapsedMs);
        this.explanation = explanation;
    }

    public String getExplanation() { return explanation; }
}
