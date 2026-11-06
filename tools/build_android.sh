#!/usr/bin/env bash
# Build the current unsigned Android app from a prepared source snapshot.
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
read -r VERSION VERSION_CODE < <(python3 -c 'import json; p=json.load(open("packaging/COMPONENTS.json")); print(p["version"], p["version_code"])')
VERSION_NAME="$VERSION-beta-preview"
OUT="build/KinegraHome-$VERSION-UNSIGNED.apk"
test ! -e "$OUT" || { echo 'Refusing to overwrite an existing APK' >&2; exit 1; }
for executable in "$JAVA" "$JDK/bin/jar" "$JDK/bin/javac" "$AAPT2" "$D8" "$ZIPALIGN" python3 unzip zip; do
  command -v "$executable" >/dev/null || { echo "MISSING_TOOL $executable";exit 3; }
done
test -r "$ANDROID" || { echo "MISSING_PINNED_ANDROID_PLATFORM"; exit 3; }
echo "6cea1df3efb77103ac3e2beb9bf4718964b0e0869ab16d39d29d5cbae1c147ad  $ANDROID" | sha256sum --check --status
mkdir -p build
WORK="$(mktemp -d build/.kinegra-home-XXXXXX)"
trap 'case "$WORK" in build/.kinegra-home-*) rm -rf -- "$WORK";; esac' EXIT
mkdir -p "$WORK/classes" "$WORK/testclasses" "$WORK/dex" "$WORK/res/values" "$WORK/res/xml" "$WORK/res/drawable-nodpi"
cp android/res/values/strings.xml "$WORK/res/values/"
cp android/res/xml/metaxy_dream.xml android/res/xml/session_shortcuts.xml "$WORK/res/xml/"
cp assets/launcher/banner-kinegra-home-320x180.png "$WORK/res/drawable-nodpi/metaxy_banner.png"
cp assets/launcher/icon-512.png "$WORK/res/drawable-nodpi/metaxy_icon.png"
bash tools/test.sh
bash tools/build_window_broker.sh
mkdir -p "$WORK/assets"
mkdir -p "$WORK/assets/licenses"
cp LICENSE "$WORK/assets/licenses/PROJECT-LICENSE.txt"
cp third_party/adblib/LICENSE "$WORK/assets/licenses/ADBLIB-LICENSE.txt"
cp THIRD-PARTY-NOTICES.md "$WORK/assets/licenses/THIRD-PARTY-NOTICES.md"
cp build/kinegra-home-window-broker.jar "$WORK/assets/window-broker.jar"
sha256sum "$WORK/assets/window-broker.jar" | cut -d' ' -f1 > "$WORK/assets/window-broker.sha256"
"$JDK/bin/javac" --release 8 -cp "$ANDROID" -d "$WORK/classes" \
  android/dream/DreamCadence.java android/dream/MetaxyDreamService.java \
  android/dashboard/ActionIcon.java android/dashboard/ControllerIcons.java android/dashboard/StockHome.java android/dashboard/ControllerHatPolicy.java android/dashboard/HomeMenuPolicy.java android/dashboard/RecentApps.java android/dashboard/RecentCanvasPolicy.java android/dashboard/CanvasTaskHistory.java android/dashboard/LaunchRegistry.java android/dashboard/RemoteFocusPolicy.java android/dashboard/RemoteUiSession.java android/dashboard/ControllerMotionPolicy.java android/dashboard/DashboardPresentationPolicy.java android/dashboard/ModalControllerPolicy.java android/dashboard/DiagnosticPresentation.java android/dashboard/DashboardActivity.java \
  android/dashboard/PairGeometry.java android/dashboard/BrokerPolicy.java android/dashboard/PaneRecoveryPolicy.java android/dashboard/RecoveryNoticePolicy.java android/dashboard/PairProtocol.java \
  android/dashboard/ChoiceKeyPolicy.java android/dashboard/WindowBrokerProvider.java android/dashboard/WindowBrokerClient.java \
  android/dashboard/SessionShortcutPolicy.java android/dashboard/PairState.java android/dashboard/PairUi.java android/dashboard/PairActivity.java android/dashboard/ProjectionState.java android/dashboard/ProjectionGeometry.java android/dashboard/ShortcutPolicy.java android/dashboard/CanvasClient.java android/dashboard/PairCanvasActivity.java android/dashboard/SessionShortcutService.java android/dashboard/LocalAdbSession.java android/dashboard/LocalAdbBootstrap.java third_party/adblib/src/com/cgutman/adblib/AdbCrypto.java third_party/adblib/src/com/cgutman/adblib/AdbBase64.java
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
"$AAPT2" link -A "$WORK/assets" -I "$ANDROID" --manifest android/AndroidManifest.xml \
   --min-sdk-version 26 --target-sdk-version 34 \
   --version-code "$VERSION_CODE" --version-name "$VERSION_NAME" \
   -o "$WORK/resources-unsigned.apk" "$WORK/resources.zip"
zip -j -q -X "$WORK/resources-unsigned.apk" "$WORK/dex/classes.dex"
# Keep the previous candidate intact if alignment or archive validation fails.
"$ZIPALIGN" -f 4 "$WORK/resources-unsigned.apk" "$WORK/aligned.apk"
test -s "$WORK/aligned.apk"
"$ZIPALIGN" -c 4 "$WORK/aligned.apk"
unzip -tq "$WORK/aligned.apk" >/dev/null
# Validate the new artifact before replacing the previous good candidate.
"$AAPT2" dump badging "$WORK/aligned.apk" > "$WORK/badging.txt"
grep -F "package: name='tv.metaxy.proof' versionCode='$VERSION_CODE' versionName='$VERSION_NAME'" "$WORK/badging.txt" >/dev/null
grep -F "sdkVersion:'26'" "$WORK/badging.txt" >/dev/null
grep -F "targetSdkVersion:'34'" "$WORK/badging.txt" >/dev/null
grep -F "leanback-launchable-activity: name='tv.metaxy.proof.DashboardActivity'" "$WORK/badging.txt" >/dev/null
(umask 077; set -o noclobber; cat "$WORK/aligned.apk" > "$OUT")
echo FINAL_UNSIGNED_APK
sha256sum "$OUT"
grep -E "^package:|^sdkVersion:|^targetSdkVersion:|leanback-launchable" "$WORK/badging.txt"
"$AAPT2" dump permissions "$OUT"
echo KINEGRA_HOME_UNSIGNED_BUILD_PASS
