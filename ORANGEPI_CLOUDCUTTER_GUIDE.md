# Setting Up Cloudcutter on Orange Pi Zero Plus

Your Orange Pi Zero Plus is **perfect** for OTA flashing Tuya bulbs! This guide will walk you through the complete setup.

## Quick Start

### Step 1: Copy Setup Script to Orange Pi

```bash
# From your Mac, copy the setup script to Orange Pi
scp setup_cloudcutter_orangepi.sh root@192.168.1.10:/root/
```

### Step 2: SSH into Orange Pi and Run Setup

```bash
# SSH into your Orange Pi
ssh root@192.168.1.10

# Make script executable and run it
chmod +x /root/setup_cloudcutter_orangepi.sh
/root/setup_cloudcutter_orangepi.sh
```

The script will:
- ✅ Install all dependencies (git, python3, hostapd, dnsmasq, etc.)
- ✅ Clone tuya-cloudcutter
- ✅ Detect your WiFi adapter
- ✅ Stop interfering services
- ✅ Prepare everything for flashing

### Step 3: Run Cloudcutter

After setup completes, run cloudcutter (it will tell you the wifi adapter name):

```bash
cd /root/tuya-cloudcutter
./tuya-cloudcutter.sh -w wlan0  # Use your actual adapter name
```

---

## Detailed Instructions

### What Cloudcutter Will Do

1. **Create a fake WiFi AP** that mimics the Tuya cloud
2. **Intercept the bulb's pairing request**
3. **Inject custom firmware** (Tasmota) during setup
4. **Return control** of the bulb to you with new firmware

### Flashing Process

1. **Start cloudcutter** on Orange Pi
2. **Web interface opens** at http://192.168.1.10:8000 (or similar)
3. **Put bulb in pairing mode**:
   - Turn bulb ON and OFF **3 times** rapidly
   - Bulb should start blinking rapidly
4. **Bulb connects to fake AP**
5. **Cloudcutter intercepts** and flashes Tasmota
6. **Success!** Bulb reboots with Tasmota

### After Flashing

Once a bulb is flashed with Tasmota:

1. **Find the bulb**: It creates AP "tasmota-XXXX"
2. **Connect to it** from your phone/computer
3. **Configure WiFi**: Point it to your home network
4. **Configure MQTT** (optional but recommended for fast control)
5. **Done!** Bulb is now locally controlled

---

## Troubleshooting

### "No WiFi adapter found"

```bash
# Check for WiFi interfaces
iw dev

# If nothing, check USB WiFi adapters
lsusb

# Try unblocking
rfkill list
rfkill unblock wifi
```

### "Failed to start hostapd"

```bash
# Stop conflicting services
systemctl stop hostapd
systemctl stop dnsmasq
systemctl stop NetworkManager
systemctl stop wpa_supplicant

# Kill any processes using the adapter
killall hostapd
killall dnsmasq
killall wpa_supplicant
```

### Cloudcutter hangs or doesn't detect bulb

1. **Ensure bulb is in pairing mode** (blinking rapidly)
2. **Try different bulb** - some may be patched
3. **Check distance** - bulb should be close to Orange Pi
4. **Restart cloudcutter** and try again

### WiFi adapter issues

If Orange Pi's built-in WiFi doesn't work well:
- Use a **USB WiFi dongle** (RTL8188 based recommended)
- Pass it with `-w wlan1` or similar

---

## Testing Your First Bulb

### Recommended First Test

1. **Choose ONE bulb** from a lamp you can easily access
2. **Unplug all other smart bulbs** to avoid confusion
3. **Run cloudcutter**
4. **Put bulb in pairing mode** (3x on/off)
5. **Wait for magic** ✨

### If It Works

🎉 Congratulations! You can now:
- Flash more bulbs the same way
- Configure them for MQTT control
- Build smooth animations

### If It Doesn't Work

