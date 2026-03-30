package com.rajivnarula.storyoflifetime.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WorldModel — direct constructor, getters, and edge cases.
 */
class WorldModelTest {

    @Test
    void directConstructor_storesAllFields() {
        List<String> facts = List.of("Fact one.", "Fact two.");
        WorldModel wm = new WorldModel("Start state.", "End state.", facts, "realistic");

        assertEquals("Start state.", wm.getStartState());
        assertEquals("End state.",   wm.getEndState());
        assertEquals(facts,          wm.getFacts());
        assertEquals("realistic",    wm.getWorldType());
    }

    @Test
    void getFactCount_reflectsFactListSize() {
        WorldModel wm = new WorldModel("S", "E", List.of("a", "b", "c"), "grounded");
        assertEquals(3, wm.getFactCount());
    }

    @Test
    void nullWorldType_defaultsToRealistic() {
        WorldModel wm = new WorldModel("S", "E", List.of(), null);
        assertEquals("realistic", wm.getWorldType());
    }

    @Test
    void emptyFactList_givesZeroCount() {
        WorldModel wm = new WorldModel("S", "E", List.of(), "fantastical");
        assertEquals(0, wm.getFactCount());
        assertTrue(wm.getFacts().isEmpty());
    }

    @Test
    void allWorldTypes_accepted() {
        for (String wt : new String[]{"grounded", "realistic", "fantastical", "outlandish"}) {
            WorldModel wm = new WorldModel("S", "E", List.of(), wt);
            assertEquals(wt, wm.getWorldType());
        }
    }
}
