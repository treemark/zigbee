package com.appliedvillainy.hue.service;

import com.appliedvillainy.hue.model.LightCommand;
import com.appliedvillainy.hue.model.LightDto;
import io.github.zeroone3010.yahueapi.Color;
import io.github.zeroone3010.yahueapi.v2.Hue;
import io.github.zeroone3010.yahueapi.v2.Light;
import io.github.zeroone3010.yahueapi.v2.UpdateState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing Hue lights/Zigbee bulbs via the Philips Hue Bridge.
 */
@Service
public class HueLightService {

    private static final Logger logger = LoggerFactory.getLogger(HueLightService.class);
    
    private final Hue hue;

    public HueLightService(Hue hue) {
        this.hue = hue;
    }

    /**
     * Get all lights connected to the Hue Bridge.
     */
    public List<LightDto> getAllLights() {
        logger.info("Fetching all lights from Hue Bridge");
        Map<UUID, Light> lights = hue.getLights();
        
        return lights.entrySet().stream()
                .map(entry -> mapToDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Get a specific light by its ID.
     */
    public Optional<LightDto> getLightById(UUID id) {
        logger.info("Fetching light with ID: {}", id);
        Map<UUID, Light> lights = hue.getLights();
        Light light = lights.get(id);
        
        if (light == null) {
            return Optional.empty();
        }
        return Optional.of(mapToDto(id, light));
    }

    /**
     * Get a light by its name.
     */
    public Optional<LightDto> getLightByName(String name) {
        logger.info("Fetching light with name: {}", name);
        Map<UUID, Light> lights = hue.getLights();
        
        return lights.entrySet().stream()
                .filter(entry -> entry.getValue().getName().equalsIgnoreCase(name))
                .map(entry -> mapToDto(entry.getKey(), entry.getValue()))
                .findFirst();
    }

    /**
     * Turn on a light.
     */
    public boolean turnOn(UUID id) {
        logger.info("Turning on light: {}", id);
        Light light = hue.getLights().get(id);
        if (light != null) {
            light.turnOn();
            return true;
        }
        return false;
    }

    /**
     * Turn off a light.
     */
    public boolean turnOff(UUID id) {
        logger.info("Turning off light: {}", id);
        Light light = hue.getLights().get(id);
        if (light != null) {
            light.turnOff();
            return true;
        }
        return false;
    }

    /**
     * Apply a command to a specific light.
     */
    public boolean applyCommand(UUID id, LightCommand command) {
        logger.info("Applying command to light: {} - {}", id, command);
        Light light = hue.getLights().get(id);
        
        if (light == null) {
            logger.warn("Light not found: {}", id);
            return false;
        }

        try {
            // Build an UpdateState object with all the requested changes
            UpdateState state = new UpdateState();
            
            // Handle on/off state
            if (command.getOn() != null) {
                if (command.getOn()) {
                    state.on();
                } else {
                    state.off();
                }
            }

            // Handle brightness
            if (command.getBrightness() != null) {
                state.brightness(command.getBrightness().intValue());
            }

            // Handle hue and saturation together
            if (command.getHue() != null && command.getSaturation() != null) {
                // Convert hue (0-65535) and saturation (0-254) to RGB color
                // Hue API uses hue/saturation, so we need to create a Color from it
                float hueNormalized = command.getHue() / 65535.0f; // 0-1
                float satNormalized = command.getSaturation() / 254.0f; // 0-1
                float brightness = 1.0f; // Full brightness for color calculation
                
                // Convert HSB to RGB
                int rgb = java.awt.Color.HSBtoRGB(hueNormalized, satNormalized, brightness);
                state.color(Color.of(rgb));
            } else if (command.getHue() != null) {
                // Just hue, assume full saturation
                float hueNormalized = command.getHue() / 65535.0f;
                int rgb = java.awt.Color.HSBtoRGB(hueNormalized, 1.0f, 1.0f);
                state.color(Color.of(rgb));
            } else if (command.getSaturation() != null) {
                // Just saturation - not very useful alone, skip
                logger.debug("Saturation without hue - skipping color change");
            }

            // Note: Color temperature would need different handling in yahueapi
            // For now, we skip it as it's not used in our color rotation test

            // Apply the state to the light
            light.setState(state);

            return true;
        } catch (Exception e) {
            logger.error("Failed to apply command to light {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Turn on all lights.
     */
    public void turnOnAllLights() {
        logger.info("Turning on all lights");
        hue.getLights().values().forEach(Light::turnOn);
    }

    /**
     * Turn off all lights.
     */
    public void turnOffAllLights() {
        logger.info("Turning off all lights");
        hue.getLights().values().forEach(Light::turnOff);
    }

    /**
     * Set brightness for all lights.
     */
    public void setBrightnessForAll(int brightness) {
        logger.info("Setting brightness {} for all lights", brightness);
        hue.getLights().values().forEach(light -> light.setBrightness(brightness));
    }

    /**
     * Map a Light object to a LightDto.
     */
    private LightDto mapToDto(UUID id, Light light) {
        LightDto dto = new LightDto();
        dto.setId(id);
        dto.setName(light.getName());
        dto.setOn(light.isOn());
        
        // Get additional properties if available
        try {
            // Note: Some properties may not be available depending on bulb type
            // and yahueapi version
        } catch (Exception e) {
            logger.debug("Could not fetch all properties for light {}: {}", id, e.getMessage());
        }
        
        return dto;
    }
}
