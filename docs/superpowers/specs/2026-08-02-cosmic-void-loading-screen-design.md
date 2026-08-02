# Cosmic Void Loading Screen — Design

**Date:** 2026-08-02
**Status:** Approved by user — implemented in commit after `25fd6e6`
**Supersedes:** `2026-08-02-minecraft-loading-screen-design.md` (Minecraft-blocky version rejected after review)

## Goal

Replace the loading screen (`ui/component/LoadingScreen.kt`) with a **cosmic void / space**
experience that matches the Deep Cosmic Void palette: a slowly spinning void vortex with
spiral particles, drifting nebula clouds, and an elegant glow. Keeps the hybrid MC elements
the user requested: minecraft.otf title/status text and the classic green XP-bar progress.

## Requirements

- Cosmic void / space vibe (Deep Cosmic Void palette: obsidian `#0B0714`, purple `#9D4EDD`,
  lavender `#C084FC`)
- Central **void portal / vortex**: 7 concentric elliptical rings, slow continuous spin,
  differential rotation speeds (inner faster), colors from bright lavender (inner) to deep
  purple (outer)
- **Spiral particles**: ~34 glowing dots orbiting the vortex with differential rotation so
  spiral arms form naturally; brighter/larger closer to the core
- **Nebula clouds**: 3 large drifting radial-gradient blobs (Primary/Secondary/SecondaryLight,
  low alpha) behind the vortex
- **Title** "VOIDCLIENT" in minecraft.otf with hard dark outline + lavender glow rings
- **XP bar**: classic green Minecraft XP bar (track + green gradient fill + white highlight
  + 10% segmentation ticks), synced to progress
- **Status text** in minecraft font: `%` (green) + status line
- Keep ~4.5s fake-load timing and `onDone()` contract; fade-out handoff in MainActivity stays
- Implementation: pure Compose Canvas, no new assets

## Implementation

### `ui/theme/Theme.kt`
- **Remove** `Grass`, `GrassDark`, `Dirt`, `DirtDark` (no longer used)
- **Keep** `XpGreen`, `XpGreenDark`, `XpTrack`

### `ui/component/LoadingScreen.kt` (rewrite)
Private pieces inside the file:

1. **`McFont`** — `FontFamily(Font(R.font.minecraft))`
2. **`McText(text, fontSize, color, ...)`** — MC font + hard 4-direction dark outline
   (px→dp via `with(density) { x.toDp() }`)
3. **`NebulaBlob` / `generateNebulaBlobs()`** — 3 seeded blobs (color, radius 0.45–0.65,
   phase, speed, alpha ~0.10–0.16)
4. **`NebulaCanvas(drift)`** — full-screen canvas; each blob drifts via
   `sin/cos(drift * 2π * speed + phase)` around the screen, drawn as a radial gradient
   `color.copy(alpha)` → transparent circle
5. **`OrbitParticle` / `generateOrbitParticles(34)`** — seeded, radiusFrac 0.18→1.0,
   speed 1.6→0.5 (differential rotation), random phase
6. **`VortexCanvas(time, pulse)`** — centered canvas (~290×200dp):
   - inner glow disc: radial gradient `Primary(0.20·pulse)` → transparent
   - 7 rings: `RING_COLORS` (SecondaryLight → PrimaryLight → Primary → Secondary →
     PrimaryDark → SecondaryVariant → PrimaryDark); ring radius `baseR·(1 + 0.16i)`,
     ellipse `ry = 0.62·r`, rotation `time·360·(1.25 − 0.11i) + 24i` degrees via
     `rotate(pivot)` + `drawOval(Stroke)`, alpha 0.5→0.18, stroke 2.5→1.2dp
   - core: white-lavender dot (pulsing) + glow dot
   - particles: `angle = time·360·speed + phase`, `r = 1.35·baseR·radiusFrac`,
     `drawCircle(SecondaryLight, alpha 0.35→0.75, radius 1.5→3px)`
7. **`XpBar(progress)`** — unchanged green MC XP bar from previous version
8. **`LoadingScreen(onDone)`** — same 5-step timing loop; infinite transitions:
   `vortexTime` (0→1, 9s linear), `nebulaDrift` (0→1, 14s linear), `glow` (0.45→1,
   1.8s reverse), `corePulse` (0.7→1, 1.4s reverse)

Layout (centered column over full-screen obsidian):
```
NebulaCanvas (full screen)
vignette (radial gradient overlay)
Column:
  "VOIDCLIENT" McText 30sp + glow rings
  VortexCanvas 290×200dp
  XpBar
  % McText (XpGreen) + status McText
```

### `activity/MainActivity.kt`
No change — fade-out `AnimatedVisibility` handoff already in place.

## Verification
- `:app:compileDebugKotlin` + CI APK build green
- Manual: vortex spins, spiral particles orbit, nebula drifts, XP bar fills, status text
  advances, fade-out reveals Home. User must install the freshly built APK (the previous
  "no change" report was caused by testing an older build).
