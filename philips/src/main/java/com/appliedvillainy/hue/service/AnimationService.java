package com.appliedvillainy.hue.service;

import com.appliedvillainy.hue.model.LightDto;
import io.github.zeroone3010.yahueapi.v2.Hue;
import io.github.zeroone3010.yahueapi.v2.Light;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service for running light animations.
 */
@Service
public class AnimationService {

    private static final Logger logger = LoggerFactory.getLogger(AnimationService.class);

    private final Hue hue;
    private final HueLightService lightService;
    
    // Track running animations
    private final Map<String, AtomicBoolean> runningAnimations = new ConcurrentHashMap<>();

    public AnimationService(Hue hue, HueLightService lightService) {
        this.hue = hue;
        this.lightService = lightService;
    }

    /**
     * Run a pulse animation on a specific light.
     */
    @Async
    public void pulseLight(UUID lightId, int cycles, long intervalMs) {
        String animationKey = "pulse-" + lightId;
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);

        logger.info("Starting pulse animation on light {} for {} cycles", lightId, cycles);

        try {
            Light light = hue.getLights().get(lightId);
            if (light == null) {
                logger.warn("Light not found: {}", lightId);
                return;
            }

            for (int i = 0; i < cycles && running.get(); i++) {
                light.turnOn();
                Thread.sleep(intervalMs);
                light.turnOff();
                Thread.sleep(intervalMs);
            }
            // Leave light on at the end
            light.turnOn();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Pulse animation interrupted");
        } finally {
            runningAnimations.remove(animationKey);
        }
    }

    /**
     * Run a breathing/fade animation on a specific light.
     */
    @Async
    public void breatheLight(UUID lightId, int cycles, long cycleDurationMs) {
        String animationKey = "breathe-" + lightId;
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);

        logger.info("Starting breathe animation on light {} for {} cycles", lightId, cycles);

        try {
            Light light = hue.getLights().get(lightId);
            if (light == null) {
                logger.warn("Light not found: {}", lightId);
                return;
            }

            int steps = 20;
            long stepDelay = cycleDurationMs / (steps * 2);

            for (int cycle = 0; cycle < cycles && running.get(); cycle++) {
                // Fade up
                for (int i = 0; i <= steps && running.get(); i++) {
                    int brightness = (i * 100) / steps;
                    light.setBrightness(brightness);
                    Thread.sleep(stepDelay);
                }
                // Fade down
                for (int i = steps; i >= 0 && running.get(); i--) {
                    int brightness = (i * 100) / steps;
                    light.setBrightness(brightness);
                    Thread.sleep(stepDelay);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Breathe animation interrupted");
        } finally {
            runningAnimations.remove(animationKey);
        }
    }

    /**
     * Run a sequential on/off pattern across all lights.
     */
    @Async
    public void sequentialLights(int cycles, long delayMs) {
        String animationKey = "sequential";
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);

        logger.info("Starting sequential animation for {} cycles", cycles);

        try {
            List<LightDto> lights = lightService.getAllLights();
            
            for (int cycle = 0; cycle < cycles && running.get(); cycle++) {
                // Turn on lights sequentially
                for (LightDto light : lights) {
                    if (!running.get()) break;
                    lightService.turnOn(light.getId());
                    Thread.sleep(delayMs);
                }
                // Turn off lights sequentially
                for (LightDto light : lights) {
                    if (!running.get()) break;
                    lightService.turnOff(light.getId());
                    Thread.sleep(delayMs);
                }
            }
            // Turn all lights on at the end
            lightService.turnOnAllLights();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Sequential animation interrupted");
        } finally {
            runningAnimations.remove(animationKey);
        }
    }

    /**
     * Run a random blink pattern across all lights.
     */
    @Async
    public void randomBlink(int durationSeconds) {
        String animationKey = "random-blink";
        AtomicBoolean running = new AtomicBoolean(true);
        runningAnimations.put(animationKey, running);

        logger.info("Starting random blink animation for {} seconds", durationSeconds);

        try {
            List<LightDto> lights = lightService.getAllLights();
            Random random = new Random();
            long endTime = System.currentTimeMillis() + (durationSeconds * 1000L);

            while (System.currentTimeMillis() < endTime && running.get()) {
                // Pick a random light
                LightDto randomLight = lights.get(random.nextInt(lights.size()));
                
                // Toggle it
                if (randomLight.isOn()) {
                    lightService.turnOff(randomLight.getId());
                } else {
                    lightService.turnOn(randomLight.getId());
                }
                
                Thread.sleep(100 + random.nextInt(400));
            }
            // Turn all lights on at the end
            lightService.turnOnAllLights();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Random blink animation interrupted");
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
            logger.info("Stopping animation: {}", animationKey);
            return true;
        }
        return false;
    }

    /**
     * Stop all running animations.
     */
    public void stopAllAnimations() {
        logger.info("Stopping all animations");
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
