package com.lockerline;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

/** A valid code opens its door once, frees it and stamps the dwell; a second try is refused.*/
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:pickup-test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PickupTest {

@TestConfiguration
    static class Clocks {
        @Bean
        @Primary
        MovableClock movableClock() {
            return new MovableClock(Instant.parse("2026-09-25T13:10:00Z"));
        }
    }

    @Autowired
    MockMvc http;

    @Autowired
    Clock clock;

    ResultActions pickup(String code) throws Exception {
        return http.perform(post("/pickups")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\"}"));
    }
    String drop() throws Exception {
        String body = http.perform(post("/drops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sizeClass\":\"M\",\"flat\":\"302\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return new ObjectMapper().readTree(body).path("code").asText();
    }

    @Test
    void aValidCodeOpensTheDoorAndStampsHowLongItWaited() throws Exception {
        String code = drop();

        ((MovableClock) clock).advance(Duration.ofMinutes(90));

        pickup(code)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.door").value("D21"))
                .andExpect(jsonPath("$.dwellSeconds").value(5400))
                .andExpect(jsonPath("$.openedAt").value("2026-09-25T14:40:00Z"));

        http.perform(get("/towers/A/doors/D21"))
                .andExpect(jsonPath("$.state").value("free"));

        http.perform(get("/parcels/P001"))
                .andExpect(jsonPath("$.state").value("collected"));
    }

    @Test
    void theSameCodeTwiceIsRefusedAsUsed() throws Exception {
        String code = drop();

        pickup(code).andExpect(status().isOk());

        pickup(code)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/errors/code-used"));
    }

}