package com.appliedvillainy.hue.service;

import com.appliedvillainy.hue.model.MqttDeviceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service for running animations on MQTT devices.
 */
@Service
public class MqttAnimationService {

    private static final Logger logger = LoggerFactory.getLogger(MqttAnimationService.class);

    private final MqttDeviceService mqttDeviceService;
    
    // Track running animations
    private final Map<String, AtomicBoolean> runningAnimations = new ConcurrentHashMap<>();

    public MqttAnimationService(MqttDeviceService mqttDeviceService) {
        this.mqttDeviceService = mqttDeviceService;
    }

    /**
     * Run a pulse animation on a specific MQTT device.
     */
    @Async
    public void pulseDevice(String friendlyName, int cycles, long intervalMs) {
        String animationKey = "mqtt-pulse-" + friendlyName;
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);

        logger.info("Starting pulse animation on MQTT device {} for {} cycles", friendlyName, cycles);

        try {
            for (int i = 0; i < cycles && running.get(); i++) {
                mqttDeviceService.turnOn(friendlyName);
                Thread.sleep(intervalMs);
                mqttDeviceService.turnOff(friendlyName);
                Thread.sleep(intervalMs);
            }
            // Leave light on at the end
            mqttDeviceService.turnOn(friendlyName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Pulse animation interrupted");
        } catch (Exception e) {
            logger.error("Error during pulse animation: {}", e.getMessage(), e);
        } finally {
            runningAnimations.remove(animationKey);
        }
    }

    /**
     * Run a breathing/fade animation on a specific MQTT device.
     */
    @Async
    public void breatheDevice(String friendlyName, int cycles, long cycleDurationMs) {
        String animationKey = "mqtt-breathe-" + friendlyName;
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);

        logger.info("Starting breathe animation on MQTT device {} for {} cycles", friendlyName, cycles);

