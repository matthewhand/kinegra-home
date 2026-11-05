#!/usr/bin/env bash
# Offline unsigned Shieldarchy dashboard v6 with original DreamService. No Shield changes.
# No network, signing, ADB, device changes, or secret-store access.
set -euo pipefail
# APK ZIP timestamps and javac source encoding must not depend on caller locale.
export TZ=UTC LC_ALL=C.UTF-8
cd "$(dirname "$0")/.."
JDK="${JAVA_HOME:?Set JAVA_HOME to a JDK 17 installation}"
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
test -n "$SDK" || { echo "Set ANDROID_HOME or ANDROID_SDK_ROOT to the Android SDK" >&2; exit 3; }
export PATH="$JDK/bin:$PATH"
JAVA="$JDK/bin/java"
ANDROID="$SDK/platforms/android-34/android.jar"
AAPT2="$SDK/build-tools/34.0.0/aapt2"
D8="$SDK/build-tools/34.0.0/d8"
ZIPALIGN="$SDK/build-tools/34.0.0/zipalign"
OUT="build/Shieldarchy-Dashboard-v0.6-rebuilt-UNSIGNED.apk"
for executable in "$JAVA" "$JDK/bin/jar" "$JDK/bin/javac" "$AAPT2" "$D8" "$ZIPALIGN" python3 unzip zip; do
  command -v "$executable" >/dev/null || { echo "MISSING_TOOL $executable";exit 3; }
done
test -r "$ANDROID" || { echo "MISSING_PINNED_ANDROID_PLATFORM"; exit 3; }
echo WINDOW_BROKER_SECURITY_PYTHON_TEST
python3 tests/test_window_broker_security.py
echo "6cea1df3efb77103ac3e2beb9bf4718964b0e0869ab16d39d29d5cbae1c147ad  $ANDROID" | sha256sum --check --status
mkdir -p build
WORK="$(mktemp -d build/.shieldarchy-asus-v06-XXXXXX)"
trap 'case "$WORK" in build/.shieldarchy-asus-v06-*) rm -rf -- "$WORK";; esac' EXIT
mkdir -p "$WORK/classes" "$WORK/testclasses" "$WORK/dex" "$WORK/res/values" "$WORK/res/xml" "$WORK/res/drawable-nodpi"
cp harness/res/values/strings.xml "$WORK/res/values/"
cp harness/res/xml/metaxy_dream.xml harness/res/xml/session_shortcuts.xml "$WORK/res/xml/"
cp assets/launcher/banner-kinegra-home-320x180.png "$WORK/res/drawable-nodpi/metaxy_banner.png"
cp assets/launcher/icon-512.png "$WORK/res/drawable-nodpi/metaxy_icon.png"
echo PURE_JVM_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" android/dream/DreamCadence.java tests/test_dream_cadence.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dream.test_dream_cadence
echo REAL_LAUNCH_REGISTRY_PURE_JAVA_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" \
  android/dashboard/LaunchRegistry.java tests/test_launch_registry.java
echo REMOTE_FOCUS_POLICY_PURE_JVM_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" \
  android/dashboard/RemoteFocusPolicy.java tests/test_remote_focus_policy.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_remote_focus_policy
echo REMOTE_UI_SESSION_OFFLINE_END_TO_END_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" \
  android/dashboard/LaunchRegistry.java android/dashboard/RemoteFocusPolicy.java android/dashboard/RemoteUiSession.java tests/test_remote_ui_session.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_remote_ui_session
echo ADVERSARIAL_REMOTE_UI_JVM_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" \
  android/dashboard/LaunchRegistry.java android/dashboard/RemoteFocusPolicy.java \
  android/dashboard/RemoteUiSession.java tests/test_adversarial_remote_ui.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_adversarial_remote_ui
echo FINAL_REMOTE_UI_REGRESSION_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" \
  android/dashboard/RemoteFocusPolicy.java android/dashboard/RemoteUiSession.java \
  tests/test_final_remote_ui_regressions.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_final_remote_ui_regressions
echo ACCESSIBLE_DASHBOARD_PRESENTATION_POLICY_JVM_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" \
  android/dashboard/DashboardPresentationPolicy.java tests/test_dashboard_presentation_policy.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_dashboard_presentation_policy
echo DASHBOARD_MENU_LABEL_JVM_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" \
  android/dashboard/DashboardPresentationPolicy.java tests/test_dashboard_menu_label.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_dashboard_menu_label
