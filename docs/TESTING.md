# Validation and support scope

The current public beta is **0.9.25 / code 61** (Android version name `0.9.25-beta-preview`). The signed APK was installed in place on the original NVIDIA SHIELD running SHIELD Experience 9.x (Android 11). Its hash, original certificate and retained installation identity were verified.

## Automated evidence

The offline build runs Java policy and window-broker checks. Session-shortcut tests cover exact accessibility-component matching, Android's global accessibility state and honest availability captions. Reload/recovery fixtures cover every failed-pane mask and repeated health polls. Two regressions exercise the actual Android reload callback: they reproduce Dismiss failures in the prior implementation and pass with the current fix.

Layout fixtures cover narrow/portrait, standard-TV and ultrawide dimensions. Host fixtures are distinct from physical-device evidence; they do not establish universal Android TV or controller compatibility.

## Beta limits

Side-by-side Split View is the main workflow. PiP is experimental and some app pairs may show a blank pane. DRM, orientation, minimum app sizes and audio focus depend on Android and the chosen apps.

Development uses an ultrawide display. Broader physical portrait/16:9, touch, controller, firmware and long-session coverage remains limited.

Bug reports should include versions, input device, layout, steps and expected behavior. Performance reports should state duration and display refresh. Remove account details, private addresses, camera imagery and opaque access URLs from reports.
