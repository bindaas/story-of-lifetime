package com.rajivnarula.storyoflifetime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FactGenerateRequest — default values and field accessors.
 */
class FactGenerateRequestTest {

    @Test
    void testDefaultFactCountIsFive() {
        FactGenerateRequest req = new FactGenerateRequest();
        assertEquals(5, req.getFactCount());
    }

    @Test
    void testSettersAndGetters() {
        FactGenerateRequest req = new FactGenerateRequest();
        req.setStartState("Born in Texas.");
        req.setEndState("Became Health Minister.");
        req.setCreativity("medium");
        req.setContradiction("low");
        req.setWorldType("realistic");
        req.setFactCount(7);

        assertEquals("Born in Texas.",        req.getStartState());
        assertEquals("Became Health Minister.", req.getEndState());
        assertEquals("medium",                req.getCreativity());
        assertEquals("low",                   req.getContradiction());
        assertEquals("realistic",             req.getWorldType());
        assertEquals(7,                       req.getFactCount());
    }

    @Test
    void testZeroFactCount() {
        FactGenerateRequest req = new FactGenerateRequest();
        req.setFactCount(0);
        assertEquals(0, req.getFactCount());
    }

    @Test
    void testMaxFactCount() {
        FactGenerateRequest req = new FactGenerateRequest();
        req.setFactCount(10);
        assertEquals(10, req.getFactCount());
    }
}
