package com.rajivnarula.storyoflifetime;

/**
 * Full response sent back to the browser.
 * Includes outline, story, and separate cost breakdown for Planner and Writer.
 */
public class StoryResponse {

    private final String outline;
    private final String story;
    private final int    factCount;
    private final String storyLength;

    // Planner metrics
    private final String plannerModel;
    private final int    plannerInputTokens;
    private final int    plannerOutputTokens;
    private final double plannerCostUsd;
    private final long   plannerElapsedMs;

    // Writer metrics
    private final String writerModel;
    private final int    writerInputTokens;
    private final int    writerOutputTokens;
    private final double writerCostUsd;
    private final long   writerElapsedMs;

    public StoryResponse(String outline, String story, int factCount, String storyLength,
                         String plannerModel, int plannerInputTokens, int plannerOutputTokens,
                         double plannerCostUsd, long plannerElapsedMs,
                         String writerModel, int writerInputTokens, int writerOutputTokens,
                         double writerCostUsd, long writerElapsedMs) {
        this.outline             = outline;
        this.story               = story;
        this.factCount           = factCount;
        this.storyLength         = storyLength;
        this.plannerModel        = plannerModel;
        this.plannerInputTokens  = plannerInputTokens;
        this.plannerOutputTokens = plannerOutputTokens;
        this.plannerCostUsd      = plannerCostUsd;
        this.plannerElapsedMs    = plannerElapsedMs;
        this.writerModel         = writerModel;
        this.writerInputTokens   = writerInputTokens;
        this.writerOutputTokens  = writerOutputTokens;
        this.writerCostUsd       = writerCostUsd;
        this.writerElapsedMs     = writerElapsedMs;
    }

    public String getOutline()              { return outline; }
    public String getStory()                { return story; }
    public int    getFactCount()            { return factCount; }
    public String getStoryLength()          { return storyLength; }

    public String getPlannerModel()         { return plannerModel; }
    public int    getPlannerInputTokens()   { return plannerInputTokens; }
    public int    getPlannerOutputTokens()  { return plannerOutputTokens; }
    public double getPlannerCostUsd()       { return plannerCostUsd; }
    public long   getPlannerElapsedMs()     { return plannerElapsedMs; }

    public String getWriterModel()          { return writerModel; }
    public int    getWriterInputTokens()    { return writerInputTokens; }
    public int    getWriterOutputTokens()   { return writerOutputTokens; }
    public double getWriterCostUsd()        { return writerCostUsd; }
    public long   getWriterElapsedMs()      { return writerElapsedMs; }

    public double getTotalCostUsd()         { return plannerCostUsd + writerCostUsd; }
    public long   getTotalElapsedMs()       { return plannerElapsedMs + writerElapsedMs; }
}
