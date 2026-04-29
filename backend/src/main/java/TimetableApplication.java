package com.timetable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Smart Timetable System — No Database Edition
 *
 * Run: mvn spring-boot:run
 * API: http://localhost:8080/api/generate  (POST)
 */
@SpringBootApplication
public class TimetableApplication {
    public static void main(String[] args) {
        SpringApplication.run(TimetableApplication.class, args);
    }
}
