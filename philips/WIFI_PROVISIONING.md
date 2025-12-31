# WiFi Provisioning for OpenBeken Devices

The HueBridgeApplication now includes automatic WiFi provisioning for OpenBeken devices. This feature automatically discovers, configures, and connects OpenBeken smart devices to your WiFi network and embedded MQTT broker.

## Overview

The WiFi Provisioning Service:
1. **Scans** for OpenBeken access points (e.g., `OpenBeken_XXXXXX`)
2. **Connects** to discovered devices
3. **Configures** them with your WiFi credentials
4. **Sets up** MQTT connection to your embedded Moquette broker
5. **Automatically** manages the entire setup process

## Prerequisites

### System Requirements
- **macOS**, **Linux**, or **Windows** with WiFi capabilities
- Administrator/sudo privileges (required for WiFi operations)
- OpenBeken device in AP mode (factory reset state)

### Software Requirements
- Java 17+
- Network utilities:
  - **macOS**: `networksetup`, `airport` (built-in)
  - **Linux**: `nmcli` (NetworkManager)
  - **Windows**: `netsh` (built-in)

## Configuration

### 1. Enable WiFi Provisioning

Edit `philips/src/main/resources/application.yml`:

```yaml
wifi:
  provisioning:
    enabled: true  # Enable automatic provisioning
    scan-interval-seconds: 60  # Scan every 60 seconds
    target-ssid-pattern: "OpenBeken_.*"  # Match OpenBeken devices
    
    # Your WiFi network
    wifi-ssid: "YourHomeNetwork"
    wifi-password: "YourWiFiPassword"
    
    # MQTT broker (auto = use local IP address)
    mqtt-host: "auto"  # or specify like "192.168.1.100"
    mqtt-port: 1883
    mqtt-topic: "openbeken"
    
    # Device settings
    device-config-timeout: 30000  # 30 seconds
    device-ap-password: ""  # Usually empty for OpenBeken
```

### 2. Configure Moquette Broker

Ensure the embedded MQTT broker is enabled:

```yaml
moquette:
  enabled: true
  port: 1883
  websocket-port: 8883
  allow-anonymous: true
```

### 3. Start the Application

```bash
./gradlew :philips:bootRun
```

## How It Works

### Automatic Discovery Process

```
┌─────────────────────────────────────────────────────────────┐
│  1. Scan for WiFi Networks                                  │
│     └─> Find SSIDs matching pattern "OpenBeken_.*"         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  2. Connect to Device AP                                    │
│     └─> Connects your machine to OpenBeken_XXXXXX          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  3. Configure Device                                        │
│     ├─> WiFi SSID & Password                               │
│     ├─> MQTT Broker Address (auto-detected local IP)       │
│     ├─> MQTT Port (1883)                                   │
│     └─> MQTT Topic (openbeken)                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  4. Reboot Device                                           │
│     └─> Device reboots and connects to your WiFi           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  5. Device Connected!                                       │
│     └─> Publishes MQTT messages to embedded broker         │
└─────────────────────────────────────────────────────────────┘
```

### OpenBeken HTTP API

The service uses OpenBeken's built-in HTTP API (typically at `http://192.168.4.1`):

- **WiFi Config**: `GET /cfg?ssid=SSID&password=PASSWORD`
- **MQTT Config**: `GET /cfg_mqtt?host=HOST&port=PORT&topic=TOPIC`
- **Reboot**: `GET /reboot`

## Usage

### Manual Trigger (if needed)

While provisioning runs automatically, you can also trigger it manually:

```bash
# Run the application with provisioning enabled
./gradlew :philips:bootRun
```

The service will:
- Start scanning immediately (after 5 second delay)
- Continue scanning every 60 seconds (configurable)
- Skip already provisioned devices

### Monitoring

Check logs for provisioning status:

```
2025-12-30 16:00:00 [main] INFO  WiFiProvisioningService - WiFi Provisioning Service Started
2025-12-30 16:00:00 [main] INFO  WiFiProvisioningService - Target SSID Pattern: OpenBeken_.*
2025-12-30 16:00:05 [scheduling-1] INFO  WiFiProvisioningService - Found new OpenBeken device: OpenBeken_A1B2C3
2025-12-30 16:00:05 [scheduling-1] INFO  WiFiProvisioningService - Step 1: Connecting to device AP...
2025-12-30 16:00:10 [scheduling-1] INFO  WiFiProvisioningService - Auto-detected local IP: 192.168.1.100
2025-12-30 16:00:10 [scheduling-1] INFO  WiFiProvisioningService - Step 2: Configuring device...
2025-12-30 16:00:15 [scheduling-1] INFO  WiFiProvisioningService - ✓ Successfully configured device: OpenBeken_A1B2C3
2025-12-30 16:00:15 [scheduling-1] INFO  WiFiProvisioningService - Device OpenBeken_A1B2C3 should now be connected!
```

