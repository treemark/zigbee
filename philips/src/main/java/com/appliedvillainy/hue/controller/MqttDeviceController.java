package com.appliedvillainy.hue.controller;

import com.appliedvillainy.hue.model.MqttDeviceDto;
import com.appliedvillainy.hue.service.MqttDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for MQTT device discovery and control via Zigbee2MQTT.
 */
@RestController
@RequestMapping("/api/mqtt/devices")
@Tag(name = "MQTT Devices", description = "Discover and control MQTT devices via Zigbee2MQTT")
public class MqttDeviceController {

    private static final Logger logger = LoggerFactory.getLogger(MqttDeviceController.class);

    private final MqttDeviceService mqttDeviceService;

    public MqttDeviceController(MqttDeviceService mqttDeviceService) {
        this.mqttDeviceService = mqttDeviceService;
    }

    @GetMapping
    @Operation(summary = "Get all discovered MQTT devices")
    public ResponseEntity<List<MqttDeviceDto>> getAllDevices() {
        logger.info("Getting all MQTT devices");
        List<MqttDeviceDto> devices = mqttDeviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/{friendlyName}")
    @Operation(summary = "Get a specific MQTT device by friendly name")
    public ResponseEntity<MqttDeviceDto> getDevice(@PathVariable String friendlyName) {
        logger.info("Getting MQTT device: {}", friendlyName);
        return mqttDeviceService.getDevice(friendlyName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/discover")
    @Operation(summary = "Trigger device discovery")
    public ResponseEntity<String> discoverDevices() {
        logger.info("Triggering MQTT device discovery");
        mqttDeviceService.discoverDevices();
        return ResponseEntity.ok("Device discovery triggered");
    }

    @PostMapping("/{friendlyName}/on")
    @Operation(summary = "Turn on a device")
    public ResponseEntity<String> turnOn(@PathVariable String friendlyName) {
        try {
            logger.info("Turning on MQTT device: {}", friendlyName);
            mqttDeviceService.turnOn(friendlyName);
            return ResponseEntity.ok("Device turned on");
        } catch (Exception e) {
            logger.error("Failed to turn on device: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to turn on device: " + e.getMessage());
        }
    }

    @PostMapping("/{friendlyName}/off")
    @Operation(summary = "Turn off a device")
    public ResponseEntity<String> turnOff(@PathVariable String friendlyName) {
        try {
            logger.info("Turning off MQTT device: {}", friendlyName);
            mqttDeviceService.turnOff(friendlyName);
            return ResponseEntity.ok("Device turned off");
        } catch (Exception e) {
            logger.error("Failed to turn off device: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to turn off device: " + e.getMessage());
        }
    }

    @PostMapping("/{friendlyName}/toggle")
    @Operation(summary = "Toggle a device on/off")
    public ResponseEntity<String> toggle(@PathVariable String friendlyName) {
        try {
            logger.info("Toggling MQTT device: {}", friendlyName);
            mqttDeviceService.toggle(friendlyName);
            return ResponseEntity.ok("Device toggled");
        } catch (Exception e) {
            logger.error("Failed to toggle device: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to toggle device: " + e.getMessage());
        }
    }

    @PostMapping("/{friendlyName}/brightness")
    @Operation(summary = "Set device brightness (0-255)")
    public ResponseEntity<String> setBrightness(
            @PathVariable String friendlyName,
            @RequestParam int brightness) {
        try {
            logger.info("Setting brightness for MQTT device {}: {}", friendlyName, brightness);
            mqttDeviceService.setBrightness(friendlyName, brightness);
            return ResponseEntity.ok("Brightness set to " + brightness);
        } catch (Exception e) {
            logger.error("Failed to set brightness: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to set brightness: " + e.getMessage());
        }
    }

    @PostMapping("/{friendlyName}/color-temp")
    @Operation(summary = "Set device color temperature")
    public ResponseEntity<String> setColorTemp(
            @PathVariable String friendlyName,
            @RequestParam int colorTemp) {
        try {
            logger.info("Setting color temperature for MQTT device {}: {}", friendlyName, colorTemp);
            mqttDeviceService.setColorTemp(friendlyName, colorTemp);
            return ResponseEntity.ok("Color temperature set to " + colorTemp);
        } catch (Exception e) {
            logger.error("Failed to set color temperature: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to set color temperature: " + e.getMessage());
        }
    }

    @PostMapping("/{friendlyName}/command")
    @Operation(summary = "Send a custom command to a device")
    public ResponseEntity<String> sendCommand(
            @PathVariable String friendlyName,
            @RequestBody Map<String, Object> command) {
        try {
            logger.info("Sending custom command to MQTT device {}: {}", friendlyName, command);
            mqttDeviceService.sendCommand(friendlyName, command);
            return ResponseEntity.ok("Command sent successfully");
        } catch (Exception e) {
            logger.error("Failed to send command: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to send command: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Get MQTT connection status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        boolean connected = mqttDeviceService.isConnected();
        int deviceCount = mqttDeviceService.getAllDevices().size();
        
        return ResponseEntity.ok(Map.of(
            "connected", connected,
            "deviceCount", deviceCount
        ));
    }
}
