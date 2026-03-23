package com.rajivnarula.storyoflifetime;

/**
 * Holds the output of the PlannerAgent —
 * the outline text plus token usage and cost metadata.
 */
public class PlannerResult {

    private final String outline;
    private final int    inputTokens;
    private final int    outputTokens;
    private final double costUsd;
    private final long   elapsedMs;

    public PlannerResult(String outline, int inputTokens, int outputTokens,
                         double costUsd, long elapsedMs) {
        this.outline      = outline;
        this.inputTokens  = inputTokens;
        this.outputTokens = outputTokens;
        this.costUsd      = costUsd;
        this.elapsedMs    = elapsedMs;
    }

    public String getOutline()       { return outline; }
    public int    getInputTokens()   { return inputTokens; }
    public int    getOutputTokens()  { return outputTokens; }
    public double getCostUsd()       { return costUsd; }
    public long   getElapsedMs()     { return elapsedMs; }
}
