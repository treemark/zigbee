package com.appliedvillainy.hue.controller;

import com.appliedvillainy.hue.model.LightCommand;
import com.appliedvillainy.hue.model.LightDto;
import com.appliedvillainy.hue.service.HueLightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing Zigbee lights via Philips Hue Bridge.
 */
@RestController
@RequestMapping("/api/lights")
@Tag(name = "Lights", description = "Control Zigbee lights via Philips Hue Bridge")
public class LightController {

    private final HueLightService lightService;

    public LightController(HueLightService lightService) {
        this.lightService = lightService;
    }

    @GetMapping
    @Operation(summary = "Get all lights", description = "Returns a list of all lights connected to the Hue Bridge")
    public ResponseEntity<List<LightDto>> getAllLights() {
        List<LightDto> lights = lightService.getAllLights();
        return ResponseEntity.ok(lights);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a light by ID", description = "Returns details of a specific light")
    public ResponseEntity<LightDto> getLightById(
            @Parameter(description = "Light UUID") @PathVariable UUID id) {
        return lightService.getLightById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search for a light by name", description = "Returns a light matching the given name")
    public ResponseEntity<LightDto> getLightByName(
            @Parameter(description = "Light name") @RequestParam String name) {
        return lightService.getLightByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/on")
    @Operation(summary = "Turn on a light", description = "Turns on the specified light")
    public ResponseEntity<Void> turnOn(
            @Parameter(description = "Light UUID") @PathVariable UUID id) {
        boolean success = lightService.turnOn(id);
        return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/off")
    @Operation(summary = "Turn off a light", description = "Turns off the specified light")
    public ResponseEntity<Void> turnOff(
            @Parameter(description = "Light UUID") @PathVariable UUID id) {
        boolean success = lightService.turnOff(id);
        return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/toggle")
    @Operation(summary = "Toggle a light", description = "Toggles the light on/off state")
    public ResponseEntity<LightDto> toggle(
            @Parameter(description = "Light UUID") @PathVariable UUID id) {
        return lightService.getLightById(id)
                .map(light -> {
                    if (light.isOn()) {
                        lightService.turnOff(id);
                    } else {
                        lightService.turnOn(id);
                    }
                    return ResponseEntity.ok(lightService.getLightById(id).orElse(light));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update light state", description = "Apply a command to update the light state")
    public ResponseEntity<Void> updateLight(
            @Parameter(description = "Light UUID") @PathVariable UUID id,
            @Valid @RequestBody LightCommand command) {
        boolean success = lightService.applyCommand(id, command);
        return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/all/on")
    @Operation(summary = "Turn on all lights", description = "Turns on all lights connected to the bridge")
    public ResponseEntity<Void> turnOnAll() {
        lightService.turnOnAllLights();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/all/off")
    @Operation(summary = "Turn off all lights", description = "Turns off all lights connected to the bridge")
    public ResponseEntity<Void> turnOffAll() {
        lightService.turnOffAllLights();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/all/brightness")
    @Operation(summary = "Set brightness for all lights", description = "Sets the brightness level for all lights")
    public ResponseEntity<Void> setBrightnessForAll(
            @Parameter(description = "Brightness level (0-100)") @RequestParam int brightness) {
        if (brightness < 0 || brightness > 100) {
            return ResponseEntity.badRequest().build();
        }
        lightService.setBrightnessForAll(brightness);
        return ResponseEntity.ok().build();
    }
}
