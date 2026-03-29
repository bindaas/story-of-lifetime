package com.rajivnarula.storyoflifetime;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WorldModel — direct constructor, getters, edge cases.
 */
class WorldModelTest {

    private static final String START = "John was born in Texas in 1975.";
    private static final String END   = "John became Health Minister of India.";
    private static final List<String> FACTS = Arrays.asList(
            "John moved to New York at age 18.",
            "John earned a medical degree at 26.",
            "John relocated to India at 35."
    );

    @Test
    void testDirectConstructorAndGetters() {
        WorldModel model = new WorldModel(START, END, FACTS, "realistic");
        assertEquals(START,       model.getStartState());
        assertEquals(END,         model.getEndState());
        assertEquals(FACTS,       model.getFacts());
        assertEquals("realistic", model.getWorldType());
    }

    @Test
    void testFactCount() {
        WorldModel model = new WorldModel(START, END, FACTS, "grounded");
        assertEquals(3, model.getFactCount());
    }

    @Test
    void testZeroFacts() {
        WorldModel model = new WorldModel(START, END, Collections.emptyList(), "grounded");
        assertEquals(0, model.getFactCount());
        assertTrue(model.getFacts().isEmpty());
    }

    @Test
    void testNullWorldTypeDefaultsToRealistic() {
        WorldModel model = new WorldModel(START, END, FACTS, null);
        assertEquals("realistic", model.getWorldType());
    }

    @Test
    void testAllWorldTypes() {
        for (String wt : new String[]{"grounded", "realistic", "fantastical", "outlandish"}) {
            WorldModel model = new WorldModel(START, END, FACTS, wt);
            assertEquals(wt, model.getWorldType());
        }
    }

    @Test
    void testSingleFact() {
        WorldModel model = new WorldModel(START, END, List.of("One fact."), "fantastical");
        assertEquals(1, model.getFactCount());
        assertEquals("One fact.", model.getFacts().get(0));
    }
}
