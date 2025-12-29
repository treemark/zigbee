package com.appliedvillainy.hue.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Philips Hue Bridge discovery response from https://discovery.meethue.com
 */
public class BridgeDiscoveryDto {

    private String id;
    
    @JsonProperty("internalipaddress")
    private String internalIpAddress;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInternalIpAddress() {
        return internalIpAddress;
    }

    public void setInternalIpAddress(String internalIpAddress) {
        this.internalIpAddress = internalIpAddress;
    }

    @Override
    public String toString() {
        return "BridgeDiscoveryDto{" +
                "id='" + id + '\'' +
                ", internalIpAddress='" + internalIpAddress + '\'' +
                '}';
    }
}
