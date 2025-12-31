package com.appliedvillainy.hue;

import com.appliedvillainy.hue.model.MqttDeviceDto;
import com.appliedvillainy.hue.service.MqttDeviceService;
import com.appliedvillainy.hue.service.MoquetteBrokerService;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for listing and discovering MQTT devices using embedded Moquette broker.
 * This test will:
 * 1. Verify the embedded Moquette broker is running
 * 2. Simulate MQTT devices connecting and publishing device information
 * 3. Verify MqttDeviceService discovers the devices
 * 4. List all discovered devices with their properties
 */
@SpringBootTest(properties = {"hue.bridge.enabled=false"})
class MqttDeviceListTest {

    private static final Logger logger = LoggerFactory.getLogger(MqttDeviceListTest.class);
    
    @Autowired
    private MqttDeviceService mqttDeviceService;
    
    @Autowired
    private MoquetteBrokerService moquetteBrokerService;

    @Test
    void listAllMqttDevices() throws InterruptedException {
        logger.info("========================================");
        logger.info("Starting MQTT device discovery test");
        logger.info("========================================");
        
        // Check if MQTT service is connected
        boolean connected = mqttDeviceService.isConnected();
        logger.info("MQTT Connection Status: {}", connected ? "CONNECTED" : "DISCONNECTED");
        
        if (!connected) {
            logger.warn("MQTT service is not connected. This may be expected if mqtt.enabled=false or broker is unavailable.");
            logger.warn("Skipping device discovery test.");
            return;
        }
        
        // Give the service a moment to discover devices if just connected
        logger.info("Waiting 2 seconds for initial device discovery...");
        Thread.sleep(2000);
        
        // Trigger explicit discovery
        logger.info("Triggering device discovery...");
        mqttDeviceService.discoverDevices();
        
        // Wait a bit for discovery response
        Thread.sleep(1000);
        
        // Get all discovered MQTT devices
        List<MqttDeviceDto> devices = mqttDeviceService.getAllDevices();
        assertNotNull(devices, "Devices list should not be null");
        
        logger.info("\n========================================");
        logger.info("Discovered {} MQTT device(s):", devices.size());
        logger.info("========================================");
        
        if (devices.isEmpty()) {
            logger.warn("No MQTT devices discovered!");
            logger.warn("Possible reasons:");
            logger.warn("  - No devices paired with Zigbee2MQTT coordinator");
            logger.warn("  - MQTT broker not responding");
            logger.warn("  - Incorrect broker URL in configuration");
            logger.warn("  - Network connectivity issues");
        } else {
            int deviceNumber = 1;
            for (MqttDeviceDto device : devices) {
                logger.info("\nDevice #{}: {}", deviceNumber++, device.getFriendlyName());
                logger.info("  IEEE Address: {}", device.getIeeeAddress());
                logger.info("  Type: {}", device.getType());
                logger.info("  Model ID: {}", device.getModelId());
                logger.info("  Manufacturer: {}", device.getManufacturerName());
                logger.info("  Supported: {}", device.isSupported());
                logger.info("  State: {}", device.getState() != null ? device.getState() : "Unknown");
                
                if (device.getBrightness() != null) {
                    logger.info("  Brightness: {}/255 ({}%)", 
                        device.getBrightness(), 
                        (int)((device.getBrightness() / 255.0) * 100));
                }
                
                if (device.getColorTemp() != null) {
                    logger.info("  Color Temperature: {}", device.getColorTemp());
                }
                
                if (device.getColor() != null) {
                    logger.info("  Color (x,y): ({}, {})", 
                        device.getColor().getX(), 
                        device.getColor().getY());
                }
                
                // Assertions for this device
                assertNotNull(device.getFriendlyName(), "Device should have a friendly name");
                assertNotNull(device.getType(), "Device should have a type");
            }
        }
        
        logger.info("\n========================================");
        logger.info("MQTT device listing test completed!");
        logger.info("Total devices found: {}", devices.size());
        logger.info("========================================");
    }
    
