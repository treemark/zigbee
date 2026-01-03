package com.appliedvillainy.hue.service;

import com.appliedvillainy.hue.config.MqttConfig;
import com.appliedvillainy.hue.model.MqttDeviceDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for discovering and controlling MQTT devices via Zigbee2MQTT.
 */
@Service
@org.springframework.context.annotation.DependsOn("moquetteBrokerService")
public class MqttDeviceService implements MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(MqttDeviceService.class);

    private final MqttConfig mqttConfig;
    private final ObjectMapper objectMapper;
    
    private MqttClient mqttClient;
    private final Map<String, MqttDeviceDto> discoveredDevices = new ConcurrentHashMap<>();
    private boolean connected = false;

    public MqttDeviceService(MqttConfig mqttConfig) {
        this.mqttConfig = mqttConfig;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        if (!mqttConfig.isEnabled()) {
            logger.info("MQTT is disabled, skipping initialization");
            return;
        }
        
        try {
            connect();
            discoverDevices();
        } catch (Exception e) {
            logger.error("Failed to initialize MQTT service: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void cleanup() {
        disconnect();
    }

    /**
     * Connect to the MQTT broker.
     */
    public void connect() throws MqttException {
        if (connected) {
            logger.info("Already connected to MQTT broker");
            return;
        }

        logger.info("Connecting to MQTT broker at {}", mqttConfig.getBrokerUrl());
        
        mqttClient = new MqttClient(
            mqttConfig.getBrokerUrl(),
            mqttConfig.getClientId() + "_" + UUID.randomUUID().toString().substring(0, 8)
        );
        
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);
        
        mqttClient.setCallback(this);
        mqttClient.connect(options);
        
        // Subscribe to bridge topics for device discovery
        mqttClient.subscribe(mqttConfig.getBaseTopic() + "/bridge/devices", mqttConfig.getQos());
        mqttClient.subscribe(mqttConfig.getBaseTopic() + "/bridge/event", mqttConfig.getQos());
        mqttClient.subscribe(mqttConfig.getBaseTopic() + "/#", mqttConfig.getQos());
        
        connected = true;
        logger.info("Successfully connected to MQTT broker");
    }

    /**
     * Disconnect from the MQTT broker.
     */
    public void disconnect() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                logger.info("Disconnected from MQTT broker");
            } catch (MqttException e) {
                logger.error("Error disconnecting from MQTT broker: {}", e.getMessage());
            }
        }
        connected = false;
    }

    /**
     * Discover all devices from Zigbee2MQTT.
     */
    public void discoverDevices() {
        if (!connected) {
            logger.warn("Not connected to MQTT broker, cannot discover devices");
            return;
        }

        try {
            // Request device list from bridge
            logger.info("Requesting device list from Zigbee2MQTT bridge");
            MqttMessage message = new MqttMessage("".getBytes());
            message.setQos(mqttConfig.getQos());
            mqttClient.publish(mqttConfig.getBaseTopic() + "/bridge/request/devices", message);
        } catch (MqttException e) {
            logger.error("Failed to request device list: {}", e.getMessage(), e);
        }
    }

    /**
     * Get all discovered devices.
     */
    public List<MqttDeviceDto> getAllDevices() {
        return new ArrayList<>(discoveredDevices.values());
    }

    /**
     * Get a specific device by friendly name.
     */
    public Optional<MqttDeviceDto> getDevice(String friendlyName) {
        return Optional.ofNullable(discoveredDevices.get(friendlyName));
    }

    /**
     * Send a command to a specific device.
     */
    public void sendCommand(String friendlyName, Map<String, Object> command) throws MqttException {
        if (!connected) {
            throw new IllegalStateException("Not connected to MQTT broker");
        }

        try {
            String commandJson = objectMapper.writeValueAsString(command);
            String topic = mqttConfig.getBaseTopic() + "/" + friendlyName + "/set";
            
            MqttMessage message = new MqttMessage(commandJson.getBytes());
            message.setQos(mqttConfig.getQos());
            
            logger.info("Sending command to {}: {}", friendlyName, commandJson);
            mqttClient.publish(topic, message);
        } catch (Exception e) {
            logger.error("Failed to send command to device {}: {}", friendlyName, e.getMessage(), e);
            throw new MqttException(e);
        }
    }

    /**
     * Turn on a device.
     */
    public void turnOn(String friendlyName) throws MqttException {
        Map<String, Object> command = new HashMap<>();
        command.put("state", "ON");
        sendCommand(friendlyName, command);
    }

    /**
     * Turn off a device.
     */
    public void turnOff(String friendlyName) throws MqttException {
        Map<String, Object> command = new HashMap<>();
        command.put("state", "OFF");
        sendCommand(friendlyName, command);
    }

    /**
     * Toggle a device.
     */
    public void toggle(String friendlyName) throws MqttException {
        Map<String, Object> command = new HashMap<>();
        command.put("state", "TOGGLE");
        sendCommand(friendlyName, command);
    }

    /**
     * Set brightness of a device (0-255).
     */
    public void setBrightness(String friendlyName, int brightness) throws MqttException {
        Map<String, Object> command = new HashMap<>();
        command.put("state", "ON");
        command.put("brightness", Math.max(0, Math.min(255, brightness)));
        sendCommand(friendlyName, command);
    }

    /**
     * Set color temperature of a device.
     */
    public void setColorTemp(String friendlyName, int colorTemp) throws MqttException {
        Map<String, Object> command = new HashMap<>();
        command.put("state", "ON");
        command.put("color_temp", colorTemp);
        sendCommand(friendlyName, command);
    }

    // MqttCallback implementations

    @Override
    public void connectionLost(Throwable cause) {
        logger.error("Connection to MQTT broker lost: {}", cause.getMessage());
        connected = false;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        logger.debug("Message arrived on topic {}: {}", topic, payload);

        try {
            // Handle bridge device list response
            if (topic.equals(mqttConfig.getBaseTopic() + "/bridge/devices")) {
                handleDeviceList(payload);
            }
            // Handle individual device state updates
            else if (topic.startsWith(mqttConfig.getBaseTopic() + "/") && 
                     !topic.contains("/bridge/") && 
                     !topic.endsWith("/set") &&
                     !topic.endsWith("/get")) {
                handleDeviceState(topic, payload);
            }
        } catch (Exception e) {
            logger.error("Error processing MQTT message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.debug("Message delivery complete");
    }

    /**
     * Handle device list response from bridge.
     */
    private void handleDeviceList(String payload) {
        try {
            List<Map<String, Object>> devices = objectMapper.readValue(
                payload, 
                new TypeReference<List<Map<String, Object>>>() {}
            );

            logger.info("Received device list with {} devices", devices.size());

            for (Map<String, Object> deviceMap : devices) {
                String friendlyName = (String) deviceMap.get("friendly_name");
                String type = (String) deviceMap.get("type");
                
                // Skip coordinator and routers
                if ("Coordinator".equals(type) || "Router".equals(type)) {
                    continue;
                }

                MqttDeviceDto device = new MqttDeviceDto(friendlyName);
                device.setIeeeAddress((String) deviceMap.get("ieee_address"));
                device.setType(type);
                device.setModelId((String) deviceMap.get("model_id"));
                device.setManufacturerName((String) deviceMap.get("manufacturer"));
                device.setSupported((Boolean) deviceMap.getOrDefault("supported", true));
                
                discoveredDevices.put(friendlyName, device);
                logger.info("Discovered device: {} ({})", friendlyName, device.getModelId());
            }
        } catch (Exception e) {
            logger.error("Failed to parse device list: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle device state update.
     */
    private void handleDeviceState(String topic, String payload) {
        try {
            String friendlyName = extractFriendlyName(topic);
            if (friendlyName == null) {
                return;
            }

            Map<String, Object> stateMap = objectMapper.readValue(
                payload,
                new TypeReference<Map<String, Object>>() {}
            );

            MqttDeviceDto device = discoveredDevices.computeIfAbsent(
                friendlyName,
                MqttDeviceDto::new
            );

            // Update device state
            if (stateMap.containsKey("state")) {
                device.setState((String) stateMap.get("state"));
            }
            if (stateMap.containsKey("brightness")) {
                device.setBrightness((Integer) stateMap.get("brightness"));
            }
            if (stateMap.containsKey("color_temp")) {
                device.setColorTemp((Integer) stateMap.get("color_temp"));
            }

            logger.debug("Updated state for device {}: {}", friendlyName, payload);
        } catch (Exception e) {
            logger.error("Failed to parse device state: {}", e.getMessage(), e);
        }
    }

    /**
     * Extract friendly name from topic.
     */
    private String extractFriendlyName(String topic) {
        String prefix = mqttConfig.getBaseTopic() + "/";
        if (!topic.startsWith(prefix)) {
            return null;
        }
        
        String remaining = topic.substring(prefix.length());
        int slashIndex = remaining.indexOf('/');
        
        return slashIndex > 0 ? remaining.substring(0, slashIndex) : remaining;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Get the MQTT client for use by other services.
     */
    public MqttClient getMqttClient() {
        return mqttClient;
    }
}
