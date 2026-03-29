package com.rajivnarula.storyoflifetime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CriticResult — the core APPROVED/REJECTED decision logic
 * that drives the Planner→Critic feedback loop.
 */
class CriticResultTest {

    @Test
    void testApprovedDecision() {
        CriticResult result = new CriticResult(
                CriticResult.Decision.APPROVED, "Outline is coherent.",
                100, 20, 0.0005, 1500L);
        assertTrue(result.isApproved());
        assertEquals(CriticResult.Decision.APPROVED, result.getDecision());
    }

    @Test
    void testRejectedDecision() {
        CriticResult result = new CriticResult(
                CriticResult.Decision.REJECTED, "Milestone 3 contradicts fact 2.",
                100, 20, 0.0005, 1500L);
        assertFalse(result.isApproved());
        assertEquals(CriticResult.Decision.REJECTED, result.getDecision());
    }

    @Test
    void testReasonPreserved() {
        String reason = "Timeline is implausible — character cannot be in two places at once.";
        CriticResult result = new CriticResult(
                CriticResult.Decision.REJECTED, reason, 0, 0, 0, 0L);
        assertEquals(reason, result.getReason());
    }

    @Test
    void testTokensAndCost() {
        CriticResult result = new CriticResult(
                CriticResult.Decision.APPROVED, "OK",
                1200, 85, 0.00415, 3200L);
        assertEquals(1200,    result.getInputTokens());
        assertEquals(85,      result.getOutputTokens());
        assertEquals(0.00415, result.getCostUsd(), 0.000001);
        assertEquals(3200L,   result.getElapsedMs());
    }

    @Test
    void testDecisionEnumValues() {
        assertEquals(2, CriticResult.Decision.values().length);
        assertNotNull(CriticResult.Decision.valueOf("APPROVED"));
        assertNotNull(CriticResult.Decision.valueOf("REJECTED"));
    }
}
