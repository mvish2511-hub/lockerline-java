package com.lockerline;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Moving the window from 48 to 24 changes the outcome of the window rows and of no other row. */
class RuleTableShapeTest {

    @Test
    void onlyRowsTaggedWindowChangeBetween48And24() throws IOException {
        List<String[]> rows;
        try (InputStream in = RuleTableShapeTest.class.getResourceAsStream("/rules.csv")) {
            rows = new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().skip(1)
                    .filter(l -> !l.isBlank()).map(l -> l.split(",", -1)).toList();
        }
        List<String> moved = rows.stream()
                .filter(r -> !(r[4] + r[5]).equals(r[6] + r[7]))
                .map(r -> r[0] + " [" + r[1] + "]").toList();
        assertThat(moved).isNotEmpty().allMatch(m -> m.endsWith("[window]"));
    }
}
