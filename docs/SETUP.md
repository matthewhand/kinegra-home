# Setup and recovery

The supported development target is NVIDIA SHIELD with Android 11. Keep the stock launcher and an authorized recovery computer available. Installing the APK alone does not grant control of other apps.

1. Enable Developer options and Network debugging on your own Shield.
2. Enable resizable/freeform activities for two-app layouts. On compatible Android 11 firmware these developer settings may be exposed in the UI. A trusted computer can inspect `enable_freeform_support` and `force_resizable_activities` under Android's global settings; the optional computer helper requires both to read `1`.
3. On a deliberately configured development device, the corresponding commands are `adb shell settings put global enable_freeform_support 1` and `adb shell settings put global force_resizable_activities 1`. These are device-wide settings. Some firmware may need a restart. Record previous values if you intend to restore them; the app does not silently grant this access.
4. Open Kinegra Home, select two installed apps and choose **Connect locally**. Approve the app's debugging prompt and remember that approval if desired. Its generated key stays in app-private storage.
5. Launch Split View. Each selected app is initially prepared fullscreen before returning to the canvas.

Enable **Kinegra Home session shortcuts** in Android Accessibility settings only if you want the cross-app remote shortcuts. Ordinary launcher buttons work without that service. Pair selection and layout controls are native Android UI; apps may pause when unfocused or decline particular window sizes.

If a pane closes, use its individual Reload action. If helper support is unavailable, return to the stock launcher and reconnect locally. An optional trusted-computer starter is available after building the helper: `python3 tools/start_window_broker.py --serial YOUR_DEVICE_SERIAL`. Verify your own device before using it. Do not uninstall or clear data to force an update through a signing mismatch.

PiP can still show a blank pane with some applications. Check that app fullscreen first, and use Split View for the primary supported workflow. Task visibility alone does not prove rendered content.
