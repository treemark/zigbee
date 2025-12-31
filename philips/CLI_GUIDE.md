# HueBridge CLI Guide

Interactive command-line interface for controlling MQTT devices, running animations, and provisioning OpenBeken devices.

## Features

- 🎮 **Interactive Shell** - Easy-to-use command interface
- 💡 **Device Control** - Turn lights on/off, adjust brightness
- 🌈 **Animations** - Run pulse, rainbow, and breathe effects
- 📡 **WiFi Provisioning** - Automatically configure OpenBeken devices
- 🔍 **Device Discovery** - Find and list MQTT devices
- 🚫 **No Web Server** - Lightweight, runs without port 8080

## Quick Start

### Run CLI Mode

```bash
./gradlew :philips:bootRun --args='--spring.profiles.active=cli'
```

You'll see the interactive prompt:

```
╔═══════════════════════════════════════════════════════════╗
║                                                           ║
║        HueBridge CLI - MQTT Device Controller            ║
║                                                           ║
║  Control lights, run animations, provision devices       ║
║  Type 'help' for available commands                      ║
║                                                           ║
╚═══════════════════════════════════════════════════════════╝

huebridge> 
```

## Available Commands

### Device Management

#### `list` or `ls`
List all discovered MQTT devices

```bash
huebridge> list

┌─────────────────────────────────────────────────────────────────┐
│ Device Name               │ State      │ Brightness │ Type       │
├─────────────────────────────────────────────────────────────────┤
│ Living Room Light         │ ON         │ 255        │ Router     │
│ Bedroom Light             │ OFF        │ 128        │ Router     │
│ Kitchen Light             │ ON         │ 200        │ Router     │
└─────────────────────────────────────────────────────────────────┘

Total devices: 3
```

#### `info <device-name>`
Show detailed information about a specific device

```bash
huebridge> info Living Room Light

┌─────────────────────────────────────────────┐
│ Device Information                          │
├─────────────────────────────────────────────┤
│ Name:         Living Room Light             │
│ IEEE Address: 0x00158d0001234567            │
│ Type:         Router                        │
│ Model:        CB3S Smart Bulb               │
│ Manufacturer: OpenBeken                     │
│ State:        ON                            │
│ Brightness:   255                           │
│ Color Temp:   370                           │
│ Color (x,y):  0.450, 0.410                  │
└─────────────────────────────────────────────┘
```

#### `discover`
Trigger MQTT device discovery

```bash
huebridge> discover
Triggering device discovery...
✓ Discovery complete. Found 3 device(s)
```

#### `status`
Show system status

```bash
huebridge> status

┌─────────────────────────────────────────────┐
│ System Status                               │
├─────────────────────────────────────────────┤
│ MQTT Connected:    ✓ Yes                    │
│ Devices Found:     3                        │
│ Animations Running: 2                       │
│ Provisioned Devices: 5                      │
└─────────────────────────────────────────────┘
```

### Device Control

#### `on <device-name> [brightness]`
Turn device on, optionally set brightness (0-255)

```bash
huebridge> on Living Room Light
✓ Turned ON: Living Room Light (brightness: 255)

huebridge> on Bedroom Light 128
✓ Turned ON: Bedroom Light (brightness: 128)
```

#### `off <device-name>`
Turn device off

```bash
huebridge> off Kitchen Light
✓ Turned OFF: Kitchen Light
```

#### `brightness <device-name> <value>`
Set device brightness (0-255)

```bash
huebridge> brightness Living Room Light 200
✓ Set brightness: Living Room Light = 200
```

### Animations

#### `pulse <device-name> [cycles] [interval-ms]`
Run pulse (on/off) animation

```bash
huebridge> pulse Living Room Light 5 500
Starting pulse animation: Living Room Light (5 cycles, 500ms interval)
✓ Animation started
```

**Parameters:**
- `cycles` - Number of on/off cycles (default: 5)
- `interval-ms` - Time between transitions in milliseconds (default: 500)

