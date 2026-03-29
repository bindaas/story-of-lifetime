package com.rajivnarula.storyoflifetime;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for all five agent result classes —
 * constructor + getters for FactGeneratorResult, PlannerResult,
 * WriterResult, ExplainerResult, and CriticResult (basic fields).
 */
class ResultClassesTest {

    // ── FactGeneratorResult ────────────────────────────────────────────────────

    @Test
    void factGeneratorResult_getters() {
        List<String> facts = List.of("Fact one.", "Fact two.", "Fact three.");
        FactGeneratorResult r = new FactGeneratorResult(facts, 500, 80, 0.0018, 2000L);
        assertEquals(facts,   r.getFacts());
        assertEquals(3,       r.getFacts().size());
        assertEquals(500,     r.getInputTokens());
        assertEquals(80,      r.getOutputTokens());
        assertEquals(0.0018,  r.getCostUsd(), 0.000001);
        assertEquals(2000L,   r.getElapsedMs());
    }

    @Test
    void factGeneratorResult_emptyFacts() {
        FactGeneratorResult r = new FactGeneratorResult(List.of(), 0, 0, 0.0, 0L);
        assertTrue(r.getFacts().isEmpty());
        assertEquals(0, r.getInputTokens());
        assertEquals(0, r.getOutputTokens());
        assertEquals(0.0, r.getCostUsd(), 0.000001);
        assertEquals(0L, r.getElapsedMs());
    }

    // ── PlannerResult ──────────────────────────────────────────────────────────

    @Test
    void plannerResult_getters() {
        String outline = "Milestone 1: Start — Begin journey.\nMilestone 2: End — Reach destination.";
        PlannerResult r = new PlannerResult(outline, 800, 150, 0.0042, 4500L);
        assertEquals(outline, r.getOutline());
        assertEquals(800,     r.getInputTokens());
        assertEquals(150,     r.getOutputTokens());
        assertEquals(0.0042,  r.getCostUsd(), 0.000001);
        assertEquals(4500L,   r.getElapsedMs());
    }

    // ── WriterResult ───────────────────────────────────────────────────────────

    @Test
    void writerResult_getters() {
        String story = "Once upon a time, John was born in Texas...";
        WriterResult r = new WriterResult(story, 2000, 600, 0.0510, 12000L);
        assertEquals(story,   r.getStory());
        assertEquals(2000,    r.getInputTokens());
        assertEquals(600,     r.getOutputTokens());
        assertEquals(0.0510,  r.getCostUsd(), 0.000001);
        assertEquals(12000L,  r.getElapsedMs());
    }

    // ── ExplainerResult ────────────────────────────────────────────────────────

    @Test
    void explainerResult_getters() {
        String explanation = "The new facts changed the middle section significantly.";
        ExplainerResult r = new ExplainerResult(explanation, 3000, 300, 0.0135, 8000L);
        assertEquals(explanation, r.getExplanation());
        assertEquals(3000,        r.getInputTokens());
        assertEquals(300,         r.getOutputTokens());
        assertEquals(0.0135,      r.getCostUsd(), 0.000001);
        assertEquals(8000L,       r.getElapsedMs());
    }
}
