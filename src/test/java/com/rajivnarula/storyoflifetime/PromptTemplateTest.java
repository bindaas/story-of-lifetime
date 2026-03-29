package com.rajivnarula.storyoflifetime;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for prompt template files.
 * Verifies that every required placeholder exists in each prompt —
 * catches accidental deletion during edits.
 */
class PromptTemplateTest {

    private String load(String resourcePath) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull(is, "Prompt file not found: " + resourcePath);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // ── Fact Generator ─────────────────────────────────────────────────────────

    @Test
    void factGeneratorPrompt_exists() throws IOException {
        assertNotNull(load("prompts/factgenerator_prompt.txt"));
    }

    @Test
    void factGeneratorPrompt_containsRequiredPlaceholders() throws IOException {
        String prompt = load("prompts/factgenerator_prompt.txt");
        assertContains(prompt, "{{START_STATE}}");
        assertContains(prompt, "{{END_STATE}}");
        assertContains(prompt, "{{CREATIVITY}}");
        assertContains(prompt, "{{CONTRADICTION}}");
        assertContains(prompt, "{{WORLD_TYPE}}");
        assertContains(prompt, "{{FACT_COUNT}}");
    }

    // ── Planner ────────────────────────────────────────────────────────────────

    @Test
    void plannerPrompt_exists() throws IOException {
        assertNotNull(load("prompts/planner_prompt.txt"));
    }

    @Test
    void plannerPrompt_containsRequiredPlaceholders() throws IOException {
        String prompt = load("prompts/planner_prompt.txt");
        assertContains(prompt, "{{START_STATE}}");
        assertContains(prompt, "{{END_STATE}}");
        assertContains(prompt, "{{FACTS}}");
        assertContains(prompt, "{{FEEDBACK_SECTION}}");
    }

    // ── Critic ─────────────────────────────────────────────────────────────────

    @Test
    void criticPrompt_exists() throws IOException {
        assertNotNull(load("prompts/critic_prompt.txt"));
    }

    @Test
    void criticPrompt_containsRequiredPlaceholders() throws IOException {
        String prompt = load("prompts/critic_prompt.txt");
        assertContains(prompt, "{{START_STATE}}");
        assertContains(prompt, "{{END_STATE}}");
        assertContains(prompt, "{{FACTS}}");
        assertContains(prompt, "{{OUTLINE}}");
        assertContains(prompt, "{{FEEDBACK_SECTION}}");
    }

    // ── Writer ─────────────────────────────────────────────────────────────────

    @Test
    void writerPrompt_exists() throws IOException {
        assertNotNull(load("prompts/writer_prompt.txt"));
    }

    @Test
    void writerPrompt_containsRequiredPlaceholders() throws IOException {
        String prompt = load("prompts/writer_prompt.txt");
        assertContains(prompt, "{{START_STATE}}");
        assertContains(prompt, "{{END_STATE}}");
        assertContains(prompt, "{{FACTS}}");
        assertContains(prompt, "{{OUTLINE}}");
        assertContains(prompt, "{{WORLD_TYPE}}");
        assertContains(prompt, "{{STORY_LENGTH}}");
    }

    // ── Explainer ──────────────────────────────────────────────────────────────

    @Test
    void explainerPrompt_exists() throws IOException {
        assertNotNull(load("prompts/explainer_prompt.txt"));
    }

    @Test
    void explainerPrompt_containsRequiredPlaceholders() throws IOException {
        String prompt = load("prompts/explainer_prompt.txt");
        assertContains(prompt, "{{START_STATE}}");
        assertContains(prompt, "{{END_STATE}}");
        assertContains(prompt, "{{ORIGINAL_FACTS}}");
        assertContains(prompt, "{{ADDITIONAL_FACTS}}");
        assertContains(prompt, "{{STORY_V1}}");
        assertContains(prompt, "{{STORY_V2}}");
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private void assertContains(String text, String placeholder) {
        assertTrue(text.contains(placeholder),
                "Prompt is missing required placeholder: " + placeholder);
    }
}
