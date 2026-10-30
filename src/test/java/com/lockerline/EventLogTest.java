package com.lockerline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lockerline.parcel.EventLog;

/** Every transition is logged in order, and the log can only be added to and read.*/
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:event-log-test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EventLogTest {

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

    @Test
    void aParcelsHistoryIsDroppedCodeIssuedOpenedWithTheirTimes() throws Exception {
        String body = http.perform(post("/drops").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sizeClass\":\"M\",\"flat\":\"302\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode drop = new ObjectMapper().readTree(body);
        ((MovableClock) clock).advance(Duration.ofHours(44));
        http.perform(post("/pickups").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + drop.path("code").asText() + "\"}"))
                .andExpect(status().isOk());

        http.perform(get("/parcels/" + drop.path("parcelId").asText()))
                .andExpect(jsonPath("$.events.length()").value(3))
                .andExpect(jsonPath("$.events[2].kind").value("opened"))
                .andExpect(jsonPath("$.events[2].at").value("2026-09-27T09:10:00Z"))
                .andExpect(jsonPath("$.events[2].dwellSeconds").value(158400))
                .andExpect(jsonPath("$.events[?(@.kind == 'dropped')].door").value("D21"));
    }

    @Test
    void theLogCanOnlyAppendAndRead() {
        String[] publicMethods = Arrays.stream(EventLog.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()) && !m.isSynthetic())
                .map(Method::getName).sorted().toArray(String[]::new);
        assertThat(publicMethods).containsExactly("append", "forParcel");
    }
}