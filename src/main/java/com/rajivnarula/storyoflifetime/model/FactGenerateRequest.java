package com.rajivnarula.storyoflifetime.model;

/**
 * JSON payload for the /api/generate-facts endpoint.
 */
public class FactGenerateRequest {

    private String startState;
    private String endState;
    private String creativity;
    private String contradiction;
    private String worldType;     // grounded | realistic | fantastical | outlandish
    private int    factCount = 5; // number of facts to generate (0–10)

    public String getStartState()              { return startState; }
    public void   setStartState(String v)      { this.startState = v; }
    public String getEndState()                { return endState; }
    public void   setEndState(String v)        { this.endState = v; }
    public String getCreativity()              { return creativity; }
    public void   setCreativity(String v)      { this.creativity = v; }
    public String getContradiction()           { return contradiction; }
    public void   setContradiction(String v)   { this.contradiction = v; }
    public String getWorldType()               { return worldType; }
    public void   setWorldType(String v)       { this.worldType = v; }
    public int    getFactCount()               { return factCount; }
    public void   setFactCount(int v)          { this.factCount = v; }
}
