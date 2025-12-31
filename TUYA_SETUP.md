# Tuya Local Control Setup Guide


systemctl restart systemd-resolved

./tuya-cloudcutter.sh -w wlan1 -s AV-nest maxvector
apt-get install network-manager

Selected Device Slug: daybetter-rgbct-bulb-v1.2.16
Selected Profile: oem-bk7231n-light-ty-1.2.16-sdk-2.3.1-40.00
Selected Firmware: OpenBeken-v1.18.219_bk7231n.ug.bin



## Prerequisites
✅ Tuya Developer Account (you have this)
✅ tinytuya installed in venv-tuya

## Step 1: Get Tuya Cloud Credentials

You need 3 pieces of information from your Tuya Developer account:

1. **API Access ID** (also called Client ID)
2. **API Access Secret** (also called Client Secret)  
3. **User ID** (from your Tuya Smart/Smart Life app account)

### How to get these:

1. Go to https://iot.tuya.com/
2. Log in with your developer account
3. Navigate to **Cloud** → **Development**
4. Create a new project or select existing one
5. Go to the project and find:
   - **Access ID/Client ID**
   - **Access Secret/Client Secret**
6. For User ID:
   - Link your Smart Life/Tuya Smart app account to this project
   - The User ID is typically your country code + phone number (e.g., 1-555-1234567)

## Step 2: Configure Tuya Project APIs

**IMPORTANT**: Before running the wizard, configure your project:

1. Go to https://iot.tuya.com/
2. Select your project (or create a new one)
3. Go to **Service API** (or **API** section)
4. Click **Go to Authorize** 
5. Subscribe/Enable these APIs:
   - **IoT Core** (required) - Click "Subscribe" if not already
   - **Authorization** (required)
   - **Smart Home Scene Linkage** (helps with device discovery)
6. **CRITICAL**: Link your app account:
   - Go to **Devices** tab → **Link Tuya App Account** (or **Link Devices**)
   - Scan the QR code with your Smart Life or Tuya Smart app
   - Your devices must appear in the IoT Console device list
7. Note your **Data Center Region** (US/EU/CN/IN) - you'll need this for the wizard

**Common Errors & Fixes:**

**Error 28841107: 'The data center is suspended'**
This means you need to explicitly enable data centers for your project:

**How to Enable Data Center:**
1. In your Tuya IoT project, look for **Cloud** section or **Overview** page
2. You should see a list of **Data Centers** with regions (US, EU, China, India)
3. Each data center will show "Available" or buttons to enable
4. Click **Enable** or **Authorize** for your region (e.g., "Central Europe Data Center" or "Western America Data Center")
5. Alternative locations to find this:
   - **Cloud** → **API** → Check available data centers
   - **Overview** page → Look for "Data Center" status
   - **Project Settings** → Check data center availability
6. After enabling, wait 1-2 minutes before running the wizard

**If you still can't find it:**
- Your project might be using the old dashboard
- Try creating a **NEW** project - new projects often have data centers pre-enabled
- Or contact Tuya support - some accounts need data center access manually enabled

**Error 1106: 'permission deny'**
- Your Smart Life/Tuya Smart app account is NOT properly linked
- Fix:
  1. In your Tuya project, go to **Devices** → **Link Tuya App Account**
  2. Scan the QR code with your Smart Life or Tuya Smart app
  3. Make sure your devices show up in the Tuya IoT Console under **Devices**
  4. Verify you selected the correct region (US/EU/CN/IN) that matches where your app account was created
  5. Wait a few minutes after linking before running wizard

## Step 3: Run the Tuya Wizard

The wizard will:
- Connect to Tuya Cloud using your credentials
- Download all device information (IDs, local keys, IPs)
- Save everything to `devices.json`
- Scan your local network for devices

Run it with:
```bash
source venv-tuya/bin/activate
python -m tinytuya wizard
```

Follow the prompts and enter your:
- API Access ID
- API Access Secret  
- API region (us, eu, cn, in)
- Device selection

## Step 3: Test Device Discovery

After the wizard completes, you'll have a `devices.json` file with:
- Device IDs
- Local encryption keys
- Device IPs
- Device names

## Step 4: Test Basic Control

Create a simple test script to control a device.

## Step 5: Color Rotation Test

Similar to what we did with Hue lights!

---

**Next Command to Run:**
```bash
source venv-tuya/bin/activate
python -m tinytuya wizard
```
