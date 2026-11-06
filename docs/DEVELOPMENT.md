# Source layout

| Path | Purpose |
| --- | --- |
| `android/AndroidManifest.xml`, `android/res/` | App configuration and resources |
| `android/dashboard/` | Launcher, app selection, Split View, PiP and controller/session controls |
| `android/dream/` | Included screensaver |
| `tools/windowbroker/` | Task/window helper and compile-only Android declarations |
| `tools/` | Offline build, host tests, source checks and optional helper starter |
| `tests/` | JVM policy tests and Python security/reload regressions |
| `assets/` | Current launcher artwork and README walkthrough |
| `packaging/` | Component inventory and exact source manifest |
| `third_party/` | Retained AdbLib cryptography source and licence |

Build with `bash tools/build.sh`; run host tests with `bash tools/test.sh`. See [BUILDING.md](../BUILDING.md) for the toolchain.

The app supplies its own loopback debugging transport. The Android package and helper process identifiers retain their compatibility names. Only AdbLib's two cryptography adapter classes are used.

When changing source, regenerate `packaging/SOURCE-MANIFEST.json` and run `python3 tools/check_source.py`. Keep generated outputs, credentials, signing keys and personal screenshots out of commits. Bug reports should include version, firmware, input device, layout and reproduction steps.
