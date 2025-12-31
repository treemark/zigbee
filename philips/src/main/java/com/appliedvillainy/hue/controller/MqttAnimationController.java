package com.appliedvillainy.hue.controller;

import com.appliedvillainy.hue.service.MqttAnimationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST controller for running animations on MQTT devices.
 */
@RestController
@RequestMapping("/api/mqtt/animations")
@Tag(name = "MQTT Animations", description = "Run light animations on MQTT devices via Zigbee2MQTT")
public class MqttAnimationController {

    private static final Logger logger = LoggerFactory.getLogger(MqttAnimationController.class);

    private final MqttAnimationService mqttAnimationService;

    public MqttAnimationController(MqttAnimationService mqttAnimationService) {
        this.mqttAnimationService = mqttAnimationService;
    }

    @PostMapping("/pulse/{friendlyName}")
    @Operation(summary = "Run pulse animation on a specific device")
    public ResponseEntity<String> pulseDevice(
            @PathVariable String friendlyName,
            @RequestParam(defaultValue = "10") int cycles,
            @RequestParam(defaultValue = "500") long intervalMs) {
        
        logger.info("Starting pulse animation on device: {}", friendlyName);
        mqttAnimationService.pulseDevice(friendlyName, cycles, intervalMs);
        return ResponseEntity.ok("Pulse animation started on device: " + friendlyName);
    }

    @PostMapping("/breathe/{friendlyName}")
    @Operation(summary = "Run breathing/fade animation on a specific device")
    public ResponseEntity<String> breatheDevice(
            @PathVariable String friendlyName,
            @RequestParam(defaultValue = "5") int cycles,
            @RequestParam(defaultValue = "3000") long cycleDurationMs) {
        
        logger.info("Starting breathe animation on device: {}", friendlyName);
        mqttAnimationService.breatheDevice(friendlyName, cycles, cycleDurationMs);
        return ResponseEntity.ok("Breathe animation started on device: " + friendlyName);
    }

    @PostMapping("/sequential")
    @Operation(summary = "Run sequential on/off pattern across all devices")
    public ResponseEntity<String> sequentialDevices(
            @RequestParam(defaultValue = "3") int cycles,
            @RequestParam(defaultValue = "500") long delayMs) {
        
        logger.info("Starting sequential animation");
        mqttAnimationService.sequentialDevices(cycles, delayMs);
        return ResponseEntity.ok("Sequential animation started");
    }

    @PostMapping("/random-blink")
    @Operation(summary = "Run random blink pattern across all devices")
    public ResponseEntity<String> randomBlink(
            @RequestParam(defaultValue = "30") int durationSeconds) {
        
        logger.info("Starting random blink animation");
        mqttAnimationService.randomBlink(durationSeconds);
        return ResponseEntity.ok("Random blink animation started for " + durationSeconds + " seconds");
    }

    @PostMapping("/color-temp-sweep/{friendlyName}")
    @Operation(summary = "Run color temperature sweep animation")
    public ResponseEntity<String> colorTempSweep(
            @PathVariable String friendlyName,
            @RequestParam(defaultValue = "5") int cycles,
            @RequestParam(defaultValue = "153") int minTemp,
            @RequestParam(defaultValue = "500") int maxTemp,
            @RequestParam(defaultValue = "4000") long cycleDurationMs) {
        
        logger.info("Starting color temp sweep animation on device: {}", friendlyName);
        mqttAnimationService.colorTempSweep(friendlyName, cycles, minTemp, maxTemp, cycleDurationMs);
        return ResponseEntity.ok("Color temp sweep animation started on device: " + friendlyName);
    }

    @PostMapping("/wave")
    @Operation(summary = "Run wave pattern across all devices")
    public ResponseEntity<String> wavePattern(
            @RequestParam(defaultValue = "3") int cycles,
            @RequestParam(defaultValue = "300") long delayMs) {
        
        logger.info("Starting wave animation");
        mqttAnimationService.wavePattern(cycles, delayMs);
        return ResponseEntity.ok("Wave animation started");
    }

    @PostMapping("/stop/{animationKey}")
    @Operation(summary = "Stop a specific animation")
    public ResponseEntity<String> stopAnimation(@PathVariable String animationKey) {
        logger.info("Stopping animation: {}", animationKey);
        boolean stopped = mqttAnimationService.stopAnimation(animationKey);
        
        if (stopped) {
            return ResponseEntity.ok("Animation stopped: " + animationKey);
        } else {
            return ResponseEntity.ok("Animation not found or already stopped: " + animationKey);
        }
    }

    @PostMapping("/stop-all")
    @Operation(summary = "Stop all running animations")
    public ResponseEntity<String> stopAllAnimations() {
        logger.info("Stopping all MQTT animations");
        mqttAnimationService.stopAllAnimations();
        return ResponseEntity.ok("All MQTT animations stopped");
    }

    @GetMapping("/running")
    @Operation(summary = "Get list of currently running animations")
    public ResponseEntity<Set<String>> getRunningAnimations() {
        Set<String> runningAnimations = mqttAnimationService.getRunningAnimations();
        return ResponseEntity.ok(runningAnimations);
    }

    @GetMapping("/status")
    @Operation(summary = "Check if any animation is running")
    public ResponseEntity<Boolean> isAnyAnimationRunning() {
        boolean running = mqttAnimationService.isAnyAnimationRunning();
        return ResponseEntity.ok(running);
    }
}
