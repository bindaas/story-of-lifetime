package com.rajivnarula.storyoflifetime.result;

import java.util.List;

/**
 * Holds the output of FactGeneratorAgent — the generated facts list.
 */
public class FactGeneratorResult extends AgentResult {

    private final List<String> facts;

    public FactGeneratorResult(List<String> facts, int inputTokens, int outputTokens,
                               double costUsd, long elapsedMs) {
        super(inputTokens, outputTokens, costUsd, elapsedMs);
        this.facts = facts;
    }

    public List<String> getFacts() { return facts; }
}