echo RESPONSIVE_DASHBOARD_LAYOUT_POLICY_JVM_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" \
  android/dashboard/DashboardPresentationPolicy.java tests/test_dashboard_responsive_layout_policy.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_dashboard_responsive_layout_policy
echo HELD_CONFIRMATION_AND_JOYSTICK_HAT_REGRESSION_JVM_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses"   android/dashboard/RemoteFocusPolicy.java android/dashboard/RemoteUiSession.java   android/dashboard/ControllerMotionPolicy.java tests/test_input_hold_focus_guard.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_input_hold_focus_guard
echo ANDROID_GAMEPAD_MOTION_POLICY_JVM_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" \
  android/dashboard/ControllerMotionPolicy.java tests/test_controller_motion_policy.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_controller_motion_policy
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_launch_registry
echo MODAL_CONTROLLER_POLICY_JVM_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" android/dashboard/ModalControllerPolicy.java tests/test_modal_controller_policy.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_modal_controller_policy
echo DIAGNOSTIC_PRESENTATION_JVM_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" android/dashboard/DiagnosticPresentation.java tests/test_diagnostic_presentation.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_diagnostic_presentation
echo FREEFORM_GEOMETRY_AND_BROKER_ACCESS_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" android/dashboard/PairGeometry.java android/dashboard/BrokerPolicy.java android/dashboard/ChoiceKeyPolicy.java tests/test_pair_geometry.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_pair_geometry
echo PROJECTION_GEOMETRY_AND_SESSION_SHORTCUTS_JVM_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" android/dashboard/ProjectionGeometry.java android/dashboard/ShortcutPolicy.java tests/test_projection.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_projection
echo HOME_MENU_POLICY_JVM_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" android/dashboard/HomeMenuPolicy.java android/dashboard/RemoteFocusPolicy.java android/dashboard/RemoteUiSession.java tests/test_home_menu.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_home_menu
echo CONTROLLER_HAT_JVM_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" android/dashboard/ControllerHatPolicy.java tests/test_controller_hat.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_controller_hat
echo LOCAL_ADB_TRANSPORT_JVM_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" android/dashboard/LocalAdbSession.java third_party/adblib/src/com/cgutman/adblib/AdbCrypto.java third_party/adblib/src/com/cgutman/adblib/AdbBase64.java tests/test_local_adb.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.proof.test_local_adb
echo PANE_RECOVERY_AND_NOTICE_POLICY_JVM_TEST
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" android/dashboard/PaneRecoveryPolicy.java android/dashboard/RecoveryNoticePolicy.java tests/test_pane_recovery.java tests/test_recovery_notice.java
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_pane_recovery
"$JAVA" -cp "$WORK/testclasses" tv.metaxy.dashboard.test_recovery_notice
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" android/dashboard/SessionShortcutPolicy.java tests/test_session_shortcut_policy.java
"$JAVA" -cp "$WORK/testclasses" test_session_shortcut_policy
echo ANDROID_DREAM_AND_REAL_DASHBOARD_COMPILE
"$JDK/bin/javac" --release 8 -d "$WORK/testclasses" android/dashboard/RecentCanvasPolicy.java tests/test_recent_canvas_policy.java
"$JAVA" -cp "$WORK/testclasses" test_recent_canvas_policy
bash tools/build_window_broker.sh
mkdir -p "$WORK/assets"
mkdir -p "$WORK/assets/licenses"
cp LICENSE "$WORK/assets/licenses/PROJECT-LICENSE.txt"
cp third_party/adblib/LICENSE "$WORK/assets/licenses/ADBLIB-LICENSE.txt"
cp THIRD-PARTY-NOTICES.md "$WORK/assets/licenses/THIRD-PARTY-NOTICES.md"
cp build/kinegrarchy-window-broker.jar "$WORK/assets/window-broker.jar"
sha256sum "$WORK/assets/window-broker.jar" | cut -d' ' -f1 > "$WORK/assets/window-broker.sha256"
"$JDK/bin/javac" --release 8 -cp "$ANDROID" -d "$WORK/classes" \
  android/dream/DreamCadence.java android/dream/MetaxyDreamService.java \
  android/dashboard/ActionIcon.java android/dashboard/ControllerIcons.java android/dashboard/StockHome.java android/dashboard/ControllerHatPolicy.java android/dashboard/HomeMenuPolicy.java android/dashboard/RecentApps.java android/dashboard/RecentCanvasPolicy.java android/dashboard/CanvasTaskHistory.java android/dashboard/LaunchRegistry.java android/dashboard/RemoteFocusPolicy.java android/dashboard/RemoteUiSession.java android/dashboard/ControllerMotionPolicy.java android/dashboard/DashboardPresentationPolicy.java android/dashboard/ModalControllerPolicy.java android/dashboard/DiagnosticPresentation.java android/dashboard/DashboardActivity.java \
  android/dashboard/PairGeometry.java android/dashboard/BrokerPolicy.java android/dashboard/PaneRecoveryPolicy.java android/dashboard/RecoveryNoticePolicy.java android/dashboard/PairProtocol.java \
  android/dashboard/ChoiceKeyPolicy.java android/dashboard/WindowBrokerProvider.java android/dashboard/WindowBrokerClient.java \
  android/dashboard/SessionShortcutPolicy.java android/dashboard/PairState.java android/dashboard/PairUi.java android/dashboard/PairActivity.java android/dashboard/ProjectionState.java android/dashboard/ProjectionGeometry.java android/dashboard/ShortcutPolicy.java android/dashboard/CanvasClient.java android/dashboard/WindowSessionLink.java android/dashboard/PairCanvasActivity.java android/dashboard/SessionShortcutService.java android/dashboard/LocalAdbSession.java android/dashboard/LocalAdbBootstrap.java third_party/adblib/src/com/cgutman/adblib/AdbCrypto.java third_party/adblib/src/com/cgutman/adblib/AdbBase64.java
