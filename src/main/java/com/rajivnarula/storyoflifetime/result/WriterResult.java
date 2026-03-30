package com.rajivnarula.storyoflifetime.result;

/**
 * Holds the output of WriterAgent — the generated story prose.
 */
public class WriterResult extends AgentResult {

    private final String story;

    public WriterResult(String story, int inputTokens, int outputTokens,
                        double costUsd, long elapsedMs) {
        super(inputTokens, outputTokens, costUsd, elapsedMs);
        this.story = story;
    }

    public String getStory() { return story; }
}
