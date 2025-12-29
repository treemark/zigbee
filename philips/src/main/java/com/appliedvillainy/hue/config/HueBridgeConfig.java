package com.appliedvillainy.hue.config;

import com.appliedvillainy.hue.service.BridgeDiscoveryService;
import io.github.zeroone3010.yahueapi.v2.Hue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for connecting to the Philips Hue Bridge.
 * Supports autodiscovery if IP is not provided or set to "auto".
 */
@Configuration
@ConfigurationProperties(prefix = "hue.bridge")
public class HueBridgeConfig {

    private static final Logger logger = LoggerFactory.getLogger(HueBridgeConfig.class);

    private String ip;
    private String apiKey;

    @Autowired
    private BridgeDiscoveryService bridgeDiscoveryService;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Creates a Hue API client bean connected to the configured bridge.
     * If IP is not configured or set to "auto", attempts to discover the bridge automatically.
     */
    @Bean
    public Hue hueClient() {
        String bridgeIp = ip;
        
        // Attempt autodiscovery if IP is not set or is set to "auto"
        if (bridgeIp == null || bridgeIp.isEmpty() || "auto".equalsIgnoreCase(bridgeIp.trim())) {
            logger.info("Bridge IP not configured or set to 'auto', attempting autodiscovery...");
            bridgeIp = bridgeDiscoveryService.discoverBridgeIp();
            
            if (bridgeIp == null || bridgeIp.isEmpty()) {
                throw new IllegalStateException(
                    "Failed to autodiscover Hue Bridge. Please configure hue.bridge.ip manually or ensure bridge is accessible."
                );
            }
            logger.info("Successfully autodiscovered bridge at IP: {}", bridgeIp);
        } else {
            logger.info("Using configured bridge IP: {}", bridgeIp);
        }
        
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Hue Bridge API key must be configured (hue.bridge.api-key)");
        }
        
        return new Hue(bridgeIp, apiKey);
    }
}
