package com.lockerline;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;

/** The rule table with the free window at its default, 48 hours. */
class RuleTableAt48Test extends RuleTable {

    @Override
    int window() {
        return 48;
    }

    @Test
    void settingsShowTheLiveWindow() throws Exception {
        http.perform(get("/settings")).andExpect(jsonPath("$.freeWindowHours").value(48));
    }
}
