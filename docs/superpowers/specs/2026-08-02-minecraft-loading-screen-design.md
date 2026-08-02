# Minecraft-Blocky Loading Screen — Design

**Date:** 2026-08-02
**Status:** Approved by user ("Build. I fully approve.") — implemented without pushing
**Related:** UI overhaul spec (`2026-08-02-ui-overhaul-design.md`), Deep Cosmic Void theme

## Goal

Replace the current loading screen (`ui/component/LoadingScreen.kt`) with a Minecraft-blocky
experience that still matches the Deep Cosmic Void palette: procedural voxel "V" logo that
assembles block-by-block, a rotating isometric grass cube, a classic green XP-bar progress
fill, pixelated starfield, and the bundled `minecraft.otf` font.

## Requirements

- Minecraft-blocky vibe, Minecraft font (`res/font/minecraft.otf`) for title + status text
- Animated voxel "V" logo (per-block assembly, bottom-up, staggered spring pop-in)
- Rotating isometric grass cube (grass-green top, dirt-brown sides, blocky grid lines) with bob
- Classic green Minecraft XP-bar progress (pixel-segmented fill)
- Pixelated starfield background (squares, not round dots) + vignette
- Keep ~4.5s total fake-load duration; status text maps to progress
- `onDone` contract unchanged; add a brief fade-out handoff to the main UI
- Implementation approach: pure Compose Canvas (Approach 1), no new assets

## Implementation

### `ui/theme/Theme.kt`
Add to `WColors`:
- `Grass = 0xFF7CBD5B`, `GrassDark = 0xFF5A9E3B` (grass block top face)
- `Dirt = 0xFF8B5A2B`, `DirtDark = 0xFF5E3A1E` (grass block side faces)
- `XpGreen = 0xFF64FF64`, `XpGreenDark = 0xFF2BB52B`, `XpTrack = 0xFF0A2E0A` (XP bar)

### `ui/component/LoadingScreen.kt` (rewrite, ~380 lines)
Private composables inside the file:

1. **`McFont`** — `FontFamily(Font(R.font.minecraft))`, loaded once.
2. **`McText(text, size, color, outline)`** — Text in McFont with a hard dark outline
   (drawn via `drawBehind`, 4 offset dark rects/text shadows — classic MC style).
3. **`PixelStarfield(phase)`** — `Canvas` behind everything: ~40 fixed random star
   positions (seeded once via `remember`), each drawn as a small square whose alpha
   twinkles from a single infinite transition using per-star phase offsets.
4. **`VoxelVLogo(assembled: Boolean)`** — 9x7 boolean mask of the letter "V". Each
   occupied cell draws a 3-face voxel block (top face lighter, front face base, side
   face darker, 1px bevel gaps). Blocks pop in bottom-up row-by-row: per-block
   `Animatable`/delay-driven scale 0→1 with `SpringBouncy`, staggered
   `delayMillis = row * 90 + col * 30` driven by progress (starts immediately,
   completes ~1.5s). When assembled, whole logo breathes (scale 1.0→1.03).
5. **`GrassCube(angle)`** — isometric cube projection: rotating `angle` around Y,
   draws 3 visible faces (top = Grass/GrassDark grid, left/right = Dirt/DirtDark),
   blocky grid lines on each face, gentle vertical bob, positioned right of the logo.
6. **`XpBar(progress, glow)`** — classic MC XP bar: dark green track
   (`XpTrack`, pixelated border), fill in vertical green gradient (XpGreen → XpGreenDark)
   segmented into pixel squares, fills left-to-right. Glows when complete.
7. **`LoadingScreen(onDone)`** — layout: starfield → vignette → centered Column:
   VoxelVLogo, McText "VOIDCLIENT" (white, outline, lavender glow), XpBar, `%` +
   status McText. Timing loop identical to current (5 steps, ~4.5s). On 100%:
   brief hold (800ms), XP bar flash, then `onDone()`.

### `activity/MainActivity.kt`
- Wrap loading in a fade-out handoff: `Box { Navigation(); AnimatedVisibility(visible =
  showLoading, exit = fadeOut(tween(400))) { LoadingScreen(onDone = { showLoading = false }) } }`.
- Navigation() (NavHost/MainScreen incl. video bg) composes underneath so the reveal is seamless.

## Out of scope
- No new drawable/vector assets, no font changes, no timing changes to relay/service init.
- `minecraft_bg.gif`, `void_background.mp4` untouched (loading screen stays canvas-only).

## Verification
- Code compiles (`:app:compileDebugKotlin`) and CI APK build passes — to run after user
  confirms; per user instruction nothing built/pushed in this pass.
- Manual: logo assembles bottom-up, cube rotates/bobs, XP bar fills, status text
  advances, fade-out reveals Home.
