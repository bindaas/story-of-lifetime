package com.rajivnarula.storyoflifetime;

import java.util.List;

/**
 * Full response sent back to the browser.
 * Includes outline, critic decisions, story, and cost breakdown per agent.
 */
public class StoryResponse {

    // Story content
    private final String       outline;
    private final String       story;
    private final int          factCount;
    private final String       storyLength;
    private final int          plannerAttempts;

    // Critic decisions (one per Planner attempt)
    private final List<String> criticDecisions;  // "APPROVED" or "REJECTED"
    private final List<String> criticReasons;

    // Planner metrics (summed across all attempts)
    private final String plannerModel;
    private final int    plannerInputTokens;
    private final int    plannerOutputTokens;
    private final double plannerCostUsd;
    private final long   plannerElapsedMs;

    // Critic metrics (summed across all attempts)
    private final String criticModel;
    private final int    criticInputTokens;
    private final int    criticOutputTokens;
    private final double criticCostUsd;
    private final long   criticElapsedMs;

    // Writer metrics
    private final String writerModel;
    private final int    writerInputTokens;
    private final int    writerOutputTokens;
    private final double writerCostUsd;
    private final long   writerElapsedMs;

    public StoryResponse(String outline, String story, int factCount, String storyLength,
                         int plannerAttempts, List<String> criticDecisions, List<String> criticReasons,
                         String plannerModel, int plannerInputTokens, int plannerOutputTokens,
                         double plannerCostUsd, long plannerElapsedMs,
                         String criticModel, int criticInputTokens, int criticOutputTokens,
                         double criticCostUsd, long criticElapsedMs,
                         String writerModel, int writerInputTokens, int writerOutputTokens,
                         double writerCostUsd, long writerElapsedMs) {
        this.outline              = outline;
        this.story                = story;
        this.factCount            = factCount;
        this.storyLength          = storyLength;
        this.plannerAttempts      = plannerAttempts;
        this.criticDecisions      = criticDecisions;
        this.criticReasons        = criticReasons;
        this.plannerModel         = plannerModel;
        this.plannerInputTokens   = plannerInputTokens;
        this.plannerOutputTokens  = plannerOutputTokens;
        this.plannerCostUsd       = plannerCostUsd;
        this.plannerElapsedMs     = plannerElapsedMs;
        this.criticModel          = criticModel;
        this.criticInputTokens    = criticInputTokens;
        this.criticOutputTokens   = criticOutputTokens;
        this.criticCostUsd        = criticCostUsd;
        this.criticElapsedMs      = criticElapsedMs;
        this.writerModel          = writerModel;
        this.writerInputTokens    = writerInputTokens;
        this.writerOutputTokens   = writerOutputTokens;
        this.writerCostUsd        = writerCostUsd;
        this.writerElapsedMs      = writerElapsedMs;
    }

    public String       getOutline()             { return outline; }
    public String       getStory()               { return story; }
    public int          getFactCount()           { return factCount; }
    public String       getStoryLength()         { return storyLength; }
    public int          getPlannerAttempts()     { return plannerAttempts; }
    public List<String> getCriticDecisions()     { return criticDecisions; }
    public List<String> getCriticReasons()       { return criticReasons; }

    public String getPlannerModel()              { return plannerModel; }
    public int    getPlannerInputTokens()        { return plannerInputTokens; }
    public int    getPlannerOutputTokens()       { return plannerOutputTokens; }
    public double getPlannerCostUsd()            { return plannerCostUsd; }
    public long   getPlannerElapsedMs()          { return plannerElapsedMs; }

    public String getCriticModel()               { return criticModel; }
    public int    getCriticInputTokens()         { return criticInputTokens; }
    public int    getCriticOutputTokens()        { return criticOutputTokens; }
    public double getCriticCostUsd()             { return criticCostUsd; }
    public long   getCriticElapsedMs()           { return criticElapsedMs; }

    public String getWriterModel()               { return writerModel; }
    public int    getWriterInputTokens()         { return writerInputTokens; }
    public int    getWriterOutputTokens()        { return writerOutputTokens; }
    public double getWriterCostUsd()             { return writerCostUsd; }
    public long   getWriterElapsedMs()           { return writerElapsedMs; }

    public double getTotalCostUsd()  { return plannerCostUsd + criticCostUsd + writerCostUsd; }
    public long   getTotalElapsedMs(){ return plannerElapsedMs + criticElapsedMs + writerElapsedMs; }
}
