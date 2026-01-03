package com.appliedvillainy.hue.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * High-performance animation service for OpenBeken devices.
 * Optimized for large-scale coordinated light displays with high update rates.
 * 
 * Uses MQTT QoS 0 (fire-and-forget) for maximum throughput and minimal latency.
 */
@Slf4j
@Service
public class OpenBekenAnimationService {

    private final MqttDeviceService mqttDeviceService;
    private static final int QOS_0 = 0; // Fire and forget - no acknowledgment
    private static final boolean RETAINED = false; // Don't retain animation commands
    
    // Track running animations
    private final ConcurrentHashMap<String, AtomicBoolean> runningAnimations = new ConcurrentHashMap<>();

    public OpenBekenAnimationService(MqttDeviceService mqttDeviceService) {
        this.mqttDeviceService = mqttDeviceService;
        log.info("OpenBeken Animation Service initialized");
    }

    /**
     * Broadcast a command to a group of devices using a shared group topic.
     * This is the most efficient way to control multiple devices simultaneously.
     * 
     * @param group The group topic name (e.g., "animations")
     * @param command The OpenBeken command (e.g., "POWER1", "Dimmer", "HsbColor")
     * @param value The command value
     */
    public void broadcastToGroup(String group, String command, String value) {
        String topic = "cmnd/" + group + "/" + command;
        publish(topic, value);
    }

    /**
     * Send a command to a specific device.
     * 
     * @param deviceId The device client ID
     * @param command The OpenBeken command
     * @param value The command value
     */
    public void sendToDevice(String deviceId, String command, String value) {
        String topic = "cmnd/" + deviceId + "/" + command;
        publish(topic, value);
    }

    /**
     * Pulse animation - rapid on/off blinking.
     * Suitable for attention-grabbing effects or synchronized flashes.
     * 
     * @param group Device group or individual device ID
     * @param cycles Number of on/off cycles
     * @param intervalMs Time between state changes (ms)
     * @return CompletableFuture that completes when animation finishes
     */
    @Async
    public CompletableFuture<Void> animatePulse(String group, int cycles, int intervalMs) {
        String animationKey = "pulse-" + group;
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);
        
        log.info("Starting pulse animation for group '{}': {} cycles @ {}ms", group, cycles, intervalMs);
        
