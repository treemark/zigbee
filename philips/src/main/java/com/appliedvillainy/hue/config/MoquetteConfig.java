package com.appliedvillainy.hue.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for embedded Moquette MQTT broker.
 */
@Configuration
@ConfigurationProperties(prefix = "moquette")
public class MoquetteConfig {

    private static final Logger logger = LoggerFactory.getLogger(MoquetteConfig.class);

    private boolean enabled = true;
    private int port = 1883;
    private int websocketPort = 8883;
    private boolean allowAnonymous = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            logger.info("Moquette embedded MQTT broker is enabled");
        }
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        logger.info("Moquette broker port set to: {}", port);
    }

    public int getWebsocketPort() {
        return websocketPort;
    }

    public void setWebsocketPort(int websocketPort) {
        this.websocketPort = websocketPort;
    }

    public boolean isAllowAnonymous() {
        return allowAnonymous;
    }

    public void setAllowAnonymous(boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }
}
