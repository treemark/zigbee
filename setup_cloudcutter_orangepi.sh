#!/bin/bash
# Setup script for tuya-cloudcutter on Orange Pi Zero Plus
# Run this script ON the Orange Pi as root

set -e

echo "=================================="
echo "Tuya Cloudcutter Setup for Orange Pi"
echo "=================================="

# Update system
echo "📦 Updating system..."
apt-get update -qq
apt-get install -y git python3 python3-pip python3-venv hostapd dnsmasq rfkill iw curl

# Install Docker (required by cloudcutter)
echo "🐳 Installing Docker..."
if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh
    rm get-docker.sh
    echo "✅ Docker installed"
else
    echo "✅ Docker already installed"
fi

# Clone cloudcutter
echo "📥 Cloning tuya-cloudcutter..."
cd /root
if [ -d "tuya-cloudcutter" ]; then
    echo "⚠️  tuya-cloudcutter already exists, updating..."
    cd tuya-cloudcutter
    git pull
else
    git clone https://github.com/tuya-cloudcutter/tuya-cloudcutter.git
    cd tuya-cloudcutter
fi

# Check wifi adapter
echo ""
echo "🔍 Detecting WiFi adapter..."
WIFI_ADAPTER=$(iw dev | grep Interface | awk '{print $2}' | head -n 1)

if [ -z "$WIFI_ADAPTER" ]; then
    echo "❌ No WiFi adapter found!"
    echo "   Please ensure your Orange Pi has a working WiFi adapter"
    exit 1
fi

echo "✅ Found WiFi adapter: $WIFI_ADAPTER"

# Stop services that might interfere
echo ""
echo "🛑 Stopping interfering services..."
systemctl stop hostapd 2>/dev/null || true
systemctl stop dnsmasq 2>/dev/null || true
systemctl stop NetworkManager 2>/dev/null || true

# Unblock WiFi
echo "📡 Unblocking WiFi..."
rfkill unblock wifi

echo ""
echo "=================================="
echo "✅ Setup Complete!"
echo "=================================="
echo ""
echo "📝 Next steps:"
echo "   1. Run cloudcutter with:"
echo "      cd /root/tuya-cloudcutter"
echo "      ./tuya-cloudcutter.sh -w $WIFI_ADAPTER"
echo ""
echo "   2. Follow the on-screen instructions"
echo "   3. Put your bulb in pairing mode (3x on/off)"
echo ""
echo "WiFi Adapter: $WIFI_ADAPTER"
echo "=================================="
