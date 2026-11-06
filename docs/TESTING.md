# Testing and compatibility

## Run tests

```sh
bash tools/test.sh
python3 tools/check_source.py
```

The host suite covers launcher/input policies, layout geometry, accessibility shortcut availability, loopback helper boundaries and pane recovery. The reload callback regressions exercise the production callback with deterministic UI/helper boundaries. A full `bash tools/build.sh` also compiles the app/helper and checks package metadata.

Layout fixtures cover portrait, standard-TV and ultrawide dimensions. Tests use synthetic tasks and temporary state; no live account, camera or device is needed.

## Device coverage

Tested on an original NVIDIA SHIELD with SHIELD Experience 9.x (Android 11) and an ultrawide display. Broader physical portrait/16:9, touch, controller, firmware and long-session coverage is limited.

Split View is the main workflow. PiP is experimental and some app pairs may show a blank pane. DRM, orientation, minimum app sizes and audio focus depend on Android and the chosen apps.

For a change affecting device behavior, check launch, focus, swap, Back-hold, app exit/recovery and end-layout with the relevant remote/controller. Reports should include versions, layout, steps and expected behavior. Remove account details, private addresses and personal imagery from attachments.
