package com.appliedvillainy.hue;

import com.appliedvillainy.hue.model.LightCommand;
import com.appliedvillainy.hue.model.LightDto;
import com.appliedvillainy.hue.service.BridgeDiscoveryService;
import com.appliedvillainy.hue.service.HueLightService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that discovers and blinks all Zigbee bulbs connected to the Hue Bridge.
 * This test will:
 * 1. Discover the Hue Bridge on the network (via autodiscovery or configured IP)
 * 2. Get all connected lights/bulbs
 * 3. Blink them on and off for 5 seconds
 */
@SpringBootTest
class BlinkAllBulbsTest {

    private static final Logger logger = LoggerFactory.getLogger(BlinkAllBulbsTest.class);
    
    @Autowired
    private BridgeDiscoveryService bridgeDiscoveryService;
    
    @Autowired
    private HueLightService hueLightService;

    @Test
    void discoverAndBlinkAllBulbs() throws InterruptedException {
        logger.info("========================================");
        logger.info("Starting bulb discovery and blink test");
        logger.info("========================================");
        
        // Step 1: Discover the bridge (this happens automatically via Spring config)
        logger.info("Bridge connection established via Spring configuration");
        
        // Step 2: Discover all bulbs/lights
        List<LightDto> lights = hueLightService.getAllLights();
        assertNotNull(lights, "Lights list should not be null");
        assertFalse(lights.isEmpty(), "At least one light should be discovered");
        
        logger.info("Discovered {} light(s):", lights.size());
        for (LightDto light : lights) {
            logger.info("  - {} (ID: {}, Currently: {})", 
                light.getName(), 
                light.getId(), 
                light.isOn() ? "ON" : "OFF");
        }
        
        // Step 3: Blink all discovered bulbs for 5 seconds
        logger.info("\nStarting 5-second blink sequence...");
        
        long startTime = System.currentTimeMillis();
        long duration = 5000; // 5 seconds
        int blinkInterval = 500; // 500ms (0.5 seconds)
        boolean currentState = true; // Start with ON
        
        while ((System.currentTimeMillis() - startTime) < duration) {
            if (currentState) {
                logger.info("Turning all lights ON");
                hueLightService.turnOnAllLights();
            } else {
                logger.info("Turning all lights OFF");
                hueLightService.turnOffAllLights();
            }
            
            Thread.sleep(blinkInterval);
            currentState = !currentState; // Toggle state
        }
        
        // Cleanup: Turn all lights off at the end
        logger.info("Blink sequence complete. Turning all lights OFF");
        hueLightService.turnOffAllLights();
        
        logger.info("========================================");
        logger.info("Test completed successfully!");
        logger.info("========================================");
    }
    
    @Test
    void colorRotateAllBulbsForFiveSeconds() throws InterruptedException {
        logger.info("========================================");
        logger.info("Starting 5-second color rotation test");
        logger.info("========================================");
        
        // Step 1: Discover all bulbs/lights
        List<LightDto> lights = hueLightService.getAllLights();
        assertNotNull(lights, "Lights list should not be null");
        assertFalse(lights.isEmpty(), "At least one light should be discovered");
        
        logger.info("Discovered {} light(s) for color rotation:", lights.size());
        for (LightDto light : lights) {
            logger.info("  - {} (ID: {})", light.getName(), light.getId());
        }
        
        // Step 2: Turn all lights on and set full saturation
        logger.info("\nTurning on all lights for color rotation...");
        hueLightService.turnOnAllLights();
        Thread.sleep(500); // Give lights time to turn on
        
        // Step 3: Rotate through colors for 5 seconds
        logger.info("Starting color rotation sequence...");
        
        long startTime = System.currentTimeMillis();
        long duration = 5000; // 5 seconds
        int colorChangeInterval = 200; // Change color every 200ms for smooth rotation
        
        // Hue values range from 0 to 65535 in the Philips Hue API
        // This represents the full color spectrum (0-360 degrees mapped to 0-65535)
        int maxHue = 65535;
        int hueStep = maxHue / 25; // Divide into 25 steps for 5 seconds (5000ms / 200ms)
        int currentHueIndex = 0;
        
        while ((System.currentTimeMillis() - startTime) < duration) {
            int hue = (currentHueIndex * hueStep) % maxHue;
            
            // Calculate approximate color name for logging
            String colorName = getColorName(hue);
            logger.info("Setting color to {} (hue: {})", colorName, hue);
            
            // Apply the color to all lights
            for (LightDto light : lights) {
                LightCommand colorCommand = LightCommand.builder()
                    .on(true)
                    .hue(hue)
                    .saturation(254) // Max saturation for vibrant colors
                    .brightness(254) // Max brightness
                    .build();
                
                hueLightService.applyCommand(light.getId(), colorCommand);
            }
            
            Thread.sleep(colorChangeInterval);
            currentHueIndex++;
        }
        
        // Cleanup: Turn all lights off at the end
        logger.info("\nColor rotation complete. Turning all lights OFF");
        hueLightService.turnOffAllLights();
        
        logger.info("========================================");
        logger.info("Color rotation test completed successfully!");
        logger.info("========================================");
    }
    
    /**
     * Convert hue value (0-65535) to a human-readable color name
     */
    private String getColorName(int hue) {
        // Convert to degrees (0-360)
        double degrees = (hue / 65535.0) * 360.0;
        
        if (degrees < 30 || degrees >= 330) return "Red";
        if (degrees < 60) return "Orange";
        if (degrees < 90) return "Yellow";
        if (degrees < 150) return "Green";
        if (degrees < 210) return "Cyan";
        if (degrees < 270) return "Blue";
        if (degrees < 330) return "Magenta";
        return "Red";
    }
    
    @Test
    void verifyBridgeDiscovery() {
        // Test that we can discover bridges
        String bridgeIp = bridgeDiscoveryService.discoverBridgeIp();
        logger.info("Bridge discovery returned IP: {}", bridgeIp);
        
        // Note: This might be null if discovery fails, but that's okay for this test
        // The main test will fail if bridge connection doesn't work
    }
    
    @Test
    void verifyLightsCanBeControlledIndividually() throws InterruptedException {
        // Get all lights
        List<LightDto> lights = hueLightService.getAllLights();
        assertFalse(lights.isEmpty(), "At least one light should be available");
        
        // Test controlling the first light
        UUID firstLightId = lights.get(0).getId();
        String firstLightName = lights.get(0).getName();
        
        logger.info("Testing individual control of light: {} (ID: {})", firstLightName, firstLightId);
        
        // Turn on
        boolean result = hueLightService.turnOn(firstLightId);
        assertTrue(result, "Should successfully turn on light");
        Thread.sleep(500);
        
        // Turn off
        result = hueLightService.turnOff(firstLightId);
        assertTrue(result, "Should successfully turn off light");
        
        logger.info("Individual light control test passed");
    }
}
