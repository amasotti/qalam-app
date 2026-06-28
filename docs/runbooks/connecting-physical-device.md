# Runbook: Connecting a Physical Android Device

This guide explains how to connect your personal Android phone to Android Studio for development and debugging. 

## Prerequisites
- Android Phone (running Android 11 or higher for the best Wireless Debugging experience).
- USB-C or Micro-USB cable.
- Both laptop and phone connected to the same Wi-Fi network.

---

## Step 1: Enable Developer Options on the Phone
Android hides developer tools by default.
1. Open **Settings** on your phone.
2. Go to **About Phone** (usually at the bottom).
3. Find the **Build Number** entry.
4. Tap **Build Number** seven (7) times. You will see a toast message: *"You are now a developer!"*
5. Go back to the main Settings menu and navigate to **System > Developer options** (or search for "Developer options" in Settings).

## Step 2: Enable USB Debugging
1. In **Developer options**, scroll down to the **Debugging** section.
2. Toggle **USB debugging** to **On**.
3. Connect your phone to your laptop via USB.
4. A prompt will appear on your phone: *"Allow USB debugging?"*
5. Check **"Always allow from this computer"** and tap **Allow**.

## Step 3: Wireless Debugging (Recommended)
Once USB debugging is authorized, you can switch to Wireless Debugging to untether from the cable.

### Method A: Pairing via QR Code (Android 11+)
1. In **Developer options**, find and tap **Wireless debugging** (don't just toggle the switch, tap the text to enter the sub-menu).
2. Toggle **Wireless debugging** to **On**.
3. Ensure your phone is on the same Wi-Fi as your laptop.
4. In Android Studio, click the **Device Manager** icon (usually top right or under `View > Tool Windows > Device Manager`).
5. Select the **Physical** tab.
6. Click **Pair using Wi-Fi**.
7. On your phone, select **Pair device with QR code**.
8. Scan the QR code displayed in Android Studio.

### Method B: Manual Connection via ADB
If the QR code fails, use the terminal:
1. Connect via USB first.
2. Open your terminal in Android Studio.
3. Restart adb in TCP mode: `adb tcpip 5555`
4. Find your phone's IP address (Settings > About Phone > Status > IP Address).
5. Connect: `adb connect <YOUR_PHONE_IP_ADDRESS>:5555`
6. Unplug the USB cable.

## Step 4: Running the App
1. In the Android Studio toolbar, look at the device dropdown (next to the green "Run" arrow).
2. Your phone should now appear in the list (e.g., "Google Pixel 8" or "Samsung SM-G991B").
3. Select your phone.
4. Press **Shift + F10** (or the green Run icon) to deploy the app.

---

## Troubleshooting

### Device not listed?
- Run `adb devices` in the terminal. If it's empty, check your cable or try a different USB port.
- Check if your phone is in "File Transfer" (MTP) mode instead of "Charging only" mode (though modern ADB usually handles both).

### Authorization Prompt doesn't appear?
- In **Developer options**, tap **Revoke USB debugging authorizations** and reconnect the cable.

### Wireless Debugging disconnects?
- Android sometimes disables Wireless Debugging when the phone sleeps or disconnects from Wi-Fi. Check the toggle in Developer options.
- Ensure your Wi-Fi isn't a "Public" network with AP isolation enabled (which prevents devices from talking to each other).
