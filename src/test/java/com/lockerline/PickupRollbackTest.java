package com.lockerline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.lockerline.door.DoorRepository;

/**
 * The close is one transaction. Freeing the door - the write after the parcel update - is made to
 * fail; the pickup answers 500 and the parcel is still holding, its door still held.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:pickup-rollback-test")
class PickupRollbackTest {

    @Autowired
    TestRestTemplate http;

    @MockitoSpyBean
    DoorRepository doors;

    @Test
    void aPickupThatFailsPartWayChangesNothing() {
        Map<?, ?> drop = http.postForObject("/drops", Map.of("sizeClass", "M", "flat", "302"), Map.class);
        doThrow(new IllegalStateException("injected: the door cannot be freed")).when(doors).free(anyLong());

        int status = http.postForEntity("/pickups", Map.of("code", drop.get("code")), String.class)
                .getStatusCode().value();
        assertThat(status).isEqualTo(500);

        assertThat(http.getForObject("/parcels/" + drop.get("parcelId"), Map.class).get("state")).isEqualTo("holding");
        assertThat(http.getForObject("/towers/A/doors/" + drop.get("door"), Map.class).get("state"))
                .isEqualTo("holding");
    }
}