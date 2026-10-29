package com.lockerline;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Booking a drop, over HTTP through MockMvc on the whole app. Every test gets a fresh in-memory wall. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:drop-test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DropTest {

    @Autowired
    MockMvc http;

    ResultActions drop(String body) throws Exception {
        return http.perform(post("/drops").contentType(MediaType.APPLICATION_JSON).content(body));
    }

@Test
    void aMediumParcelGetsTheFirstFreeMediumDoor() throws Exception {
        drop("{\"sizeClass\":\"M\",\"flat\":\"302\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.door").value("D21"))
                .andExpect(header().string("Location", "/parcels/P001"));
        http.perform(get("/towers/A/doors/D21"))
                .andExpect(jsonPath("$.state").value("holding"))
                .andExpect(jsonPath("$.parcelId").value("P001"));
    }

    @Test
    void anUnknownFlatIsRefusedAndNothingChanges() throws Exception {
        drop("{\"sizeClass\":\"M\",\"flat\":\"999\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/errors/no-such-flat"));
        http.perform(get("/towers/A/doors"))                .andExpect(jsonPath("$.free").value(48));
    }

     @Test
    void aSizeThatDoesNotExistIsAValidationError() throws Exception {
        drop("{\"sizeClass\":\"XL\",\"flat\":\"302\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/errors/validation"))
                .andExpect(jsonPath("$.errors[0].field").value("sizeClass"));
    }

    @Test
    void aMissingFlatIsAValidationError() throws Exception {
        drop("{\"sizeClass\":\"M\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("flat"));
    }

    @Test
    void whenEveryLargeDoorIsTakenTheFreeSizesAreListed() throws Exception {
        for (int i = 0; i < 8; i++) {
            drop("{\"sizeClass\":\"L\",\"flat\":\"302\"}").andExpect(status().isCreated());
        }
        drop("{\"sizeClass\":\"L\",\"flat\":\"302\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("/errors/no-door-free"))
                .andExpect(jsonPath("$.freeSizes.length()").value(2));
    }
}
