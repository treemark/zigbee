#!/usr/bin/env python3
"""
Tuya Color Rotation Test Script
Rotates all bulbs through the color spectrum for 10 seconds
Similar to the Hue color rotation test

This script communicates DIRECTLY with your bulbs over your LOCAL network.
No cloud communication during color changes (only local UDP/TCP to bulb IPs).
"""

import tinytuya
import json
import time
import colorsys

def load_devices():
    """Load devices from devices.json"""
    try:
        with open('devices.json', 'r') as f:
            data = json.load(f)
            # devices.json is a list of devices, not a dict with 'devices' key
            if isinstance(data, list):
                return data
            return data.get('devices', [])
    except FileNotFoundError:
        print("❌ devices.json not found!")
        print("   Run: python -m tinytuya wizard")
        return []

def hue_to_color_name(hue):
    """Convert hue (0-360) to color name"""
    if hue < 30 or hue >= 330:
        return "Red"
    elif hue < 60:
        return "Orange"
    elif hue < 90:
        return "Yellow"
    elif hue < 150:
        return "Green"
    elif hue < 210:
        return "Cyan"
    elif hue < 270:
        return "Blue"
    elif hue < 330:
        return "Magenta"
    return "Red"

def hsv_to_tuya_color(hue, saturation=100, value=100):
    """
    Convert HSV to Tuya color format
    
    Args:
        hue: 0-360
        saturation: 0-100
        value: 0-100 (brightness)
    
    Returns:
        dict with Tuya color format (normalized 0-1)
    """
    # Tuya set_hsv expects values normalized to 0-1:
    # - Hue: 0-1 (0-360 degrees)
    # - Saturation: 0-1 (0-100%)
    # - Value: 0-1 (0-100% brightness)
    
    return {
        'h': hue / 360.0,           # 0-360 -> 0-1
        's': saturation / 100.0,    # 0-100 -> 0-1
        'v': value / 100.0          # 0-100 -> 0-1
    }

def test_color_rotation():
    """Rotate all bulbs through color spectrum for 60 seconds"""
    devices_info = load_devices()
    
    if not devices_info:
        print("❌ No devices found in devices.json")
        return
    
    print("=" * 50)
    print("Starting 60-second color rotation test")
    print("=" * 50)
    
    # Create device objects for all bulbs (skip devices without IP addresses)
    print("\n⏱️  Initializing devices...")
    init_start = time.time()
    bulbs = []
    for device_info in devices_info:
        device_name = device_info.get('name', 'Unknown')
        ip_address = device_info.get('ip', '')
        
        # Skip devices without IP addresses
        if not ip_address or ip_address == '':
            print(f"⏩ Skipping {device_name} (no IP address)")
            continue
            
        # Skip non-bulb devices (like smart plugs)
        category = device_info.get('category', '')
        if category not in ['dj', '']:  # 'dj' is the bulb category
            print(f"⏩ Skipping {device_name} (not a bulb, category: {category})")
            continue
        
        print(f"📱 Found: {device_name}")
        
        # Handle version - default to 3.3 if empty or missing
        version = device_info.get('version', '3.3')
        if not version or version == '':
            version = '3.3'
        
        bulb = tinytuya.BulbDevice(
            dev_id=device_info['id'],
            address=ip_address,
            local_key=device_info['key'],
            version=float(version)
        )
        
        # Initialize bulb by getting its status (required for set_hsv to work)
        try:
            bulb.status()  # Fetches device capabilities
        except Exception as e:
            print(f"   ⚠️  Warning: Could not get status for {device_name}: {e}")
        
        bulbs.append({
            'device': bulb,
            'name': device_name
        })
    
    init_elapsed = time.time() - init_start
    print(f"⏱️  Initialization took {init_elapsed:.2f} seconds\n")
    
    if not bulbs:
        print("❌ No bulbs to control")
        return
    
    print(f"✅ Found {len(bulbs)} bulb(s) for color rotation\n")
    
    # Turn all bulbs on (fast with nowait)
    print("💡 Turning all bulbs ON...")
    on_start = time.time()
    for bulb_info in bulbs:
        bulb_info['device'].turn_on(nowait=True)
    time.sleep(1.0)  # Give bulbs time to turn on
    on_elapsed = time.time() - on_start
    print(f"⏱️  Turn-on took {on_elapsed:.2f} seconds\n")
    
    # Rotate through colors for 60 seconds with FAST animation
    print("🌈 Starting color rotation sequence (60 seconds)...\n")
    
    start_time = time.time()
    duration = 60.0  # 60 seconds
    color_change_interval = 0.05  # Change every 50ms for smooth animation
    
    # Calculate number of steps
    num_steps = int(duration / color_change_interval)
    hue_step = 360 / num_steps
    
    current_hue = 0
    step = 0
    last_print_time = start_time
    loop_times = []
    
    while (time.time() - start_time) < duration:
        loop_start = time.time()
        
        # Print status only once per second (not every frame)
        if loop_start - last_print_time >= 1.0:
            elapsed = loop_start - start_time
            color_name = hue_to_color_name(current_hue)
            avg_loop = sum(loop_times[-20:]) / len(loop_times[-20:]) if loop_times else 0
            print(f"🎨 {elapsed:5.1f}s - {color_name:8s} (hue: {int(current_hue):3d}°) - {len(loop_times)} frames - avg: {avg_loop*1000:.1f}ms/loop")
            last_print_time = loop_start
        
        # Convert to Tuya format
        color = hsv_to_tuya_color(
            hue=current_hue,
            saturation=100,  # Full saturation
            value=100        # Full brightness
        )
        
        # Apply to all bulbs (measure time for this)
        send_start = time.time()
        for bulb_info in bulbs:
            try:
                # Use set_hsv with individual parameters
                bulb_info['device'].set_hsv(
                    h=color['h'],
                    s=color['s'],
                    v=color['v'],
                    nowait=True  # Don't wait for response (faster)
                )
            except Exception as e:
                print(f"   ⚠️  Error setting color for {bulb_info['name']}: {e}")
        send_elapsed = time.time() - send_start
        
        # Track loop timing
        loop_elapsed = time.time() - loop_start
        loop_times.append(loop_elapsed)
        
        # Next color (account for time already spent)
        sleep_time = max(0, color_change_interval - loop_elapsed)
        if sleep_time > 0:
            time.sleep(sleep_time)
        step += 1
        current_hue = (step * hue_step) % 360
    
    # Print statistics
    total_elapsed = time.time() - start_time
    avg_loop_time = sum(loop_times) / len(loop_times) if loop_times else 0
    print(f"\n📊 Statistics:")
    print(f"   Total frames: {len(loop_times)}")
    print(f"   Actual duration: {total_elapsed:.2f}s")
    print(f"   Average loop time: {avg_loop_time*1000:.2f}ms")
    print(f"   Target interval: {color_change_interval*1000:.2f}ms")
    
    print("\n🌈 Color rotation complete!")
    
    # Turn all bulbs off (fast with nowait)
    print("🌙 Turning all bulbs OFF...")
    for bulb_info in bulbs:
        bulb_info['device'].turn_off(nowait=True)
    
    print("\n✅ Test complete!")

if __name__ == "__main__":
    print("\n🌈 TUYA COLOR ROTATION TEST\n")
    test_color_rotation()
