package com.appliedvillainy.hue.controller;

import com.appliedvillainy.hue.service.OpenBekenAnimationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for high-performance OpenBeken device animations.
 * Optimized for large-scale coordinated light displays.
 */
@Slf4j
@RestController
@RequestMapping("/api/openbeken/animations")
@RequiredArgsConstructor
@Tag(name = "OpenBeken Animations", description = "High-performance animation control for OpenBeken devices")
public class OpenBekenAnimationController {

    private final OpenBekenAnimationService animationService;

    @PostMapping("/pulse/{group}")
    @Operation(summary = "Pulse animation", 
               description = "Rapid on/off blinking. Use group name for multiple devices or device ID for single device.")
    public ResponseEntity<String> pulse(
            @Parameter(description = "Group name or device ID") @PathVariable String group,
            @Parameter(description = "Number of on/off cycles") @RequestParam(defaultValue = "10") int cycles,
            @Parameter(description = "Time between state changes in milliseconds") @RequestParam(defaultValue = "500") int intervalMs) {
        
        log.info("Starting pulse animation: group={}, cycles={}, intervalMs={}", group, cycles, intervalMs);
        animationService.animatePulse(group, cycles, intervalMs);
        return ResponseEntity.ok("Pulse animation started for group: " + group);
    }

    @PostMapping("/brightness-sweep/{group}")
    @Operation(summary = "Brightness sweep animation", 
               description = "Smooth fade from dark to bright and back. Recommended rate: 20-30 Hz")
    public ResponseEntity<String> brightnessSweep(
            @Parameter(description = "Group name or device ID") @PathVariable String group,
            @Parameter(description = "Number of complete sweeps") @RequestParam(defaultValue = "3") int cycles,
            @Parameter(description = "Brightness step size (smaller = smoother)") @RequestParam(defaultValue = "5") int steps,
            @Parameter(description = "Delay between updates in milliseconds") @RequestParam(defaultValue = "50") int delayMs) {
        
        log.info("Starting brightness sweep: group={}, cycles={}, steps={}, delayMs={}", 
                 group, cycles, steps, delayMs);
        animationService.animateBrightnessSweep(group, cycles, steps, delayMs);
        return ResponseEntity.ok("Brightness sweep started for group: " + group);
    }

    @PostMapping("/wave")
    @Operation(summary = "Wave animation", 
               description = "Sequential activation creating a wave effect across devices")
    public ResponseEntity<String> wave(
            @Parameter(description = "Ordered list of device IDs") @RequestBody List<String> devices,
            @Parameter(description = "Delay between device activations in milliseconds") @RequestParam(defaultValue = "100") int delayMs,
            @Parameter(description = "Duration each device stays on in milliseconds") @RequestParam(defaultValue = "500") int duration) {
        
        log.info("Starting wave animation across {} devices, delayMs={}, duration={}", 
                 devices.size(), delayMs, duration);
        animationService.animateWave(devices, delayMs, duration);
        return ResponseEntity.ok("Wave animation started across " + devices.size() + " devices");
    }

    @PostMapping("/color-rotation/{group}")
    @Operation(summary = "Color rotation animation", 
               description = "Cycles through HSB color spectrum for RGB devices")
    public ResponseEntity<String> colorRotation(
            @Parameter(description = "Group name or device ID") @PathVariable String group,
            @Parameter(description = "Number of complete rotations") @RequestParam(defaultValue = "3") int cycles,
            @Parameter(description = "Hue step size (smaller = smoother)") @RequestParam(defaultValue = "10") int hueStep,
            @Parameter(description = "Delay between color updates in milliseconds") @RequestParam(defaultValue = "30") int delayMs) {
        
        log.info("Starting color rotation: group={}, cycles={}, hueStep={}, delayMs={}", 
                 group, cycles, hueStep, delayMs);
        animationService.animateColorRotation(group, cycles, hueStep, delayMs);
        return ResponseEntity.ok("Color rotation started for group: " + group);
    }

    @PostMapping("/chase")
    @Operation(summary = "Chase animation", 
               description = "Devices light up in sequence, one at a time (running light effect)")
    public ResponseEntity<String> chase(
            @Parameter(description = "Ordered list of device IDs") @RequestBody List<String> devices,
            @Parameter(description = "Number of complete chase sequences") @RequestParam(defaultValue = "3") int cycles,
            @Parameter(description = "Delay between transitions in milliseconds") @RequestParam(defaultValue = "200") int delayMs) {
        
        log.info("Starting chase animation across {} devices: cycles={}, delayMs={}", 
                 devices.size(), cycles, delayMs);
        animationService.animateChase(devices, cycles, delayMs);
        return ResponseEntity.ok("Chase animation started across " + devices.size() + " devices");
    }

