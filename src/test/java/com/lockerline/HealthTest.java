package com.lockerline;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** The health path a container is checked on answers UP, from the same app that serves the wall. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:health-test")
@AutoConfigureMockMvc
class HealthTest {

    @Autowired
    MockMvc http;

    @Test
    void healthIsUpAlongsideTheWall() throws Exception {
        http.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        http.perform(get("/towers/A/doors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doors.length()").value(48));
    }
}
