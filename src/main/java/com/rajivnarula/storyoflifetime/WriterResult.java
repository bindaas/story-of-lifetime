package com.rajivnarula.storyoflifetime;

/**
 * Holds the output of the WriterAgent —
 * the story text plus token usage and cost metadata.
 */
public class WriterResult {

    private final String story;
    private final int    inputTokens;
    private final int    outputTokens;
    private final double costUsd;
    private final long   elapsedMs;

    public WriterResult(String story, int inputTokens, int outputTokens,
                        double costUsd, long elapsedMs) {
        this.story        = story;
        this.inputTokens  = inputTokens;
        this.outputTokens = outputTokens;
        this.costUsd      = costUsd;
        this.elapsedMs    = elapsedMs;
    }

    public String getStory()        { return story; }
    public int    getInputTokens()  { return inputTokens; }
    public int    getOutputTokens() { return outputTokens; }
    public double getCostUsd()      { return costUsd; }
    public long   getElapsedMs()    { return elapsedMs; }
}
