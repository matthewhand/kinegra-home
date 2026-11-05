# Kinegra Home

![Original Kinegra Home banner](docs/branding/kinegra-home-banner-v2.png)

**Two apps. One screen. A remote in your hand.**

Kinegra Home is an Android TV launcher for adjustable Split View and experimental picture in picture. Keep a browser beside a game or player, swap their positions, and control the workspace with a TV remote.

![Kinegra Home Split View walkthrough](assets/readme/kinegra-home-split-remote.gif)

*Staged walkthrough using earlier 0.9.14/0.9.16 captures. Presentation timing is not a performance benchmark. [Capture versions and attribution](assets/readme/README.md).*

## Install

Prepared beta candidate: **0.9.25 / code 61**. Releases are available only when published on the [Releases page](https://github.com/matthewhand/kinegra-home/releases). To install a signed release, use an Android TV sideloading tool, or:

```sh
adb connect DEVICE_IP:ADB_PORT
adb install -r KinegraHome-0.9.25.apk
```

The tested target is the original NVIDIA SHIELD on Android 11. The dashboard supports API 26+, while two-app window support uses Android 11 platform APIs. Other firmware and TV hardware are not established targets. Keep app data and the same signing key when upgrading.

Two-app layouts need an owner-authorized local debugging helper. Enable the required developer settings and approve the app's connection once. [Setup and recovery](docs/SETUP.md).

## Use two apps

1. Open Home and choose **Split View** or **Picture in Picture**.
2. Choose two different installed apps. Labels read **Left app / Right app** or **Main app / PiP app**.
3. Select **Connect locally** if window support is disconnected, then **Launch**. Initial preparation briefly opens each app before returning to the canvas.
4. Use the pane controls to choose the active app, adjust the layout or swap positions.

Split View supports 20:80–80:20 ratios. A video-shaped layout reserves a full-height 16:9 pane when the display is wide enough. PiP uses a 20–40% inset in any corner. App-specific video borders may remain.

| Action | Shield remote | ASUS controller |
| --- | --- | --- |
| Choose a control | D-pad, OK | D-pad, A |
| Swap views | Back + OK | Select + Start |
| Open session controls | Hold Back | Hold Select |
| Send Back to the active app | Short Back | Short Select |

Shortcuts while another app has focus require the optional **Kinegra Home session shortcuts** accessibility service. When the service is off, the app chooser and session menu show that Back-hold is unavailable and offer **Enable session shortcuts**. This opens Android settings for your choice. The service does not retrieve screen content or log accessibility events. **End layout** restores the selected tasks to fullscreen; **Home** opens the stock launcher.

If an app closes, an inline warning offers **Reload left/right app** or **Reload main/PiP app**. Reload restarts that package and may lose its unsaved state. Dismissed warnings stay dismissed until the failure changes or recovery completes.

## Beta scope

Split View is the primary demonstrated workflow. **PiP is experimental and some app pairs can render blank.** DRM, orientation, minimum app sizes and audio focus depend on Android and the selected apps. Touch mapping, long sessions, physical portrait/16:9 coverage and wider controller/hardware acceptance remain open. Development has used an ultrawide display; layout fixtures also cover narrow and standard-TV sizes.

The canvas is a visual arrangement, not a security boundary. Kinegra Home keeps the stock launcher available and does not replace the default Home role. It does not mix audio or change volume. See [privacy and local debugging](PRIVACY.md) and [validation](docs/TESTING.md).

## Build and contribute

Use JDK 17, Android SDK Platform 34 and Build-Tools 34.0.0 on Linux. The source builds an unsigned APK without private assets, keys or another project checkout. [Build and signing instructions](BUILDING.md).

Bug reports should include the app/firmware versions, layout, steps and expected behavior. Remove account details and private addresses from attachments. [Issues](https://github.com/matthewhand/kinegra-home/issues).

[Current release scope and validation](docs/RELEASE.md).

## License

[MIT](LICENSE) covers original code, documentation and artwork. AdbLib, third-party marks and screenshot content retain their own terms; see [notices](THIRD-PARTY-NOTICES.md). Open the packaged licenses through **About / Donate → Open-source licenses**.
