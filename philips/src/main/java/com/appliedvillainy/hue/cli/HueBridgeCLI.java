package com.appliedvillainy.hue.cli;

import com.appliedvillainy.hue.model.MqttDeviceDto;
import com.appliedvillainy.hue.service.MqttAnimationService;
import com.appliedvillainy.hue.service.MqttDeviceService;
import com.appliedvillainy.hue.service.WiFiProvisioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Command-line interface for HueBridge operations.
 * Provides interactive commands for device management, animations, and provisioning.
 */
@Component
@ConditionalOnProperty(name = "cli.enabled", havingValue = "true")
public class HueBridgeCLI implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(HueBridgeCLI.class);

    private final MqttDeviceService mqttDeviceService;
    private final MqttAnimationService mqttAnimationService;
    private final WiFiProvisioningService wifiProvisioningService;
    private final com.appliedvillainy.hue.service.OpenBekenAnimationService openBekenAnimationService;

    public HueBridgeCLI(MqttDeviceService mqttDeviceService,
                        MqttAnimationService mqttAnimationService,
                        @Autowired(required = false) WiFiProvisioningService wifiProvisioningService,
                        @Autowired(required = false) com.appliedvillainy.hue.service.OpenBekenAnimationService openBekenAnimationService) {
        this.mqttDeviceService = mqttDeviceService;
        this.mqttAnimationService = mqttAnimationService;
        this.wifiProvisioningService = wifiProvisioningService;
        this.openBekenAnimationService = openBekenAnimationService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Give services time to initialize
        Thread.sleep(2000);
        
        // If command-line arguments provided, run in non-interactive mode
        if (args.length > 0) {
            runNonInteractiveMode(args);
            System.exit(0);
            return;
        }
        
        // Otherwise, run in interactive mode
        runInteractiveMode();
    }

    /**
     * Run a single command from command-line arguments and exit
     */
    private void runNonInteractiveMode(String[] args) {
        try {
            executeCommand(args);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            logger.error("Command execution error", e);
            System.exit(1);
        }
    }

    /**
     * Run interactive prompt mode
     */
    private void runInteractiveMode() {
        printBanner();
        
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.print("\nhuebridge> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+");

            try {
                String result = executeCommand(parts);
                if ("EXIT".equals(result)) {
                    running = false;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                logger.error("Command execution error", e);
            }
        }

        scanner.close();
        System.exit(0);
    }

    /**
     * Execute a command from the provided arguments
     * @param parts Command and arguments
     * @return "EXIT" if should exit, null otherwise
     */
    private String executeCommand(String[] parts) throws Exception {
        if (parts.length == 0) {
            return null;
        }

        String command = parts[0].toLowerCase();

        switch (command) {
                    case "help":
                    case "?":
                        printHelp();
                        break;
                    
                    case "list":
                    case "ls":
                        listDevices();
                        break;
                    
                    case "info":
                        if (parts.length < 2) {
                            System.out.println("Usage: info <device-name>");
                        } else {
                            String deviceName = String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
                            showDeviceInfo(deviceName);
                        }
                        break;
                    
                    case "on":
                        if (parts.length < 2) {
                            System.out.println("Usage: on <device-name> [brightness]");
                        } else {
                            String deviceName = parts.length == 3 ? parts[1] : 
                                String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
                            int brightness = parts.length == 3 ? Integer.parseInt(parts[2]) : 255;
                            turnOn(deviceName, brightness);
                        }
                        break;
                    
                    case "off":
                        if (parts.length < 2) {
                            System.out.println("Usage: off <device-name>");
                        } else {
                            String deviceName = String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
                            turnOff(deviceName);
                        }
                        break;
                    
                    case "brightness":
                    case "bri":
                        if (parts.length < 3) {
                            System.out.println("Usage: brightness <device-name> <0-255>");
                        } else {
                            String deviceName = String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length - 1));
                            int brightness = Integer.parseInt(parts[parts.length - 1]);
                            setBrightness(deviceName, brightness);
                        }
                        break;
                    
                    case "pulse":
                        if (parts.length < 2) {
                            System.out.println("Usage: pulse <device-name> [cycles] [interval-ms]");
                        } else {
                            String deviceName = parts[1];
                            int cycles = parts.length >= 3 ? Integer.parseInt(parts[2]) : 5;
                            long interval = parts.length >= 4 ? Long.parseLong(parts[3]) : 500;
                            runPulseAnimation(deviceName, cycles, interval);
                        }
                        break;
                    
                    case "rainbow":
                        if (parts.length < 2) {
                            System.out.println("Usage: rainbow <device-id> [cycles] [hue-step] [delay-ms]");
                            System.out.println("Example: rainbow obk17811957 3 10 30");
                        } else {
                            String deviceId = parts[1];
                            int cycles = parts.length >= 3 ? Integer.parseInt(parts[2]) : 3;
                            int hueStep = parts.length >= 4 ? Integer.parseInt(parts[3]) : 10;
                            int delayMs = parts.length >= 5 ? Integer.parseInt(parts[4]) : 30;
                            runRainbowColorRotation(deviceId, cycles, hueStep, delayMs);
                        }
                        break;
                    
                    case "breathe":
                        if (parts.length < 2) {
                            System.out.println("Usage: breathe <device-name> [cycles] [duration-ms]");
                        } else {
                            String deviceName = parts[1];
                            int cycles = parts.length >= 3 ? Integer.parseInt(parts[2]) : 3;
                            long duration = parts.length >= 4 ? Long.parseLong(parts[3]) : 4000;
                            runBreatheAnimation(deviceName, cycles, duration);
                        }
                        break;
                    
                    case "stop":
                        if (parts.length < 2) {
                            mqttAnimationService.stopAllAnimations();
                            System.out.println("✓ Stopped all animations");
                        } else {
                            String deviceName = String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
                            mqttAnimationService.stopAnimation(deviceName);
                            System.out.println("✓ Stopped animation for: " + deviceName);
                        }
                        break;
                    
                    case "animations":
                        listRunningAnimations();
                        break;
                    
                    case "provision":
                        if (parts.length == 2) {
                            // Provision specific SSID
                            provisionSpecificDevice(parts[1]);
                        } else {
                            // Scan and provision
                            provisionDevices();
                        }
                        break;
                    
                    case "provisioned":
                        listProvisionedDevices();
                        break;
                    
                    case "discover":
                        discoverDevices();
                        break;
                    
                    case "status":
                        showStatus();
                        break;
                    
                    case "obk":
                    case "openbeken":
                        if (parts.length < 3) {
                            System.out.println("Usage: obk <device-id> <on|off|toggle|dimmer> [value]");
                            System.out.println("Examples:");
                            System.out.println("  obk obk17811957 on           - Turn device on");
                            System.out.println("  obk obk17811957 off          - Turn device off");
                            System.out.println("  obk obk17811957 toggle       - Toggle device");
                            System.out.println("  obk obk17811957 dimmer 50    - Set brightness to 50%");
                        } else {
                            controlOpenBeken(parts);
                        }
                        break;
                    
                case "exit":
                case "quit":
                case "q":
                    System.out.println("Goodbye!");
                    return "EXIT";
                
                default:
                    System.out.println("Unknown command: " + command);
                    System.out.println("Type 'help' for available commands");
        }
        
        return null;
    }

    private void printBanner() {
        System.out.println("\n" +
            "╔═══════════════════════════════════════════════════════════╗\n" +
            "║                                                           ║\n" +
            "║        HueBridge CLI - MQTT Device Controller            ║\n" +
            "║                                                           ║\n" +
            "║  Control lights, run animations, provision devices       ║\n" +
            "║  Type 'help' for available commands                      ║\n" +
            "║                                                           ║\n" +
            "╚═══════════════════════════════════════════════════════════╝\n");
    }

    private void printHelp() {
        System.out.println("\nAvailable Commands:");
        System.out.println("\n=== Device Management ===");
        System.out.println("  list, ls                    - List all MQTT devices");
        System.out.println("  info <device-name>          - Show detailed device information");
        System.out.println("  discover                    - Trigger device discovery");
        System.out.println("  status                      - Show system status");
        
        System.out.println("\n=== Device Control ===");
        System.out.println("  on <device-name> [brightness]   - Turn device on (brightness 0-255)");
        System.out.println("  off <device-name>               - Turn device off");
        System.out.println("  brightness <device-name> <val>  - Set brightness (0-255)");
        
        System.out.println("\n=== Animations ===");
        System.out.println("  pulse <device> [cycles] [interval]     - Pulse animation");
        System.out.println("  rainbow <device> [duration] [steps]    - Rainbow color cycle");
        System.out.println("  breathe <device> [cycles] [duration]   - Breathing fade effect");
        System.out.println("  stop [device-name]                     - Stop animations");
        System.out.println("  animations                              - List running animations");
        
        System.out.println("\n=== WiFi Provisioning ===");
        System.out.println("  provision                      - Scan and provision OpenBeken devices");
        System.out.println("  provision <ssid>               - Provision specific device by SSID");
        System.out.println("  provisioned                    - List provisioned devices");
        
        System.out.println("\n=== General ===");
        System.out.println("  help, ?                     - Show this help message");
        System.out.println("  exit, quit, q               - Exit CLI (interactive mode only)");
        
        System.out.println("\n=== Usage ===");
        System.out.println("  Interactive mode: Run without arguments to start interactive shell");
        System.out.println("  Non-interactive:  java -jar app.jar <command> [args...]");
        System.out.println("  Example:          java -jar app.jar list");
        System.out.println("  Example:          java -jar app.jar on \"Living Room\" 200");
        System.out.println();
    }

    private void listDevices() {
        List<MqttDeviceDto> devices = mqttDeviceService.getAllDevices();
        
        if (devices.isEmpty()) {
            System.out.println("\nNo devices found.");
            System.out.println("Try running 'discover' to search for devices.");
            return;
        }

        System.out.println("\n┌─────────────────────────────────────────────────────────────────┐");
        System.out.printf("│ %-25s │ %-10s │ %-10s │ %-10s │%n", "Device Name", "State", "Brightness", "Type");
        System.out.println("├─────────────────────────────────────────────────────────────────┤");
        
        for (MqttDeviceDto device : devices) {
            String state = device.getState() != null ? device.getState() : "Unknown";
            String brightness = device.getBrightness() != null ? device.getBrightness().toString() : "N/A";
            String type = device.getType() != null ? device.getType() : "Unknown";
            System.out.printf("│ %-25s │ %-10s │ %-10s │ %-10s │%n",
                truncate(device.getFriendlyName(), 25),
                state,
                brightness,
                truncate(type, 10));
        }
        
        System.out.println("└─────────────────────────────────────────────────────────────────┘");
        System.out.printf("\nTotal devices: %d\n", devices.size());
    }

    private void showDeviceInfo(String deviceName) {
        var device = mqttDeviceService.getDevice(deviceName);
        
        if (device.isEmpty()) {
            System.out.println("Device not found: " + deviceName);
            return;
        }

        MqttDeviceDto d = device.get();
        System.out.println("\n┌─────────────────────────────────────────────┐");
        System.out.println("│ Device Information                          │");
        System.out.println("├─────────────────────────────────────────────┤");
        System.out.printf("│ Name:         %-30s│%n", d.getFriendlyName());
        System.out.printf("│ IEEE Address: %-30s│%n", d.getIeeeAddress());
        System.out.printf("│ Type:         %-30s│%n", d.getType());
        System.out.printf("│ Model:        %-30s│%n", d.getModelId());
        System.out.printf("│ Manufacturer: %-30s│%n", d.getManufacturerName());
        System.out.printf("│ State:        %-30s│%n", d.getState());
        System.out.printf("│ Brightness:   %-30s│%n", d.getBrightness());
        System.out.printf("│ Color Temp:   %-30s│%n", d.getColorTemp());
        if (d.getColor() != null) {
            System.out.printf("│ Color (x,y):  %.3f, %.3f%17s│%n", d.getColor().getX(), d.getColor().getY(), "");
        }
        System.out.println("└─────────────────────────────────────────────┘");
    }

    private void turnOn(String deviceName, int brightness) {
        try {
            Map<String, Object> command = new HashMap<>();
            command.put("state", "ON");
            command.put("brightness", brightness);
            
            mqttDeviceService.sendCommand(deviceName, command);
            System.out.printf("✓ Turned ON: %s (brightness: %d)\n", deviceName, brightness);
        } catch (Exception e) {
            System.out.println("Failed to turn on device: " + e.getMessage());
        }
    }

    private void turnOff(String deviceName) {
        try {
            Map<String, Object> command = new HashMap<>();
            command.put("state", "OFF");
            
            mqttDeviceService.sendCommand(deviceName, command);
            System.out.println("✓ Turned OFF: " + deviceName);
        } catch (Exception e) {
            System.out.println("Failed to turn off device: " + e.getMessage());
        }
    }

    private void setBrightness(String deviceName, int brightness) {
        if (brightness < 0 || brightness > 255) {
            System.out.println("Brightness must be between 0 and 255");
            return;
        }
        
        try {
            Map<String, Object> command = new HashMap<>();
            command.put("brightness", brightness);
            
            mqttDeviceService.sendCommand(deviceName, command);
            System.out.printf("✓ Set brightness: %s = %d\n", deviceName, brightness);
        } catch (Exception e) {
            System.out.println("Failed to set brightness: " + e.getMessage());
        }
    }

    private void runPulseAnimation(String deviceName, int cycles, long interval) {
        System.out.printf("Starting pulse animation: %s (%d cycles, %dms interval)\n", 
            deviceName, cycles, interval);
        mqttAnimationService.pulseDevice(deviceName, cycles, interval);
        System.out.println("✓ Animation started");
    }

    private void runRainbowColorRotation(String deviceId, int cycles, int hueStep, int delayMs) {
        if (openBekenAnimationService == null) {
            System.out.println("✗ OpenBeken Animation Service not available");
            return;
        }
        
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║        Rainbow Color Rotation Animation via MQTT         ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.printf("Device ID: %s\n", deviceId);
        System.out.printf("Cycles: %d complete rotations\n", cycles);
        System.out.printf("Hue Step: %d° (%d steps per rotation)\n", hueStep, 360/hueStep);
        System.out.printf("Delay: %dms between updates\n", delayMs);
        System.out.printf("Total Duration: ~%.1f seconds\n", (cycles * (360.0/hueStep) * delayMs) / 1000.0);
        System.out.println("Protocol: MQTT QoS 0 (fire-and-forget)");
        System.out.println();
        System.out.println("→ Starting color rotation animation...");
        
        try {
            openBekenAnimationService.animateColorRotation(deviceId, cycles, hueStep, delayMs);
            System.out.println("✓ Rainbow animation started successfully!");
            System.out.println("✓ Animation running in background");
            System.out.println();
            System.out.println("─────────────────────────────────────────────────────────────");
            System.out.println("The bulb will cycle through:");
            System.out.println("  Red → Orange → Yellow → Green → Cyan → Blue → Magenta → Red");
            System.out.println();
            System.out.println("Use 'animations' to see running animations");
            System.out.println("Use 'stop' to stop all animations");
            System.out.println("─────────────────────────────────────────────────────────────");
        } catch (Exception e) {
            System.out.println("\n✗ Error starting animation: " + e.getMessage());
            logger.error("Rainbow animation error", e);
        }
    }

    private void runBreatheAnimation(String deviceName, int cycles, long duration) {
        System.out.printf("Starting breathe animation: %s (%d cycles, %dms duration)\n", 
            deviceName, cycles, duration);
        mqttAnimationService.breatheDevice(deviceName, cycles, duration);
        System.out.println("✓ Animation started");
    }

    private void listRunningAnimations() {
        var animations = mqttAnimationService.getRunningAnimations();
        
        if (animations.isEmpty()) {
            System.out.println("\nNo animations running");
            return;
        }

        System.out.println("\nRunning Animations:");
        animations.forEach(anim -> System.out.println("  - " + anim));
    }

    private void provisionDevices() {
        if (wifiProvisioningService == null) {
            System.out.println("\nWiFi provisioning is not enabled.");
            System.out.println("Set wifi.provisioning.enabled=true in application-cli.yml to enable.");
            return;
        }
        
        System.out.println("\nScanning for OpenBeken devices to provision...");
        System.out.println("This may take a minute...");
        System.out.println("NOTE: On macOS, scanning is restricted. Use 'provision <ssid>' instead.");
        
        try {
            wifiProvisioningService.scanForDevices();
            System.out.println("✓ Provisioning scan completed");
        } catch (Exception e) {
            System.out.println("✗ Provisioning failed: " + e.getMessage());
        }
    }

    private void provisionSpecificDevice(String ssid) {
        if (wifiProvisioningService == null) {
            System.out.println("\nWiFi provisioning is not enabled.");
            System.out.println("Set wifi.provisioning.enabled=true in application-cli.yml to enable.");
            return;
        }
        
        System.out.printf("\nProvisioning device: %s\n", ssid);
        System.out.println("Make sure you're connected to this device's WiFi network first!");
        System.out.println();
        
        try {
            boolean success = wifiProvisioningService.provisionSpecificDevice(ssid);
            if (success) {
                System.out.println("✓ Device provisioned successfully");
            } else {
                System.out.println("✗ Provisioning failed");
            }
        } catch (Exception e) {
            System.out.println("✗ Provisioning failed: " + e.getMessage());
        }
    }

    private void listProvisionedDevices() {
        if (wifiProvisioningService == null) {
            System.out.println("\nWiFi provisioning is not enabled.");
            return;
        }
        
        var provisioned = wifiProvisioningService.getProvisionedDevices();
        
        if (provisioned.isEmpty()) {
            System.out.println("\nNo devices have been provisioned yet");
            return;
        }

        System.out.println("\nProvisioned Devices:");
        provisioned.forEach(device -> System.out.println("  ✓ " + device));
    }

    private void discoverDevices() {
        System.out.println("\nTriggering device discovery...");
        mqttDeviceService.discoverDevices();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        List<MqttDeviceDto> devices = mqttDeviceService.getAllDevices();
        System.out.printf("✓ Discovery complete. Found %d device(s)\n", devices.size());
    }

    private void showStatus() {
        System.out.println("\n┌─────────────────────────────────────────────┐");
        System.out.println("│ System Status                               │");
        System.out.println("├─────────────────────────────────────────────┤");
        System.out.printf("│ MQTT Connected:    %-25s│%n", 
            mqttDeviceService.isConnected() ? "✓ Yes" : "✗ No");
        System.out.printf("│ Devices Found:     %-25d│%n", 
            mqttDeviceService.getAllDevices().size());
        System.out.printf("│ Animations Running: %-24d│%n", 
            mqttAnimationService.getRunningAnimations().size());
        int provisionedCount = wifiProvisioningService != null ? 
            wifiProvisioningService.getProvisionedDevices().size() : 0;
        System.out.printf("│ Provisioned Devices: %-23d│%n", provisionedCount);
        System.out.println("└─────────────────────────────────────────────┘");
    }

    private void controlOpenBeken(String[] parts) {
        if (openBekenAnimationService == null) {
            System.out.println("OpenBeken service not available");
            return;
        }

        String deviceId = parts[1];
        String command = parts[2].toLowerCase();
        
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║     OpenBeken Device Control via MQTT                     ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.printf("Device: %s\n", deviceId);
        System.out.printf("Command: %s\n", command);
        System.out.println("Protocol: MQTT (QoS 0)");
        System.out.println();

        try {
            switch (command) {
                case "on":
                    System.out.println("→ Publishing MQTT message:");
                    System.out.println("  Topic: cmnd/" + deviceId + "/POWER1");
                    System.out.println("  Payload: 1");
                    openBekenAnimationService.sendToDevice(deviceId, "POWER1", "1");
                    System.out.println("\n✓ MQTT message published successfully");
                    System.out.println("✓ Device should now be ON");
                    break;
                    
                case "off":
                    System.out.println("→ Publishing MQTT message:");
                    System.out.println("  Topic: cmnd/" + deviceId + "/POWER1");
                    System.out.println("  Payload: 0");
                    openBekenAnimationService.sendToDevice(deviceId, "POWER1", "0");
                    System.out.println("\n✓ MQTT message published successfully");
                    System.out.println("✓ Device should now be OFF");
                    break;
                    
                case "toggle":
                    System.out.println("→ Publishing MQTT message:");
                    System.out.println("  Topic: cmnd/" + deviceId + "/POWER1");
                    System.out.println("  Payload: 2");
                    openBekenAnimationService.sendToDevice(deviceId, "POWER1", "2");
                    System.out.println("\n✓ MQTT message published successfully");
                    System.out.println("✓ Device should now be TOGGLED");
                    break;
                    
                case "dimmer":
                    if (parts.length < 4) {
                        System.out.println("✗ Error: Dimmer command requires a value (0-100)");
                        return;
                    }
                    int brightness = Integer.parseInt(parts[3]);
                    if (brightness < 0 || brightness > 100) {
                        System.out.println("✗ Error: Brightness must be between 0 and 100");
                        return;
                    }
                    System.out.println("→ Publishing MQTT message:");
                    System.out.println("  Topic: cmnd/" + deviceId + "/Dimmer");
                    System.out.println("  Payload: " + brightness);
                    openBekenAnimationService.sendToDevice(deviceId, "Dimmer", String.valueOf(brightness));
                    System.out.println("\n✓ MQTT message published successfully");
                    System.out.printf("✓ Device brightness set to %d%%\n", brightness);
                    break;
                    
                default:
                    System.out.println("✗ Unknown command: " + command);
                    System.out.println("Available commands: on, off, toggle, dimmer");
            }
            
            System.out.println("\n─────────────────────────────────────────────────────────────");
            System.out.println("Check device web UI to verify: http://192.168.86.66/");
            System.out.println("─────────────────────────────────────────────────────────────");
            
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            logger.error("OpenBeken control error", e);
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "N/A";
        return str.length() <= maxLength ? str : str.substring(0, maxLength - 3) + "...";
    }
}
