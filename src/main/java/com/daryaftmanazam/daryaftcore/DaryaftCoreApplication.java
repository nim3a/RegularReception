package com.daryaftmanazam.daryaftcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Daryaft Core.
 * Enables scheduling for automated tasks.
 */
@SpringBootApplication
@EnableScheduling
public class DaryaftCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(DaryaftCoreApplication.class, args);
    }
}
