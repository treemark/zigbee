#!/usr/bin/env python3
"""
Tuya Color Rotation Test Script (FAST VERSION with Threading)
Rotates all bulbs through the color spectrum for 60 seconds
Uses threading to send commands in PARALLEL for smooth animation

This script communicates DIRECTLY with your bulbs over your LOCAL network.
"""

import tinytuya
import json
import time
from concurrent.futures import ThreadPoolExecutor
import threading

def load_devices():
    """Load devices from devices.json"""
    try:
        with open('devices.json', 'r') as f:
            data = json.load(f)
            if isinstance(data, list):
                return data
            return data.get('devices', [])
    except FileNotFoundError:
        print("❌ devices.json not found!")
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

def set_bulb_color(bulb_device, h, s, v):
    """Set bulb color in a separate thread"""
    try:
        bulb_device.set_hsv(h=h, s=s, v=v, nowait=True)
    except Exception:
        pass  # Silently ignore errors during rapid animation

def test_color_rotation():
    """Rotate all bulbs through color spectrum for 60 seconds"""
    devices_info = load_devices()
    
    if not devices_info:
        print("❌ No devices found in devices.json")
        return
    
    print("=" * 60)
    print("TUYA COLOR ROTATION TEST - FAST VERSION (Threading)")
    print("=" * 60)
    
    # Create device objects (skip status() - it's slow!)
    print("\n⏱️  Initializing devices (fast mode - skipping status check)...")
    init_start = time.time()
    bulbs = []
    
    for device_info in devices_info:
        device_name = device_info.get('name', 'Unknown')
        ip_address = device_info.get('ip', '')
        
        if not ip_address:
            continue
            
        category = device_info.get('category', '')
        if category not in ['dj', '']:
            continue
        
        version = device_info.get('version', '3.3')
        if not version:
            version = '3.3'
        
        bulb = tinytuya.BulbDevice(
            dev_id=device_info['id'],
            address=ip_address,
            local_key=device_info['key'],
            version=float(version)
        )
        
        bulbs.append({
            'device': bulb,
            'name': device_name
        })
    
    init_elapsed = time.time() - init_start
    print(f"✅ Found {len(bulbs)} bulb(s) in {init_elapsed:.2f}s\n")
    
    if not bulbs:
        print("❌ No bulbs to control")
        return
    
    # Create thread pool for parallel commands
    executor = ThreadPoolExecutor(max_workers=len(bulbs))
    
    # Turn all bulbs on in parallel
    print("💡 Turning all bulbs ON (parallel)...")
    on_start = time.time()
    futures = [executor.submit(lambda b: b['device'].turn_on(nowait=True), bulb) for bulb in bulbs]
    for f in futures:
        f.result()  # Wait for all to complete
    on_elapsed = time.time() - on_start
    print(f"⏱️  Turn-on took {on_elapsed:.2f}s\n")
    
    time.sleep(1.5)  # Give bulbs time to turn on
    
    # Rotate through colors for 60 seconds
    print("🌈 Starting color rotation (60s with PARALLEL commands)...\n")
    
    start_time = time.time()
    duration = 60.0
    color_change_interval = 0.1  # 100ms = 10 FPS
    
    num_steps = int(duration / color_change_interval)
    hue_step = 360 / num_steps
    
    current_hue = 0
    step = 0
    last_print_time = start_time
    loop_times = []
    
    while (time.time() - start_time) < duration:
        loop_start = time.time()
        
        # Print status once per second
        if loop_start - last_print_time >= 1.0:
            elapsed = loop_start - start_time
            color_name = hue_to_color_name(current_hue)
            avg_loop = sum(loop_times[-20:]) / len(loop_times[-20:]) if loop_times else 0
            fps = 1.0 / avg_loop if avg_loop > 0 else 0
            print(f"🎨 {elapsed:5.1f}s - {color_name:8s} (hue: {int(current_hue):3d}°) - {len(loop_times)} frames - {fps:.1f} FPS")
            last_print_time = loop_start
        
        # Convert to normalized HSV (0-1)
        h = current_hue / 360.0
        s = 1.0  # Full saturation
        v = 1.0  # Full brightness
        
        # Send color commands to ALL bulbs in PARALLEL
        futures = [executor.submit(set_bulb_color, bulb['device'], h, s, v) for bulb in bulbs]
        
        # Wait for all commands to complete
        for f in futures:
            f.result()
        
        loop_elapsed = time.time() - loop_start
        loop_times.append(loop_elapsed)
        
        # Sleep for remaining time
        sleep_time = max(0, color_change_interval - loop_elapsed)
        if sleep_time > 0:
            time.sleep(sleep_time)
        
        step += 1
        current_hue = (step * hue_step) % 360
    
    # Statistics
    total_elapsed = time.time() - start_time
    avg_loop_time = sum(loop_times) / len(loop_times) if loop_times else 0
    avg_fps = 1.0 / avg_loop_time if avg_loop_time > 0 else 0
    
    print(f"\n📊 Statistics:")
    print(f"   Total frames: {len(loop_times)}")
    print(f"   Actual duration: {total_elapsed:.2f}s")
    print(f"   Average FPS: {avg_fps:.1f}")
    print(f"   Average loop time: {avg_loop_time*1000:.1f}ms")
    print(f"   Target interval: {color_change_interval*1000:.0f}ms")
    
    print("\n🌈 Color rotation complete!")
    
    # Turn all bulbs off in parallel
    print("🌙 Turning all bulbs OFF (parallel)...")
    futures = [executor.submit(lambda b: b['device'].turn_off(nowait=True), bulb) for bulb in bulbs]
    for f in futures:
        f.result()
    
    executor.shutdown()
    print("\n✅ Test complete!")

if __name__ == "__main__":
    print("\n🌈 TUYA COLOR ROTATION TEST (FAST)\n")
    test_color_rotation()
