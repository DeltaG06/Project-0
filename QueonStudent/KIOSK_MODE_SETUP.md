# Kiosk Mode Setup for Queon Student App

## 🔒 Security Levels

The app supports 3 levels of security:

### Level 1: Basic Mode (Current - Easy to Bypass ❌)
- **What it does**: Immersive mode, back button blocked
- **Problem**: User can still swipe up and unpin easily
- **Use case**: Development/testing only

### Level 2: Device Admin Mode (Medium Security ⚠️)
- **What it does**: Enhanced lock task + device policies
- **Setup**: User must manually enable Device Admin
- **Problem**: User can still disable admin and exit
- **Use case**: Semi-trusted environments

### Level 3: Device Owner Mode (UNBREAKABLE 🔐)
- **What it does**: Full kiosk mode - CANNOT be bypassed
- **Setup**: Requires factory reset + ADB command
- **Benefit**: User CANNOT exit without admin password
- **Use case**: Production exams

---

## 🚀 Setup for UNBREAKABLE Kiosk Mode (Recommended)

### Prerequisites
- USB cable
- ADB installed on PC
- **Device must be factory reset** (Device Owner requires clean device)

### Step-by-Step Instructions

#### 1. Factory Reset the Phone
```
Settings → System → Reset → Factory Data Reset
```
⚠️ **WARNING**: This will erase ALL data on the device!

#### 2. Skip Initial Setup (Don't add Google account yet)
- Skip Wi-Fi setup or connect without account
- Skip all Google account prompts
- Complete minimal setup to reach home screen

#### 3. Enable Developer Options & USB Debugging
```
Settings → About Phone → Tap "Build Number" 7 times
Settings → Developer Options → Enable "USB Debugging"
```

#### 4. Connect Phone to PC via USB
- Accept USB debugging prompt on phone
- Verify connection:
```bash
adb devices
```

#### 5. Install the Queon App
```bash
cd c:\Queon-Project\Project-0\QueonStudent
.\gradlew installDebug
```

#### 6. Set App as Device Owner
```bash
adb shell dpm set-device-owner com.gaurav.queon/.QueonDeviceAdminReceiver
```

You should see:
```
Success: Device owner set to package com.gaurav.queon
```

#### 7. Test Kiosk Mode
- Open Queon app
- Start an exam (scan ENTRY QR)
- Try to exit → **YOU CAN'T!** 🎉
- The app will show: "🔒 Exam mode activated - Full Kiosk Mode (Device Owner)"

---

## 🔓 How to Exit Device Owner Mode (For Testing)

If you need to remove device owner (for development):

```bash
# Remove device owner
adb shell dpm remove-active-admin com.gaurav.queon/.QueonDeviceAdminReceiver

# Or factory reset again
```

---

## 📱 Alternative: Manual Device Admin (Weaker)

If you can't factory reset, use Device Admin (less secure):

1. Open Queon app
2. Go to Settings (add a settings button in HomeScreen)
3. Tap "Enable Device Admin"
4. Accept the permission prompt

**Note**: This is still bypassable - user can go to Settings and disable it.

---

## 🎯 Production Deployment Checklist

For real exam centers:

- [ ] Factory reset all exam devices
- [ ] Set Queon as Device Owner on each device
- [ ] Test kiosk mode (try to exit - should be impossible)
- [ ] Configure Wi-Fi to connect to backend server
- [ ] Disable "Remove Device Owner" option in app
- [ ] Keep admin PC with ADB access for emergency unlock

---

## Current Status

After rebuilding the app, it will show one of these messages:

- **"Full Kiosk Mode (Device Owner)"** ✅ Unbreakable
- **"Enhanced Mode (Device Admin)"** ⚠️ Can be disabled
- **"Basic Mode (Manual Pinning Required)"** ❌ Easy to bypass

**For production exams, you MUST use Device Owner mode!**

---

## 🕵️ Cheating Detection System (New)

Even if a student manages to bypass the Kiosk mode (or if you are using the weaker "Basic Mode"), the app now includes **Active Incident Reporting**.

### How it works
1.  **Focus Tracking**: If the student tries to switch apps, minimize Queon, or answer a call, the app detects `ON_PAUSE` lifecycle event.
2.  **Instant Reporting**: The app immediately sends a `FOCUS_LOST` incident report to the backend server with a timestamp.
3.  **Kiosk Bypass**: If Kiosk mode fails to start (e.g. pinned mode cancelled), a `KIOSK_BYPASS_ATTEMPT` is reported.

### Backend Logs
You can see these incidents in the backend console (and later in the admin dashboard):
```
🚨 INCIDENT REPORTED [FOCUS_LOST] for Exam 123
Details: App lost focus (minimized or switched)
Time: 12/14/2025, 12:30:45 PM
```

### Strategy
- **Deterrence**: Tell students "The app reports if you switch screens."
- **Enforcement**: Anyone with a `FOCUS_LOST` incident gets their exam invalidated.


