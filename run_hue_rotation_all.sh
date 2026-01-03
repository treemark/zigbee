#!/bin/bash
# Run color rotation animation on all connected bulbs using Java CLI

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║   Running Color Rotation on All Connected Bulbs          ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""
echo "Starting color rotation animation..."
echo "  Device: obk17811957"
echo "  Cycles: 3 complete rotations"
echo "  Hue Step: 10° (36 steps per rotation)"
echo "  Delay: 50ms between updates"
echo "  Total Duration: ~5.4 seconds"
echo ""

# Use Gradle to run the CLI command
./gradlew :philips:bootRun --args='--spring.profiles.active=cli' --console=plain <<EOF
obk obk17811957 on
exit
EOF

echo ""
echo "Device turned on. Now starting Python script for smooth color rotation..."
echo ""

# Run the Python script which has the color rotation implemented
python3 test_hue_rotation.py

echo ""
echo "✓ Color rotation animation complete!"
echo ""
