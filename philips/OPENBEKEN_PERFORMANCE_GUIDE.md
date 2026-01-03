# OpenBeken High-Performance Animation Guide

## Performance Comparison: HTTP vs MQTT

### For High-Speed Coordinated Animations with Multiple Devices

## TL;DR: Use MQTT with QoS 0

For your use case (large number of lights, high update rates, coordinated animations), **MQTT is significantly better** than HTTP.

## Detailed Comparison

### HTTP Direct Method
```bash
curl "http://192.168.86.66/cm?cmnd=POWER1%201"
```

**Pros:**
- ✅ Direct communication, no broker
- ✅ Immediate response feedback
- ✅ Simple for single device control

**Cons:**
- ❌ TCP connection overhead per request
- ❌ HTTP request/response cycle adds latency
- ❌ Need to send individual request to each device
- ❌ Limited connection pool (browsers/systems limit concurrent connections)
- ❌ No multicast capability
- ❌ Higher CPU overhead for many devices
- ❌ Difficult to synchronize multiple devices

**Performance:** ~50-100 updates/second per device (with connection reuse)

### MQTT Method (RECOMMENDED)
```bash
mosquitto_pub -h localhost -p 1883 -t 'cmnd/obk17811957/POWER1' -m '1'
```

**Pros:**
- ✅ Persistent connections (no reconnection overhead)
- ✅ Publish once, multiple subscribers receive (fan-out)
- ✅ QoS 0 (fire-and-forget) is extremely fast
- ✅ Much lower CPU overhead
- ✅ Natural support for group commands
- ✅ Can broadcast to all devices with one message
- ✅ Better for coordinated/synchronized animations
- ✅ Broker handles message distribution

**Cons:**
- ⚠️ Single point of failure (broker) - but embedded Moquette is reliable
- ⚠️ Slight latency added by broker (~1-5ms locally)

**Performance:** ~500-1000+ updates/second aggregate (broker dependent)

## Performance Metrics

### Single Device Update Rate
- **HTTP**: 50-100 updates/sec (limited by request/response cycle)
- **MQTT QoS 0**: 200-500 updates/sec per device
- **MQTT QoS 1**: 100-200 updates/sec per device (with acknowledgment)

### Multiple Device Broadcast (10 devices)
- **HTTP**: Need 10 separate requests = 5-10 updates/sec per device
- **MQTT Group Topic**: 1 publish reaches all = 100+ updates/sec per device

### Network Bandwidth
- **HTTP**: ~500 bytes per request (headers + connection overhead)
- **MQTT QoS 0**: ~50-100 bytes per message (minimal protocol overhead)

### CPU Usage (for 100 devices @ 10 updates/sec)
- **HTTP**: High (managing 1000 connections/sec)
- **MQTT**: Low (persistent connections, efficient pub/sub)

## Recommended Architecture for Animation

### 1. Use MQTT with QoS 0 for Animations

QoS 0 = "Fire and forget" - no acknowledgment, fastest possible delivery

```java
// Set QoS to 0 for animation commands
mqtt.publish("cmnd/group/all/POWER1", "1", 0, false);
```

### 2. Group Devices by Zone/Function

OpenBeken supports group topics. Configure devices to listen to group commands:

**Device Configuration:**
- **Client Topic**: `obk17811957` (unique per device)
- **Group Topic**: `animations` (shared by all animation devices)

This allows you to:
```bash
# Control all devices at once
mosquitto_pub -h localhost -p 1883 -t 'cmnd/animations/POWER1' -m '1'

# Control individual device
mosquitto_pub -h localhost -p 1883 -t 'cmnd/obk17811957/POWER1' -m '1'
```

### 3. Optimize Update Strategy

**Bad (Sequential HTTP):**
```bash
for device in device1 device2 device3; do
    curl "http://$device/cm?cmnd=POWER1%201"  # Slow, sequential
done
```

**Good (Parallel MQTT):**
```bash
# Single publish reaches all devices simultaneously
mosquitto_pub -h localhost -p 1883 -t 'cmnd/animations/POWER1' -m '1'
```

**Best (Batched Updates with State Tracking):**
```java
// Only send updates when state changes
if (device.getBrightness() != targetBrightness) {
    mqtt.publish("cmnd/" + device.getId() + "/Dimmer", targetBrightness, 0, false);
}
```

### 4. Use Channel-Specific Commands

OpenBeken supports various commands for different effects:

```bash
# Brightness (0-100)
mosquitto_pub -t 'cmnd/obk17811957/Dimmer' -m '50'

# Color (for RGB devices) - Hue, Saturation, Brightness
mosquitto_pub -t 'cmnd/obk17811957/HsbColor' -m '120,100,50'

# Power
mosquitto_pub -t 'cmnd/obk17811957/POWER1' -m '1'

# Transition time (for smooth fades)
mosquitto_pub -t 'cmnd/obk17811957/Speed' -m '5'
```

## Optimal Configuration

### MQTT Broker Settings (application.yml)

```yaml
moquette:
  enabled: true
  port: 1883
  websocket-port: 8883
  allow-anonymous: true
  # For high throughput, consider:
  # - Increase buffer sizes
  # - Optimize threading
```

### Device Group Configuration

Configure all animation devices with:
- **Host**: `192.168.1.5` (your broker)
- **Port**: `1883`
- **Client Topic**: `obk{DEVICE_ID}` (unique)
- **Group Topic**: `animations` (shared)
- **QoS**: 0 (fastest)

## Animation Patterns: Best Practices

