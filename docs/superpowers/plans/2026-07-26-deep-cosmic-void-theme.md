# Deep Cosmic Void Theme Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Apply the Deep Cosmic Void theme across the Voidclient UI (colors, overlays, ClickGUI, welcome titles, buttons, shortcuts).

**Architecture:** Modifying specific theme files (`Theme.kt`) to change hex values, updating texts in `HomePage.kt` and `WNavigation.kt`, and adjusting UI styling in `OverlayClickGUI.kt` and `OverlayShortcutButton.kt`.

**Tech Stack:** Jetpack Compose, Kotlin, Material 3.

## Global Constraints
- Do not modify functional logic of any module or overlay.
- Keep all Jetpack Compose layout containers intact (alignments, paddings, list indices).

---

### Task 1: Update Theme Colors in `Theme.kt`

**Files:**
- Modify: `app/src/main/java/com/voidclient/client/ui/theme/Theme.kt`

- [ ] **Step 1: Edit WColors and ClickGUIColors**
Replace the old colors with deep cosmic void hexes (Deep Obsidian background, Dark Violet surface, electric purples).

```kotlin
object WColors {
    // Primary purples
    val Primary = Color(0xFF9D4EDD)
    val PrimaryLight = Color(0xFFC084FC)
    val PrimaryDark = Color(0xFF5B1F8E)
    val OnPrimary = Color(0xFFFFFFFF)

    // Secondary purples
    val Secondary = Color(0xFFA855F7)
    val SecondaryVariant = Color(0xFF7E22CE)
    val SecondaryLight = Color(0xFFE0AAFF)
    val OnSecondary = Color(0xFFFFFFFF)

    // Accent lavender
    val Accent = Color(0xFFC084FC)
    val AccentLight = Color(0xFFE0AAFF)
    val AccentDark = Color(0xFF7B2FBE)

    val Background = Color(0xFF0B0714)
    val Surface = Color(0xFF120C22)
    val SurfaceVariant = Color(0xFF18102B)
    val SurfaceContainer = Color(0xFF160E27)

    val OnBackground = Color(0xFFF1F5F9)
    val OnSurface = Color(0xFFF1F5F9)
    val OnSurfaceVariant = Color(0xFF94A3B8)
...
```

- [ ] **Step 2: Commit**
```bash
git add app/src/main/java/com/voidclient/client/ui/theme/Theme.kt
git commit -m "style: update Theme.kt colors to Deep Cosmic Void"
```

---

### Task 2: Rebrand Header in `WNavigation.kt`

**Files:**
- Modify: `app/src/main/java/com/voidclient/client/ui/component/WNavigation.kt`

- [ ] **Step 1: Update CompactWHeader text**
Change the header text from "W" to a glowing, uppercase "VOID" text.

- [ ] **Step 2: Commit**
```bash
git add app/src/main/java/com/voidclient/client/ui/component/WNavigation.kt
git commit -m "style: update sidebar header text to VOID"
```

---

### Task 3: Revamp OverlayClickGUI and Shortcut Buttons

**Files:**
- Modify: `app/src/main/java/com/voidclient/client/overlay/gui/classic/OverlayClickGUI.kt`
- Modify: `app/src/main/java/com/voidclient/client/overlay/gui/classic/OverlayShortcutButton.kt`

- [ ] **Step 1: Reskin ClickGUI background colors**
Update `OverlayClickGUI.kt` colors to match the theme.
```kotlin
private val DarkBackground = Color(0xFF0B0714)
private val SidebarBackground = Color(0xFF120C22)
private val HeaderBackground = Color(0xFF160E27)
private val AccentPrimary = Color(0xFF9D4EDD)
private val TextPrimary = Color(0xFFF1F5F9)
private val TextSecondary = Color(0xFF94A3B8)
private val ButtonBackground = Color(0xFF18102B)
```

- [ ] **Step 2: Fix Shortcut Button border color**
Change the border color in `OverlayShortcutButton.kt` from hardcoded red/white to primary/onPrimary theme colors.
```kotlin
        val borderColor by animateColorAsState(
            targetValue = if (module.isEnabled) WColors.PrimaryLight else Color.Transparent,
            label = "borderColor"
        )
        val textColor by animateColorAsState(
            targetValue = if (module.isEnabled) WColors.PrimaryLight else Color.White,
            label = "textColor"
        )
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/java/com/voidclient/client/overlay/gui/classic/OverlayClickGUI.kt app/src/main/java/com/voidclient/client/overlay/gui/classic/OverlayShortcutButton.kt
git commit -m "style: revamp ClickGUI and Shortcut buttons with Deep Cosmic Void theme"
```

---

### Task 4: Rebrand Greeting in `HomePage.kt`

**Files:**
- Modify: `app/src/main/java/com/voidclient/client/router/main/HomePage.kt`

- [ ] **Step 1: Replace any remaining WClient or W references**
Ensure welcome card and subtitles reference "Voidclient" and "the void" style instead of older "WClient" / "W".

- [ ] **Step 2: Commit**
```bash
git add app/src/main/java/com/voidclient/client/router/main/HomePage.kt
git commit -m "style: update welcome card branding to Voidclient"
```
