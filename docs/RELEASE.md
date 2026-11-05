# Kinegra Home 0.9.25 beta candidate

Adjustable Split View is the primary workflow. The chooser uses Left/Right or Main/PiP app roles; session-shortcut availability is visible, and failed panes can be reloaded individually. This candidate fixes reload warnings reappearing after Dismiss when a pane still needs recovery.

## Release scope

The target is the original NVIDIA SHIELD running Android 11. Picture in picture remains experimental: some app pairs can show a blank pane. DRM, orientation, app minimum sizes and audio focus depend on the selected apps. Broader physical portrait/16:9, controller and long-session acceptance remains open.

## Validation and distribution

The reload fix has a regression check that fails against the prior implementation and passes after the change. Policy tests exercise Dismiss across every failed-pane mask and repeated health polls. See [validation](TESTING.md) and [setup](SETUP.md).

This source snapshot is a publication candidate. Offline build and signing receipts accompany the candidate artifacts separately. The previously installed 0.9.24 is a distinct baseline; its device evidence does not establish acceptance of 0.9.25. No installation or hands-on acceptance of this candidate is claimed here.

Release packaging should include the signed APK, source archive, component inventory, license bundle and checksums. Preserve the original signing identity and app data when upgrading. Original project code and artwork use MIT; dependencies retain their own terms.
