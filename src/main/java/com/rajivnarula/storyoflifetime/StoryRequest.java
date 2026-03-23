package com.rajivnarula.storyoflifetime;

/**
 * JSON payload sent from the browser form.
 * Separate model + temperature for Planner and Writer.
 */
public class StoryRequest {

    private String startState;
    private String endState;
    private String facts;

    private String plannerModel;
    private double plannerTemperature;

    private String writerModel;
    private double writerTemperature;

    private String storyLength;

    public String getStartState()               { return startState; }
    public void   setStartState(String v)       { this.startState = v; }

    public String getEndState()                 { return endState; }
    public void   setEndState(String v)         { this.endState = v; }

    public String getFacts()                    { return facts; }
    public void   setFacts(String v)            { this.facts = v; }

    public String getPlannerModel()             { return plannerModel; }
    public void   setPlannerModel(String v)     { this.plannerModel = v; }

    public double getPlannerTemperature()       { return plannerTemperature; }
    public void   setPlannerTemperature(double v) { this.plannerTemperature = v; }

    public String getWriterModel()              { return writerModel; }
    public void   setWriterModel(String v)      { this.writerModel = v; }

    public double getWriterTemperature()        { return writerTemperature; }
    public void   setWriterTemperature(double v)  { this.writerTemperature = v; }

    public String getStoryLength()              { return storyLength; }
    public void   setStoryLength(String v)      { this.storyLength = v; }
}
