package com.lockerline;

import java.time.Instant;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** A movable clock in place of the app's own, for tests that import it (the rule table and its subclasses). */
@TestConfiguration
public class MovableClockConfig {

    @Bean
    @Primary
    MovableClock movableClock() {
        return new MovableClock(Instant.parse("2026-09-25T13:10:00Z"));
    }
}
