package com.lockerline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.Clock;
import java.time.Duration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The rule table. One row per case (src/test/resources/rules.csv): its setup, its action, and the status and
 * type expected with the free window at 48 hours and at 24. Every row runs on a fresh wall, on the whole app through
 * MockMvc, with a clock the row moves. The two subclasses run the same table at each window.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:rule-table")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(MovableClockConfig.class)
abstract class RuleTable {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    MockMvc http;

    @Autowired
    Clock clock;

    /** The free window this run uses: 48 or 24. */
    abstract int window();

    @ParameterizedTest(name = "{0} [{1}]")
    @CsvFileSource(resources = "/rules.csv", numLinesToSkip = 1)
    void rule(String name, String tag, String setup, String action,int status48, String type48, int status24,
              String type24) throws Exception {
        String code = null;
        for (String step : setup == null ? new String[0] : setup.split(";")) {
            String[] w = step.trim().split(" ");
            switch (w[0]) {
                case "fill" -> fill(w[1]);
                case "drop" -> code = send("/drops", "{\"sizeClass\":\"M\",\"flat\":\"302\"}").path("code").asText();
                case "pickup" -> send("/pickups", "{\"code\":\"" + code + "\"}");
                case "advance" -> ((MovableClock) clock).advance(Duration.ofHours(Integer.parseInt(w[1])));
                default -> throw new IllegalArgumentException("unknown step " + step);
            }
        }
   String[] a = action.split(" ");
        MockHttpServletResponse answer = "drop".equals(a[0])
                ? perform("/drops", "{\"sizeClass\":\"" + a[1] + "\",\"flat\":\"" + a[2] + "\"}")
                : perform("/pickups", "{\"code\":\"" + (a.length > 1 ? a[1] : code) + "\"}");

        int status = window() == 48 ? status48 : status24;
        String type = window() == 48 ? type48 : type24;
        assertThat(answer.getStatus()).as(name + " status").isEqualTo(status);
        if (type != null) {
            assertThat(JSON.readTree(answer.getContentAsString()).path("type").asText()).as(name + " type")
                    .isEqualTo(type);
        }
    }

    private void fill(String size) throws Exception {
        while (perform("/drops", "{\"sizeClass\":\"" + size + "\",\"flat\":\"302\"}").getStatus() == 201) {
            // keep dropping until no door of this size is free
        }
    }

    private JsonNode send(String path, String body) throws Exception {
        return JSON.readTree(perform(path, body).getContentAsString());
    }

    private MockHttpServletResponse perform(String path, String body) throws Exception {
        return http.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn()
                .getResponse();
    }
}