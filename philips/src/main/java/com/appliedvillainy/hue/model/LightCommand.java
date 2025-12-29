package com.appliedvillainy.hue.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Command object for controlling a light's state.
 */
public class LightCommand {

    private Boolean on;
    
    @Min(0)
    @Max(100)
    private Double brightness;
    
    @Min(153)
    @Max(500)
    private Integer colorTemperature;
    
    private LightDto.ColorXY color;
    
    @Min(0)
    @Max(65535)
    private Integer hue;
    
    @Min(0)
    @Max(254)
    private Integer saturation;
    
    /**
     * Transition time in milliseconds
     */
    @Min(0)
    private Long transitionTime;

    public LightCommand() {
    }

    public Boolean getOn() {
        return on;
    }

    public void setOn(Boolean on) {
        this.on = on;
    }

    public Double getBrightness() {
        return brightness;
    }

    public void setBrightness(Double brightness) {
        this.brightness = brightness;
    }

    public Integer getColorTemperature() {
        return colorTemperature;
    }

    public void setColorTemperature(Integer colorTemperature) {
        this.colorTemperature = colorTemperature;
    }

    public LightDto.ColorXY getColor() {
        return color;
    }

    public void setColor(LightDto.ColorXY color) {
        this.color = color;
    }

    public Integer getHue() {
        return hue;
    }

    public void setHue(Integer hue) {
        this.hue = hue;
    }

    public Integer getSaturation() {
        return saturation;
    }

    public void setSaturation(Integer saturation) {
        this.saturation = saturation;
    }

    public Long getTransitionTime() {
        return transitionTime;
    }

    public void setTransitionTime(Long transitionTime) {
        this.transitionTime = transitionTime;
    }

    /**
     * Builder for creating LightCommand instances.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final LightCommand command = new LightCommand();

        public Builder on(boolean on) {
            command.setOn(on);
            return this;
        }

        public Builder brightness(double brightness) {
            command.setBrightness(brightness);
            return this;
        }

        public Builder colorTemperature(int colorTemperature) {
            command.setColorTemperature(colorTemperature);
            return this;
        }

        public Builder color(double x, double y) {
            command.setColor(new LightDto.ColorXY(x, y));
            return this;
        }

        public Builder hue(int hue) {
            command.setHue(hue);
            return this;
        }

        public Builder saturation(int saturation) {
            command.setSaturation(saturation);
            return this;
        }

        public Builder transitionTime(long transitionTime) {
            command.setTransitionTime(transitionTime);
            return this;
        }

        public LightCommand build() {
            return command;
        }
    }
}
