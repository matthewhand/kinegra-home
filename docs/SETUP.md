# Setup and recovery

These steps configure two-app window support on NVIDIA SHIELD with SHIELD Experience 9.x (Android 11). Keep the stock launcher available during setup.

1. Enable Developer options and Network debugging on your own Shield.
2. Enable resizable/freeform activities for two-app layouts. On compatible Android 11 firmware these developer settings may be exposed in the UI. A trusted computer can inspect `enable_freeform_support` and `force_resizable_activities` under Android's global settings; the optional computer helper requires both to read `1`.
3. On a deliberately configured development device, the corresponding commands are `adb shell settings put global enable_freeform_support 1` and `adb shell settings put global force_resizable_activities 1`. These are device-wide settings. Some firmware may need a restart. Record previous values if you intend to restore them; the app does not silently grant this access.
4. Open Kinegra Home, select two installed apps and choose **Connect locally**. Approve the app's debugging prompt and remember that approval if desired. Its generated key stays in app-private storage.
5. Launch Split View. Each selected app is initially prepared fullscreen before returning to the canvas.

For cross-app remote shortcuts, enable **Kinegra Home session shortcuts** in Android Accessibility settings. Ordinary launcher buttons work without that service. Pair selection and layout controls are native Android UI; apps may pause when unfocused or decline particular window sizes.

If a pane closes, use its individual Reload action. If helper support is unavailable, return to the stock launcher and reconnect locally. An optional trusted-computer starter is available after building the helper: `python3 tools/start_window_broker.py --serial YOUR_DEVICE_SERIAL`. Verify your own device before using it. Do not uninstall or clear data to force an update through a signing mismatch.

PiP can still show a blank pane with some applications. Check that app fullscreen first, and use Split View for the primary supported workflow. Task visibility alone does not prove rendered content.

## Start Kinegra Home at boot

**Requires an installed Kinegra Home 0.9.27 or later.** The [0.9.25 beta](https://github.com/matthewhand/kinegra-home/releases/tag/v0.9.25) on GitHub uses the manual setup above.

1. Connect to your SHIELD over ADB. Replace `DEVICE_IP:ADB_PORT` with your device’s Network debugging address.

   ```sh
   adb connect DEVICE_IP:ADB_PORT
   adb -s DEVICE_IP:ADB_PORT shell cmd shortcut get-default-launcher --user 0
   ```

2. Record the current Home component so you can restore it. Select Kinegra as Android Home for user 0 and verify the selection.

   ```sh
   adb -s DEVICE_IP:ADB_PORT shell cmd package set-home-activity --user 0 tv.metaxy.proof/.DashboardActivity
   adb -s DEVICE_IP:ADB_PORT shell cmd shortcut get-default-launcher --user 0
   ```

   The second command should report Kinegra’s `tv.metaxy.proof.DashboardActivity`. Some SHIELD firmware gives the stock launcher priority when you press Home even with this role assigned.

3. Enable **Kinegra Home session shortcuts** in Android Accessibility settings. This is required for boot startup on SHIELD firmware that prioritizes the stock launcher. Wait a few seconds so the service can record the current boot, then reboot. Kinegra should open the Dashboard automatically once Android unlocks user 0.
4. For automatic **Split View**, complete the manual two-app setup above once and leave Network debugging enabled for the local window helper. When the Dashboard first receives focus, it gives you five seconds to press a remote button or touch the screen to cancel. The saved pair opens if both apps are available and local window setup has been completed. App preparation can take additional time.

### Restore stock startup

Set Android Home back to the component you recorded before selecting Kinegra. On the tested SHIELD firmware, the stock component is `com.google.android.tvlauncher/.MainActivity`.

```sh
adb -s DEVICE_IP:ADB_PORT shell cmd package set-home-activity --user 0 com.google.android.tvlauncher/.MainActivity
```

That Home role change turns off Kinegra boot startup. You can still use Kinegra and its session shortcuts by opening it from the apps list.