"$JDK/bin/jar" --create --file "$WORK/classes.jar" -C "$WORK/classes" .
echo DEX
timeout 90 "$D8" --min-api 26 --lib "$ANDROID" --output "$WORK/dex" "$WORK/classes.jar"
test -s "$WORK/dex/classes.dex"
# Stable APK ZIP entry timestamps for the app DEX.
touch -t 198001010000 "$WORK/dex/classes.dex"
# zip preserves source file mode: fix the historic 0664 metadata even when
# the caller uses umask 0077. WORK itself remains a private mktemp directory.
chmod 0664 "$WORK/dex/classes.dex"
echo RESOURCE_LINK
"$AAPT2" compile --dir "$WORK/res" -o "$WORK/resources.zip"
"$AAPT2" link -A "$WORK/assets" -I "$ANDROID" --manifest harness/AndroidManifest.xml \
   --min-sdk-version 26 --target-sdk-version 34 \
   --version-code 6 --version-name 0.6.0-controller-mvp \
   -o "$WORK/resources-unsigned.apk" "$WORK/resources.zip"
zip -j -q -X "$WORK/resources-unsigned.apk" "$WORK/dex/classes.dex"
# Keep the previous candidate intact if alignment or archive validation fails.
"$ZIPALIGN" -f 4 "$WORK/resources-unsigned.apk" "$WORK/aligned.apk"
test -s "$WORK/aligned.apk"
"$ZIPALIGN" -c 4 "$WORK/aligned.apk"
unzip -tq "$WORK/aligned.apk" >/dev/null
# Validate the new artifact before replacing the previous good candidate.
"$AAPT2" dump badging "$WORK/aligned.apk" > "$WORK/badging.txt"
grep -F "package: name='tv.metaxy.proof' versionCode='6' versionName='0.6.0-controller-mvp'" "$WORK/badging.txt" >/dev/null
grep -F "sdkVersion:'26'" "$WORK/badging.txt" >/dev/null
grep -F "targetSdkVersion:'34'" "$WORK/badging.txt" >/dev/null
grep -F "leanback-launchable-activity: name='tv.metaxy.proof.DashboardActivity'" "$WORK/badging.txt" >/dev/null
mv -f -- "$WORK/aligned.apk" "$OUT"
echo FINAL_UNSIGNED_APK
sha256sum "$OUT"
grep -E "^package:|^sdkVersion:|^targetSdkVersion:|leanback-launchable" "$WORK/badging.txt"
"$AAPT2" dump permissions "$OUT"
echo SHIELDARCHY_REAL_GAMEPAD_DASHBOARD_V6_OFFLINE_BUILD_PASS_UNSIGNED_NOT_INSTALLED
