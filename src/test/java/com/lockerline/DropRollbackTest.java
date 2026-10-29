package com.lockerline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.lockerline.door.DoorRepository;

/**
 * The drop is one transaction. Holding the door - the write after the parcel insert - is made to fail;
 * the drop answers 500 and leaves no parcel and no held door behind.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:drop-rollback-test")
class DropRollbackTest {

@Autowired
    TestRestTemplate http;

    @MockitoSpyBean
    DoorRepository doors;

    @Test
    void aDropThatFailsPartWayLeavesNothing() {
        doThrow(new IllegalStateException("injected: the door cannot be held")).when(doors).hold(anyLong(), anyLong());

        ResponseEntity<String> drop = http.postForEntity(           "/drops", Map.of("sizeClass", "M", "flat", "302"),
                String.class);
        assertThat(drop.getStatusCode().value()).isEqualTo(500);

        assertThat(http.getForEntity("/parcels/P001", String.class)                .getStatusCode().value()).isEqualTo(404);
        assertThat(http.getForObject("/towers/A/doors", Map.class).get("free")).isEqualTo(48);
    }
}
