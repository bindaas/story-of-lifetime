package com.rajivnarula.storyoflifetime.result;

/**
 * Base class for all agent result types.
 * Holds the four common metrics every agent returns: token counts, cost, and elapsed time.
 */
public abstract class AgentResult {

    private final int    inputTokens;
    private final int    outputTokens;
    private final double costUsd;
    private final long   elapsedMs;

    protected AgentResult(int inputTokens, int outputTokens, double costUsd, long elapsedMs) {
        this.inputTokens  = inputTokens;
        this.outputTokens = outputTokens;
        this.costUsd      = costUsd;
        this.elapsedMs    = elapsedMs;
    }

    public int    getInputTokens()  { return inputTokens; }
    public int    getOutputTokens() { return outputTokens; }
    public double getCostUsd()      { return costUsd; }
    public long   getElapsedMs()    { return elapsedMs; }
}
