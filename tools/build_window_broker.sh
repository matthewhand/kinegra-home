#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "$0")/.." && pwd)
JDK="${JAVA_HOME:?Set JAVA_HOME to a JDK 17 installation}"
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
test -n "$SDK" || { echo "Set ANDROID_HOME or ANDROID_SDK_ROOT to the Android SDK" >&2; exit 3; }
export JAVA_HOME="$JDK" PATH="$JDK/bin:$PATH"
mkdir -p "$root/build/window-broker/classes" "$root/build/window-broker/dex"
mkdir -p "$root/build/window-broker/compile-only"
"$JDK/bin/javac" --release 8 -cp "$SDK/platforms/android-34/android.jar" -d "$root/build/window-broker/compile-only" "$root/tools/windowbroker/compile-only/android/window/TaskOrganizer.java"
"$JDK/bin/jar" cf "$root/build/window-broker/compile-only.jar" -C "$root/build/window-broker/compile-only" .
"$JDK/bin/javac" --release 8 -cp "$SDK/platforms/android-34/android.jar:$root/build/window-broker/compile-only.jar" -d "$root/build/window-broker/classes" \
 "$root/android/dashboard/PairGeometry.java" "$root/android/dashboard/BrokerPolicy.java" "$root/android/dashboard/PairProtocol.java" "$root/tools/windowbroker/KinegrarchyWindowBroker.java"
"$JDK/bin/jar" cf "$root/build/window-broker/classes.jar" -C "$root/build/window-broker/classes" .
"$SDK/build-tools/34.0.0/d8" --min-api 30 --classpath "$root/build/window-broker/compile-only.jar" --lib "$SDK/platforms/android-34/android.jar" --output "$root/build/window-broker/dex" "$root/build/window-broker/classes.jar"
"$JDK/bin/jar" --create --file "$root/build/kinegra-home-window-broker.jar" --date=1980-01-01T00:00:02Z -C "$root/build/window-broker/dex" classes.dex
sha256sum "$root/build/kinegra-home-window-broker.jar"
