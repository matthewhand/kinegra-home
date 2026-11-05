# Building Kinegra Home

The current application is 0.9.25 / code 61. Builds produce an unsigned APK and do not connect to a device. No private source repository, signing key or installed APK is needed to compile the Java application and its helper.

## Toolchain

Use Linux, JDK 17, Python 3, `zip`, `unzip`, `tar`, Git, Android SDK Platform 34 and Android SDK Build-Tools 34.0.0. The existing reviewed build was made on Ubuntu. Install the SDK platform and build-tools before running the offline build.

Set your own toolchain locations; the scripts do not assume another project's checkout:

```sh
export JAVA_HOME="/path/to/jdk-17"
export ANDROID_HOME="/path/to/android-sdk"
export PATH="$JAVA_HOME/bin:$PATH"
bash tools/build_dashboard_0925_offline.sh
```

`ANDROID_SDK_ROOT` is accepted when `ANDROID_HOME` is unset. The helper archive uses a fixed ZIP timestamp, avoiding timestamp-only changes to its checksum. The build verifies the pinned Android 34 platform JAR SHA-256: `6cea1df3efb77103ac3e2beb9bf4718964b0e0869ab16d39d29d5cbae1c147ad`. A different platform artifact stops the build. Review the pin deliberately when changing SDK inputs.

Run from a clean committed checkout. The versioned build exports that commit, runs the existing Java policy checks, compiles the helper and application, and produces `build/Kinegrarchy-v0.9.25-code59-UNSIGNED.apk`. It refuses to overwrite that candidate; keep an existing artifact and use a fresh checkout for another build. Build the helper separately with `bash tools/build_window_broker.sh` only when needed for the optional computer-assisted starter.

## Signing and installation

Sign and align an APK using Android's signing tools and a key you control. Project signing keys are not distributed. Release builds include accessible project/AdbLib notices in About and in the APK assets. An update to an existing installation must use that installation's original key; a newly chosen key cannot update it in place. Do not uninstall or clear app data to work around a mismatch on the development Shield.

Install your signed build with an Android TV sideloading tool, or use `adb install -r YOUR_SIGNED_APK.apk` after selecting and verifying your own device. Documentation uses placeholders rather than a private device address. Android 11 SHIELD is the tested target; API 26 is the ordinary dashboard minimum, while the task helper uses Android 11 platform APIs.

Include [LICENSE](LICENSE) and [third-party notices](THIRD-PARTY-NOTICES.md) with release materials. Keep source, APK version, checksum and signing-certificate identity together when publishing a build.

## Validation limits

The source has narrow/wide layout and policy fixtures plus bounded, muted Shield recovery checks. This is not broad physical-TV, controller, DRM or endurance acceptance. PiP rendering and touch mapping remain experimental. See [validation](docs/TESTING.md).
