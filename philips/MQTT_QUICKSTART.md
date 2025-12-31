# MQTT Device Quick Start Guide

This guide will help you quickly get started with MQTT device discovery and animation from the HueBridgeApplication.

## Prerequisites

1. A running Zigbee2MQTT broker
2. At least one Zigbee device paired with your coordinator
3. Java 17 or later installed

## Quick Start

### 1. Start the Application

```bash
cd /Users/treemark/Development/git_reposatories/zigbee
./gradlew :philips:bootRun
```

The application will:
- Start on port 8080
- Connect to MQTT broker at `tcp://192.168.86.212:6682`
- Automatically discover devices on startup

### 2. Verify MQTT Connection

```bash
curl http://localhost:8080/api/mqtt/devices/status
```

Expected response:
```json
{
  "connected": true,
  "deviceCount": 2
}
```

### 3. List Discovered Devices

```bash
curl http://localhost:8080/api/mqtt/devices
```

This will show all discovered MQTT devices with their properties.

### 4. Control a Device

Replace `{friendlyName}` with the actual device name from step 3:

```bash
# Turn on
curl -X POST http://localhost:8080/api/mqtt/devices/{friendlyName}/on

# Turn off
curl -X POST http://localhost:8080/api/mqtt/devices/{friendlyName}/off

# Set brightness to 50%
curl -X POST "http://localhost:8080/api/mqtt/devices/{friendlyName}/brightness?brightness=128"
```

### 5. Run Animations

#### Pulse Animation (Quick Blink)
```bash
curl -X POST "http://localhost:8080/api/mqtt/animations/pulse/{friendlyName}?cycles=5&intervalMs=500"
```

#### Breathe Animation (Smooth Fade)
```bash
curl -X POST "http://localhost:8080/api/mqtt/animations/breathe/{friendlyName}?cycles=3&cycleDurationMs=3000"
```

#### Wave Pattern (All Devices)
```bash
curl -X POST "http://localhost:8080/api/mqtt/animations/wave?cycles=2&delayMs=300"
```

#### Sequential Pattern (All Devices)
```bash
curl -X POST "http://localhost:8080/api/mqtt/animations/sequential?cycles=2&delayMs=500"
```

### 6. Use Swagger UI

For a visual interface:
1. Open http://localhost:8080/swagger-ui.html
2. Navigate to "MQTT Devices" or "MQTT Animations" sections
3. Try out the endpoints interactively

## Configuration

Edit `philips/src/main/resources/application.yml` to change settings:

```yaml
mqtt:
  enabled: true                            # Set to false to disable MQTT
  broker-url: tcp://192.168.86.212:6682  # Your broker URL
  base-topic: zigbee2mqtt                 # MQTT base topic
  client-id: hue-bridge-app               # Client identifier
  qos: 1                                  # Quality of service
```

## Testing with a Specific Device

If you know your device's friendly name (e.g., "Living Room Light"), try this complete sequence:

```bash
# Store the device name
DEVICE="Living Room Light"

# Turn on
curl -X POST "http://localhost:8080/api/mqtt/devices/${DEVICE}/on"
sleep 2

# Run pulse animation
curl -X POST "http://localhost:8080/api/mqtt/animations/pulse/${DEVICE}?cycles=5&intervalMs=500"
sleep 10

# Run breathe animation
curl -X POST "http://localhost:8080/api/mqtt/animations/breathe/${DEVICE}?cycles=3&cycleDurationMs=2000"
sleep 15

# Set to medium brightness
curl -X POST "http://localhost:8080/api/mqtt/devices/${DEVICE}/brightness?brightness=150"
```

## Troubleshooting

### Application won't start
- Check if port 8080 is available
- Verify Java 17+ is installed: `java -version`

### MQTT connection failed
- Verify broker is running: `nc -zv 192.168.86.212 6682`
- Check broker URL in `application.yml`
- Temporarily disable MQTT by setting `mqtt.enabled: false`

### No devices discovered
- Ensure devices are paired with Zigbee2MQTT
- Manually trigger discovery: `curl -X POST http://localhost:8080/api/mqtt/devices/discover`
- Check Zigbee2MQTT logs

### Animation not working
- Verify device is online: `curl http://localhost:8080/api/mqtt/devices`
- Check if device supports brightness: Some devices only support on/off
- View running animations: `curl http://localhost:8080/api/mqtt/animations/running`

## Available Animations

| Animation | Description | Parameters |
|-----------|-------------|------------|
| pulse | Quick on/off blinks | cycles, intervalMs |
| breathe | Smooth brightness fade | cycles, cycleDurationMs |
| sequential | Turn on/off in sequence | cycles, delayMs |
| random-blink | Random device blinks | durationSeconds |
| color-temp-sweep | Warm to cool sweep | cycles, minTemp, maxTemp, cycleDurationMs |
| wave | Brightness wave effect | cycles, delayMs |

## Next Steps

- See [MQTT_DEVICES.md](MQTT_DEVICES.md) for complete API documentation
- Explore the Swagger UI for all available endpoints
- Customize animations by adjusting parameters
- Combine MQTT devices with Philips Hue lights for mixed lighting scenes
