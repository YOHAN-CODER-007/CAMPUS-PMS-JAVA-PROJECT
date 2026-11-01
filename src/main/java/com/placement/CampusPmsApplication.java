package com.placement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Campus Placement Management System (Campus-PMS).
 * A Spring Boot 3 web application built for college campus placements.
 */
@SpringBootApplication
@EnableScheduling
public class CampusPmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusPmsApplication.class, args);
        System.out.println("==================================================");
        System.out.println("  Campus Placement Management System is Running!  ");
        System.out.println("  Access URL: http://localhost:8080               ");
        System.out.println("==================================================");
    }
}
