# MQTT Device Discovery and Animation

This document describes how to discover and animate MQTT devices (Zigbee2MQTT) from the HueBridgeApplication.

## Overview

The application now supports discovering and controlling Zigbee devices via MQTT through Zigbee2MQTT. This allows you to:
- Automatically discover MQTT devices connected to your Zigbee2MQTT broker
- Control individual devices (on/off, brightness, color temperature)
- Run various light animations on MQTT devices
- Integrate MQTT devices alongside Philips Hue lights

## Configuration

Configure MQTT settings in `src/main/resources/application.yml`:

```yaml
mqtt:
  enabled: true                            # Enable/disable MQTT functionality
  broker-url: tcp://192.168.86.212:6682  # Your Zigbee2MQTT broker URL
  base-topic: zigbee2mqtt                 # MQTT base topic
  client-id: hue-bridge-app               # MQTT client ID
  qos: 1                                  # Quality of service level
```

## API Endpoints

### Device Discovery and Control

#### Get All MQTT Devices
```bash
GET /api/mqtt/devices
```

Example response:
```json
[
  {
    "friendlyName": "Living Room Light",
    "ieeeAddress": "0x00158d0001234567",
    "type": "EndDevice",
    "state": "ON",
    "brightness": 200,
    "modelId": "TRADFRI bulb E27 WS opal 980lm",
    "manufacturerName": "IKEA"
  }
]
```

#### Get Specific Device
```bash
GET /api/mqtt/devices/{friendlyName}
```

#### Discover Devices
```bash
POST /api/mqtt/devices/discover
```

#### Turn On Device
```bash
POST /api/mqtt/devices/{friendlyName}/on
```

#### Turn Off Device
```bash
POST /api/mqtt/devices/{friendlyName}/off
```

#### Toggle Device
```bash
POST /api/mqtt/devices/{friendlyName}/toggle
```

#### Set Brightness (0-255)
```bash
POST /api/mqtt/devices/{friendlyName}/brightness?brightness=200
```

#### Set Color Temperature
```bash
POST /api/mqtt/devices/{friendlyName}/color-temp?colorTemp=350
```

#### Send Custom Command
```bash
POST /api/mqtt/devices/{friendlyName}/command
Content-Type: application/json

{
  "state": "ON",
  "brightness": 150,
  "color_temp": 300
}
```

#### Get MQTT Connection Status
```bash
GET /api/mqtt/devices/status
```

### Animation Endpoints

#### Pulse Animation
Blinks a device on and off repeatedly.
```bash
POST /api/mqtt/animations/pulse/{friendlyName}?cycles=10&intervalMs=500
```

#### Breathe Animation
Smoothly fades brightness up and down.
```bash
POST /api/mqtt/animations/breathe/{friendlyName}?cycles=5&cycleDurationMs=3000
```

#### Sequential Animation
Turns devices on and off in sequence.
```bash
POST /api/mqtt/animations/sequential?cycles=3&delayMs=500
```

#### Random Blink Animation
Randomly blinks devices for a duration.
```bash
POST /api/mqtt/animations/random-blink?durationSeconds=30
```

#### Color Temperature Sweep
Sweeps through color temperatures (warm to cool and back).
```bash
POST /api/mqtt/animations/color-temp-sweep/{friendlyName}?cycles=5&minTemp=153&maxTemp=500&cycleDurationMs=4000
```

#### Wave Pattern
Creates a wave effect across all devices with brightness variations.
```bash
POST /api/mqtt/animations/wave?cycles=3&delayMs=300
```

#### Stop Specific Animation
```bash
POST /api/mqtt/animations/stop/{animationKey}
```

Animation keys follow the format: `mqtt-{type}-{friendlyName}` for device-specific animations or `mqtt-{type}` for group animations.

Examples:
- `mqtt-pulse-Living Room Light`
- `mqtt-breathe-Bedroom Light`
- `mqtt-sequential`
- `mqtt-wave`