#### `rainbow <device-name> [duration-ms] [steps]`
Run rainbow color cycle animation

```bash
huebridge> rainbow Living Room Light 10000 50
Starting rainbow animation: Living Room Light (10000ms duration, 50 steps)
✓ Animation started
```

**Parameters:**
- `duration-ms` - Total animation duration (default: 10000)
- `steps` - Number of color steps (default: 50)

#### `breathe <device-name> [cycles] [duration-ms]`
Run breathing fade effect

```bash
huebridge> breathe Bedroom Light 3 4000
Starting breathe animation: Bedroom Light (3 cycles, 4000ms duration)
✓ Animation started
```

**Parameters:**
- `cycles` - Number of breathe cycles (default: 3)
- `duration-ms` - Duration per cycle (default: 4000)

#### `stop [device-name]`
Stop animations

```bash
# Stop specific device animation
huebridge> stop Living Room Light
✓ Stopped animation for: Living Room Light

# Stop all animations
huebridge> stop
✓ Stopped all animations
```

#### `animations`
List currently running animations

```bash
huebridge> animations
Running Animations:
  - Living Room Light
  - Bedroom Light
```

### WiFi Provisioning

#### `provision`
Scan for and provision OpenBeken devices

```bash
huebridge> provision
Scanning for OpenBeken devices to provision...
This may take a minute...
✓ Provisioning scan completed
```

**Note:** Requires sudo/admin privileges and WiFi provisioning enabled in config.

#### `provisioned`
List devices that have been provisioned

```bash
huebridge> provisioned
Provisioned Devices:
  ✓ OpenBeken_A1B2C3
  ✓ OpenBeken_D4E5F6
  ✓ OpenBeken_789ABC
```

### General

#### `help` or `?`
Show help message with all commands

```bash
huebridge> help
```

#### `exit`, `quit`, or `q`
Exit the CLI

```bash
huebridge> exit
Goodbye!
```

## Configuration

### CLI Mode Configuration

The CLI uses `application-cli.yml` profile:

```yaml
# Enable CLI
cli:
  enabled: true

# Disable web server
spring:
  main:
    web-application-type: none

# MQTT and Moquette broker stay enabled
mqtt:
  enabled: true

moquette:
  enabled: true
  port: 1883
```

### Enable WiFi Provisioning in CLI

Edit `application-cli.yml`:

```yaml
wifi:
  provisioning:
    enabled: true  # Enable provisioning
    wifi-ssid: "YourWiFiNetwork"
    wifi-password: "YourPassword"
```

Then run with sudo:

```bash
sudo ./gradlew :philips:bootRun --args='--spring.profiles.active=cli'
```

## Example Usage Session

```bash
$ ./gradlew :philips:bootRun --args='--spring.profiles.active=cli'

╔═══════════════════════════════════════════════════════════╗
║        HueBridge CLI - MQTT Device Controller            ║
╚═══════════════════════════════════════════════════════════╝

huebridge> status
┌─────────────────────────────────────────────┐
│ System Status                               │
├─────────────────────────────────────────────┤
│ MQTT Connected:    ✓ Yes                    │
│ Devices Found:     3                        │
│ Animations Running: 0                       │
│ Provisioned Devices: 0                      │
└─────────────────────────────────────────────┘

huebridge> list
┌─────────────────────────────────────────────────────────────────┐
│ Device Name               │ State      │ Brightness │ Type       │
├─────────────────────────────────────────────────────────────────┤
│ Living Room Light         │ ON         │ 255        │ Router     │
│ Bedroom Light             │ OFF        │ 128        │ Router     │
│ Kitchen Light             │ ON         │ 200        │ Router     │
└─────────────────────────────────────────────────────────────────┘

Total devices: 3

huebridge> on Bedroom Light 200
✓ Turned ON: Bedroom Light (brightness: 200)

huebridge> pulse Living Room Light 10 300
Starting pulse animation: Living Room Light (10 cycles, 300ms interval)
✓ Animation started

huebridge> rainbow Kitchen Light
Starting rainbow animation: Kitchen Light (10000ms duration, 50 steps)
✓ Animation started

huebridge> animations
Running Animations:
  - Living Room Light
  - Kitchen Light

huebridge> stop
✓ Stopped all animations

huebridge> exit
Goodbye!
```

