package com.lockerline;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** The wall, read over HTTP through MockMvc on the whole app (an in-memory database for this class). */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:wall-test")
@AutoConfigureMockMvc
class WallTest {

    @Autowired
    MockMvc http;

    @Test
    void theWallShows48FreeDoors() throws Exception {
        http.perform(get("/towers/A/doors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tower").value("A"))
                .andExpect(jsonPath("$.free").value(48))
                .andExpect(jsonPath("$.doors.length()").value(48))
                .andExpect(jsonPath("$.doors[?(@.state != 'free')]").isEmpty());
    }

    @Test
    void filteringToLargeShowsEightAndCountsThem() throws Exception {
        http.perform(get("/towers/A/doors").param("size", "L"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doors.length()").value(8))
                .andExpect(jsonPath("$.free").value(8))
                .andExpect(jsonPath("$.doors[?(@.size != 'L')]").isEmpty());
    }

    @Test
    void anUnknownSizeIs400Validation() throws Exception {
        http.perform(get("/towers/A/doors").param("size", "XL"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/errors/validation"));
    }

    @Test
    void oneFreeDoorHasNoParcelFields() throws Exception {
        http.perform(get("/towers/A/doors/D01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value("S"))
                .andExpect(jsonPath("$.state").value("free"))
                .andExpect(jsonPath("$.parcelId").doesNotExist());
    }

    @Test
    void anUnknownDoorIs404NoSuchDoor() throws Exception {
        http.perform(get("/towers/A/doors/D99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/errors/no-such-door"));
    }

    @Test
    void anUnknownTowerIs404NoSuchTower() throws Exception {
        http.perform(get("/towers/Z/doors"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/errors/no-such-tower"));
    }
}
