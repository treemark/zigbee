# Flashing Tuya Bulbs with Tasmota for Fast MQTT Control

## Why Tasmota?

**Current Problem with Tuya Protocol:**
- Tuya's native protocol is slow and heavyweight
- Each command has significant latency (100ms+ per bulb)
- Not designed for rapid color changes or animations
- Cloud-dependent architecture even for local control

**Tasmota Benefits:**
- ⚡ **10-50x faster** response times via MQTT
- 🏠 **Fully local** control (no cloud dependency)
- 🎨 **Smooth animations** at 20+ FPS easily achievable
- 🔓 **Open source** with active community
- 🛠️ **Highly customizable** with full control over hardware

---

## Overview: Two Flashing Methods

### 1. **OTA Flashing** (Over-The-Air) - EASIEST
- No disassembly required
- Works if bulb firmware is vulnerable
- Success rate: ~60% (newer bulbs may be patched)
- Tools: `tuya-cloudcutter` or `tuya-convert`

### 2. **Serial Flashing** (Hardware) - RELIABLE
- Requires disassembly and soldering
- Works on ALL compatible chips
- Success rate: ~100%
- More technical but guaranteed to work

---

## Method 1: OTA Flashing with tuya-cloudcutter

### Prerequisites
```bash
# On macOS
brew install python@3.11
pip3 install pycryptodome tornado

# Clone tuya-cloudcutter
cd ~/
git clone https://github.com/tuya-cloudcutter/tuya-cloudcutter.git
cd tuya-cloudcutter
```

### Steps

1. **Check if your bulb is compatible:**
   - Bulbs must have firmware version < 2.0
   - Check in Smart Life app: Device Settings → Device Information
   - If firmware is too new, skip to Serial Flashing method

2. **Run cloudcutter:**
   ```bash
   python3 -m cloudcutter
   ```

3. **Follow the web interface:**
   - Connect to the cloudcutter Wi-Fi network
   - Put bulb in pairing mode (usually 3x on/off cycles)
   - Select your bulb model from the list
   - Choose "Tasmota" firmware
   - Wait for flash to complete (5-10 minutes)

4. **Configure Tasmota:**
   - Connect to "tasmota-XXXX" Wi-Fi network
   - Configure your home Wi-Fi credentials
   - Set up MQTT broker details

---

## Method 2: Serial Flashing (Hardware Method)

### What You'll Need

**Hardware:**
- USB-to-Serial adapter (FTDI or CP2102) - $5-10
- Soldering iron and solder
- Jumper wires
- Screwdriver to open bulb

**Software:**
- esptool.py for flashing
- Tasmota firmware binary

### Identification