        try {
            int steps = 20;
            long stepDelay = cycleDurationMs / (steps * 2);

            for (int cycle = 0; cycle < cycles && running.get(); cycle++) {
                // Fade up
                for (int i = 0; i <= steps && running.get(); i++) {
                    int brightness = (i * 255) / steps;  // MQTT uses 0-255 for brightness
                    mqttDeviceService.setBrightness(friendlyName, brightness);
                    Thread.sleep(stepDelay);
                }
                // Fade down
                for (int i = steps; i >= 0 && running.get(); i--) {
                    int brightness = (i * 255) / steps;
                    mqttDeviceService.setBrightness(friendlyName, brightness);
                    Thread.sleep(stepDelay);
                }
            }
            // Leave at full brightness
            mqttDeviceService.setBrightness(friendlyName, 255);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Breathe animation interrupted");
        } catch (Exception e) {
            logger.error("Error during breathe animation: {}", e.getMessage(), e);
        } finally {
            runningAnimations.remove(animationKey);
        }
    }

    /**
     * Run a sequential on/off pattern across all MQTT devices.
     */
    @Async
    public void sequentialDevices(int cycles, long delayMs) {
        String animationKey = "mqtt-sequential";
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);

        logger.info("Starting sequential animation on MQTT devices for {} cycles", cycles);

        try {
            List<MqttDeviceDto> devices = mqttDeviceService.getAllDevices();
            
            if (devices.isEmpty()) {
                logger.warn("No MQTT devices available for sequential animation");
                return;
            }

            for (int cycle = 0; cycle < cycles && running.get(); cycle++) {
                // Turn on devices sequentially
                for (MqttDeviceDto device : devices) {
                    if (!running.get()) break;
                    mqttDeviceService.turnOn(device.getFriendlyName());
                    Thread.sleep(delayMs);
                }
                // Turn off devices sequentially
                for (MqttDeviceDto device : devices) {
                    if (!running.get()) break;
                    mqttDeviceService.turnOff(device.getFriendlyName());
                    Thread.sleep(delayMs);
                }
            }
            // Turn all devices on at the end
            for (MqttDeviceDto device : devices) {
                mqttDeviceService.turnOn(device.getFriendlyName());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Sequential animation interrupted");
        } catch (Exception e) {
            logger.error("Error during sequential animation: {}", e.getMessage(), e);
        } finally {
            runningAnimations.remove(animationKey);
        }
    }

    /**
     * Run a random blink pattern across all MQTT devices.
     */
    @Async
    public void randomBlink(int durationSeconds) {
        String animationKey = "mqtt-random-blink";
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);

        logger.info("Starting random blink animation on MQTT devices for {} seconds", durationSeconds);

        try {
            List<MqttDeviceDto> devices = mqttDeviceService.getAllDevices();
            
            if (devices.isEmpty()) {
                logger.warn("No MQTT devices available for random blink animation");
                return;
            }

            Random random = new Random();
            long endTime = System.currentTimeMillis() + (durationSeconds * 1000L);

            while (System.currentTimeMillis() < endTime && running.get()) {
                // Pick a random device
                MqttDeviceDto randomDevice = devices.get(random.nextInt(devices.size()));
                
                // Toggle it
                mqttDeviceService.toggle(randomDevice.getFriendlyName());
                
                Thread.sleep(100 + random.nextInt(400));
            }
            // Turn all devices on at the end
            for (MqttDeviceDto device : devices) {
                mqttDeviceService.turnOn(device.getFriendlyName());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Random blink animation interrupted");
        } catch (Exception e) {
            logger.error("Error during random blink animation: {}", e.getMessage(), e);
        } finally {
            runningAnimations.remove(animationKey);
        }
    }

    /**
     * Run a color temperature sweep animation.
     */
    @Async
    public void colorTempSweep(String friendlyName, int cycles, int minTemp, int maxTemp, long cycleDurationMs) {
        String animationKey = "mqtt-colortemp-" + friendlyName;
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);

        logger.info("Starting color temp sweep animation on MQTT device {} for {} cycles", friendlyName, cycles);

        try {
            int steps = 20;
            long stepDelay = cycleDurationMs / (steps * 2);

            for (int cycle = 0; cycle < cycles && running.get(); cycle++) {
                // Warm to cool
                for (int i = 0; i <= steps && running.get(); i++) {
                    int temp = minTemp + ((maxTemp - minTemp) * i) / steps;
                    mqttDeviceService.setColorTemp(friendlyName, temp);
                    Thread.sleep(stepDelay);
                }
                // Cool to warm
                for (int i = steps; i >= 0 && running.get(); i--) {
                    int temp = minTemp + ((maxTemp - minTemp) * i) / steps;
                    mqttDeviceService.setColorTemp(friendlyName, temp);
                    Thread.sleep(stepDelay);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Color temp sweep animation interrupted");
        } catch (Exception e) {
            logger.error("Error during color temp sweep animation: {}", e.getMessage(), e);
        } finally {
            runningAnimations.remove(animationKey);
        }
    }

    /**
     * Run a wave pattern across all MQTT devices.
     */
    @Async
    public void wavePattern(int cycles, long delayMs) {
        String animationKey = "mqtt-wave";
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);

        logger.info("Starting wave animation on MQTT devices for {} cycles", cycles);

        try {
            List<MqttDeviceDto> devices = mqttDeviceService.getAllDevices();
            
            if (devices.isEmpty()) {
                logger.warn("No MQTT devices available for wave animation");
                return;
            }

            for (int cycle = 0; cycle < cycles && running.get(); cycle++) {
                // Wave forward
                for (int i = 0; i < devices.size() && running.get(); i++) {
                    for (int j = 0; j < devices.size(); j++) {
                        if (!running.get()) break;
                        
                        String friendlyName = devices.get(j).getFriendlyName();
                        
                        // Calculate brightness based on distance from wave position
                        int distance = Math.abs(i - j);
                        int brightness = Math.max(0, 255 - (distance * 60));
                        
                        if (brightness > 0) {
                            mqttDeviceService.setBrightness(friendlyName, brightness);
                        } else {
                            mqttDeviceService.turnOff(friendlyName);
                        }
                    }
                    Thread.sleep(delayMs);
                }
            }
            // Turn all devices on at full brightness at the end
            for (MqttDeviceDto device : devices) {
                mqttDeviceService.setBrightness(device.getFriendlyName(), 255);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Wave animation interrupted");
        } catch (Exception e) {
            logger.error("Error during wave animation: {}", e.getMessage(), e);
        } finally {
            runningAnimations.remove(animationKey);
        }
    }

    /**
     * Stop a specific animation.
     */
    public boolean stopAnimation(String animationKey) {
        AtomicBoolean running = runningAnimations.get(animationKey);
        if (running != null) {
            running.set(false);
            logger.info("Stopping MQTT animation: {}", animationKey);
            return true;
        }
        return false;
    }

    /**
     * Stop all running animations.
     */
    public void stopAllAnimations() {
        logger.info("Stopping all MQTT animations");
        runningAnimations.values().forEach(running -> running.set(false));
        runningAnimations.clear();
    }

    /**
     * Get list of currently running animations.
     */
    public Set<String> getRunningAnimations() {
        return new HashSet<>(runningAnimations.keySet());
    }

    /**
     * Check if any animation is currently running.
     */
    public boolean isAnyAnimationRunning() {
        return !runningAnimations.isEmpty();
    }
}
