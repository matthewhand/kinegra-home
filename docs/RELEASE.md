# Kinegra Home 0.9.25 public beta

Arrange two apps side by side, adjust pane widths, swap positions and control the workspace with a TV remote. Optional accessibility session shortcuts provide Back-hold controls while another app has focus. Individual failed panes can be reloaded.

This release fixes reload warnings returning after Dismiss. The regression tests cover the production reload callback and repeated recovery checks.

The tested target is an original NVIDIA SHIELD with SHIELD Experience 9.x (Android 11), using an ultrawide display. PiP is experimental and some app pairs may show a blank pane. Other hardware and long-session coverage is limited.

Download `KinegraHome-0.9.25.apk` from [Releases](https://github.com/matthewhand/kinegra-home/releases/tag/v0.9.25). Android displays `0.9.25-beta-preview`, code 61. Preserve app data and the same signing identity when updating.

The release includes its signed APK, exact source archive, component inventory, licence bundle, source manifest and checksums. Those artifacts stay tied to the release tag; later source cleanup does not replace them. Original code, documentation and artwork use MIT; dependencies and third-party screenshot content retain their terms.

[Setup](SETUP.md) · [Build](../BUILDING.md) · [Tests and compatibility](TESTING.md)
