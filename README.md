<div align="center">

# ✦ Voidclient

### Minecraft Bedrock Edition Utility Client

![Minecraft](https://img.shields.io/badge/Minecraft-Bedrock%201.26.10-green?logo=minecraft&logoColor=white)
![Platform](https://img.shields.io/badge/Platform-Android-orange?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![License](https://img.shields.io/badge/License-GPLv3-blue?logo=gnu&logoColor=white)
![API](https://img.shields.io/badge/Min%20SDK-28-purple)

</div>

---

## About

**Voidclient** is a high-performance utility client for **Minecraft Bedrock Edition** on Android. It uses a MITM (Man-in-the-Middle) packet interception approach to provide combat, movement, and visual enhancements — **without modifying game memory**.

Built on top of [WClient](https://github.com/RetrivedMods/WClient), Voidclient rebrands and extends the foundation with a Deep Cosmic Void theme, new features, and a refreshed identity.

### Highlights
- **MC 1.26.10 (Protocol 944)** — kept up to date with the latest Bedrock releases
- **50+ cheat modules** across Combat, Motion, Visual, and Misc — each with configurable settings
- **Deep Cosmic Void theme** — electric purple UI with a looping fullscreen video background
- **Overlay GUI** — floating ClickGUI with shortcut buttons for quick toggle
- **Anti-cheat bypasses** — PositionSpoof and TimingSpoof for cleaner movement packets
- **MITM relay** — no memory injection, no root required

> **85% of this project's core architecture, relay system, and module framework originates from WClient by RetrivedMods. Voidclient would not exist without their work.**

---

## Credits

| Contribution | Credit |
|---|---|
| Core architecture & relay system | **WClient (RetrivedMods)** — 65% |
| Module framework & packet handling | **WClient (RetrivedMods)** |
| Original codebase & protocol layer | **WClient (RetrivedMods)** |
| Voidclient rebrand, theme & extras | **DoTo.dev** |
| Deep Cosmic Void theme design | **DoTo.dev** |
| MC 1.26.10 protocol support | **DoTo.dev** |
| Vendored CloudburstMC Protocol sync | **DoTo.dev** |
| New modules & anti-cheat bypasses | **DoTo.dev** |
| Video background & UI polish | **DoTo.dev** |
| Further more support for newer versions of Minecraft | **DoTo.dev** |

> WClient is the original project. Voidclient is a fork that builds upon their incredible work.
> Check out the original: [github.com/RetrivedMods/WClient](https://github.com/RetrivedMods/WClient)

### What Sets Voidclient Apart

- **Stays current** — built for the latest Bedrock versions, not stuck behind on old protocols
- **Thoughtful module design** — each module ships with customizable settings out of the box
- **Clean UI** — Deep Cosmic Void theme with a looping video background, not just another dark theme
- **Lightweight relay** — MITM approach keeps things fast without heavy memory hooks
- **Active development** — new features and bypasses added regularly

---

## Features

### Combat
| Module | Description |
|--------|-------------|
| VAura | Automated PvP combat with target switching |
| Killaura | Continuous entity targeting |
| AutoFight | Smart automatic combat |
| InfiniteAura | Extended range combat |
| ACA | Advanced Combat Aura |
| AutoTotem | Automatic totem equipping |
| AutoHvH | Auto Hive vs Hive combat |
| EnemyHunter | Target specific enemies |
| AntiKnockback | Reduce knockback effect |
| AntiCrystal | Counter crystal exploits |
| HitAndRun | Hit and retreat tactics |
| Hitbox | Expand entity hitboxes |
| CrystalSmash | Auto crystal breaking |
| TriggerBot | Click-to-attack automation |
| HotbarSwitcher | Auto weapon switching |

### Motion
| Module | Description |
|--------|-------------|
| Fly | Flight in survival mode |
| Speed | Movement speed boost |
| Sprint | Auto-sprint toggle |
| NoClip | Walk through blocks |
| JetPack | Jetpack-style flight |
| HighJump | Enhanced jump height |
| Bhop | Bunny hop physics |
| Spider | Wall climbing |
| AirJump | Jump in mid-air |
| AntiAFK | Anti-AFK automation |
| AutoWalk | Automatic walking |
| PlayerTP | Teleport to players |
| MotionFly | Motion-based flight |
| NoSlow | No slowdown when using items (bow/shield/food) |

### Visual
| Module | Description |
|--------|-------------|
| ESP | Entity highlighting |
| StorageESP | Chest/shulker/furnace ESP through walls |
| NameTags | Enhanced name display |
| Coordinates | Position overlay |
| Minimap | Mini map display |
| Crosshair | Custom crosshair |
| TargetHud | Target information HUD |
| Fullbright | Full brightness |
| SpeedDisplay | Speed indicator |
| NetworkInfo | Network stats |
| WorldState | World information |
| DamageText | Damage numbers |
| PlayerJoin | Join notifications |
| NoHurtCamera | Disable hurt camera |
| Zoom | Camera zoom |

### Misc
| Module | Description |
|--------|-------------|
| Watermark | RGB animated watermark |
| ArrayList | Active module list |
| PieChart | Visual pie chart |
| PositionSpoof | Anti-cheat position jitter |
| TimingSpoof | Anti-cheat timing jitter |
| AutoFish | Auto fishing rod cast/reel |

---

## Configuration

Modules are configurable via JSON. Settings persist across sessions.

```json
{
  "modules": {
    "VAura": { "enabled": true, "range": 4.5, "cps": 25 },
    "Fly": { "enabled": false, "speed": 1.4 },
    "ESP": { "enabled": true }
  }
}
```

Configs can be imported/exported from the in-app **Config** tab.

---

## Building

### Prerequisites
- Android Studio (latest stable)
- JDK 17
- Android SDK 36

### Steps
```bash
git clone https://github.com/zaxxthepole/Void-Client-Mobile-1.0.git
cd Void-Client-Mobile-1.0
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/`.

---

## Project Structure

```
Voidclient/
├── app/                          # Main application module
│   ├── src/main/java/
│   │   └── com/voidclient/client/
│   │       ├── game/             # Module system & game logic
│   │       │   ├── module/       # All cheat modules
│   │       │   ├── entity/       # Entity tracking
│   │       │   └── world/        # World state
│   │       ├── overlay/          # GUI overlays
│   │       ├── service/          # Background services
│   │       ├── render/           # Overlay rendering (ESP, StorageESP)
│   │       └── ui/               # App UI (Compose)
│   │           ├── component/    # VideoBackground, Navigation, etc.
│   │           └── theme/        # Deep Cosmic Void theme colors
│   └── src/main/res/
│       └── raw/                  # Video background (void_background.mp4)
├── relay/                        # VRelay packet interception
│   ├── src/main/kotlin/com/voidclient/vrelay/
│   │   ├── WRelay.kt            # Main relay server
│   │   ├── WRelaySession.kt     # Session management
│   │   ├── listener/            # Packet listeners
│   │   └── util/                # Utilities
│   ├── Protocol/                # Bedrock protocol codec
│   └── Network/                 # RakNet transport
└── settings.gradle.kts
```

---

## Platform Support

| Platform | Status |
|----------|--------|
| Android | Primary |
| Other | May work via custom network setup |

---

## UI & Theme

Voidclient uses the **Deep Cosmic Void** theme — a dark purple palette with electric violet accents and a looping video background.

| Element | Color |
|---------|-------|
| Background | Deep Obsidian `#0B0714` |
| Surface | Dark Violet `#120C22` |
| Accent | Electric Purple `#9D4EDD` |
| Glow | Lavender `#C084FC` |
| Text | Slate `#F1F5F9` |

The main UI features a fullscreen looping video background with a semi-transparent dark overlay. The overlay GUI (ClickGUI, shortcut buttons, HUD elements) is rendered via Android WindowManager overlays and is independent from the main UI.

---

## License

Licensed under the **GNU General Public License v3.0 (GPLv3)**.

### You Can
- Use personally and modify
- Redistribute with source code included
- Create content (videos, tutorials)

### You Cannot
- Distribute binaries without source code
- Claim ownership of original code

Full license: [GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html)

---

## Disclaimer

This software is provided **"AS IS"** without warranty. It is intended **solely for educational and research purposes**. Users are responsible for compliance with applicable laws and server rules.

**Voidclient is not affiliated with Mojang Studios or Microsoft.**

---

## Community

<div align="center">

**Join the community for updates and support:**

[![Discord](https://img.shields.io/badge/Discord-Join-5865F2?logo=discord&logoColor=white)](https://discord.gg/AM3ZpaXHW5)

</div>

---

<div align="center">

**Built with care by DoTo.dev**

*Powered by [WClient](https://github.com/RetrivedMods/WClient) — 65% of the core*

</div>