## Platform-Specific Notes

### macOS
- Requires **Administrator privileges** for WiFi operations
- Uses `networksetup` and `airport` commands
- You may be prompted for sudo password

**Run with sudo:**
```bash
sudo ./gradlew :philips:bootRun
```

### Linux
- Requires **NetworkManager** (`nmcli`)
- Requires sudo privileges
- Install NetworkManager if not present:
  ```bash
  # Ubuntu/Debian
  sudo apt-get install network-manager
  
  # RHEL/CentOS
  sudo yum install NetworkManager
  ```

**Run with sudo:**
```bash
sudo ./gradlew :philips:bootRun
```

### Windows
- Uses built-in `netsh` command
- Requires **Administrator privileges**
- Run terminal as Administrator

## Troubleshooting

### Device Not Found

**Problem**: Service doesn't detect OpenBeken device

**Solutions**:
1. Ensure device is in AP mode (factory reset if needed)
2. Check SSID pattern in config matches device name
3. Verify WiFi is enabled on your computer
4. Check logs for scanning errors

### Connection Failed

**Problem**: Cannot connect to device AP

**Solutions**:
1. Verify device AP is visible in WiFi settings
2. Check `device-ap-password` config (usually empty)
3. Ensure no other process is controlling WiFi
4. Try manual connection to verify device AP works

### Configuration Failed

**Problem**: Device doesn't connect to WiFi after configuration

**Solutions**:
1. Verify WiFi SSID and password are correct
2. Check WiFi network is 2.4GHz (OpenBeken usually doesn't support 5GHz)
3. Ensure MQTT broker is accessible from device
4. Check firewall settings allow MQTT connections (port 1883)

### Permission Errors

**Problem**: `Permission denied` when scanning or connecting

**Solutions**:
- Run application with sudo/administrator privileges
- On macOS: `sudo ./gradlew :philips:bootRun`
- On Linux: `sudo ./gradlew :philips:bootRun`
- On Windows: Run terminal as Administrator

## Advanced Configuration

### Custom SSID Pattern

To provision devices with different naming patterns:

```yaml
wifi:
  provisioning:
    target-ssid-pattern: "MyDevice_.*"  # Match "MyDevice_XXX"
```

### Fixed MQTT Broker IP

Instead of auto-detection:

```yaml
wifi:
  provisioning:
    mqtt-host: "192.168.1.100"  # Specific IP
```

### Faster/Slower Scanning

Adjust scan frequency:

```yaml
wifi:
  provisioning:
    scan-interval-seconds: 30  # Scan every 30 seconds (faster)
    # or
    scan-interval-seconds: 300  # Scan every 5 minutes (slower)
```

## Security Considerations

⚠️ **Important Security Notes**:

1. **WiFi Credentials**: Stored in plaintext in `application.yml`
   - Use environment variables for production
   - Restrict file permissions: `chmod 600 application.yml`

2. **MQTT Security**: Default config allows anonymous access
   - Enable authentication in production
   - Use TLS/SSL for secure MQTT

3. **Network Access**: Service modifies system WiFi settings
   - Requires elevated privileges
   - Only run on trusted networks
   - Disable when not provisioning devices

## Integration with MQTT Device Service

Once provisioned, devices automatically:
1. Connect to your WiFi network
2. Connect to embedded MQTT broker
3. Publish state to configured MQTT topic
4. Become discoverable via `MqttDeviceService`
5. Can be controlled via REST API
6. Support animations via `MqttAnimationService`

## REST API Endpoints

After provisioning, control devices via:

- `GET /api/mqtt/devices` - List all discovered MQTT devices
- `POST /api/mqtt/devices/{name}/command` - Send commands
- `POST /api/mqtt/animations/{name}/pulse` - Run animations
- `POST /api/mqtt/animations/{name}/rainbow` - Color effects

## Example Workflow

1. **Power on OpenBeken device** (factory reset if needed)
2. **Device creates AP**: `OpenBeken_A1B2C3`
3. **Service detects device** (within 60 seconds)
4. **Automatic configuration** begins
5. **Device connects** to your WiFi
6. **Device appears** in MQTT device list
7. **Control via REST API** or animations

## Disabling WiFi Provisioning

To disable the feature:

```yaml
wifi:
  provisioning:
    enabled: false
```

Or simply don't configure WiFi credentials - service won't run without them.

## Support

For issues or questions:
- Check application logs for detailed error messages
- Ensure OpenBeken firmware is up to date
- Verify network connectivity
- Test manual WiFi connection to device AP first
