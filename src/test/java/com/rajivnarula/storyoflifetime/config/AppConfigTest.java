package com.rajivnarula.storyoflifetime.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AppConfig — default values, story length mapping, per-request constructor.
 */
class AppConfigTest {

    @Test
    void defaultConstructor_loadsDefaults() throws Exception {
        AppConfig config = new AppConfig();
        assertNotNull(config.getFactGeneratorModel());
        assertNotNull(config.getPlannerModel());
        assertNotNull(config.getCriticModel());
        assertNotNull(config.getWriterModel());
        assertNotNull(config.getExplainerModel());
        assertTrue(config.getMaxTokens() > 0);
        assertTrue(config.getCriticMaxAttempts() > 0);
    }

    @Test
    void defaultConstructor_storyLengthDefaultsMedium() throws Exception {
        AppConfig config = new AppConfig();
        assertEquals("4 to 6 paragraphs", config.getStoryLength());
        assertEquals("medium", config.getStoryLengthRaw());
    }

    @Test
    void storyLength_shortMapsToShortDescription() throws Exception {
        AppConfig config = new AppConfig("m", 0.3, "m", 0.1, "m", 0.8, "short");
        assertEquals("2 to 3 paragraphs", config.getStoryLength());
        assertEquals("short", config.getStoryLengthRaw());
    }

    @Test
    void storyLength_longMapsToLongDescription() throws Exception {
        AppConfig config = new AppConfig("m", 0.3, "m", 0.1, "m", 0.8, "long");
        assertEquals("8 to 10 paragraphs", config.getStoryLength());
    }

    @Test
    void storyLength_mediumMapsToMediumDescription() throws Exception {
        AppConfig config = new AppConfig("m", 0.3, "m", 0.1, "m", 0.8, "medium");
        assertEquals("4 to 6 paragraphs", config.getStoryLength());
    }

    @Test
    void storyLength_unknownValueDefaultsToMedium() throws Exception {
        AppConfig config = new AppConfig("m", 0.3, "m", 0.1, "m", 0.8, "epic");
        assertEquals("4 to 6 paragraphs", config.getStoryLength());
    }

    @Test
    void storyLength_caseInsensitive() throws Exception {
        AppConfig config = new AppConfig("m", 0.3, "m", 0.1, "m", 0.8, "SHORT");
        assertEquals("2 to 3 paragraphs", config.getStoryLength());
    }

    @Test
    void perRequestConstructor_overridesModelsAndTemperatures() throws Exception {
        AppConfig config = new AppConfig(
                "claude-opus-4-6", 0.5,
                "claude-haiku-4-5-20251001", 0.2,
                "claude-sonnet-4-6", 0.9,
                "long"
        );
        assertEquals("claude-opus-4-6",           config.getPlannerModel());
        assertEquals(0.5,                          config.getPlannerTemperature(), 0.001);
        assertEquals("claude-haiku-4-5-20251001",  config.getCriticModel());
        assertEquals(0.2,                          config.getCriticTemperature(), 0.001);
        assertEquals("claude-sonnet-4-6",          config.getWriterModel());
        assertEquals(0.9,                          config.getWriterTemperature(), 0.001);
        assertEquals("8 to 10 paragraphs",         config.getStoryLength());
    }
}
