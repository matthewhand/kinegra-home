# Build Kinegra Home

## Requirements

Linux, JDK 17, Python 3, Git, zip/unzip, Android SDK Platform 34 and Build-Tools 34.0.0. Prepare these tools before building; the build runs offline.

```sh
export JAVA_HOME="/path/to/jdk-17"
export ANDROID_HOME="/path/to/android-sdk"
export PATH="$JAVA_HOME/bin:$PATH"
bash tools/build.sh
```

Run from a clean committed checkout. The script exports that commit, checks the source manifest, runs the tests, compiles the task helper and app, and produces **`build/KinegraHome-0.9.25-UNSIGNED.apk`**. Existing APKs are never overwritten. `ANDROID_SDK_ROOT` is also accepted.

The Android platform JAR is pinned to SHA-256 `6cea1df3efb77103ac3e2beb9bf4718964b0e0869ab16d39d29d5cbae1c147ad`. Toolchain/dependency changes require an intentional pin update. The helper archive uses fixed ZIP timestamps.

## Tests

```sh
bash tools/test.sh
python3 tools/check_source.py
```

Host policy and security tests require JDK 17 and Python 3, without an Android SDK or device. See [testing](docs/TESTING.md) and [source layout](docs/DEVELOPMENT.md).

## Signing and updates

The build produces an unsigned APK. Sign and align it with Android's signing tools and a key you control. To update an existing installation, use its original signing key and preserve app data. Install your signed build with a sideloading tool or `adb install -r YOUR_SIGNED_APK.apk`.

Include [LICENSE](LICENSE) and [third-party notices](THIRD-PARTY-NOTICES.md) with binary distributions. Build/signing inputs and user data do not belong in source control.

The published 0.9.25 APK and its exact source archive remain attached to the release tag. Later source cleanup does not replace those artifacts. For the optional computer-assisted helper, run `bash tools/build_window_broker.sh`, then follow [setup](docs/SETUP.md).
