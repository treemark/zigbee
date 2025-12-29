package com.appliedvillainy.hue.controller;

import com.appliedvillainy.hue.service.AnimationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * REST controller for light animations.
 */
@RestController
@RequestMapping("/api/animations")
@Tag(name = "Animations", description = "Run animations on Zigbee lights")
public class AnimationController {

    private final AnimationService animationService;

    public AnimationController(AnimationService animationService) {
        this.animationService = animationService;
    }

    @PostMapping("/pulse/{lightId}")
    @Operation(summary = "Pulse a light", description = "Run a pulse (on/off) animation on a specific light")
    public ResponseEntity<Map<String, String>> pulseLight(
            @Parameter(description = "Light UUID") @PathVariable UUID lightId,
            @Parameter(description = "Number of cycles") @RequestParam(defaultValue = "5") int cycles,
            @Parameter(description = "Interval in milliseconds") @RequestParam(defaultValue = "500") long intervalMs) {
        
        animationService.pulseLight(lightId, cycles, intervalMs);
        return ResponseEntity.accepted().body(Map.of(
                "status", "started",
                "animation", "pulse-" + lightId
        ));
    }

    @PostMapping("/breathe/{lightId}")
    @Operation(summary = "Breathe animation", description = "Run a breathing/fade animation on a specific light")
    public ResponseEntity<Map<String, String>> breatheLight(
            @Parameter(description = "Light UUID") @PathVariable UUID lightId,
            @Parameter(description = "Number of cycles") @RequestParam(defaultValue = "3") int cycles,
            @Parameter(description = "Cycle duration in milliseconds") @RequestParam(defaultValue = "4000") long cycleDurationMs) {
        
        animationService.breatheLight(lightId, cycles, cycleDurationMs);
        return ResponseEntity.accepted().body(Map.of(
                "status", "started",
                "animation", "breathe-" + lightId
        ));
    }

    @PostMapping("/sequential")
    @Operation(summary = "Sequential lights", description = "Turn lights on/off in sequence")
    public ResponseEntity<Map<String, String>> sequentialLights(
            @Parameter(description = "Number of cycles") @RequestParam(defaultValue = "2") int cycles,
            @Parameter(description = "Delay between each light in milliseconds") @RequestParam(defaultValue = "300") long delayMs) {
        
        animationService.sequentialLights(cycles, delayMs);
        return ResponseEntity.accepted().body(Map.of(
                "status", "started",
                "animation", "sequential"
        ));
    }

    @PostMapping("/random-blink")
    @Operation(summary = "Random blink", description = "Randomly toggle lights for a duration")
    public ResponseEntity<Map<String, String>> randomBlink(
            @Parameter(description = "Duration in seconds") @RequestParam(defaultValue = "10") int durationSeconds) {
        
        animationService.randomBlink(durationSeconds);
        return ResponseEntity.accepted().body(Map.of(
                "status", "started",
                "animation", "random-blink"
        ));
    }

    @GetMapping
    @Operation(summary = "Get running animations", description = "List all currently running animations")
    public ResponseEntity<Set<String>> getRunningAnimations() {
        return ResponseEntity.ok(animationService.getRunningAnimations());
    }

    @DeleteMapping("/{animationKey}")
    @Operation(summary = "Stop an animation", description = "Stop a specific running animation")
    public ResponseEntity<Void> stopAnimation(
            @Parameter(description = "Animation key (e.g., 'pulse-{lightId}')") @PathVariable String animationKey) {
        
        boolean stopped = animationService.stopAnimation(animationKey);
        return stopped ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping
    @Operation(summary = "Stop all animations", description = "Stop all running animations")
    public ResponseEntity<Void> stopAllAnimations() {
        animationService.stopAllAnimations();
        return ResponseEntity.ok().build();
    }
}
