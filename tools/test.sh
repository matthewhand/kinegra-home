#!/usr/bin/env bash
# Host-only policy/security regressions. No Android SDK, signing or device access.
set -euo pipefail
root=$(cd "$(dirname "$0")/.." && pwd)
cd "$root"
JDK="${JAVA_HOME:?Set JAVA_HOME to a JDK 17 installation}"
export PATH="$JDK/bin:$PATH"
JAVA="$JDK/bin/java"
mkdir -p build
WORK=$(mktemp -d "$root/build/home-tests-XXXXXXXX")
trap 'case "$WORK" in "$root"/build/home-tests-*) rm -rf -- "$WORK";; esac' EXIT
mkdir -p "$WORK/testclasses"
python3 -m unittest discover -s tests -p 'test*.py'
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
echo KINEGRA_HOME_HOST_TESTS_PASS
