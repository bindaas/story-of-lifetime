package com.rajivnarula.storyoflifetime.result;

/**
 * Holds the output of one CriticAgent evaluation —
 * the APPROVED/REJECTED decision and the reason behind it.
 */
public class CriticResult extends AgentResult {

    public enum Decision { APPROVED, REJECTED }

    private final Decision decision;
    private final String   reason;

    public CriticResult(Decision decision, String reason,
                        int inputTokens, int outputTokens,
                        double costUsd, long elapsedMs) {
        super(inputTokens, outputTokens, costUsd, elapsedMs);
        this.decision = decision;
        this.reason   = reason;
    }

    public boolean  isApproved()  { return decision == Decision.APPROVED; }
    public Decision getDecision() { return decision; }
    public String   getReason()   { return reason; }
}
