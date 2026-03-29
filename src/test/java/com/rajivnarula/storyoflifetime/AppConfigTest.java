package com.rajivnarula.storyoflifetime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AppConfig — story length translation and defaults loaded from system.properties.
 */
class AppConfigTest {

    @Test
    void testDefaultsLoadedFromProperties() throws Exception {
        AppConfig config = new AppConfig();
        assertEquals("claude-sonnet-4-6", config.getFactGeneratorModel());
        assertEquals(0.9, config.getFactGeneratorTemperature(), 0.001);
        assertEquals("claude-sonnet-4-6", config.getPlannerModel());
        assertEquals(0.3, config.getPlannerTemperature(), 0.001);
        assertEquals("claude-sonnet-4-6", config.getCriticModel());
        assertEquals(0.1, config.getCriticTemperature(), 0.001);
        assertEquals(3,   config.getCriticMaxAttempts());
        assertEquals("claude-opus-4-6",   config.getWriterModel());
        assertEquals(0.8, config.getWriterTemperature(), 0.001);
        assertEquals("claude-sonnet-4-6", config.getExplainerModel());
        assertEquals(0.3, config.getExplainerTemperature(), 0.001);
        assertEquals(2048, config.getMaxTokens());
    }

    @Test
    void testStoryLengthShort() throws Exception {
        AppConfig config = new AppConfig("m", 0.3, "m", 0.1, "m", 0.8, "short");
        assertEquals("2 to 3 paragraphs", config.getStoryLength());
    }

    @Test
    void testStoryLengthMedium() throws Exception {
        AppConfig config = new AppConfig("m", 0.3, "m", 0.1, "m", 0.8, "medium");
        assertEquals("4 to 6 paragraphs", config.getStoryLength());
    }

    @Test
    void testStoryLengthLong() throws Exception {
        AppConfig config = new AppConfig("m", 0.3, "m", 0.1, "m", 0.8, "long");
        assertEquals("8 to 10 paragraphs", config.getStoryLength());
    }

    @Test
    void testStoryLengthUnknownDefaultsMedium() throws Exception {
        AppConfig config = new AppConfig("m", 0.3, "m", 0.1, "m", 0.8, "unknown");
        assertEquals("4 to 6 paragraphs", config.getStoryLength());
    }

    @Test
    void testStoryLengthCaseInsensitive() throws Exception {
        AppConfig config = new AppConfig("m", 0.3, "m", 0.1, "m", 0.8, "SHORT");
        assertEquals("2 to 3 paragraphs", config.getStoryLength());
    }

    @Test
    void testGetStoryLengthRaw() throws Exception {
        AppConfig config = new AppConfig("m", 0.3, "m", 0.1, "m", 0.8, "long");
        assertEquals("long", config.getStoryLengthRaw());
    }

    @Test
    void testPerRequestConstructorOverridesModels() throws Exception {
        AppConfig config = new AppConfig(
                "claude-opus-4-6", 0.5,
                "claude-haiku-4-5-20251001", 0.2,
                "claude-opus-4-6", 0.9,
                "short");
        assertEquals("claude-opus-4-6",          config.getPlannerModel());
        assertEquals(0.5,                         config.getPlannerTemperature(), 0.001);
        assertEquals("claude-haiku-4-5-20251001", config.getCriticModel());
        assertEquals(0.2,                         config.getCriticTemperature(), 0.001);
        assertEquals("claude-opus-4-6",           config.getWriterModel());
        assertEquals(0.9,                         config.getWriterTemperature(), 0.001);
        // factGenerator and explainer still come from system.properties
        assertEquals("claude-sonnet-4-6", config.getFactGeneratorModel());
        assertEquals("claude-sonnet-4-6", config.getExplainerModel());
    }
}
