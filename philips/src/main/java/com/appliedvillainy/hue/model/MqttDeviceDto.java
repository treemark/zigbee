package com.appliedvillainy.hue.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing an MQTT device (Zigbee2MQTT).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MqttDeviceDto {
    
    private String friendlyName;
    private String ieeeAddress;
    private String type;
    private boolean online;
    
    // Device state
    private String state;  // "ON" or "OFF"
    private Integer brightness;
    private Integer colorTemp;
    private ColorXY color;
    
    // Device info
    private String modelId;
    private String manufacturerName;
    private boolean supported;

    public MqttDeviceDto() {
    }

    public MqttDeviceDto(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    @JsonProperty("friendly_name")
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getIeeeAddress() {
        return ieeeAddress;
    }

    @JsonProperty("ieee_address")
    public void setIeeeAddress(String ieeeAddress) {
        this.ieeeAddress = ieeeAddress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getBrightness() {
        return brightness;
    }

    public void setBrightness(Integer brightness) {
        this.brightness = brightness;
    }

    public Integer getColorTemp() {
        return colorTemp;
    }

    @JsonProperty("color_temp")
    public void setColorTemp(Integer colorTemp) {
        this.colorTemp = colorTemp;
    }

    public ColorXY getColor() {
        return color;
    }

    public void setColor(ColorXY color) {
        this.color = color;
    }

    public String getModelId() {
        return modelId;
    }

    @JsonProperty("model_id")
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    @JsonProperty("manufacturer")
    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public boolean isOn() {
        return "ON".equalsIgnoreCase(state);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ColorXY {
        private double x;
        private double y;

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }
}
