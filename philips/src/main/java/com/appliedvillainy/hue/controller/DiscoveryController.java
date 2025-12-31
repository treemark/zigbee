package com.appliedvillainy.hue.controller;

import com.appliedvillainy.hue.model.BridgeDiscoveryDto;
import com.appliedvillainy.hue.service.BridgeDiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for discovering Philips Hue Bridges on the network.
 */
@RestController
@RequestMapping("/api/discovery")
@Tag(name = "Discovery", description = "Discover Philips Hue Bridge")
@org.springframework.boot.autoconfigure.condition.ConditionalOnBean(io.github.zeroone3010.yahueapi.v2.Hue.class)
public class DiscoveryController {

    private final BridgeDiscoveryService bridgeDiscoveryService;

    @Autowired
    public DiscoveryController(BridgeDiscoveryService bridgeDiscoveryService) {
        this.bridgeDiscoveryService = bridgeDiscoveryService;
    }

    /**
     * Discover all Philips Hue Bridges on the local network.
     *
     * @return List of discovered bridges
     */
    @GetMapping("/bridges")
    @Operation(summary = "Discover all Hue Bridges", 
               description = "Queries https://discovery.meethue.com to find all Philips Hue Bridges on the local network")
    public ResponseEntity<List<BridgeDiscoveryDto>> discoverBridges() {
        List<BridgeDiscoveryDto> bridges = bridgeDiscoveryService.discoverAllBridges();
        
        if (bridges.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(bridges);
    }

    /**
     * Discover the first available Philips Hue Bridge.
     *
     * @return IP address of the first discovered bridge
     */
    @GetMapping("/bridge/ip")
    @Operation(summary = "Discover first Hue Bridge IP", 
               description = "Returns the IP address of the first discovered Philips Hue Bridge")
    public ResponseEntity<Map<String, String>> discoverBridgeIp() {
        String ip = bridgeDiscoveryService.discoverBridgeIp();
        
        if (ip == null || ip.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(Map.of("ip", ip));
    }
}
