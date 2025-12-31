# Orange Pi Network Considerations for Cloudcutter

## The Issue

**YES, cloudcutter will disconnect your Orange Pi from WiFi while running.**

Here's why:
- Cloudcutter takes control of the WiFi adapter (wlan0)
- It creates a fake Access Point to trick the Tuya bulb
- Your Orange Pi can't be BOTH a WiFi client AND an AP simultaneously (on same adapter)
- While cloudcutter runs, Orange Pi is offline from your network

## Solutions

### Solution 1: Use Ethernet (BEST OPTION) ✅

Connect Orange Pi via Ethernet cable:
- ✅ Orange Pi stays on network via eth0
- ✅ WiFi adapter (wlan0) used for cloudcutter
- ✅ SSH session stays connected
- ✅ Can monitor progress remotely

**Setup:**
```bash
# Plug in Ethernet cable
# Orange Pi will have TWO connections:
# - eth0: Connected to your network (192.168.1.10 or new IP)
# - wlan0: Used by cloudcutter for fake AP
```

Check with:
```bash
ip addr
# Should see both eth0 and wlan0
```

---

### Solution 2: Use USB WiFi Dongle

If you have a spare USB WiFi adapter:
- ✅ Built-in WiFi (wlan0) stays connected to network
- ✅ USB WiFi (wlan1) used for cloudcutter
- ✅ Orange Pi never loses connectivity

**Setup:**
```bash
# Plug in USB WiFi dongle
# Check it's detected
iw dev

# Run cloudcutter with USB adapter
./tuya-cloudcutter.sh -w wlan1  # Use USB adapter
```

---

### Solution 3: Physical Access (Traditional Method)

Connect monitor + keyboard to Orange Pi:
- Run cloudcutter directly on the device
- No SSH needed
- WiFi disconnection doesn't matter

---

### Solution 4: Run Cloudcutter in Sessions

Use `screen` or `tmux` to keep cloudcutter running even if SSH disconnects:

```bash
# SSH into Orange Pi
ssh root@192.168.1.10

# Start a screen session
screen -S cloudcutter

# Run cloudcutter
cd /root/tuya-cloudcutter
./tuya-cloudcutter.sh -w wlan0

# Detach: Press Ctrl+A then D
# SSH session can now disconnect, cloudcutter keeps running

# To reattach later (via Ethernet or after cloudcutter stops):
screen -r cloudcutter
```

---

## Recommended Setup Flow

### Best Practice (With Ethernet)

```bash
# 1. Connect Orange Pi via ETHERNET
# 2. SSH via Ethernet IP (check router)
ssh root@192.168.1.10

# 3. Verify you have both connections
ip addr
# eth0: 192.168.1.10 (Ethernet - stays connected)
# wlan0: (will be used by cloudcutter)

# 4. Run cloudcutter using wlan0
cd /root/tuya-cloudcutter
./tuya-cloudcutter.sh -w wlan0

# 5. SSH stays connected via eth0!
# 6. Monitor cloudcutter output in real-time
```

### Without Ethernet

```bash
# 1. SSH into Orange Pi
ssh root@192.168.1.10

# 2. Start screen session
screen -S cloudcutter

# 3. Run cloudcutter
cd /root/tuya-cloudcutter
./tuya-cloudcutter.sh -w wlan0

# 4. Detach: Ctrl+A then D
# 5. Exit SSH (cloudcutter keeps running)

# 6. After flashing is done:
# - Orange Pi will reconnect to WiFi automatically
# - SSH back in: ssh root@192.168.1.10
# - Reattach screen: screen -r cloudcutter
```

---

## What Happens During Flashing

### Timeline

1. **Before cloudcutter**: Orange Pi connected to your WiFi (192.168.1.10)
2. **Start cloudcutter**: WiFi adapter taken over
   - Orange Pi loses WiFi connection
   - If on Ethernet: stays accessible
   - If SSH via WiFi: connection drops
3. **Cloudcutter creates fake AP**: "cloudcutter-XXXX" or similar
4. **Bulb connects** to fake AP
5. **Flash happens** (5-10 minutes)
6. **Cloudcutter exits**: Releases WiFi adapter
7. **After cloudcutter**: Orange Pi reconnects to your WiFi automatically

---

## Checking Network Status

### Before Running Cloudcutter

```bash
# Check current network config
ip addr

# Check current WiFi connection
iwconfig

# Check routes
ip route
```

### If Orange Pi Has Multiple Network Interfaces

```bash
# List all interfaces
ip link show

# Typical Orange Pi Zero Plus:
# eth0 - Ethernet
# wlan0 - WiFi
# wlan1 - USB WiFi (if plugged in)
```

---

## Troubleshooting

### "Can't reconnect after cloudcutter finishes"

```bash
# Check if WiFi is blocked
rfkill list
rfkill unblock wifi

# Restart network service
systemctl restart networking

# Or manually reconnect
nmcli device wifi connect "YourSSID" password "YourPassword"
```

### "SSH dropped in the middle of flashing"

If you were SSH'd via WiFi:
- **Don't worry!** Cloudcutter keeps running
- Wait 10-15 minutes for flash to complete
- Orange Pi will reconnect automatically
- SSH back in to check results

### "Need to monitor progress remotely"

**Best solution**: Connect via Ethernet!

Alternative: Use the web interface
- Cloudcutter may start a web interface
- Access it via Orange Pi's IP (if on Ethernet)
- Or access from phone connected to the fake AP

---

## Recommended Hardware Setup

**Ideal Setup for Batch Flashing:**

```
Orange Pi Zero Plus
├── Ethernet cable → Router (stays connected)
├── Built-in WiFi → Used by cloudcutter
├── Power supply
└── (Optional) Monitor + keyboard for direct access

With this setup:
✅ Never lose SSH connection
✅ Monitor cloudcutter output in real-time
✅ Flash multiple bulbs in a row
✅ No interruptions
```

---

## Summary

**Question**: Will cloudcutter disconnect Orange Pi from network?
**Answer**: YES, if using WiFi for both network and cloudcutter

**Best Solutions (in order)**:
1. ✅ **Use Ethernet** - Orange Pi stays online via eth0, WiFi used for cloudcutter
2. ✅ **Use USB WiFi Dongle** - One for network, one for cloudcutter  
3. ⚠️ **Use screen/tmux** - Let SSH drop, cloudcutter keeps running
4. ⚠️ **Physical access** - Monitor + keyboard directly on Orange Pi

**Bottom Line**: Connect Orange Pi via **Ethernet cable** for the best experience!
