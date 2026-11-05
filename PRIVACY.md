# Privacy and local debugging

Kinegra Home stores selected apps, layout preferences and its generated local debugging key in Android app-private storage. Backups are disabled. Source and release packages contain no device key, user app data or fixed private network address.

The ordinary launcher discovers installed launchable apps. Two-app layouts are an explicit opt-in: the app connects to the device's loopback debugging port, authenticates with an owner-approved key and starts a shell-owned task helper restricted to this app's UID. The helper opens no network listener. Android's Network debugging service is separate and grants powerful access to approved clients; use a trusted network and authorized recovery computer.

The optional accessibility service filters session shortcuts while a workspace is active. It does not retrieve screen content or log accessibility events. Android and the selected apps retain their own storage, networking and audio behavior. The canvas is not an isolation boundary between apps.

About links open the selected browser on GitHub; they do not silently upload diagnostics. Share only deliberate, redacted reports. This project includes no analytics service or model inference runtime.
