package com.lockerline;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** The society halves the free window. The same table and rows, with the 24-hour column expected. */
@TestPropertySource(properties = "free-window-hours=24")
class RuleTableAt24Test extends RuleTable {

    @Override
    int window() {
        return 24;
    }

    @Test
    void settingsShowTheLiveWindow() throws Exception {
        http.perform(get("/settings")).andExpect(jsonPath("$.freeWindowHours").value(24));
    }
}
