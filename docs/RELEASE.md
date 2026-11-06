# Kinegra Home 0.9.25 public beta

Side-by-side Split View is the main workflow: arrange two apps, choose Left/Right app roles, adjust their widths and swap positions. The optional session-shortcut service provides remote Back-hold controls while another app has focus. Individual failed panes can be reloaded. This release fixes reload warnings returning after Dismiss while a pane still needs recovery.

PiP is experimental and some app pairs may show a blank pane. DRM, orientation, app minimum sizes and audio focus depend on Android and the selected apps. Development targets the original NVIDIA SHIELD on Android 11 with an ultrawide display; broader hardware, physical portrait/16:9, touch and endurance coverage remains limited.

## Validation

The signed 0.9.25 / code 61 APK (`0.9.25-beta-preview` in Android) was installed in place. The original certificate, APK hash and retained installation identity were verified. Matthew confirms testing every version through this release. The offline build and policy checks passed. Two actual reload-callback regressions reproduce the old Dismiss failures and pass with the fix. [Validation](TESTING.md) · [Setup](SETUP.md).

## Distribution

The source begins with a clean publication snapshot, without former private review/development history. Final documentation and manifest updates do not change the reviewed APK's application or build inputs. Release metadata identifies the exact final source and unchanged signed artifact.

Install `KinegraHome-0.9.25.apk`; building from source produces `build/Kinegrarchy-v0.9.25-code61-UNSIGNED.apk`. Preserve the package, original signing identity and app data when updating. Source, component inventory, licence bundle and SHA-256 checksums accompany the release. Original code, documentation and artwork use MIT; dependencies and third-party screenshot content retain their terms. The README walkthrough retains its labelled earlier staged-capture versions and is not a performance benchmark.
