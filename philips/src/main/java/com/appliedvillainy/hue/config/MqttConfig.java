package com.appliedvillainy.hue.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for MQTT broker connection (Zigbee2MQTT).
 */
@Configuration
@ConfigurationProperties(prefix = "mqtt")
public class MqttConfig {

    private static final Logger logger = LoggerFactory.getLogger(MqttConfig.class);

    private String brokerUrl = "tcp://192.168.86.212:6682"; // Default from ZigbeeMqttClient
    private String baseTopic = "zigbee2mqtt";
    private String clientId = "hue-bridge-app";
    private int qos = 1;
    private boolean enabled = true;

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
        logger.info("MQTT Broker URL set to: {}", brokerUrl);
    }

    public String getBaseTopic() {
        return baseTopic;
    }

    public void setBaseTopic(String baseTopic) {
        this.baseTopic = baseTopic;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