### Wave Effect (Sequential Activation)
```bash
#!/bin/bash
DEVICES=("obk17811957" "obk17811958" "obk17811959")
DELAY=0.1  # 100ms between devices

for device in "${DEVICES[@]}"; do
    mosquitto_pub -h localhost -p 1883 -t "cmnd/$device/POWER1" -m '1' &
    sleep $DELAY
done
wait
```

### Synchronized Flash (All at Once)
```bash
# All devices respond simultaneously
mosquitto_pub -h localhost -p 1883 -t 'cmnd/animations/POWER1' -m 'TOGGLE'
```

### Smooth Brightness Sweep
```bash
#!/bin/bash
for brightness in {0..100..5}; do
    mosquitto_pub -h localhost -p 1883 -t 'cmnd/animations/Dimmer' -m "$brightness"
    sleep 0.05  # 20 updates/second
done
```

### Color Rotation (RGB Devices)
```bash
#!/bin/bash
for hue in {0..360..10}; do
    # Hue, Saturation, Brightness
    mosquitto_pub -h localhost -p 1883 -t 'cmnd/animations/HsbColor' -m "$hue,100,50"
    sleep 0.03  # ~33 updates/second
done
```

## Advanced: Java Service for High-Performance Animations

For maximum performance, use the embedded MQTT client directly in Java:

```java
@Service
public class OpenBekenAnimationService {
    
    private final MqttClient mqttClient;
    private static final int QOS_0 = 0; // Fire and forget
    
    public void broadcastToGroup(String group, String command, String value) {
        String topic = "cmnd/" + group + "/" + command;
        mqttClient.publish(topic, value.getBytes(), QOS_0, false);
    }
    
    public void animatePulse(String group, int cycles, int intervalMs) {
        for (int i = 0; i < cycles; i++) {
            broadcastToGroup(group, "POWER1", "ON");
            Thread.sleep(intervalMs / 2);
            broadcastToGroup(group, "POWER1", "OFF");
            Thread.sleep(intervalMs / 2);
        }
    }
    
    public void animateBrightnessSweep(String group, int steps, int delayMs) {
        for (int brightness = 0; brightness <= 100; brightness += steps) {
            broadcastToGroup(group, "Dimmer", String.valueOf(brightness));
            Thread.sleep(delayMs);
        }
    }
    
    public void animateWave(List<String> devices, int delayMs) {
        for (String device : devices) {
            String topic = "cmnd/" + device + "/POWER1";
            mqttClient.publish(topic, "ON".getBytes(), QOS_0, false);
            Thread.sleep(delayMs);
        }
    }
}
```

## Network Optimization

### 1. Use Wired Connections When Possible
- Reduce WiFi congestion
- Lower latency and jitter
- More reliable for time-critical animations

### 2. Optimize WiFi
- Use 5GHz band if devices support it
- Minimize interference
- Strong signal strength for all devices

### 3. Network Segmentation
- Put animation devices on dedicated VLAN/subnet
- Reduce broadcast traffic
- QoS prioritization for MQTT traffic

### 4. Broker Tuning
Consider upgrading to Mosquitto if you need:
- Even higher throughput
- Better resource management
- Advanced features (bridging, persistence)

```bash
# Install Mosquitto (optional)
brew install mosquitto

# Run with optimized settings
mosquitto -c mosquitto.conf
```

## Testing Performance

### Measure Update Rate
```bash
#!/bin/bash
# Send 1000 updates and measure time
START=$(date +%s%N)
for i in {1..1000}; do
    mosquitto_pub -h localhost -p 1883 -t 'cmnd/animations/POWER1' -m 'TOGGLE'
done
END=$(date +%s%N)

DURATION=$(( (END - START) / 1000000 ))  # Convert to milliseconds
RATE=$(( 1000 * 1000 / DURATION ))

echo "Duration: ${DURATION}ms"
echo "Rate: ${RATE} updates/second"
```

### Monitor MQTT Broker
```bash
# Subscribe to all topics and count messages
mosquitto_sub -h localhost -p 1883 -t '#' -v | pv -l > /dev/null
```

## Recommended Update Rates

Based on typical device capabilities and network conditions:

| Scenario | Recommended Rate | Protocol |
|----------|-----------------|----------|
| Smooth brightness fades | 20-30 Hz | MQTT QoS 0 |
| Fast blinking/pulse | 10-20 Hz | MQTT QoS 0 |
| Color transitions | 30-60 Hz | MQTT QoS 0 |
| Synchronized flash | As fast as needed | MQTT QoS 0 |
| Sequential wave | Device-dependent | MQTT QoS 0 |
| Status monitoring | 1-5 Hz | MQTT QoS 1 |

**Note:** OpenBeken devices typically handle 20-50 updates/second reliably. Beyond that, devices may drop messages or become unresponsive.

## Summary

### For Your Use Case: Large-Scale Coordinated Animations

✅ **Use MQTT with:**
- QoS 0 (fastest, no acknowledgments)
- Group topics for synchronized effects
- Persistent connections via embedded broker
- Batched updates (only send when state changes)

✅ **Expected Performance:**
- 20-50 updates/second per device (reliable)
- Broadcast to 100+ devices simultaneously
- Sub-10ms latency on local network

❌ **Avoid HTTP for:**
- Multiple devices
- High update rates
- Coordinated/synchronized effects
- Anything > 10 updates/second aggregate

## Next Steps

1. Configure all devices with the same group topic: `animations`
2. Test with a small group (3-5 devices) first
3. Measure actual performance on your network
4. Scale up gradually while monitoring
5. Consider implementing Java-based animation service for complex patterns

See `OpenBekenAnimationService.java` (to be created) for a production-ready implementation.
