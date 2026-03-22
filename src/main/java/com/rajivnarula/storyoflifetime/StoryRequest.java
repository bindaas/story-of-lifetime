package com.rajivnarula.storyoflifetime;

/**
 * Represents the JSON payload sent from the browser form.
 * Each field maps directly to a form input.
 */
public class StoryRequest {

    private String startState;
    private String endState;
    private String facts;       // raw textarea — one fact per line
    private String model;
    private double temperature;
    private String storyLength;

    public String getStartState()          { return startState; }
    public void setStartState(String v)    { this.startState = v; }

    public String getEndState()            { return endState; }
    public void setEndState(String v)      { this.endState = v; }

    public String getFacts()               { return facts; }
    public void setFacts(String v)         { this.facts = v; }

    public String getModel()               { return model; }
    public void setModel(String v)         { this.model = v; }

    public double getTemperature()         { return temperature; }
    public void setTemperature(double v)   { this.temperature = v; }

    public String getStoryLength()         { return storyLength; }
    public void setStoryLength(String v)   { this.storyLength = v; }
}
