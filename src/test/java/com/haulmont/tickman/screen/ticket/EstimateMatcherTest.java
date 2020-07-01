package com.haulmont.tickman.screen.ticket;

import com.haulmont.tickman.entity.Ticket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EstimateMatcherTest {

    @Test
    void matches() {
        Ticket ticket = new Ticket();

        ticket.setEstimate(null);
        assertTrue(EstimateMatcher.matches(ticket, "0"));
        assertTrue(EstimateMatcher.matches(ticket, "5, 0"));
        assertTrue(EstimateMatcher.matches(ticket, "0, 5"));
        assertFalse(EstimateMatcher.matches(ticket, "5"));

        ticket.setEstimate(5);
        assertTrue(EstimateMatcher.matches(ticket, "5"));
        assertTrue(EstimateMatcher.matches(ticket, "5,8"));
        assertTrue(EstimateMatcher.matches(ticket, "8,5"));
        assertTrue(EstimateMatcher.matches(ticket, ">=5"));
        assertTrue(EstimateMatcher.matches(ticket, ">3"));
        assertTrue(EstimateMatcher.matches(ticket, "<=5"));
        assertTrue(EstimateMatcher.matches(ticket, "<8"));
        assertFalse(EstimateMatcher.matches(ticket, "3"));
        assertFalse(EstimateMatcher.matches(ticket, "a"));
    }
}