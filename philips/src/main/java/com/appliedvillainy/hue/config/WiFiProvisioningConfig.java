package com.appliedvillainy.hue.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for automatic WiFi provisioning of OpenBeken devices.
 */
@Configuration
@ConfigurationProperties(prefix = "wifi.provisioning")
public class WiFiProvisioningConfig {

    private static final Logger logger = LoggerFactory.getLogger(WiFiProvisioningConfig.class);

    private boolean enabled = false;
    private int scanIntervalSeconds = 60;
    private String targetSsidPattern = "OpenBeken_.*";
    
    // Target WiFi network to configure devices with
    private String wifiSsid;
    private String wifiPassword;
    
    // MQTT broker configuration for devices
    private String mqttHost = "auto";  // "auto" means use local IP
    private int mqttPort = 1883;
    private String mqttTopic = "openbeken";
    
    // OpenBeken device configuration
    private int deviceConfigTimeout = 30000;  // 30 seconds
    private String deviceApPassword = "";  // Usually no password for OpenBeken AP

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            logger.info("WiFi provisioning service is enabled");
        }
    }

    public int getScanIntervalSeconds() {
        return scanIntervalSeconds;
    }

    public void setScanIntervalSeconds(int scanIntervalSeconds) {
        this.scanIntervalSeconds = scanIntervalSeconds;
    }

    public String getTargetSsidPattern() {
        return targetSsidPattern;
    }

    public void setTargetSsidPattern(String targetSsidPattern) {
        this.targetSsidPattern = targetSsidPattern;
    }

    public String getWifiSsid() {
        return wifiSsid;
    }

    public void setWifiSsid(String wifiSsid) {
        this.wifiSsid = wifiSsid;
    }

    public String getWifiPassword() {
        return wifiPassword;
    }

    public void setWifiPassword(String wifiPassword) {
        this.wifiPassword = wifiPassword;
    }

    public String getMqttHost() {
        return mqttHost;
    }

    public void setMqttHost(String mqttHost) {
        this.mqttHost = mqttHost;
    }

    public int getMqttPort() {
        return mqttPort;
    }

    public void setMqttPort(int mqttPort) {
        this.mqttPort = mqttPort;
    }

    public String getMqttTopic() {
        return mqttTopic;
    }

    public void setMqttTopic(String mqttTopic) {
        this.mqttTopic = mqttTopic;
    }

    public int getDeviceConfigTimeout() {
        return deviceConfigTimeout;
    }

    public void setDeviceConfigTimeout(int deviceConfigTimeout) {
        this.deviceConfigTimeout = deviceConfigTimeout;
    }

    public String getDeviceApPassword() {
        return deviceApPassword;
    }

    public void setDeviceApPassword(String deviceApPassword) {
        this.deviceApPassword = deviceApPassword;
    }
}
