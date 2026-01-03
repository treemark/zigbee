# OpenBeken Device MQTT Setup Guide

## Device Information
- **Device IP**: http://192.168.86.66/
- **Device Type**: OpenBK7231N_17811957
- **Current Status**: MQTT not configured
- **MAC Address**: 10:5A:17:81:19:57

## MQTT Broker Configuration

Your system has an embedded Moquette MQTT broker running on:
- **Host**: 192.168.1.5 (your machine's local IP)
- **Port**: 1883
- **WebSocket Port**: 8883

## Quick Setup Steps

### Option 1: Manual Configuration (Recommended)

1. **Access the OpenBeken device web interface**:
   ```
   http://192.168.86.66/
   ```

2. **Navigate to Configuration**:
   - Click the "Config" button on the main page
   
3. **Configure MQTT Settings**:
   - **MQTT Host**: `192.168.1.5`
   - **MQTT Port**: `1883`
   - **MQTT Client ID**: `openbeken_17811957` (or any unique name)
   - **MQTT Group Topic/Base Topic**: `openbeken` (or `zigbee2mqtt` to match other devices)
   - **MQTT User**: Leave blank (anonymous access is enabled)
   - **MQTT Password**: Leave blank

4. **Save and Restart** the device

### Option 2: Direct HTTP Configuration

Use these curl commands to configure the device:

```bash
# Configure MQTT broker
curl -X POST "http://192.168.86.66/cfg" \
  -d "mqtt_host=192.168.1.5" \
  -d "mqtt_port=1883" \
  -d "mqtt_client_id=openbeken_17811957" \
  -d "mqtt_group=openbeken"

# Restart the device
curl -X POST "http://192.168.86.66/index?restart=1"
```

### Option 3: Automated Setup Script

Run this script to automatically configure the device:

```bash
#!/bin/bash
DEVICE_IP="192.168.86.66"
MQTT_HOST="192.168.1.5"
MQTT_PORT="1883"
MQTT_CLIENT_ID="openbeken_17811957"
MQTT_TOPIC="openbeken"

echo "Configuring OpenBeken device at $DEVICE_IP..."
echo "MQTT Broker: $MQTT_HOST:$MQTT_PORT"
echo "Base Topic: $MQTT_TOPIC"

# Configure MQTT settings
curl -X GET "http://$DEVICE_IP/cfg_mqtt?mqtt_host=$MQTT_HOST&mqtt_port=$MQTT_PORT&mqtt_client_id=$MQTT_CLIENT_ID&mqtt_brokerName=&mqtt_userName=&mqtt_password=&mqtt_group=$MQTT_TOPIC"

echo ""
echo "Configuration sent. Restarting device..."
sleep 2

# Restart device
curl -X GET "http://$DEVICE_IP/index?restart=1"

echo "Device restarting. Wait 10-15 seconds for it to come back online."
echo "Then verify MQTT connection at: http://$DEVICE_IP/"
```

## Verifying Configuration

1. **Wait 10-15 seconds** after restart

2. **Check device status**:
   ```bash
   curl -s http://192.168.86.66/ | grep "MQTT State"
   ```
   
   Should show: `MQTT State: Connected` (instead of "not configured")

3. **Check MQTT messages** (if you have mosquitto_sub installed):
   ```bash
   mosquitto_sub -h 192.168.1.5 -p 1883 -t "openbeken/#" -v
   ```

## Controlling the Device

### Via Application API

Once configured and connected to MQTT, discover the device:

```bash
# Start the application
cd /Users/treemark/Development/git_reposatories/zigbee
./gradlew :philips:bootRun

# In another terminal, discover devices
curl -X POST http://localhost:8080/api/mqtt/devices/discover

# List all devices
curl http://localhost:8080/api/mqtt/devices

# Control the device (use the friendly name from discovery)
curl -X POST http://localhost:8080/api/mqtt/devices/openbeken_17811957/on
curl -X POST http://localhost:8080/api/mqtt/devices/openbeken_17811957/off
```

### Via Direct MQTT Commands

If you have mosquitto_pub installed:

```bash
# Turn on
mosquitto_pub -h 192.168.1.5 -p 1883 -t "openbeken/openbeken_17811957/set" -m '{"state":"ON"}'

# Turn off
mosquitto_pub -h 192.168.1.5 -p 1883 -t "openbeken/openbeken_17811957/set" -m '{"state":"OFF"}'

# Set brightness (if supported)
mosquitto_pub -h 192.168.1.5 -p 1883 -t "openbeken/openbeken_17811957/set" -m '{"brightness":128}'
```

### Via Direct HTTP (OpenBeken API)

OpenBeken also supports direct HTTP control:

```bash
# Check current state
curl http://192.168.86.66/api/state

# Turn on (depends on channel configuration)
curl "http://192.168.86.66/cm?cmnd=POWER1%201"

# Turn off
curl "http://192.168.86.66/cm?cmnd=POWER1%200"

# Toggle
curl "http://192.168.86.66/cm?cmnd=POWER1%202"
```

## Troubleshooting

### Device won't connect to MQTT

1. **Check network connectivity**:
   ```bash
   ping 192.168.86.66
   ```

2. **Verify broker is running**:
   ```bash
   nc -zv 192.168.1.5 1883
   ```

3. **Check application logs** for MQTT broker status

4. **Access device web UI** and check "MQTT State" on main page

### Configuration not saving

- Try using the web interface (Config button) instead of HTTP commands
- Ensure device has stable power
- Check for firmware issues (version shown: 1.18.219)

### Device not discovered

1. Ensure the device is publishing to the correct topic
2. Update `application.yml` to match the base topic:
   ```yaml
   mqtt:
     base-topic: openbeken  # Must match device configuration
   ```
3. Manually trigger discovery after application restart

## Integration with Existing System

To integrate with your existing Zigbee2MQTT setup, you can either:

1. **Use the same base topic** (`zigbee2mqtt`) - devices will appear together
2. **Use a separate topic** (`openbeken`) - keeps OpenBeken devices separate
3. **Update MqttDeviceService** to listen to multiple topic patterns

Current application config uses:
```yaml
mqtt:
  base-topic: zigbee2mqtt
```

To support both, you would need to modify the service or change the OpenBeken base topic to `zigbee2mqtt`.

## Next Steps

1. Configure the device using one of the methods above
2. Verify MQTT connection on device web interface
3. Start the HueBridgeApplication
4. Discover and control the device via API or Swagger UI
