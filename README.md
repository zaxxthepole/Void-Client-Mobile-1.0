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

## Table of Contents

- [About](#about)
- [Credits](#credits)
- [Features](#features)
  - [Combat Modules](#combat-modules)
  - [Motion Modules](#motion-modules)
  - [Visual Modules](#visual-modules)
  - [Misc Modules](#misc-modules)
- [Detailed Module Reference](#detailed-module-reference)
  - [Combat Module Details](#combat-module-details)
  - [Motion Module Details](#motion-module-details)
  - [Visual Module Details](#visual-module-details)
  - [Misc Module Details](#misc-module-details)
- [How It Works](#how-it-works)
  - [MITM Relay Architecture](#mitm-relay-architecture)
  - [Packet Interception Flow](#packet-interception-flow)
  - [Module Hook System](#module-hook-system)
  - [Overlay Rendering](#overlay-rendering)
- [Installation](#installation)
  - [Prerequisites](#prerequisites)
  - [Building from Source](#building-from-source)
  - [Installing the APK](#installing-the-apk)
  - [First Launch Setup](#first-launch-setup)
- [Configuration](#configuration)
  - [Module Settings](#module-settings)
  - [JSON Config Format](#json-config-format)
  - [Import/Export](#importexport)
  - [Shortcut Buttons](#shortcut-buttons)
- [UI & Theme](#ui--theme)
  - [Deep Cosmic Void Theme](#deep-cosmic-void-theme)
  - [Video Background](#video-background)
  - [Overlay GUI](#overlay-gui)
  - [Sidebar Navigation](#sidebar-navigation)
- [Project Structure](#project-structure)
- [Protocol & Networking](#protocol--networking)
  - [Bedrock Protocol Support](#bedrock-protocol-support)
  - [Vendored Protocol Library](#vendored-protocol-library)
  - [Relay Session Lifecycle](#relay-session-lifecycle)
- [Troubleshooting](#troubleshooting)
- [FAQ](#faq)
- [Changelog](#changelog)
- [Platform Support](#platform-support)
- [Contributing](#contributing)
- [License](#license)
- [Disclaimer](#disclaimer)
- [Community](#community)

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

### Combat Modules

| Module | Description | Settings |
|--------|-------------|----------|
| VAura | Automated PvP combat with target switching | Range, CPS, Target Mode, Packet Boost |
| Killaura | Continuous entity targeting | Range, CPS, Teleport Aura, Strafe |
| AutoFight | Smart automatic combat | Hit & Run, Criticals, Multi-target |
| InfiniteAura | Extended range combat | Teleport Behind/In-Front, Lagback Bypass |
| ACA | Advanced Combat Aura | Range, Teleport Speed, Y-Offset, Keep Distance |
| AutoTotem | Automatic totem equipping | Health Threshold, Delay, Replace Offhand |
| AutoHvH | Auto Hive vs Hive combat | Range, Jitter, Multi-Packet, Anti-Void |
| EnemyHunter | Target specific enemies | Range, CPS, Rotation Interpolation, Jitter |
| AntiKnockback | Reduce knockback effect | Blocks all knockback packets |
| AntiCrystal | Counter crystal exploits | Y-Level Offset |
| HitAndRun | Hit and retreat tactics | Circle Radius, Jump Height |
| Hitbox | Expand entity hitboxes | Width, Height, Players/Mobs Filter |
| CrystalSmash | Auto crystal breaking | Range, CPS, Packets Per Attack |
| TriggerBot | Click-to-attack automation | Range, Angle, Players/Mobs Filter |
| HotbarSwitcher | Auto weapon switching | Start/End Slot, Delay, Loop, Reverse |

### Motion Modules

| Module | Description | Settings |
|--------|-------------|----------|
| Fly | Flight in survival mode | Fly Speed |
| Speed | Movement speed boost | Speed Multiplier |
| Sprint | Auto-sprint toggle | Always on when enabled |
| NoClip | Walk through blocks | Move Speed |
| JetPack | Jetpack-style flight | Speed |
| HighJump | Enhanced jump height | Jump Height (0.4–3.0) |
| Bhop | Bunny hop physics | Jump Height, Motion Interval |
| Spider | Wall climbing | Climb Speed |
| AirJump | Jump in mid-air | Sends motion when jump pressed |
| AntiAFK | Anti-AFK automation | Interval, Intensity |
| AutoWalk | Automatic walking | Walk Speed, Fall Speed |
| PlayerTP | Teleport to players | Speed, Range, Y-Offset, Jitter |
| MotionFly | Motion-based flight | Horizontal/Vertical Speed, Glide, Delay |
| NoSlow | No slowdown when using items | Remove Item Flag, Keep Sprint, Speed Multiplier |

### Visual Modules

| Module | Description | Settings |
|--------|-------------|----------|
| ESP | Entity highlighting | FOV, Stroke Width, RGB Colors, Box Mode, Tracers, Nametags, Armor |
| StorageESP | Chest/shulker/furnace ESP through walls | Range, Colors per Type, Tracers, Wireframe, Show Filters |
| NameTags | Enhanced name display | Part of ESP module |
| Coordinates | Position overlay | Font Size, Color, Background, Precision |
| Minimap | Mini map display | Size, Zoom, Dot Size, Range, Names, Transparent BG |
| Crosshair | Custom crosshair | Type (Cross/Dot/Circle/Square/Diamond/T/Plus), Color Mode, Hit Marker |
| TargetHud | Target information HUD | Position, Scale, Opacity, Player/Mob Filter |
| Fullbright | Full brightness | Night Vision Amplifier |
| SpeedDisplay | Speed indicator | Speed Smoothing, Colored Text |
| NetworkInfo | Network stats | Display Interval |
| WorldState | World information | Entity/Player Count, World Time, Chunks |
| DamageText | Damage numbers | Shows "[PlayerName] Enemy Damaged" |
| PlayerJoin | Join notifications | "[+] joined" / "[-] left" messages |
| NoHurtCamera | Disable hurt camera | Blocks hurt camera shake packets |
| Zoom | Camera zoom | Walk Speed manipulation |

### Misc Modules

| Module | Description | Settings |
|--------|-------------|----------|
| Watermark | RGB animated watermark | Custom Text, Version, Position, Font Size |
| ArrayList | Active module list | Sort Mode, Color Mode, Animated Transitions |
| PieChart | Visual pie chart | 3D Depth, Tilt, Legend, Animation |
| PositionSpoof | Anti-cheat position jitter | Jitter Amount, Vertical Jitter, Ground Spoof, Spoof Chance |
| TimingSpoof | Anti-cheat timing jitter | Tick Jitter, Motion Noise, Rotation Jitter |
| AutoFish | Auto fishing rod cast/reel | Cast Delay, Reel Delay, Auto-Recast, Randomize |

---

## Detailed Module Reference

### Combat Module Details

#### VAura

The VAura module is a versatile combat aura with multiple target modes:

- **Single Mode**: Locks onto one target until it is dead or out of range, then switches to the next nearest target.
- **Switch Mode**: Rotates through all targets in range, hitting each one in turn.
- **Multi Mode**: Attacks all targets within range simultaneously, useful for group fights.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Range | 1–50 | 4.5 | Maximum distance to target |
| CPS | 1–50 | 20 | Attacks per second |
| Packet Boost | Toggle | true | Send multiple attack packets per swing |
| Target Mode | Single/Switch/Multi | Single | How targets are selected |
| Players Only | Toggle | true | Only attack player entities |

#### Killaura

A straightforward aura that continuously attacks the nearest entity.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Range | 2–10 | 3.5 | Attack range |
| CPS | 5–30 | 15 | Attacks per second |
| Teleport Aura | Toggle | false | Teleport behind/in-front of target |
| Strafe | Toggle | false | Move in a circle around target |
| Anti-Bot | Toggle | true | Skip bot entities |

#### AutoFight

An intelligent fighter that hits and runs. It teleports to the target, attacks, then returns to its original position to avoid taking damage.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Critical Hits | Toggle | true | Always land critical hits |
| Multi-Target | Toggle | false | Attack multiple targets |
| Humanized Movement | Toggle | true | Add random variation to movement |
| Randomized Timing | Toggle | true | Vary attack timing |

#### AutoTotem

Automatically finds a Totem of Undying in your inventory and moves it to your offhand slot. If you die while holding a totem, it saves you.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Health Threshold | 1–20 | 5 | Only equip totem below this HP |
| Delay | 0–500ms | 100 | Delay between inventory actions |
| Replace Offhand | Toggle | false | Replace existing offhand item |

#### AntiKnockback

Intercepts and blocks all knockback packets (SetEntityMotion) targeting the local player. This prevents you from being pushed back by attacks, explosions, or other knockback sources.

#### AntiCrystal

Lowers your Y position slightly to avoid damage from end crystal explosions. Commonly used in Crystal PvP.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Y Offset | 0.1–1.61 | 0.5 | How far below the crystal you drop |

### Motion Module Details

#### Fly

Grants flight ability in survival mode by spoofing the MAY_FLY ability.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Fly Speed | 0.1–1.5 | 0.5 | Movement speed while flying |

**Controls:**
- Space: Move up
- Shift: Move down

#### NoClip

Allows walking through solid blocks by spoofing the NO_CLIP ability.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Move Speed | 0.1–3.0 | 1.0 | Speed while clipping through blocks |

#### Speed

Multiplies your horizontal movement speed for faster ground travel.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Speed Multiplier | 0.1–5.0 | 1.5 | How much faster you move |

#### HighJump

Enhances your jump height significantly beyond vanilla limits.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Jump Height | 0.4–3.0 | 1.5 | Height of each jump |

#### Spider

Lets you climb walls like a spider by applying upward motion when you are colliding horizontally with blocks.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Climb Speed | 0.1–2.0 | 0.5 | How fast you climb walls |

#### NoSlow

Prevents the movement slowdown that occurs when using items like bows, shields, food, or potions.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Remove Item Flag | Toggle | true | Remove START_USING_ITEM from packets |
| Keep Sprint | Toggle | true | Maintain sprint while using items |
| Speed Multiplier | 0.1–2.0 | 1.0 | Additional speed boost |
| Only When Moving | Toggle | false | Only bypass when actively moving |

#### PlayerTP

Teleports you toward the nearest enemy player. Useful for closing gaps quickly.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Speed | 0.1–5.0 | 2.0 | Teleport speed |
| Range | 1–30 | 15 | Maximum detection range |
| Y Offset | -5–5 | 0 | Vertical offset from target |
| Jitter | Toggle | true | Add random movement variation |

### Visual Module Details

#### ESP

The ESP (Extra Sensory Perception) module highlights entities through walls with boxes, nametags, and armor displays.

**Box Modes:**
- **Box2D**: Draws a 2D rectangle around the entity on screen
- **Box3D**: Draws a full 3D wireframe box
- **Corner**: Draws corner brackets instead of full lines

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| FOV | 40–110 | 110 | Field of view for projection |
| Stroke Width | 1–10 | 2.5 | Line thickness |
| Color R/G/B | 0–255 | 230/57/70 | Box color |
| Show All Entities | Toggle | false | Include mobs and non-players |
| Ignore Bots | Toggle | true | Skip bot entities |
| Box Mode | 2D/3D/Corner | 3D | How boxes are drawn |
| Tracers | Toggle | false | Draw lines to entities |
| Nametags | Toggle | true | Show entity names |
| Show Distance | Toggle | true | Include distance in nametag |
| Show Armor | Toggle | true | Show armor durability bars |

#### StorageESP

Highlights storage blocks (chests, shulkers, furnaces, barrels, hoppers, etc.) through walls. Each storage type has its own configurable color.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Range | 8–64 | 24 | Maximum detection distance |
| Chest Color R/G/B | 0–255 | 255/200/50 | Chest outline color |
| Shulker Color R/G/B | 0–255 | 200/50/255 | Shulker box outline color |
| Furnace Color R/G/B | 0–255 | 150/150/150 | Furnace outline color |
| Other Color R/G/B | 0–255 | 100/200/255 | Other storage outline color |
| Tracers | Toggle | true | Draw lines to storage |
| Wireframe | Toggle | true | Draw 3D box outlines |
| Show Chests | Toggle | true | Highlight chests |
| Show Shulkers | Toggle | true | Highlight shulker boxes |
| Show Furnaces | Toggle | true | Highlight furnaces |
| Show Others | Toggle | true | Highlight other storage types |

#### Coordinates

Displays your current position, facing direction, dimension, speed, and nether coordinate conversion in a HUD overlay.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Font Size | 8–24 | 12 | Text size |
| Precision | 0–3 | 1 | Decimal places |
| Background | Toggle | true | Semi-transparent background |
| Colored Text | Toggle | true | Use colored text |

#### Crosshair

A custom crosshair overlay with multiple shape and color options.

**Crosshair Types:**
- Cross, Dot, Circle, Square, Diamond, T-Shape, Plus

**Color Modes:**
- Static, Rainbow, Health-based, Target-based, Pulsing

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Type | Cross/Dot/etc. | Cross | Crosshair shape |
| Color Mode | Static/Rainbow/etc. | Static | How color is determined |
| Size | 1–20 | 6 | Crosshair size |
| Thickness | 1–5 | 2 | Line thickness |
| Hit Marker | Toggle | false | Show marker on hit |

#### Minimap

A minimap overlay showing nearby players, mobs, and items as colored dots on a small map.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Size | 50–200 | 100 | Map pixel size |
| Zoom | 0.5–4.0 | 1.0 | Zoom level |
| Dot Size | 1–6 | 3 | Size of entity dots |
| Range | 16–128 | 64 | Detection range |
| Show Names | Toggle | true | Display entity names |
| Show Coordinates | Toggle | false | Show coordinates on map |
| Transparent BG | Toggle | false | No background color |

### Misc Module Details

#### PositionSpoof

Adds random micro-jitter to your position packets to make anti-cheat analysis harder. The jitter is small enough to be visually imperceptible but helps mask automated movement patterns.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Jitter | 0.001–0.01 | 0.003 | Horizontal jitter amount |
| Vertical Jitter | 0.0001–0.005 | 0.001 | Vertical jitter amount |
| Ground Spoof | Toggle | true | Randomly spoof on-ground state |
| Spoof Chance | 0.1–1.0 | 0.7 | Percentage of packets to modify |

#### TimingSpoof

Adds jitter to tick timing, rotation, and motion values in packets. This makes your movement patterns look more human-like to anti-cheat systems.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Tick Jitter | 0–5 | 1 | Tick timing variation |
| Motion Noise | 0–0.01 | 0.002 | Motion value noise |
| Rotation Jitter | 0–0.5 | 0.1 | Rotation angle variation |

#### AutoFish

Automatically casts and reels in your fishing rod when a fish bites.

**Settings:**
| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| Cast Delay | 0.1–3.0s | 0.5s | Delay before casting |
| Reel Delay | 0.0–2.0s | 0.3s | Delay before reeling in |
| Auto-Recast | Toggle | true | Cast again after reeling |
| Range | 1–8 | 3 | Bobber detection range |
| Randomize Timing | Toggle | true | Add random variation to delays |

**How it works:**
1. The module detects when you are holding a fishing rod
2. It sends an `ITEM_USE` packet to cast the rod
3. It listens for fish bite events (FISH_HOOK_BUBBLE, FISH_HOOK_TIME, etc.)
4. When a bite is detected, it sends another packet to reel in
5. After reeling, it waits the configured delay and casts again

---

## How It Works

### MITM Relay Architecture

Voidclient operates by intercepting the communication between the Minecraft client and the server. Instead of modifying the game's memory (which requires root access and is easily detected), it acts as a relay:

```
Minecraft Client <---> Voidclient Relay <---> Minecraft Server
```

The relay sits between the client and server, reading and modifying packets in transit. This approach:
- Requires no root access
- Does not modify game files
- Works on any Android device (API 28+)
- Is harder for anti-cheat systems to detect

### Packet Interception Flow

Every packet sent between the client and server passes through the relay:

1. **Client sends packet** → Relay receives it
2. **Relay processes packet** → Each module gets a chance to modify or intercept it
3. **Modified packet sent to server** → Server receives the (potentially modified) packet

And in reverse:

1. **Server sends packet** → Relay receives it
2. **Relay processes packet** → Each module gets a chance to modify or intercept it
3. **Modified packet sent to client** → Client receives the (potentially modified) packet

### Module Hook System

Each module can hook into the packet flow via two methods:

```kotlin
// Called BEFORE a packet is forwarded
override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
    val packet = interceptablePacket.packet
    when (packet) {
        is SomePacket -> {
            // Modify the packet
            packet.someField = newValue
            // Or intercept (cancel) it entirely
            interceptablePacket.intercept()
        }
    }
}

// Called AFTER a packet has been processed
override fun afterPacketBound(packet: BedrockPacket) {
    // React to packets that have been processed
}
```

Modules can also:
- Send their own packets via `session.serverBound(packet)` or `session.clientBound(packet)`
- Access the local player via `session.localPlayer`
- Access world entities via `session.level.entityMap`
- Access player list via `session.level.playerMap`

### Overlay Rendering

Visual modules (ESP, StorageESP, Minimap, Crosshair, TargetHud) render using a separate Android `View` layer that sits on top of the Minecraft game surface. This overlay is drawn via `RenderOverlayView` which:

1. Calls `render(canvas)` on all enabled visual modules each frame
2. Uses `postInvalidateOnAnimation()` to trigger continuous redraws when modules are active
3. Converts 3D world coordinates to 2D screen coordinates using view-projection matrices

---

## Installation

### Prerequisites

- Android device running **API 28** (Android 9.0) or higher
- Minecraft Bedrock Edition installed
- ~50 MB of free storage

### Building from Source

#### Prerequisites for Building

- **Android Studio** (latest stable release)
- **JDK 17** or higher
- **Android SDK 36**

#### Build Steps

```bash
# Clone the repository
git clone https://github.com/zaxxthepole/Void-Client-Mobile-1.0.git

# Navigate into the project
cd Void-Client-Mobile-1.0

# Build the debug APK
./gradlew assembleDebug
```

The APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

#### Build Variants

| Variant | Description |
|---------|-------------|
| `debug` | Development build with logging |
| `release` | Optimized production build (requires signing config) |

### Installing the APK

1. Transfer the APK to your Android device
2. Enable **Install from Unknown Sources** in your device settings
3. Open the APK file and install
4. Launch Voidclient

### First Launch Setup

1. Open Voidclient
2. Grant any requested permissions
3. The relay will start automatically
4. Open Minecraft and connect to a server
5. Use the floating overlay button to access the ClickGUI

---

## Configuration

### Module Settings

Each module has its own configurable settings that persist across sessions. Settings are managed through the in-app ClickGUI.

### JSON Config Format

All module settings are stored in a single JSON file:

```json
{
  "modules": {
    "VAura": {
      "state": true,
      "values": {
        "range": 4.5,
        "cps": 25,
        "target_mode": "Single",
        "packet_boost": true,
        "players_only": true
      }
    },
    "Fly": {
      "state": false,
      "values": {
        "speed": 0.5
      }
    },
    "ESP": {
      "state": true,
      "values": {
        "fov": 110,
        "stroke_width": 2.5,
        "box_mode": "Box3D",
        "tracers": false,
        "nametags": true
      }
    },
    "StorageESP": {
      "state": false,
      "values": {
        "range": 24.0,
        "show_chests": true,
        "show_shulkers": true,
        "show_furnaces": true,
        "wireframe": true,
        "tracers": true
      }
    },
    "NoSlow": {
      "state": false,
      "values": {
        "remove_item_flag": true,
        "keep_sprint": true,
        "speed_multiplier": 1.0,
        "only_when_moving": false
      }
    },
    "AutoFish": {
      "state": false,
      "values": {
        "cast_delay": 0.5,
        "reel_delay": 0.3,
        "auto_recast": true,
        "randomize_timing": true
      }
    }
  }
}
```

### Import/Export

Configs can be managed through the in-app Config tab:

- **Export**: Save your current config to a JSON file
- **Import**: Load a config from a JSON file
- **Share**: Copy config to clipboard for sharing with others

### Shortcut Buttons

Each module can have a shortcut button displayed on the overlay. Drag the shortcut to reposition it. Tap to toggle the module on/off.

---

## UI & Theme

### Deep Cosmic Void Theme

Voidclient uses a custom dark theme inspired by deep space:

| Element | Color | Hex |
|---------|-------|-----|
| Background | Deep Obsidian | `#0B0714` |
| Surface | Dark Violet | `#120C22` |
| Surface Variant | Muted Purple | `#18102B` |
| Surface Container | Dark Purple | `#160E27` |
| Primary | Electric Purple | `#9D4EDD` |
| Primary Light | Lavender | `#C084FC` |
| Primary Dark | Deep Purple | `#5B1F8E` |
| Secondary | Bright Purple | `#A855F7` |
| Accent | Lavender | `#C084FC` |
| Text Primary | Off White | `#F1F5F9` |
| Text Secondary | Muted Gray | `#94A3B8` |
| Button Background | Dark Surface | `#18102B` |

### Video Background

The main app features a fullscreen looping video background:

- **Video**: A space-themed void animation
- **Resolution**: 1280x720 (16:9)
- **Size**: ~2.5 MB
- **Behavior**: Loops infinitely, muted, zoom-filled to screen
- **Overlay**: Semi-transparent dark overlay on top for readability

### Overlay GUI

The overlay system is completely separate from the main app UI. It uses Android's WindowManager with `TYPE_APPLICATION_OVERLAY` to draw on top of everything:

- **ClickGUI**: Full-screen module configuration panel
- **Shortcut Buttons**: Draggable floating buttons for quick toggle
- **HUD Elements**: ArrayList, Watermark, Coordinates, etc.
- **ESP/StorageESP**: Rendered via `RenderOverlayView` overlay

### Sidebar Navigation

The main app has a sidebar with icons for each page:

| Icon | Page | Description |
|------|------|-------------|
| Home | Home | Welcome card, server status, quick links |
| Account | Account | Microsoft/Xbox account login |
| Server | Server | Server connection settings |
| Cloud | Realms | Realms connection |
| Settings | Settings | App preferences |
| Info | About | Credits, legal, disclaimer |

---

## Project Structure

```
Voidclient/
├── app/                              # Main application module
│   ├── src/main/java/
│   │   └── com/voidclient/client/
│   │       ├── activity/             # MainActivity, permissions
│   │       ├── application/          # App context, startup
│   │       ├── game/                 # Module system & game logic
│   │       │   ├── Module.kt         # Base module class
│   │       │   ├── ModuleManager.kt  # Module registry
│   │       │   ├── ModuleCategory.kt # Combat/Motion/Visual/Misc
│   │       │   ├── ModuleValues.kt   # Value system (int/float/bool/enum)
│   │       │   ├── GameSession.kt    # Main session handler
│   │       │   ├── module/           # All cheat modules
│   │       │   │   ├── combat/       # 15 combat modules
│   │       │   │   ├── motion/       # 14 motion modules
│   │       │   │   ├── visual/       # 15 visual modules
│   │       │   │   └── misc/         # 7 misc modules
│   │       │   ├── entity/           # Entity tracking
│   │       │   ├── inventory/        # Inventory management
│   │       │   └── world/            # Level/world state
│   │       ├── navigation/           # Jetpack Navigation
│   │       ├── overlay/              # Overlay system
│   │       │   ├── OverlayWindow.kt  # Base overlay window
│   │       │   ├── OverlayManager.kt # Overlay lifecycle
│   │       │   ├── gui/classic/      # ClickGUI, ShortcutButton
│   │       │   └── hud/              # HUD overlays
│   │       ├── registry/             # Block/Item/Entity mappings
│   │       ├── render/               # Overlay rendering
│   │       │   └── RenderOverlayView.kt
│   │       ├── router/main/          # Page composables
│   │       │   ├── MainScreen.kt     # Root screen + VideoBackground
│   │       │   ├── HomePage.kt       # Home page
│   │       │   ├── AccountPage.kt    # Account page
│   │       │   ├── ServerPage.kt     # Server page
│   │       │   ├── RealmsPage.kt     # Realms page
│   │       │   ├── SettingsPage.kt   # Settings page
│   │       │   └── AboutPage.kt      # About page
│   │       ├── service/              # Background services
│   │       ├── ui/                   # App UI
│   │       │   ├── component/        # VideoBackground, WNavigation, etc.
│   │       │   └── theme/            # Deep Cosmic Void colors
│   │       ├── util/                 # Utilities
│   │       └── viewmodel/            # ViewModels
│   └── src/main/res/
│       ├── raw/                      # void_background.mp4
│       ├── mipmap-*/                 # App icons
│       ├── values/                   # Strings, themes
│       └── xml/                      # Manifest, network config
├── relay/                            # VRelay packet interception
│   ├── src/main/kotlin/com/voidclient/vrelay/
│   │   ├── WRelay.kt                # Main relay server
│   │   ├── WRelaySession.kt         # Session management
│   │   ├── listener/                # Packet listeners
│   │   └── util/                    # Utilities
│   ├── Protocol/                    # Bedrock protocol codec
│   │   ├── bedrock-codec/           # Packet serialization
│   │   ├── bedrock-connection/      # Connection handling
│   │   ├── Network/                 # RakNet transport
│   │   └── adventure/               # Text components
│   └── Network/                     # Low-level networking
├── gradle/
│   └── libs.versions.toml           # Version catalog
├── build.gradle.kts                 # Root build config
├── settings.gradle.kts              # Project settings
└── README.md                        # This file
```

---

## Protocol & Networking

### Bedrock Protocol Support

Voidclient currently supports **Bedrock Protocol 944** (MC 1.26.10). The protocol codec is vendored (copied) from CloudburstMC/Protocol and maintained locally.

**Supported Protocol Versions:**

| Version | Protocol | Status |
|---------|----------|--------|
| MC 1.26.10 | 944 | ✅ Supported |
| MC 1.21.x | 685–748 | Partial support |
| MC 1.20.x | 544–630 | Not supported |

### Vendored Protocol Library

The Bedrock protocol library is vendored into the project rather than used as a git submodule. This allows:
- Local modifications for module support
- Faster compilation (no submodule fetching)
- Version pinning for stability
- Custom packet handling hooks

**Key Protocol Components:**

| Component | Description |
|-----------|-------------|
| `bedrock-codec` | Packet serialization/deserialization |
| `bedrock-connection` | Connection management |
| `transport-raknet` | RakNet UDP transport layer |
| `adventure` | Text component handling |

### Relay Session Lifecycle

1. **Connection**: User opens Minecraft and connects to a server
2. **Relay Start**: Voidclient's relay intercepts the connection
3. **Protocol Negotiation**: Relay negotiates protocol version with server
4. **Session Created**: `GameSession` is initialized with player and world data
5. **Module Init**: All modules receive the session and begin packet interception
6. **Gameplay**: Packets flow through the relay, modules modify as needed
7. **Disconnect**: Player disconnects, modules are notified, session is cleaned up

---

## Troubleshooting

### Common Issues

#### "App not installing"
- Make sure **Install from Unknown Sources** is enabled
- Check that your device runs Android 9.0 (API 28) or higher
- Try uninstalling any previous version first

#### "Relay not connecting"
- Ensure Minecraft is running and connected to a server
- Check that the Voidclient service is running in the background
- Try force-stopping both apps and relaunching

#### "Modules not working"
- Make sure the module is enabled in the ClickGUI
- Check that you are connected to a server (modules need an active session)
- Some modules may not work on all servers due to anti-cheat

#### "Video background not showing"
- The video file must be present at `res/raw/void_background.mp4`
- If the APK was built without the video, rebuild from source
- Try reinstalling the APK

#### "Overlay not appearing"
- Grant overlay permission when prompted
- Check Android Settings → Apps → Voidclient → Display over other apps
- Ensure the floating button is enabled in settings

#### "Build fails"
- Make sure you have Android Studio latest stable
- JDK 17 or higher is required
- Run `./gradlew clean` before rebuilding
- Check that all dependencies are downloaded

### Performance Tips

- Disable visual modules you don't use to save battery
- Lower the ESP FOV if you experience frame drops
- Reduce the Minimap size for better performance
- Disable StorageESP when not needed (it tracks many block entities)

---

## FAQ

### General

**Q: Is Voidclient free?**
A: Yes, Voidclient is completely free and open source under the GPLv3 license.

**Q: Does it require root?**
A: No, Voidclient uses a MITM relay approach that does not require root access.

**Q: What Minecraft versions are supported?**
A: Currently MC 1.26.10 (Protocol 944). Older versions may have partial support.

**Q: Does it work on iOS/PC?**
A: Currently Android only. iOS and PC support is not planned.

### Technical

**Q: How does the MITM relay work?**
A: The relay sits between your Minecraft client and the server. It reads packets from both sides and can modify, block, or add packets before forwarding them.

**Q: Can servers detect this?**
A: The MITM approach is harder to detect than memory modification, but no cheat is 100% undetectable. Use the anti-cheat bypass modules (PositionSpoof, TimingSpoof) to reduce detection risk.

**Q: What is the difference between VAura and Killaura?**
A: VAura has multiple target modes (Single, Switch, Multi) and more advanced features. Killaura is simpler — it just attacks the nearest entity continuously.

**Q: How do I import/export configs?**
A: Use the Config tab in the app. Export saves a JSON file, Import loads one. You can share configs by copying the JSON.

**Q: Can I customize the video background?**
A: Replace `app/src/main/res/raw/void_background.mp4` with your own video (H.264 MP4, 1280x720 recommended) and rebuild.

### Modules

**Q: What does NoSlow do?**
A: It prevents the movement slowdown when using items like bows, shields, food, or potions. The server normally reduces your speed while using items; NoSlow bypasses this.

**Q: How does StorageESP work?**
A: It intercepts `BlockEntityDataPacket` from the server, which contains chest, shulker, furnace, and other storage block positions. It then renders 3D ESP boxes at those positions.

**Q: How does AutoFish work?**
A: It detects when you hold a fishing rod, automatically casts it via `InventoryTransactionPacket`, listens for fish bite events (FISH_HOOK_BUBBLE, etc.), then reels in and recasts.

**Q: What is PositionSpoof?**
A: It adds tiny random jitter to your movement packets. This makes anti-cheat analysis harder because your movement patterns look more human-like.

---

## Changelog

### v1.0.0 — Current Release

#### New Features
- MC 1.26.10 (Protocol 944) support
- Deep Cosmic Void theme with video background
- 50+ cheat modules
- Overlay GUI with shortcut buttons
- PositionSpoof and TimingSpoof anti-cheat bypasses
- StorageESP for chest/shulker/furnace highlighting
- AutoFish for automated fishing
- NoSlow for item movement bypass

#### Improvements
- Synced vendored CloudburstMC/Protocol to latest upstream
- Fixed Kotlin/Lombok interop issues across 8 files
- Updated all UI colors to Deep Cosmic Void palette
- Sidebar header rebranded to "VOID"
- ClickGUI and ShortcutButton reskinned

#### Bug Fixes
- Fixed missing base class methods after protocol sync
- Fixed overlay rendering for StorageESP
- Fixed video background scaling and audio

---

## Platform Support

| Platform | Status | Notes |
|----------|--------|-------|
| Android | Primary | API 28+ (Android 9.0+) |
| iOS | Not Supported | Would require complete rewrite |
| Windows | Not Supported | Different relay architecture needed |
| Other | May Work | Custom network setup required |

---

## Contributing

Contributions are welcome! Here's how to get started:

### Development Setup

1. Fork the repository
2. Clone your fork
3. Open in Android Studio
4. Create a feature branch
5. Make your changes
6. Test on a real device
7. Submit a pull request

### Code Style

- Follow Kotlin coding conventions
- Use the existing module pattern for new modules
- Add configurable settings via the value system
- Include meaningful commit messages

### Adding a New Module

1. Create a new file in `app/src/main/java/com/voidclient/client/game/module/<category>/`
2. Extend `Module` class
3. Implement `beforePacketBound` for packet interception
4. Add configurable settings via `intValue`, `floatValue`, `boolValue`, or `enumValue`
5. Register in `ModuleManager.kt`

Example:

```kotlin
class MyModule : Module("MyModule", ModuleCategory.Misc) {
    private val mySetting by intValue("My Setting", 5, 1..10)
    private val myToggle by boolValue("My Toggle", true)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet
        when (packet) {
            is SomePacket -> {
                // Modify packet based on settings
            }
        }
    }
}
```

### Reporting Issues

- Use the GitHub Issues page
- Include device model and Android version
- Include steps to reproduce
- Include logs if available

---

## License

Licensed under the **GNU General Public License v3.0 (GPLv3)**.

### You Can
- Use personally and modify
- Redistribute with source code included
- Create content (videos, tutorials)
- Fork and create your own version

### You Cannot
- Distribute binaries without source code
- Claim ownership of original code
- Use for commercial purposes without open-sourcing

Full license: [GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html)

---

## Disclaimer

This software is provided **"AS IS"** without warranty. It is intended **solely for educational and research purposes**. Users are responsible for compliance with applicable laws and server rules.

**Voidclient is not affiliated with Mojang Studios or Microsoft.**

Using cheats on multiplayer servers may result in bans. Use at your own risk. Always respect server rules and other players.

---

## Module Compatibility

Different servers have different anti-cheat systems. Here's a general compatibility guide:

### Server Compatibility

| Server Type | Fly | Speed | Killaura | ESP | NoSlow |
|-------------|-----|-------|----------|-----|--------|
| Vanilla | ❓ | ❓ | ❓ | ❓ | ❓ |
| Hive | ⚠️ | ⚠️ | ⚠️ | ✅ | ✅ |
| Lifeboat | ⚠️ | ⚠️ | ⚠️ | ✅ | ✅ |
| CubeCraft | ⚠️ | ❌ | ⚠️ | ✅ | ✅ |
| Mineplex | ⚠️ | ❌ | ⚠️ | ✅ | ✅ |
| Custom | ❓ | ❓ | ❓ | ✅ | ✅ |

**Legend:**
- ✅ Works well
- ⚠️ May trigger detection — use anti-cheat bypasses
- ❌ Likely detected
- ❓ Unknown — test with caution

### Anti-Cheat Bypass Modules

Use these modules together for better stealth:

| Module | What it does | Pair with |
|--------|-------------|-----------|
| PositionSpoof | Jitters position packets | All movement modules |
| TimingSpoof | Jitters tick timing | Fly, Speed |
| NoSlow | Removes item use flag | Bow, Shield, Food |

### Module Conflicts

Some modules should not be used together:

| Conflict | Reason |
|----------|--------|
| Fly + Spider | Both modify vertical movement |
| Fly + NoClip | Both spoof flight abilities |
| Speed + Sprint | Speed already includes sprint boost |
| Killaura + VAura | Both target entities — use one |
| Killaura + TriggerBot | Overlapping functionality |

---

## Security & Privacy

### Data Handling

- Voidclient does **not** collect any user data
- All configs are stored locally on your device
- No analytics, no tracking, no telemetry
- Network traffic goes only to Minecraft servers

### Permission Requirements

| Permission | Why |
|------------|-----|
| INTERNET | Connect to Minecraft servers |
| FOREGROUND_SERVICE | Keep relay running in background |
| SYSTEM_ALERT_WINDOW | Overlay GUI on top of Minecraft |
| WRITE_EXTERNAL_STORAGE | Config export to device storage |

### What Voidclient Does NOT Do

- Does not read your Minecraft credentials
- Does not access your files beyond config storage
- Does not communicate with any third-party servers
- Does not modify the Minecraft APK
- Does not require root access
- Does not install additional software

---

## Advanced Configuration

### Custom Module Values

You can add custom values to any module by extending the value system:

```kotlin
// Integer value with range
private val myInt by intValue("Setting Name", defaultValue, min..max)

// Float value with range
private val myFloat by floatValue("Setting Name", defaultValue, min..max)

// Boolean toggle
private val myBool by boolValue("Setting Name", defaultValue)

// Enum dropdown
private val myEnum by enumValue("Setting Name", DefaultOption, Options::class.java)
```

### Packet Types Reference

Common packet types used by modules:

| Packet | Direction | Used By |
|--------|-----------|---------|
| PlayerAuthInputPacket | Client → Server | Sprint, NoSlow, PositionSpoof |
| MovePlayerPacket | Client → Server | PositionSpoof |
| InventoryTransactionPacket | Client → Server | AutoFish, AutoTotem |
| MobEquipmentPacket | Both | ESP, TriggerBot |
| MobArmorEquipmentPacket | Both | ESP |
| AddEntityPacket | Server → Client | ESP, StorageESP |
| RemoveEntityPacket | Server → Client | ESP, AutoFish |
| EntityEventPacket | Server → Client | AutoFish |
| BlockEntityDataPacket | Server → Client | StorageESP |
| UpdateBlockPacket | Server → Client | StorageESP |
| LevelSoundEventPacket | Server → Client | AutoFish |
| SetEntityMotionPacket | Server → Client | AntiKnockback |
| LevelChunkPacket | Server → Client | WorldState |

### Entity Identifiers

Common entity identifiers in Bedrock:

| Identifier | Entity |
|------------|--------|
| `minecraft:player` | Player |
| `minecraft:zombie` | Zombie |
| `minecraft:skeleton` | Skeleton |
| `minecraft:creeper` | Creeper |
| `minecraft:enderman` | Enderman |
| `minecraft:fishing_hook` | Fishing Bobber |
| `minecraft:item` | Dropped Item |
| `minecraft:minecart` | Minecart |
| `minecraft:chest_minecart` | Chest Minecart |
| `minecraft:hopper_minecart` | Hopper Minecart |

### Block Entity Types

Block entities tracked by StorageESP:

| Type | Identifier | Description |
|------|------------|-------------|
| Chest | `Chest` | Standard chest |
| Trapped Chest | `TrappedChest` | Redstone-activated chest |
| Ender Chest | `EnderChest` | Ender chest |
| Shulker Box | `ShulkerBox` | Colored shulker box |
| Furnace | `Furnace` | Smelting furnace |
| Blast Furnace | `BlastFurnace` | Fast smelting |
| Smoker | `Smoker` | Food cooking |
| Hopper | `Hopper` | Item transfer |
| Dispenser | `Dispenser` | Redstone dispenser |
| Dropper | `Dropper` | Item dropper |
| Barrel | `Barrel` | Storage barrel |
| Brewing Stand | `BrewingStand` | Potion brewing |
| Enchanting Table | `EnchantingTable` | Enchantment table |
| Anvil | `Anvil` | Repair/combine items |
| Banner | `Banner` | Decorative banner |
| Skull | `Skull` | Player head |
| Sign | `Sign` | Text sign |
| Bed | `Bed` | Player bed |
| Skull | `Skull` | Mob head/player head |
| Jukebox | `Jukebox` | Music disc player |
| Lectern | `Lectern` | Book reading stand |
| Campfire | `Campfire` | Cooking campfire |
| Soul Campfire | `Campfire` | Soul fire variant |
| Bell | `Bell` | Village bell |
| Spawner | `MobSpawner` | Mob spawner |
| End Gateway | `EndGateway` | End portal gateway |

---

## Glossary

| Term | Definition |
|------|------------|
| **MITM** | Man-in-the-Middle — intercepting communication between two parties |
| **Relay** | A server that forwards packets between client and server |
| **Packet** | A unit of data sent over the network |
| **ESP** | Extra Sensory Perception — seeing things through walls |
| **Aura** | Automated combat that attacks entities automatically |
| **Bypass** | A method to avoid anti-cheat detection |
| **Spoof** | Sending fake data to appear different |
| **Hook** | Intercepting a packet to modify or block it |
| **ClickGUI** | A graphical interface for configuring modules |
| **HUD** | Heads-Up Display — overlay information on screen |
| **Overlay** | A floating window drawn on top of other apps |
| **RakNet** | UDP-based reliable networking protocol used by Bedrock |
| **Protocol** | The rules for how data is formatted and exchanged |
| **NBT** | Named Binary Tag — Minecraft's data format |
| **Chunk** | A 16x16 section of the game world |
| **Block Entity** | A block with additional data (chest, furnace, etc.) |
| **Runtime ID** | A unique identifier for an entity during a session |
| **Unique ID** | A persistent identifier for an entity across sessions |

---

## Version History

| Version | Protocol | Changes |
|---------|----------|---------|
| v1.0.0 | 944 | Initial release — MC 1.26.10 support, Deep Cosmic Void theme, 50+ modules |
| v0.9.0 | 748 | Added StorageESP, AutoFish, NoSlow, PositionSpoof, TimingSpoof |
| v0.8.0 | 748 | Deep Cosmic Void theme, video background, UI overhaul |
| v0.7.0 | 748 | MC 1.26.10 protocol support, Kotlin/Lombok fixes |
| v0.6.0 | 685 | Vendored CloudburstMC Protocol sync |
| v0.5.0 | 685 | Core module framework from WClient |
| v0.4.0 | 630 | Overlay GUI, shortcut buttons |
| v0.3.0 | 589 | ESP, Minimap, Crosshair, TargetHud |
| v0.2.0 | 544 | Combat modules (VAura, Killaura, AutoFight) |
| v0.1.0 | 527 | Initial fork from WClient |

---

## Acknowledgments

- **RetrivedMods** — for creating WClient, the foundation this project is built on
- **CloudburstMC** — for the Bedrock protocol library
- **Mojang Studios** — for Minecraft
- **JetBrains** — for Kotlin and Android Studio
- **The open source community** — for the libraries and tools that make this possible

---

## Support

If you need help:

1. Check the [FAQ](#faq) section
2. Search existing [GitHub Issues](https://github.com/zaxxthepole/Void-Client-Mobile-1.0/issues)
3. Join the [Discord](https://discord.gg/AM3ZpaXHW5) community
4. Open a new GitHub Issue with details

When reporting a bug, include:
- Device model and Android version
- Minecraft version
- Steps to reproduce
- Expected vs actual behavior
- Screenshots or logs if applicable

---

## Star History

If you find Voidclient useful, consider giving it a star on GitHub!

[![Star History Chart](https://api.star-history.com/svg?repos=zaxxthepole/Void-Client-Mobile-1.0&type=Date)](https://star-history.com/#zaxxthepole/Void-Client-Mobile-1.0&Date)

---

<div align="center">

**Built with care by DoTo.dev**

*Powered by [WClient](https://github.com/RetrivedMods/WClient) — 65% of the core*

</div>
