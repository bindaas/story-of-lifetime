package com.rajivnarula.storyoflifetime;

/**
 * Holds the output of the ExplainerAgent —
 * the diff analysis text plus token usage and cost metadata.
 */
public class ExplainerResult {

    private final String explanation;
    private final int    inputTokens;
    private final int    outputTokens;
    private final double costUsd;
    private final long   elapsedMs;

    public ExplainerResult(String explanation, int inputTokens, int outputTokens,
                           double costUsd, long elapsedMs) {
        this.explanation  = explanation;
        this.inputTokens  = inputTokens;
        this.outputTokens = outputTokens;
        this.costUsd      = costUsd;
        this.elapsedMs    = elapsedMs;
    }

    public String getExplanation()  { return explanation; }
    public int    getInputTokens()  { return inputTokens; }
    public int    getOutputTokens() { return outputTokens; }
    public double getCostUsd()      { return costUsd; }
    public long   getElapsedMs()    { return elapsedMs; }
}
