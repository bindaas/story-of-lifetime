package com.rajivnarula.storyoflifetime.model;

import java.util.List;

/**
 * JSON payload sent from the browser for the explain diff endpoint.
 */
public class ExplainRequest {

    private String       startState;
    private String       endState;
    private List<String> originalFacts;
    private List<String> additionalFacts;
    private String       storyV1;
    private String       storyV2;

    public String       getStartState()                       { return startState; }
    public void         setStartState(String v)               { this.startState = v; }
    public String       getEndState()                         { return endState; }
    public void         setEndState(String v)                 { this.endState = v; }
    public List<String> getOriginalFacts()                    { return originalFacts; }
    public void         setOriginalFacts(List<String> v)      { this.originalFacts = v; }
    public List<String> getAdditionalFacts()                  { return additionalFacts; }
    public void         setAdditionalFacts(List<String> v)    { this.additionalFacts = v; }
    public String       getStoryV1()                          { return storyV1; }
    public void         setStoryV1(String v)                  { this.storyV1 = v; }
    public String       getStoryV2()                          { return storyV2; }
    public void         setStoryV2(String v)                  { this.storyV2 = v; }
}