    @PostMapping("/synchronized-flash/{group}")
    @Operation(summary = "Synchronized flash", 
               description = "All devices in group flash simultaneously")
    public ResponseEntity<String> synchronizedFlash(
            @Parameter(description = "Group name") @PathVariable String group,
            @Parameter(description = "Number of flashes") @RequestParam(defaultValue = "5") int flashes,
            @Parameter(description = "Duration each flash stays on in milliseconds") @RequestParam(defaultValue = "100") int onDurationMs,
            @Parameter(description = "Duration between flashes in milliseconds") @RequestParam(defaultValue = "100") int offDurationMs) {
        
        log.info("Starting synchronized flash: group={}, flashes={}, onDurationMs={}, offDurationMs={}", 
                 group, flashes, onDurationMs, offDurationMs);
        animationService.animateSynchronizedFlash(group, flashes, onDurationMs, offDurationMs);
        return ResponseEntity.ok("Synchronized flash started for group: " + group);
    }

    @PostMapping("/broadcast/{group}/{command}")
    @Operation(summary = "Broadcast command to group", 
               description = "Send any OpenBeken command to all devices in a group")
    public ResponseEntity<String> broadcastCommand(
            @Parameter(description = "Group name") @PathVariable String group,
            @Parameter(description = "OpenBeken command (e.g., POWER1, Dimmer, HsbColor)") @PathVariable String command,
            @Parameter(description = "Command value") @RequestParam String value) {
        
        log.info("Broadcasting command: group={}, command={}, value={}", group, command, value);
        animationService.broadcastToGroup(group, command, value);
        return ResponseEntity.ok("Command broadcast to group: " + group);
    }

    @PostMapping("/device/{deviceId}/{command}")
    @Operation(summary = "Send command to specific device", 
               description = "Send any OpenBeken command to a single device")
    public ResponseEntity<String> deviceCommand(
            @Parameter(description = "Device ID") @PathVariable String deviceId,
            @Parameter(description = "OpenBeken command (e.g., POWER1, Dimmer, HsbColor)") @PathVariable String command,
            @Parameter(description = "Command value") @RequestParam String value) {
        
        log.info("Sending device command: deviceId={}, command={}, value={}", deviceId, command, value);
        animationService.sendToDevice(deviceId, command, value);
        return ResponseEntity.ok("Command sent to device: " + deviceId);
    }

    @PostMapping("/stop/{animationKey}")
    @Operation(summary = "Stop specific animation", 
               description = "Stop an animation by its key (e.g., 'pulse-animations')")
    public ResponseEntity<String> stopAnimation(
            @Parameter(description = "Animation key") @PathVariable String animationKey) {
        
        boolean stopped = animationService.stopAnimation(animationKey);
        if (stopped) {
            return ResponseEntity.ok("Animation stopped: " + animationKey);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/stop-all")
    @Operation(summary = "Stop all animations", 
               description = "Stop all currently running animations")
    public ResponseEntity<String> stopAllAnimations() {
        animationService.stopAllAnimations();
        return ResponseEntity.ok("All animations stopped");
    }

    @PostMapping("/emergency-stop/{group}")
    @Operation(summary = "Emergency stop", 
               description = "Immediately turn off all devices in group and stop all animations")
    public ResponseEntity<String> emergencyStop(
            @Parameter(description = "Group name") @PathVariable String group) {
        
        log.warn("Emergency stop triggered for group: {}", group);
        animationService.emergencyStop(group);
        return ResponseEntity.ok("Emergency stop executed for group: " + group);
    }

    @GetMapping("/running")
    @Operation(summary = "Get running animations", 
               description = "List all currently running animation keys")
    public ResponseEntity<Map<String, Object>> getRunningAnimations() {
        List<String> running = animationService.getRunningAnimations();
        return ResponseEntity.ok(Map.of(
            "count", running.size(),
            "animations", running
        ));
    }

    @GetMapping("/status")
    @Operation(summary = "Get service status", 
               description = "Get OpenBeken animation service status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        List<String> running = animationService.getRunningAnimations();
        return ResponseEntity.ok(Map.of(
            "service", "OpenBeken Animation Service",
            "status", "running",
            "activeAnimations", running.size(),
            "animations", running,
            "protocol", "MQTT QoS 0 (fire-and-forget)",
            "optimizedFor", "high-performance coordinated displays"
        ));
    }
}