Don't worry! You have options:
- Try a different bulb (some models may be patched)
- Check firmware version in Smart Life app
- Fall back to serial flashing (100% reliable)

---

## After Successful Flash

### Configure Tasmota for Your Network

```bash
# Bulb creates AP: tasmota-XXXX
# Connect to it, browse to: http://192.168.4.1

# Configure:
1. WiFi SSID: <your network>
2. WiFi Password: <your password>
3. Save and reboot
```

### Configure MQTT (For Fast Control)

```bash
# Find bulb on your network
# Browse to its IP address

# Go to: Configuration → Configure MQTT
Host: <your MQTT broker IP>
Port: 1883
Topic: bulb1  # or custom name
```

### Configure Bulb Template

Each bulb model needs the correct GPIO mapping. Common ones:

**For RGB CCT bulbs:**
```json
{"NAME":"Generic RGB CCT","GPIO":[0,0,0,0,416,419,0,0,417,420,418,0,0,0],"FLAG":0,"BASE":18}
```

Apply in: Configuration → Configure Template

---

## Batch Flashing Multiple Bulbs

Once you've successfully flashed one:

1. **Document the process** for your specific bulb model
2. **Flash 2-3 more** to refine your technique  
3. **Set up an assembly line**:
   - Orange Pi running cloudcutter
   - Plug bulb in
   - 3x on/off
   - Wait for flash
   - Unplug, move to next
4. **Configure in batches** after flashing

---

## SSH Access Setup (Optional)

If you want to give remote access for automation:

```bash
# On Orange Pi, generate SSH key
ssh-keygen -t rsa -b 4096

# Copy public key to your Mac
cat ~/.ssh/id_rsa.pub
# Add to your Mac's ~/.ssh/authorized_keys

# Or use password authentication
# Edit /etc/ssh/sshd_config:
PasswordAuthentication yes
systemctl restart sshd
```

---

## Complete Command Reference

### On Your Mac - Copy Files

```bash
# Copy setup script
scp setup_cloudcutter_orangepi.sh root@192.168.1.10:/root/

# Copy custom firmware (if you have it)
scp custom-firmware/*.bin root@192.168.1.10:/root/tuya-cloudcutter/custom-firmware/
```

### On Orange Pi - Run Cloudcutter

```bash
# Basic run (auto-detect adapter)
cd /root/tuya-cloudcutter
./tuya-cloudcutter.sh

# Specify adapter
./tuya-cloudcutter.sh -w wlan0

# With custom firmware
./tuya-cloudcutter.sh -w wlan0 -f tasmota.bin

# Verbose mode
./tuya-cloudcutter.sh -w wlan0 -v
```

---

## Advanced: Custom Firmware

You can flash custom Tasmota builds:

```bash
# Download specific Tasmota version
cd /root/tuya-cloudcutter/custom-firmware/
wget https://github.com/arendst/Tasmota/releases/download/v13.3.0/tasmota.bin

# Flash it
cd /root/tuya-cloudcutter
./tuya-cloudcutter.sh -w wlan0 -f tasmota.bin
```

---

## Success Rate

**Typical success rates:**
- **Older bulbs** (FW < 2.0): 80-90%
- **Newer bulbs** (FW >= 2.0): 10-30%
- **Unknown firmware**: 50-60%

If cloudcutter fails, serial flashing is always an option!

---

## Resources

- **Cloudcutter GitHub**: https://github.com/tuya-cloudcutter/tuya-cloudcutter
- **Tasmota Docs**: https://tasmota.github.io/docs/
- **Templates Database**: https://templates.blakadder.com/

---

## Summary

Your Orange Pi Zero Plus is a **perfect OTA flashing station**! With this setup:

✅ No disassembly needed
✅ Flash bulbs quickly (5-10 min each)
✅ Batch process multiple bulbs
✅ Keep it running for future bulbs

**Next Steps:**
1. Copy setup script to Orange Pi
2. Run it
3. Try flashing your first bulb!

Good luck! 🚀
