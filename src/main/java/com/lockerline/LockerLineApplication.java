package com.lockerline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** LockerLine: a bank of 48 parcel lockers for one apartment tower. The grader starts the app through this class. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class LockerLineApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockerLineApplication.class, args);
    }
}