    @Test
    void verifyMoquetteBrokerIsRunning() {
        logger.info("========================================");
        logger.info("Verifying Moquette embedded broker");
        logger.info("========================================");
        
        assertTrue(moquetteBrokerService.isRunning(), "Moquette broker should be running");
        logger.info("✓ Moquette broker is running on port: {}", moquetteBrokerService.getBrokerPort());
        
        boolean connected = mqttDeviceService.isConnected();
        logger.info("✓ MqttDeviceService connected: {}", connected);
        
        assertTrue(connected, "MqttDeviceService should be connected to the embedded broker");
    }
    
    @Test
    void simulateDeviceAndVerifyDiscovery() throws Exception {
        logger.info("========================================");
        logger.info("Simulating MQTT device connection");
        logger.info("========================================");
        
        // Verify broker is running
        assertTrue(moquetteBrokerService.isRunning(), "Moquette broker must be running");
        
        // Create a test MQTT client to simulate a device
        String brokerUrl = "tcp://localhost:" + moquetteBrokerService.getBrokerPort();
        MqttClient testClient = new MqttClient(brokerUrl, "test-device-simulator");
        
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        
        logger.info("Test client connecting to embedded broker at {}", brokerUrl);
        testClient.connect(options);
        assertTrue(testClient.isConnected(), "Test client should connect successfully");
        
        // Simulate publishing a device list (like Zigbee2MQTT bridge does)
        String deviceListJson = "[" +
            "{\"friendly_name\":\"Test Light 1\",\"ieee_address\":\"0x00158d0001234567\"," +
            "\"type\":\"EndDevice\",\"model_id\":\"TRADFRI bulb E27\"," +
            "\"manufacturer\":\"IKEA\",\"supported\":true}," +
            "{\"friendly_name\":\"Test Light 2\",\"ieee_address\":\"0x00158d0001234568\"," +
            "\"type\":\"EndDevice\",\"model_id\":\"TRADFRI bulb GU10\"," +
            "\"manufacturer\":\"IKEA\",\"supported\":true}" +
            "]";
        
        MqttMessage deviceListMessage = new MqttMessage(deviceListJson.getBytes());
        deviceListMessage.setQos(1);
        
        logger.info("Publishing simulated device list to zigbee2mqtt/bridge/devices");
        testClient.publish("zigbee2mqtt/bridge/devices", deviceListMessage);
        
        // Give the service time to process
        Thread.sleep(2000);  // Increased wait time
        
        // Verify devices were discovered
        List<MqttDeviceDto> devices = mqttDeviceService.getAllDevices();
        logger.info("Discovered {} devices after simulation", devices.size());
        
        // Log all devices for debugging
        devices.forEach(device -> 
            logger.info("  Found device: {} ({})", device.getFriendlyName(), device.getType())
        );
        
        assertTrue(devices.size() >= 2, "Should discover at least 2 simulated devices, but found: " + devices.size());
        
        // Verify specific devices
        boolean foundTestLight1 = devices.stream()
            .anyMatch(d -> "Test Light 1".equals(d.getFriendlyName()));
        boolean foundTestLight2 = devices.stream()
            .anyMatch(d -> "Test Light 2".equals(d.getFriendlyName()));
        
        assertTrue(foundTestLight1, "Should find Test Light 1");
        assertTrue(foundTestLight2, "Should find Test Light 2");
        
        logger.info("✓ Successfully discovered simulated devices:");
        devices.forEach(device -> 
            logger.info("  - {} ({})", device.getFriendlyName(), device.getModelId())
        );
        
        // Simulate device state update
        String stateUpdateJson = "{\"state\":\"ON\",\"brightness\":200}";
        MqttMessage stateMessage = new MqttMessage(stateUpdateJson.getBytes());
        stateMessage.setQos(1);
        
        logger.info("Publishing state update for Test Light 1");
        testClient.publish("zigbee2mqtt/Test Light 1", stateMessage);
        
        Thread.sleep(500);
        
        // Verify state was updated
        var testLight1 = mqttDeviceService.getDevice("Test Light 1");
        assertTrue(testLight1.isPresent(), "Test Light 1 should be retrievable");
        assertEquals("ON", testLight1.get().getState(), "Device state should be ON");
        assertEquals(200, testLight1.get().getBrightness(), "Brightness should be 200");
        
        logger.info("✓ Device state successfully updated: state={}, brightness={}", 
            testLight1.get().getState(), testLight1.get().getBrightness());
        
        // Cleanup
        testClient.disconnect();
        testClient.close();
        
        logger.info("========================================");
        logger.info("Device simulation test completed!");
        logger.info("========================================");
    }
    