## Tips & Tricks

### 1. Quick Device Names

For devices with spaces, quote the name or type it exactly:

```bash
huebridge> on "Living Room Light"
huebridge> on Living Room Light  # Also works
```

### 2. Chain Commands

Run multiple commands with semicolons (shell level):

```bash
$ echo -e "list\nstatus\nexit" | ./gradlew :philips:bootRun --args='--spring.profiles.active=cli'
```

### 3. Background Animations

Animations run asynchronously - you can continue using CLI while they run:

```bash
huebridge> rainbow Light1
huebridge> pulse Light2
huebridge> list  # CLI still responsive
```

### 4. Quick Brightness Control

Use alias for brightness:

```bash
huebridge> bri Living Room Light 150
✓ Set brightness: Living Room Light = 150
```

## Troubleshooting

### CLI Doesn't Start

**Problem:** CLI doesn't appear after running command

**Solution:** Ensure `--spring.profiles.active=cli` is specified:
```bash
./gradlew :philips:bootRun --args='--spring.profiles.active=cli'
```

### No Devices Found

**Problem:** `list` shows no devices

**Solutions:**
1. Run `discover` to trigger device discovery
2. Check MQTT broker is running (`status` command)
3. Ensure devices are connected to broker
4. Check `mqtt.enabled=true` in config

### Provisioning Fails

**Problem:** `provision` command fails

**Solutions:**
1. Run with sudo/admin privileges
2. Enable WiFi provisioning in `application-cli.yml`
3. Ensure OpenBeken device is in AP mode
4. Check WiFi adapter is available

### Animation Not Working

**Problem:** Animation command executes but nothing happens

**Solutions:**
1. Verify device supports the feature (check with `info <device>`)
2. Ensure device is online (`list` to check state)
3. Check MQTT connection (`status`)
4. Try `stop` then restart animation

## Advanced Usage

### Custom Scripts

Create shell scripts to automate tasks:

**turn-on-all.sh:**
```bash
#!/bin/bash
echo -e "on Living Room Light\non Bedroom Light\non Kitchen Light\nexit" | \
  ./gradlew :philips:bootRun --args='--spring.profiles.active=cli'
```

**morning-routine.sh:**
```bash
#!/bin/bash
cat << EOF | ./gradlew :philips:bootRun --args='--spring.profiles.active=cli'
on Bedroom Light 50
breathe Bedroom Light 3 3000
on Kitchen Light 200
exit
EOF
```

### Environment Variables

Override configuration via environment variables:

```bash
MQTT_BROKER_URL=tcp://192.168.1.100:1883 \
  ./gradlew :philips:bootRun --args='--spring.profiles.active=cli'
```

## CLI vs REST API

| Feature | CLI Mode | REST API Mode |
|---------|----------|---------------|
| Port 8080 | ❌ Not used | ✅ Required |
| Interactive | ✅ Yes | ❌ No |
| Scripting | ✅ Shell scripts | ✅ HTTP clients |
| Resource Usage | 🟢 Low | 🟡 Medium |
| Remote Access | ❌ Local only | ✅ Network accessible |
| Best For | Manual control, testing | Automation, integration |

## See Also

- [MQTT_DEVICES.md](MQTT_DEVICES.md) - MQTT device documentation
- [WIFI_PROVISIONING.md](WIFI_PROVISIONING.md) - WiFi provisioning guide
- [MQTT_QUICKSTART.md](MQTT_QUICKSTART.md) - Quick start guide
