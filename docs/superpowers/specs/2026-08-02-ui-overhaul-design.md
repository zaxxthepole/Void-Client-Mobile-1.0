# Voidclient UI Overhaul — Design Spec

**Date:** 2026-08-02
**Status:** Approved
**Palette:** Deep Cosmic Void (unchanged): bg `#0B0714`, surface `#120C22`, accent `#9D4EDD`, glow `#C084FC`

## 1. Goals

- Replace the WClient-derived UI with a professional, custom design.
- Overlay becomes a bottom slide-up panel.
- App becomes bottom-nav based with shared-element-style transitions.
- Clean dark glass aesthetic — remove the infinite glow-animation overload.
- Single unified color system (WColors only).

## 2. App UI

### Navigation
- Bottom `NavigationBar` with 4 tabs: **Home**, **Play**, **Modules**, **Settings**.
- Tab switch: spring-based animated content with fade + slight vertical parallax.
- Active indicator: purple pill (`#9D4EDD` at 15% alpha) behind the icon.

### Pages
| Tab | Content |
|-----|---------|
| Home | Hero relay-status card, quick actions (Connect/Disconnect/Overlay), recent activity |
| Play | Account section (selected account card or Add Account), server grid (2-col), Realms section |
| Modules | Search bar, category chips (All/Combat/Motion/Visual/Misc), module cards |
| Settings | Sectioned settings list (General, Overlay, Config, About); About slides in as sub-page |

### Key interactions
- Server selection: bottom sheet instead of dialog.
- Module cards: toggle + expandable settings (shared component with overlay).
- Settings: expandable sections; About as slide-in sub-page with back arrow.

## 3. Overlay GUI

- Full-screen dim layer; bottom panel slides up from bottom edge.
- Panel: drag handle, header (title + close), category chips, module list, footer (active count).
- Responsive: `fillMaxWidth(max 480dp)`, `heightIn(max 60% screen)`.
- Module cards: glass gradient, 3dp category accent bar, Material3 switch, expandable settings with staggered fade-in.
- OverlayButton stays draggable; reskinned to match.

## 4. Component System

New components in `ui/component/`:
- `VCPrimaryButton`, `VCToggleSwitch`
- `VCCard` (clean glass, gradient border, NO infinite glow)
- `VCBottomNavBar`
- `VCModuleCard` (shared by app Modules tab + overlay)
- `VCServerCard`
- `VCSettingsRow`, `VCSectionHeader`
- `VCSearchBar`

## 5. Color/Animation Cleanup

- Merge `ClickGUIColors` into `WColors`; add `Success`, `Warning`, `Info`.
- Remove ALL hardcoded colors from ModuleContent, OverlayClickGUI, SettingsPage, LoadingScreen.
- Remove `rememberInfiniteTransition` glow animations from WGlassCard/WButton/WFloatingActionButton.
- Standard animation specs in `ui/theme/Animations.kt`:
  - `SpringSoft = spring(dampingRatio = 0.8f, stiffness = Medium)`
  - `SpringBouncy = spring(dampingRatio = 0.5f, stiffness = Medium)`
  - `Fade = tween(220)`, `Slide = tween(320, FastOutSlowInEasing)`

## 6. Architecture Fixes

- Fix SettingsPage Self Destruct scope bug (was outside Scaffold padding).
- Replace global module cache with `remember`-scoped state.
- Remove dead code: `WNavigation.kt` WSidebar, `NavigationRailX.kt` (verify references first).
- Overlay sizing becomes responsive (no fixed 600x340dp).

## 7. Non-Goals

- No new modules, no packet/network changes.
- Keep all existing business logic (accounts, realms, config import/export, self-destruct) intact.
- Keep video background but reduce overlay alpha to ~0.3 on app pages.