        try {
            for (int i = 0; i < cycles && running.get(); i++) {
                broadcastToGroup(group, "POWER1", "ON");
                Thread.sleep(intervalMs / 2);
                
                if (!running.get()) break;
                
                broadcastToGroup(group, "POWER1", "OFF");
                Thread.sleep(intervalMs / 2);
            }
        } catch (InterruptedException e) {
            log.warn("Pulse animation interrupted for group '{}'", group);
            Thread.currentThread().interrupt();
        } finally {
            runningAnimations.remove(animationKey);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Brightness sweep animation - smooth fade from dark to bright and back.
     * Recommended update rate: 20-30 Hz for smooth transitions.
     * 
     * @param group Device group or individual device ID
     * @param cycles Number of complete sweeps
     * @param steps Brightness step size (smaller = smoother, more updates)
     * @param delayMs Delay between brightness updates (ms)
     * @return CompletableFuture that completes when animation finishes
     */
    @Async
    public CompletableFuture<Void> animateBrightnessSweep(String group, int cycles, int steps, int delayMs) {
        String animationKey = "brightness-" + group;
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);
        
        log.info("Starting brightness sweep for group '{}': {} cycles, step={}, delay={}ms", 
                 group, cycles, steps, delayMs);
        
        try {
            for (int cycle = 0; cycle < cycles && running.get(); cycle++) {
                // Fade up
                for (int brightness = 0; brightness <= 100 && running.get(); brightness += steps) {
                    broadcastToGroup(group, "Dimmer", String.valueOf(brightness));
                    Thread.sleep(delayMs);
                }
                
                // Fade down
                for (int brightness = 100; brightness >= 0 && running.get(); brightness -= steps) {
                    broadcastToGroup(group, "Dimmer", String.valueOf(brightness));
                    Thread.sleep(delayMs);
                }
            }
        } catch (InterruptedException e) {
            log.warn("Brightness sweep interrupted for group '{}'", group);
            Thread.currentThread().interrupt();
        } finally {
            runningAnimations.remove(animationKey);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Wave animation - sequential activation of devices creating a wave effect.
     * Each device turns on with a delay, creating a ripple effect.
     * 
     * @param devices List of device IDs in sequence order
     * @param delayMs Delay between each device activation (ms)
     * @param duration How long each device stays on (ms)
     * @return CompletableFuture that completes when animation finishes
     */
    @Async
    public CompletableFuture<Void> animateWave(List<String> devices, int delayMs, int duration) {
        String animationKey = "wave-" + String.join("-", devices);
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);
        
        log.info("Starting wave animation across {} devices, delay={}ms", devices.size(), delayMs);
        
        try {
            for (String device : devices) {
                if (!running.get()) break;
                
                // Turn on this device
                sendToDevice(device, "POWER1", "ON");
                
                // Schedule turn off after duration
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(duration);
                        sendToDevice(device, "POWER1", "OFF");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                
                Thread.sleep(delayMs);
            }
        } catch (InterruptedException e) {
            log.warn("Wave animation interrupted");
            Thread.currentThread().interrupt();
        } finally {
            runningAnimations.remove(animationKey);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Color rotation animation for RGB devices.
     * Cycles through the HSB color spectrum.
     * 
     * @param group Device group or individual device ID
     * @param cycles Number of complete color rotations
     * @param hueStep Step size for hue rotation (smaller = smoother)
     * @param delayMs Delay between color updates (ms)
     * @return CompletableFuture that completes when animation finishes
     */
    @Async
    public CompletableFuture<Void> animateColorRotation(String group, int cycles, int hueStep, int delayMs) {
        String animationKey = "color-" + group;
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);
        
        log.info("Starting color rotation for group '{}': {} cycles, hueStep={}, delay={}ms", 
                 group, cycles, hueStep, delayMs);
        
        try {
            int saturation = 100; // Full saturation
            int brightness = 50; // Medium brightness
            
            for (int cycle = 0; cycle < cycles && running.get(); cycle++) {
                for (int hue = 0; hue < 360 && running.get(); hue += hueStep) {
                    // OpenBeken HSB format: Hue,Saturation,Brightness
                    String hsbValue = hue + "," + saturation + "," + brightness;
                    broadcastToGroup(group, "HsbColor", hsbValue);
                    Thread.sleep(delayMs);
                }
            }
        } catch (InterruptedException e) {
            log.warn("Color rotation interrupted for group '{}'", group);
            Thread.currentThread().interrupt();
        } finally {
            runningAnimations.remove(animationKey);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Chase animation - devices light up in sequence, one at a time.
     * Creates a "chasing" or "running light" effect.
     * 
     * @param devices List of device IDs in sequence order
     * @param cycles Number of complete chase sequences
     * @param delayMs Delay between device transitions (ms)
     * @return CompletableFuture that completes when animation finishes
     */
    @Async
    public CompletableFuture<Void> animateChase(List<String> devices, int cycles, int delayMs) {
        String animationKey = "chase-" + String.join("-", devices);
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);
        
        log.info("Starting chase animation across {} devices: {} cycles", devices.size(), cycles);
        
        try {
            for (int cycle = 0; cycle < cycles && running.get(); cycle++) {
                for (int i = 0; i < devices.size() && running.get(); i++) {
                    // Turn on current device
                    sendToDevice(devices.get(i), "POWER1", "ON");
                    
                    // Turn off previous device
                    if (i > 0) {
                        sendToDevice(devices.get(i - 1), "POWER1", "OFF");
                    } else if (cycle > 0) {
                        // Turn off last device from previous cycle
                        sendToDevice(devices.get(devices.size() - 1), "POWER1", "OFF");
                    }
                    
                    Thread.sleep(delayMs);
                }
                
                // Turn off last device at end of cycle
                sendToDevice(devices.get(devices.size() - 1), "POWER1", "OFF");
            }
        } catch (InterruptedException e) {
            log.warn("Chase animation interrupted");
            Thread.currentThread().interrupt();
        } finally {
            runningAnimations.remove(animationKey);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Synchronized flash - all devices in group flash simultaneously.
     * Efficient for large groups using broadcast topic.
     * 
     * @param group Device group name
     * @param flashes Number of flashes
     * @param onDurationMs Duration each flash stays on (ms)
     * @param offDurationMs Duration between flashes (ms)
     * @return CompletableFuture that completes when animation finishes
     */
    @Async
    public CompletableFuture<Void> animateSynchronizedFlash(String group, int flashes, int onDurationMs, int offDurationMs) {
        String animationKey = "flash-" + group;
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);
        
        log.info("Starting synchronized flash for group '{}': {} flashes", group, flashes);
        
        try {
            for (int i = 0; i < flashes && running.get(); i++) {
                broadcastToGroup(group, "POWER1", "ON");
                Thread.sleep(onDurationMs);
                
                if (!running.get()) break;
                
                broadcastToGroup(group, "POWER1", "OFF");
                if (i < flashes - 1) { // Don't wait after last flash
                    Thread.sleep(offDurationMs);
                }
            }
        } catch (InterruptedException e) {
            log.warn("Synchronized flash interrupted for group '{}'", group);
            Thread.currentThread().interrupt();
        } finally {
            runningAnimations.remove(animationKey);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Stop a specific animation by key.
     * 
     * @param animationKey The animation identifier (e.g., "pulse-animations")
     * @return true if animation was stopped, false if not found
     */
    public boolean stopAnimation(String animationKey) {
        AtomicBoolean running = runningAnimations.get(animationKey);
        if (running != null) {
            running.set(false);
            log.info("Stopped animation: {}", animationKey);
            return true;
        }
        return false;
    }

    /**
     * Stop all running animations.
     */
    public void stopAllAnimations() {
        runningAnimations.values().forEach(running -> running.set(false));
        runningAnimations.clear();
        log.info("Stopped all animations");
    }

    /**
     * Get list of currently running animation keys.
     */
    public List<String> getRunningAnimations() {
        return runningAnimations.keySet().stream().toList();
    }

    /**
     * Emergency stop - turn off all devices in a group immediately.
     * 
     * @param group Device group name
     */
    public void emergencyStop(String group) {
        log.warn("Emergency stop for group '{}'", group);
        stopAllAnimations();
        broadcastToGroup(group, "POWER1", "OFF");
    }

    /**
     * Internal method to publish MQTT messages with QoS 0 for maximum performance.
     */
    private void publish(String topic, String payload) {
        try {
            MqttClient mqttClient = mqttDeviceService.getMqttClient();
            if (mqttClient == null || !mqttClient.isConnected()) {
                log.warn("MQTT client not connected, cannot publish to topic: {}", topic);
                return;
            }
            
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(QOS_0); // Fire and forget
            message.setRetained(RETAINED);
            
            mqttClient.publish(topic, message);
            
            log.debug("Published to {}: {}", topic, payload);
        } catch (MqttException e) {
            log.error("Failed to publish to {}: {}", topic, e.getMessage());
        }
    }
}
