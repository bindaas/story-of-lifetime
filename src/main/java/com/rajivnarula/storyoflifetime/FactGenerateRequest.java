package com.rajivnarula.storyoflifetime;

/**
 * JSON payload for the /api/generate-facts endpoint.
 */
public class FactGenerateRequest {

    private String startState;
    private String endState;
    private String creativity;
    private String contradiction;
    private String worldType;     // grounded | realistic | fantastical | outlandish

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
}
