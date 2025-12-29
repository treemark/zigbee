package com.appliedvillainy.hue.model;

import java.util.UUID;

/**
 * Data Transfer Object representing a Hue light/bulb.
 */
public class LightDto {
    
    private UUID id;
    private String name;
    private boolean on;
    private Double brightness;
    private Double colorTemperature;
    private ColorXY color;
    private String type;
    private boolean reachable;

    public LightDto() {
    }

    public LightDto(UUID id, String name, boolean on) {
        this.id = id;
        this.name = name;
        this.on = on;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public Double getBrightness() {
        return brightness;
    }

    public void setBrightness(Double brightness) {
        this.brightness = brightness;
    }

    public Double getColorTemperature() {
        return colorTemperature;
    }

    public void setColorTemperature(Double colorTemperature) {
        this.colorTemperature = colorTemperature;
    }

    public ColorXY getColor() {
        return color;
    }

    public void setColor(ColorXY color) {
        this.color = color;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    /**
     * Represents CIE xy color coordinates.
     */
    public static class ColorXY {
        private double x;
        private double y;

        public ColorXY() {
        }

        public ColorXY(double x, double y) {
            this.x = x;
            this.y = y;
        }

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
