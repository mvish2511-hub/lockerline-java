package com.lockerline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lockerline.parcel.PickupCodes;

/**
 * The pickup code, with the clock fixed at a known moment so every time is exact.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:pickup-code-test")
@AutoConfigureMockMvc
class PickupCodeTest {

    static final Instant T = Instant.parse("2026-09-25T13:10:00Z");

    @TestConfiguration
    static class FixedClock {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(T, ZoneOffset.UTC);
        }
    }

    @Autowired
    MockMvc http;

    @Autowired
    JdbcClient jdbc;

    @Autowired
    PickupCodes codes;

    @Test
    void aDropGetsASixDigitCodeThatExpiresTheWindowAfterTheDrop() throws Exception {
        String body = http.perform(post("/drops").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sizeClass\":\"S\",\"flat\":\"101\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.droppedAt").value("2026-09-25T13:10:00Z"))
                .andExpect(jsonPath("$.expiresAt").value("2026-09-27T13:10:00Z"))
                .andReturn().getResponse().getContentAsString();

        JsonNode drop = new ObjectMapper().readTree(body);
        String code = drop.path("code").asText();

        assertThat(code).matches("\\d{6}");

        String stored = jdbc.sql(
                        "SELECT code_hash FROM parcels WHERE door_id = "
                                + "(SELECT id FROM doors WHERE no = ?)")
                .param(drop.path("door").asText())
                .query(String.class)
                .single();

        assertThat(stored).isNotEqualTo(code).doesNotContain(code);
    }

    @Test
    void aCodeMatchesItsOwnHashAndAWrongCodeDoesNot() {
        String hash = codes.hash("482913");

        assertThat(codes.matches("482913", hash)).isTrue();
        assertThat(codes.matches("482914", hash)).isFalse();
    }
}
