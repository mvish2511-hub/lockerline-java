package com.lockerline.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * The app's own settings, read once at start-up from application.yml. Spring's own settings - the database
 * URL, the host and the port - stay under spring.datasource and server.
 *
 * @param freeWindowHours how long a pickup code stays valid after the drop
 * @param seedDir         the folder that holds doors.csv and residents.csv
 */
@ConfigurationProperties
public record LockerLineSettings(@DefaultValue("48") int freeWindowHours, @DefaultValue("seed") String seedDir) {
}
