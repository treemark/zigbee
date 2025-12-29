package com.appliedvillainy.hue.service;

import com.appliedvillainy.hue.model.BridgeDiscoveryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Service for discovering Philips Hue Bridge on the local network.
 */
@Service
public class BridgeDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(BridgeDiscoveryService.class);
    private static final String DISCOVERY_URL = "https://discovery.meethue.com";

    private final RestTemplate restTemplate;

    public BridgeDiscoveryService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Discovers the Hue Bridge IP address by querying the Philips discovery service.
     * 
     * @return The IP address of the first discovered bridge, or null if no bridge is found
     */
    public String discoverBridgeIp() {
        try {
            logger.info("Attempting to discover Hue Bridge via {}", DISCOVERY_URL);
            
            ResponseEntity<List<BridgeDiscoveryDto>> response = restTemplate.exchange(
                DISCOVERY_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BridgeDiscoveryDto>>() {}
            );

            List<BridgeDiscoveryDto> bridges = response.getBody();
            
            if (bridges != null && !bridges.isEmpty()) {
                BridgeDiscoveryDto firstBridge = bridges.get(0);
                String ip = firstBridge.getInternalIpAddress();
                logger.info("Discovered Hue Bridge: {} at IP: {}", firstBridge.getId(), ip);
                
                if (bridges.size() > 1) {
                    logger.info("Multiple bridges found ({}), using first one", bridges.size());
                }
                
                return ip;
            } else {
                logger.warn("No Hue Bridges found during discovery");
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Failed to discover Hue Bridge: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Discovers all Hue Bridges on the local network.
     * 
     * @return A list of discovered bridges, or an empty list if none found
     */
    public List<BridgeDiscoveryDto> discoverAllBridges() {
        try {
            logger.info("Attempting to discover all Hue Bridges via {}", DISCOVERY_URL);
            
            ResponseEntity<List<BridgeDiscoveryDto>> response = restTemplate.exchange(
                DISCOVERY_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BridgeDiscoveryDto>>() {}
            );

            List<BridgeDiscoveryDto> bridges = response.getBody();
            
            if (bridges != null && !bridges.isEmpty()) {
                logger.info("Discovered {} Hue Bridge(s)", bridges.size());
                bridges.forEach(bridge -> 
                    logger.info("  - Bridge ID: {} at IP: {}", bridge.getId(), bridge.getInternalIpAddress())
                );
                return bridges;
            } else {
                logger.warn("No Hue Bridges found during discovery");
                return List.of();
            }
            
        } catch (Exception e) {
            logger.error("Failed to discover Hue Bridges: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
