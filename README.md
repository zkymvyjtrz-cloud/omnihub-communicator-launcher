# OmniHub Communicator — Android Home Launcher

A modern, communication-first Android Home Launcher (`Kotlin` + `Jetpack Compose`) inspired by the **Clicks Communicator** and **BlackBerry 10 Hub**.

## Core Architecture & Features

1. **Unified BlackBerry Hub Stream + Inline Direct Reply (`RemoteInput`)**
   - Centralizes incoming messages across **WhatsApp, Microsoft Teams, Microsoft Outlook, Gmail, SMS/Messages, and Facebook Messenger**.
   - Powered by [`HubNotificationListenerService`](app/src/main/java/com/clicks/omnihub/service/HubNotificationListenerService.kt), which extracts live Android notifications and `RemoteInput` actions so you can reply directly inside the home screen stream without opening the target app.
   - Includes a pre-loaded **Multi-Platform Demo Stream** so you can test inline replies, smart-reply chips, and keyboard shortcuts immediately upon installation.

2. **Next-Gen Universal Omnibox Command Bar**
   - Located at the top of the launcher in [`OmniboxHeader.kt`](app/src/main/java/com/clicks/omnihub/ui/components/OmniboxHeader.kt).
   - Type anywhere to filter messages by contact/content, run slash commands (`/msg @Sarah`, `/clear`, `/reset`), or dispatch an outbound cross-platform message while choosing the delivery channel (WhatsApp, Teams, Outlook, SMS, etc.) on the fly.

3. **Person-Centric Unified Threads (`People Threads [P]`)**
   - Groups fragmented conversations by **person** across platforms (for example, merging Sarah Jenkins' urgent Microsoft Teams message and WhatsApp ping into one unified contact card in [`HubCards.kt`](app/src/main/java/com/clicks/omnihub/ui/components/HubCards.kt)).

4. **Clicks Tactile Keyboard Shortcuts & Dynamic Notification LED**
   - [`MainActivity.kt`](app/src/main/java/com/clicks/omnihub/MainActivity.kt) intercepts physical QWERTY keystrokes (Clicks Communicator, Unihertz, or Bluetooth keyboards) and pairs them with an on-screen tactile shortcut deck in [`TactileDeckAndApps.kt`](app/src/main/java/com/clicks/omnihub/ui/components/TactileDeckAndApps.kt):
     - `R` — Open Inline Direct Reply for focused message
     - `A` — Archive / Mark message Done
     - `S` — Snooze thread for 1 hour
     - `P` — Switch to Person-Centric Unified Threads
     - `F` — Toggle VIP Priority Shield
     - `L` — Open Monochrome Minimal App Launcher
     - `/` — Open Universal Omnibox Composer
     - `J` / `K` — Move focus Down / Up

5. **VIP Priority Shield & Anti-Doomscrolling Minimal App Drawer**
   - Holds back non-VIP notifications so only urgent communications surface immediately, paired with a monochrome text-only speed-dial app launcher.

## Running & Enabling Live Notifications on Android

1. Open this root folder in **Android Studio** and run the `app` configuration on any Android device or emulator (API 26+).
2. Tap **"ENABLE LIVE ACCESS"** in the top-right status bar to grant Notification Listener access to `OmniHub Universal Message Bridge`.
3. To set as your primary Home Screen, switch to the **`Minimal Apps [L]`** tab and tap **"Set Default"**.