Most Tuya bulbs use one of these chips:
- **ESP8266** (older bulbs) - Fully compatible! ✅
- **ESP32-C3** (newer bulbs) - Compatible! ✅
- **BK7231** (Beken chip) - Use LibreTiny/OpenBeken instead
- **CBU/CB2S** (Tuya's own chip) - Cannot be flashed ❌

### Steps to Flash ESP8266/ESP32 Bulbs

1. **Disassemble the bulb:**
   - Remove diffuser (usually glued)
   - Remove LED board to access MCU board
   - Identify the chip (look for ESP8266, ESP32-C3, etc.)

2. **Locate programming pins:**
   ```
   Common ESP8266 pins:
   - VCC (3.3V)
   - GND
   - TX
   - RX
   - GPIO0 (for boot mode)
   ```

3. **Connect USB-to-Serial adapter:**
   ```
   Adapter → Bulb
   VCC    → VCC (3.3V)
   GND    → GND
   TX     → RX
   RX     → TX
   ```

4. **Put chip in flash mode:**
   - Connect GPIO0 to GND
   - Power on the bulb
   - This enters bootloader mode

5. **Backup original firmware (important!):**
   ```bash
   pip3 install esptool
   
   # For ESP8266 (1MB flash)
   esptool.py --port /dev/cu.usbserial-XXX read_flash 0x00000 0x100000 tuya_backup.bin
   
   # For ESP32
   esptool.py --port /dev/cu.usbserial-XXX read_flash 0x00000 0x400000 tuya_backup.bin
   ```

6. **Flash Tasmota:**
   ```bash
   # Download Tasmota
   wget https://github.com/arendst/Tasmota/releases/download/v13.3.0/tasmota.bin
   
   # Erase flash
   esptool.py --port /dev/cu.usbserial-XXX erase_flash
   
   # Flash Tasmota (ESP8266)
   esptool.py --port /dev/cu.usbserial-XXX write_flash -fs 1MB -fm dout 0x0 tasmota.bin
   
   # Flash Tasmota (ESP32)
   esptool.py --port /dev/cu.usbserial-XXX write_flash 0x0 tasmota32c3.bin
   ```

7. **Disconnect GPIO0 and reboot:**
   - Remove GPIO0 connection
   - Power cycle the bulb
   - Bulb should create "tasmota-XXXX" Wi-Fi AP

---

## Tasmota Configuration for RGB Bulbs

### Initial Setup

1. **Connect to Tasmota:**
   - Join "tasmota-XXXX" Wi-Fi network
   - Open http://192.168.4.1 in browser

2. **Configure Wi-Fi:**
   - Enter your home Wi-Fi SSID and password
   - Save and reboot

3. **Configure MQTT:**
   ```
   Configuration → Configure MQTT
   Host: <your MQTT broker IP>
   Port: 1883
   User: (if required)
   Password: (if required)
   Topic: bulb_%06X (or custom name)
   ```

### Configure Bulb Template

Each bulb model has different GPIO mappings. Common templates:

**Generic RGB CCT Bulb (PWM):**
```json
{"NAME":"Generic RGB CCT","GPIO":[0,0,0,0,416,419,0,0,417,420,418,0,0,0],"FLAG":0,"BASE":18}
```

**Apply template:**
- Configuration → Configure Template
- Paste JSON
- Check "Activate"
- Save

---

## Setting Up MQTT Broker

### Install Mosquitto (MQTT Broker)

```bash
# macOS
brew install mosquitto
brew services start mosquitto

# Test it's running
mosquitto_sub -h localhost -t '#' -v
```

### Basic mosquitto.conf
```conf
# /opt/homebrew/etc/mosquitto/mosquitto.conf
listener 1883
allow_anonymous true
```

---

## Controlling Tasmota Bulbs via MQTT

### MQTT Command Structure

Tasmota uses simple MQTT commands:

**Turn On/Off:**
```bash
mosquitto_pub -h localhost -t "cmnd/bulb1/POWER" -m "ON"
mosquitto_pub -h localhost -t "cmnd/bulb1/POWER" -m "OFF"
```

**Set Color (HSV):**
```bash
# Hue (0-360), Saturation (0-100), Brightness (0-100)
mosquitto_pub -h localhost -t "cmnd/bulb1/HsbColor" -m "120,100,50"
```

**Set RGB:**
```bash
mosquitto_pub -h localhost -t "cmnd/bulb1/Color" -m "FF0000"  # Red
```

**Set Brightness:**
```bash
mosquitto_pub -h localhost -t "cmnd/bulb1/Dimmer" -m "75"
```

### Python Example - Fast Animation

```python
import paho.mqtt.client as mqtt
import time
import math

# Connect to MQTT broker
client = mqtt.Client()
client.connect("localhost", 1883, 60)

# List of bulb topics
bulbs = ["bulb1", "bulb2", "bulb3"]  # Add all your bulbs

# Smooth rainbow animation
duration = 60  # seconds
fps = 20  # frames per second
interval = 1.0 / fps

for frame in range(duration * fps):
    hue = (frame * 360 / (duration * fps)) % 360
    
    # Send color to ALL bulbs
    for bulb in bulbs:
        client.publish(f"cmnd/{bulb}/HsbColor", f"{int(hue)},100,100")
    
    time.sleep(interval)

client.disconnect()
```

**Performance:** This achieves smooth 20 FPS animation with minimal latency!

---

## Alternative: OpenBeken for BK7231 Chips

If your bulbs use Beken BK7231 chips (common in newer Tuya bulbs):

1. **Use OpenBeken firmware:**
   ```bash
   git clone https://github.com/openshwprojects/OpenBK7231T_App.git
   ```

2. **Flash via UART** (same hardware method as above)

3. **OpenBeken also supports MQTT** with similar command structure

---

## Troubleshooting

### Bulb won't enter flash mode
- Ensure GPIO0 is connected to GND BEFORE powering on
- Some bulbs require holding GPIO0 low during entire flash
- Check voltage: MUST be 3.3V, NOT 5V!

### Flash fails with "chip not detected"
- Wrong baud rate: try `-b 115200` or `-b 9600`
- Bad connections: check all wires
- Wrong chip: verify it's actually ESP8266/ESP32

### Bulb powers on but no Wi-Fi AP
- Flash may have succeeded but wrong firmware
- Try flashing again
- Restore from backup if needed

### MQTT not working
- Check broker is running: `mosquitto_sub -h localhost -t '#' -v`
- Verify Tasmota MQTT config matches broker
- Check topic names match

---

## Recommended Workflow

1. **Start with 1-2 bulbs** to test the process
2. **OTA first**, fall back to serial if needed
3. **Document GPIO mappings** for your specific bulb model
4. **Create template** once you have working config
5. **Flash remaining bulbs** using your proven template

---

## Resources

- **Tasmota Documentation:** https://tasmota.github.io/docs/
- **Template Repository:** https://templates.blakadder.com/
- **tuya-cloudcutter:** https://github.com/tuya-cloudcutter/tuya-cloudcutter
- **OpenBeken:** https://github.com/openshwprojects/OpenBK7231T_App
- **Tasmota Support:** https://discord.gg/Ks2Kzd4

---

## Summary

**Tasmota + MQTT = Fast, Reliable, Local Smart Home**

- ⚡ 10-50x faster than Tuya protocol
- 🎨 Smooth animations at 20+ FPS
- 🏠 Complete local control
- 🔓 No cloud dependency
- 🛠️ Full hardware access

The initial flash takes effort, but the result is worth it!
