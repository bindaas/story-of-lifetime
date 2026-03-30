package com.rajivnarula.storyoflifetime.agent;

import com.rajivnarula.storyoflifetime.config.AppConfig;
import com.rajivnarula.storyoflifetime.result.FactGeneratorResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FactGeneratorAgent.
 *
 * Note: tests that exercise factCount=0 are safe to run without an API key —
 * the agent short-circuits before making any network call.
 */
class FactGeneratorAgentTest {

    @Test
    void zeroFactCount_returnsEmptyListWithoutApiCall() throws Exception {
        AppConfig config = new AppConfig();
        FactGeneratorAgent agent = new FactGeneratorAgent(config);

        FactGeneratorResult result = agent.generate(
                "John was born in Texas in 1975.",
                "John became Health Minister of India.",
                "medium", "low", "realistic",
                0   // ← short-circuits, no API call made
        );

        assertTrue(result.getFacts().isEmpty(), "Expected empty fact list for factCount=0");
        assertEquals(0,   result.getInputTokens(),  "Expected 0 input tokens for factCount=0");
        assertEquals(0,   result.getOutputTokens(), "Expected 0 output tokens for factCount=0");
        assertEquals(0.0, result.getCostUsd(), 0.000001, "Expected $0.00 cost for factCount=0");
        assertEquals(0L,  result.getElapsedMs(), "Expected 0ms elapsed for factCount=0");
    }

    @Test
    void zeroFactCount_worldTypeDoesNotMatter() throws Exception {
        AppConfig config = new AppConfig();
        FactGeneratorAgent agent = new FactGeneratorAgent(config);

        for (String worldType : new String[]{"grounded", "realistic", "fantastical", "outlandish"}) {
            FactGeneratorResult result = agent.generate(
                    "Start.", "End.", "high", "high", worldType, 0);
            assertTrue(result.getFacts().isEmpty(),
                    "Expected empty facts for worldType=" + worldType + " with factCount=0");
        }
    }
}
