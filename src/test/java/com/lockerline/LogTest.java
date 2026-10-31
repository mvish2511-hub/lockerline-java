package com.lockerline;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;

/** Every request and every event leaves one line on stdout, and no line ever holds a pickup code. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:log-test")
@ExtendWith(OutputCaptureExtension.class)
class LogTest {

    @Autowired
    TestRestTemplate http;

    @Test
    void aDropAndAPickupAreLoggedWithoutTheCode(CapturedOutput output) {
        Map<?, ?> drop = http.postForObject("/drops", Map.of("sizeClass", "M", "flat", "302"), Map.class);
        String code = String.valueOf(drop.get("code"));
        assertThat(http.postForEntity("/pickups", Map.of("code", code), String.class).getStatusCode().value())
                .isEqualTo(200);

        String out = output.getOut();
        assertThat(out.lines().filter(l -> l.contains("method=POST") && l.contains("path=/drops"))).hasSize(1);
        assertThat(out.lines().filter(l -> l.contains("method=POST") && l.contains("path=/pickups"))).hasSize(1);
        assertThat(out).contains("kind=dropped", "kind=code_issued", "kind=opened");
        assertThat(output.getAll()).doesNotContain(code);
    }
}