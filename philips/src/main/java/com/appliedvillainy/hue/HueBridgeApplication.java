package com.appliedvillainy.hue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Spring Boot application for controlling Zigbee bulbs via Philips Hue Bridge.
 * 
 * This application provides REST endpoints to:
 * - Discover and list connected lights
 * - Control individual lights (on/off, brightness, color)
 * - Control rooms/groups of lights
 * - Run light animations
 * 
 * API Documentation available at: http://localhost:8080/swagger-ui.html
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class HueBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(HueBridgeApplication.class, args);
    }
}
