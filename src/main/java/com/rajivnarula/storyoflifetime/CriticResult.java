package com.rajivnarula.storyoflifetime;

/**
 * Holds the output of one Critic evaluation —
 * the decision (approved/rejected), reason, and cost metadata.
 */
public class CriticResult {

    public enum Decision { APPROVED, REJECTED }

    private final Decision decision;
    private final String   reason;
    private final int      inputTokens;
    private final int      outputTokens;
    private final double   costUsd;
    private final long     elapsedMs;

    public CriticResult(Decision decision, String reason,
                        int inputTokens, int outputTokens,
                        double costUsd, long elapsedMs) {
        this.decision     = decision;
        this.reason       = reason;
        this.inputTokens  = inputTokens;
        this.outputTokens = outputTokens;
        this.costUsd      = costUsd;
        this.elapsedMs    = elapsedMs;
    }

    public boolean   isApproved()      { return decision == Decision.APPROVED; }
    public Decision  getDecision()     { return decision; }
    public String    getReason()       { return reason; }
    public int       getInputTokens()  { return inputTokens; }
    public int       getOutputTokens() { return outputTokens; }
    public double    getCostUsd()      { return costUsd; }
    public long      getElapsedMs()    { return elapsedMs; }
}
