package com.rajivnarula.storyoflifetime;

/**
 * Represents the full response sent back to the browser.
 * Includes the story plus token usage and cost metadata.
 */
public class StoryResponse {

    private String story;
    private String model;
    private String storyLength;
    private int    factCount;
    private int    inputTokens;
    private int    outputTokens;
    private double costUsd;
    private long   elapsedMs;

    public StoryResponse(String story, String model, String storyLength,
                         int factCount, int inputTokens, int outputTokens,
                         double costUsd, long elapsedMs) {
        this.story        = story;
        this.model        = model;
        this.storyLength  = storyLength;
        this.factCount    = factCount;
        this.inputTokens  = inputTokens;
        this.outputTokens = outputTokens;
        this.costUsd      = costUsd;
        this.elapsedMs    = elapsedMs;
    }

    public String getStory()        { return story; }
    public String getModel()        { return model; }
    public String getStoryLength()  { return storyLength; }
    public int    getFactCount()    { return factCount; }
    public int    getInputTokens()  { return inputTokens; }
    public int    getOutputTokens() { return outputTokens; }
    public double getCostUsd()      { return costUsd; }
    public long   getElapsedMs()    { return elapsedMs; }
}