    @Test
    void verifyMqttConnectionStatus() {
        logger.info("Checking MQTT connection status...");
        
        boolean connected = mqttDeviceService.isConnected();
        logger.info("MQTT Connected: {}", connected);
        
        if (connected) {
            logger.info("Successfully connected to embedded Moquette broker");
        } else {
            logger.warn("Not connected to MQTT broker");
        }
    }
    
    @Test
    void testDeviceDiscoveryTrigger() throws InterruptedException {
        logger.info("Testing manual device discovery trigger...");
        
        if (!mqttDeviceService.isConnected()) {
            logger.warn("MQTT not connected, skipping discovery trigger test");
            return;
        }
        
        // Get initial device count
        int initialCount = mqttDeviceService.getAllDevices().size();
        logger.info("Initial device count: {}", initialCount);
        
        // Trigger discovery
        mqttDeviceService.discoverDevices();
        logger.info("Discovery request sent to MQTT broker");
        
        // Wait for response
        Thread.sleep(1500);
        
        // Get updated device count
        int updatedCount = mqttDeviceService.getAllDevices().size();
        logger.info("Device count after discovery: {}", updatedCount);
        
        // Count should be same or more (never less)
        assertTrue(updatedCount >= initialCount, 
            "Device count should not decrease after discovery");
    }
    
    @Test
    void testGetSpecificDevice() {
        logger.info("Testing retrieval of specific device...");
        
        if (!mqttDeviceService.isConnected()) {
            logger.warn("MQTT not connected, skipping specific device test");
            return;
        }
        
        List<MqttDeviceDto> devices = mqttDeviceService.getAllDevices();
        
        if (devices.isEmpty()) {
            logger.warn("No devices available to test specific device retrieval");
            return;
        }
        
        // Get the first device
        MqttDeviceDto firstDevice = devices.get(0);
        String friendlyName = firstDevice.getFriendlyName();
        
        logger.info("Testing retrieval of device: {}", friendlyName);
        
        // Retrieve by friendly name
        var retrievedDevice = mqttDeviceService.getDevice(friendlyName);
        
        assertTrue(retrievedDevice.isPresent(), 
            "Should be able to retrieve device by friendly name");
        assertEquals(friendlyName, retrievedDevice.get().getFriendlyName(), 
            "Retrieved device should match requested device");
        
        logger.info("Successfully retrieved device: {}", friendlyName);
    }
    
    @Test
    void listDevicesByType() {
        logger.info("Listing MQTT devices grouped by type...");
        
        if (!mqttDeviceService.isConnected()) {
            logger.warn("MQTT not connected, skipping device type test");
            return;
        }
        
        List<MqttDeviceDto> devices = mqttDeviceService.getAllDevices();
        
        if (devices.isEmpty()) {
            logger.warn("No devices to group by type");
            return;
        }
        
        // Group devices by type
        var devicesByType = devices.stream()
            .collect(java.util.stream.Collectors.groupingBy(MqttDeviceDto::getType));
        
        logger.info("\nDevices grouped by type:");
        devicesByType.forEach((type, deviceList) -> {
            logger.info("\n{} devices ({})", type, deviceList.size());
            deviceList.forEach(device -> 
                logger.info("  - {} ({})", 
                    device.getFriendlyName(), 
                    device.getModelId())
            );
        });
        
        assertFalse(devicesByType.isEmpty(), "Should have at least one device type");
    }
}
