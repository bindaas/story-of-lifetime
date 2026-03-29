package com.rajivnarula.storyoflifetime;

import java.util.List;

/**
 * Holds the output of the FactGeneratorAgent —
 * the generated facts plus token usage and cost metadata.
 */
public class FactGeneratorResult {

    private final List<String> facts;
    private final int          inputTokens;
    private final int          outputTokens;
    private final double       costUsd;
    private final long         elapsedMs;

    public FactGeneratorResult(List<String> facts, int inputTokens, int outputTokens,
                               double costUsd, long elapsedMs) {
        this.facts        = facts;
        this.inputTokens  = inputTokens;
        this.outputTokens = outputTokens;
        this.costUsd      = costUsd;
        this.elapsedMs    = elapsedMs;
    }

    public List<String> getFacts()        { return facts; }
    public int          getInputTokens()  { return inputTokens; }
    public int          getOutputTokens() { return outputTokens; }
    public double       getCostUsd()      { return costUsd; }
    public long         getElapsedMs()    { return elapsedMs; }
}
