# Design Spec: Deep Cosmic Void Theme for Voidclient

## 1. Overview
Rebrand the user interface of Voidclient to a "Deep Cosmic Void" aesthetic. This involves updating colors, shapes, headers, icons, and glow effects to align with the "Void" identity while maintaining 100% of the existing functionality and layouts.

## 2. Color Palette
- **Base/Background:** Deep Obsidian (`#0B0714`)
- **Card/Surface:** Dark Violet (`#120C22`)
- **Secondary Surface:** Compact Indigo-Violet (`#18102B`)
- **Accent Primary:** Electric Glowing Purple (`#9D4EDD`)
- **Accent Secondary:** Vibrant Violet (`#A855F7`)
- **Accent Light:** Lavender Glow (`#C084FC`)
- **Text Primary:** Cool Platinum (`#F1F5F9`)
- **Text Secondary:** Slate Grey (`#94A3B8`)
- **Border/Glow:** Deep cosmic glow outline (`#3A224D` scaling to `#7B2FBE` under focus/enabled states)

## 3. Scope of Modifications
- **WColors & ClickGUIColors (`Theme.kt`):** Update base hex colors to deep cosmic shades. Replace any remaining red/white accent boundaries with theme-consistent neon violet and purple hues.
- **Overlay Header (`WNavigation.kt`):** Change the compact navigation bar header from "W" to a styled, glowing "VOID" or "V" icon with electric cosmic pulse animations.
- **Classic ClickGUI & Shortcut Buttons (`OverlayClickGUI.kt`, `OverlayShortcutButton.kt`):**
  - Re-skin the main header bar and background gradients to use Deep Obsidian and Dark Violet.
  - Fix the Shortcut button's red border/text (`Color.Red`) to instead use theme-consistent glowing purple (`WColors.PrimaryLight` / `#A855F7`) when enabled.
- **Home/Welcome Cards (`HomePage.kt`):** Customize greeting card headers and text to use "Voidclient" rather than WClient, emphasizing "The Void" styling.

## 4. Risks & Mitigations
- **Risk:** Breaking Compose compile/runtime layouts.
- **Mitigation:** Only modify Color parameters, font definitions, and basic Text titles. Ensure no functional layout containers, alignments, or list variables are removed or renamed.