#### Stop All Animations
```bash
POST /api/mqtt/animations/stop-all
```

#### Get Running Animations
```bash
GET /api/mqtt/animations/running
```

#### Check Animation Status
```bash
GET /api/mqtt/animations/status
```

## Usage Examples

### Using cURL

1. **Discover devices:**
```bash
curl -X POST http://localhost:8080/api/mqtt/devices/discover
```

2. **List all devices:**
```bash
curl http://localhost:8080/api/mqtt/devices
```

3. **Turn on a device:**
```bash
curl -X POST http://localhost:8080/api/mqtt/devices/Living%20Room%20Light/on
```

4. **Set brightness:**
```bash
curl -X POST "http://localhost:8080/api/mqtt/devices/Living%20Room%20Light/brightness?brightness=150"
```

5. **Run pulse animation:**
```bash
curl -X POST "http://localhost:8080/api/mqtt/animations/pulse/Living%20Room%20Light?cycles=10&intervalMs=500"
```

6. **Run breathe animation:**
```bash
curl -X POST "http://localhost:8080/api/mqtt/animations/breathe/Living%20Room%20Light?cycles=5&cycleDurationMs=3000"
```

7. **Run wave pattern:**
```bash
curl -X POST "http://localhost:8080/api/mqtt/animations/wave?cycles=3&delayMs=300"
```

8. **Stop all animations:**
```bash
curl -X POST http://localhost:8080/api/mqtt/animations/stop-all
```

### Using Swagger UI

1. Start the application
2. Navigate to http://localhost:8080/swagger-ui.html
3. Explore the "MQTT Devices" and "MQTT Animations" sections
4. Try out the various endpoints directly from the UI

## Architecture

### Components

- **MqttConfig**: Configuration for MQTT broker connection
- **MqttDeviceDto**: Data transfer object representing an MQTT device
- **MqttDeviceService**: Service for discovering and controlling MQTT devices
- **MqttAnimationService**: Service for running animations on MQTT devices
- **MqttDeviceController**: REST endpoints for device discovery and control
- **MqttAnimationController**: REST endpoints for running animations

### How Device Discovery Works

1. On application startup, `MqttDeviceService` connects to the MQTT broker
2. It subscribes to:
   - `zigbee2mqtt/bridge/devices` - for device list responses
   - `zigbee2mqtt/bridge/event` - for device events
   - `zigbee2mqtt/#` - for all device state updates
3. It requests the device list from the Zigbee2MQTT bridge
4. Discovered devices are stored in memory and updated as state changes arrive
5. Devices can be controlled by publishing to `zigbee2mqtt/{friendlyName}/set`

### Animation System

Animations run asynchronously using Spring's `@Async` support. Multiple animations can run simultaneously, and each animation can be stopped individually or all at once.

## Troubleshooting

### MQTT Connection Issues

1. Check that your Zigbee2MQTT broker is running:
```bash
# Check if the port is accessible
nc -zv 192.168.86.212 6682
```

2. Verify the broker URL in `application.yml` is correct

3. Check application logs for connection errors:
```
Connection to MQTT broker lost: ...
```

### No Devices Discovered

1. Ensure devices are paired with your Zigbee2MQTT coordinator
2. Manually trigger discovery:
```bash
curl -X POST http://localhost:8080/api/mqtt/devices/discover
```

3. Check MQTT connection status:
```bash
curl http://localhost:8080/api/mqtt/devices/status
```

### Disable MQTT

If you don't want to use MQTT functionality, set `mqtt.enabled: false` in `application.yml`:

```yaml
mqtt:
  enabled: false
```

## Integration with Philips Hue

Both Philips Hue and MQTT devices work side-by-side in the same application:
- Philips Hue endpoints: `/api/lights/*` and `/api/animations/*`
- MQTT device endpoints: `/api/mqtt/devices/*` and `/api/mqtt/animations/*`

This allows you to create mixed lighting setups with both Philips Hue bulbs and Zigbee2MQTT devices.
