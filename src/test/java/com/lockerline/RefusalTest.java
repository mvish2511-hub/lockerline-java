package com.lockerline;

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

/** The three refusals, each with its own status and type, on the whole app with a clock the test moves. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:refusal-test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RefusalTest {

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

    ResultActions send(String path, String body) throws Exception {
        return http.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    String drop(String size) throws Exception {
        String body = send("/drops", "{\"sizeClass\":\"" + size + "\",\"flat\":\"302\"}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return new ObjectMapper().readTree(body).path("code").asText();
    }

    @Test
    void aParcelTooBigForAnyFreeDoorIs409NoDoorFree() throws Exception {
        for (int i = 0; i < 8; i++) {
            drop("L");
        }
        send("/drops", "{\"sizeClass\":\"L\",\"flat\":\"302\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/errors/no-door-free"));
    }

    @Test
    void aCodeThatNeverExistedIs404NoSuchCode() throws Exception {
        String real = drop("M");
        String wrong = real.equals("000000") ? "000001" : "000000";
        send("/pickups", "{\"code\":\"" + wrong + "\"}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/errors/no-such-code"));
    }

    @Test
    void aCodePastItsWindowIs404CodeExpired() throws Exception {
        String code = drop("M");
        ((MovableClock) clock).advance(Duration.ofHours(49));
        send("/pickups", "{\"code\":\"" + code + "\"}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/errors/code-expired"));
    }
}