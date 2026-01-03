#!/bin/bash

# OpenBeken Device Control Script
# Device: obk17811957 at 192.168.86.66
# MQTT Broker: localhost:1883

DEVICE_IP="192.168.86.66"
DEVICE_NAME="obk17811957"
APP_URL="http://localhost:8080"

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║     OpenBeken Device Control - obk17811957                    ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""
echo "Device Status:"
echo "  IP Address: $DEVICE_IP"
echo "  MQTT Name: $DEVICE_NAME"
echo "  Web UI: http://$DEVICE_IP/"
echo ""

# Check if device is reachable
if ping -c 1 -W 1 $DEVICE_IP > /dev/null 2>&1; then
    echo "✅ Device is online"
else
    echo "❌ Device is not reachable"
    exit 1
fi

# Check if application is running
if curl -s "$APP_URL/actuator/health" > /dev/null 2>&1; then
    echo "✅ Application is running"
else
    echo "❌ Application is not running. Start it with: ./gradlew :philips:bootRun"
    exit 1
fi

# Check MQTT connection status
MQTT_STATUS=$(curl -s "http://$DEVICE_IP/index?state=1" | grep -o "MQTT State: <span style=\"color:[^\"]*\">[^<]*</span>" | sed 's/<[^>]*>//g' | cut -d: -f2 | xargs)
echo "  MQTT Status: $MQTT_STATUS"
echo ""

# Show menu
echo "═══════════════════════════════════════════════════════════════"
echo "Control Methods:"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "1. Direct HTTP Commands (to device)"
echo "   - Turn on:  curl 'http://$DEVICE_IP/cm?cmnd=POWER1%201'"
echo "   - Turn off: curl 'http://$DEVICE_IP/cm?cmnd=POWER1%200'"
echo "   - Toggle:   curl 'http://$DEVICE_IP/cm?cmnd=POWER1%202'"
echo ""
echo "2. MQTT Commands (via broker)"
echo "   - Turn on:  mosquitto_pub -h localhost -p 1883 -t 'cmnd/$DEVICE_NAME/POWER1' -m '1'"
echo "   - Turn off: mosquitto_pub -h localhost -p 1883 -t 'cmnd/$DEVICE_NAME/POWER1' -m '0'"
echo ""
echo "3. Via Application API (REST)"
echo "   - Note: Currently the app is designed for Zigbee2MQTT devices"
echo "   - OpenBeken uses different MQTT topic structure"
echo "   - You can use direct HTTP or MQTT commands above"
echo ""
echo "═══════════════════════════════════════════════════════════════"
echo ""

# Interactive menu
echo "What would you like to do?"
echo "  1) Turn device ON (via HTTP)"
echo "  2) Turn device OFF (via HTTP)"
echo "  3) Toggle device (via HTTP)"
echo "  4) View device web interface (browser)"
echo "  5) Check device status"
echo "  6) Exit"
echo ""
read -p "Enter choice [1-6]: " choice

case $choice in
    1)
        echo ""
        echo "Sending ON command..."
        curl -s "http://$DEVICE_IP/cm?cmnd=POWER1%201"
        echo ""
        echo "✅ Command sent!"
        ;;
    2)
        echo ""
        echo "Sending OFF command..."
        curl -s "http://$DEVICE_IP/cm?cmnd=POWER1%200"
        echo ""
        echo "✅ Command sent!"
        ;;
    3)
        echo ""
        echo "Sending TOGGLE command..."
        curl -s "http://$DEVICE_IP/cm?cmnd=POWER1%202"
        echo ""
        echo "✅ Command sent!"
        ;;
    4)
        echo ""
        echo "Opening device web interface..."
        open "http://$DEVICE_IP/"
        ;;
    5)
        echo ""
        echo "Device Status:"
        curl -s "http://$DEVICE_IP/index?state=1" | grep -E "(MQTT State|temperature|RSSI|drivers active)"
        echo ""
        ;;
    6)
        echo "Exiting..."
        exit 0
        ;;
    *)
        echo "Invalid choice"
        exit 1
        ;;
esac

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "Next Steps:"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "1. Configure device type and pins:"
echo "   - Visit: http://$DEVICE_IP/cfg"
echo "   - Go to 'Configure Module' to set pin roles"
echo "   - Choose appropriate template for your device"
echo ""
echo "2. View Swagger UI for full API:"
echo "   - Visit: $APP_URL/swagger-ui.html"
echo ""
echo "3. Check MQTT broker logs:"
echo "   - tail -f /tmp/philips_app.log"
echo ""
echo "4. For more information:"
echo "   - See: philips/OPENBEKEN_MQTT_SETUP.md"
echo ""
