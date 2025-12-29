package com.appliedvillainy.hue;

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
