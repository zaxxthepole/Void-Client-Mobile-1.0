# Design: Module Revamp (VAura/Killaura/Fly/FreeCam) + Hardcoded Anti-Cheat Bypass

**Date:** 2026-08-02
**Status:** Approved
**Scope:** Rebuild 4 cheat modules (VAura, Killaura, Fly, FreeCam) to professional quality, and add a hardcoded, always-on anti-cheat bypass (ACB) baked into the client core — not a module.

## 1. Goals

- Rebuild VAura, Killaura, Fly, FreeCam "100%" — full professional rebuild with rich settings and anti-cheat-conscious defaults.
- Add a strong anti-cheat bypass that survives **huge public Bedrock servers** (real anti-cheat plugins).
- The ACB is **hardcoded and always on** — no module, no UI toggle.
- Architecture: core ACB service + module API (approach A), borrowing selective deep-interception techniques from approach C (in-memory packet rewriting at the listener layer).

## 2. Key Protocol Facts (verified in vendored code)

- `PlayerAuthInputPacket` fields: `rotation` (Vector3f), `position` (Vector3f), `motion` (Vector2f), `inputData` (Set<PlayerAuthInputData>), `tick` (long), `delta` (Vector3f), `interactRotation` (Vector2f), `cameraOrientation` (Vector3f), `rawMoveVector` (Vector2f).
- `InventoryTransactionPacket` (ITEM_USE_ON_ENTITY, actionType 1): `runtimeEntityId`, `hotbarSlot`, `itemInHand`, `playerPosition`, `clickPosition`, `headPosition` (Vector3f). `headPosition` is the server-side hit-check rotation hook for silent aim.
- `MovePlayerPacket.Mode`: NORMAL / TELEPORT — teleport sequencing for tp-based movement.
- `StartGamePacket.authoritativeMovementMode` is forced to SERVER by LocalPlayer — server simulates movement from auth inputs.
- The relay is a full MITM (`WRelaySession`): we can rewrite what the server sees independently of what the client renders.

## 3. Architecture

### New package: `app/src/main/java/com/voidclient/client/game/acb/`

| Component | Responsibility |
|-----------|----------------|
| `AcbState.kt` | Per-session state: last tick, position, rotation, ground state, velocity estimate, per-tick deltas. Reset on disconnect / StartGamePacket |
| `AntiCheatBypass.kt` | Orchestrator (`object Acb`), registered **first** in the packet pipeline, runs before every module on both directions |
| `TickSynchronizer` | Tracks client tick counter from PlayerAuthInputPacket; guarantees every injected packet (attack/move/teleport) carries a consistent tick; heals gaps |
| `RotationManager` | **Silent aim**: modules call `Acb.rotation.lookAt(target)` → writes aim into `interactRotation` + `headPosition` fields on attack transactions only; client camera never moves; optional smoothing cap for rotation-velocity checks |
| `GroundValidator` | Recomputes `onGround` from Y-delta/motion; silently fixes mismatches in auth inputs |
| `MoveGuard` / `TeleportProtector` | Splits any module teleport into sub-threshold steps; sequences across ticks; keeps server-side simulated path plausible |
| `PacketOrderGuard` | Attacks only fire tick-aligned; burst/boost packets jittered across the tick window; never fires between ticks |
| `VarianceEngine` | Micro-randomization of harmless fields (motion noise, rotation micro-jitter, tick jitter) to defeat pattern analysis |
| `AcbConfig.kt` | Hardcoded constants (thresholds, smoothing rates, jitter ranges, step sizes) — tuned in code, not UI |

### Data flow (C-style interception, in-memory)

```
client → relay → [ACB beforeServerBound] → modules → server
server → relay → [ACB beforeClientBound] → modules → client
```

- **serverBound**: ACB fixes ticks/ground/variance → applies attack-rotation bursts → guards movement plausibility → forwards to modules.
- **clientBound**: ACB observes `StartGamePacket` (movement mode), server `MovePlayerPacket` corrections (updates state), suppresses rubber-band corrections when a module intentionally desyncs (freecam), never lets the server "snap back" ghost positions.
- Module attacks go through a single choke point: `Acb.attack(entity)` builds the full `InventoryTransactionPacket` (correct rotation/tick/timing) — replaces raw `LocalPlayer.attack()` calls in rebuilt modules.

### Activation scope (user-approved)

- **Always-on cleanup + adaptive stealth**: ACB cleans movement/auth-input packets at all times (legit rotations, consistent ticks, correct ground state). When cheat modules activate, it adds stealth layers (rotation spoof, teleport protection, burst jitter).

## 4. Module Rebuilds

### VAuraModule (Combat) — "high-CPS multi-target pressure"

- Targeting: `Max Targets` (1–8, default 4), `Range` (default 8), sorting by distance / health / angle / random
- Attack: `CPS` (1–50, default 20) + `Packets per hit` (1–5, default 2), jittered by PacketOrderGuard; burst spread across the tick window
- Filtering: players-only / mobs-only, anti-bot (blank name/XUID check), friend exclusion
- Modes: single-target lock / sequential rotation / hit-all-in-range
- Aim: silent aim via `Acb.rotation.lookAt()`; `Swing Anim` toggle; 2D vs 3D distance option

### KillauraModule (Combat) — "surgical single-target silent aim"

- Targeting: one target, prioritized by angle-to-crosshair, then distance; auto-switch with `Switch Delay`; `Range` (default 7)
- Attack: `CPS` (default 12), packets, backswing option
- Aim: silent rotation + `Rotation Smoothing` cap
- Movement assists (polished from current):
  - `tp_aura`: teleport behind target — **step-interpolated** via `Acb.teleport` (no instant 10-block jumps), `TP Speed`, `Y Offset`, `Keep Distance`
  - `strafe`: smooth circular strafing with sub-threshold position deltas
- Filtering: players/mobs, anti-bot, friends, angle-cone "walls proxy" (no block data exists client-side; true raycast walls-check is impossible)

### FlyModule (Motion) — mode-based

- Modes (dropdown):
  - `vanilla` — current abilities approach, kept for LAN/private servers
  - `jump` — chain jumps + glide; looks like legit jump-spam; near-undetectable, slower
  - `teleport` — step-teleports upward via `Acb.teleport` under per-tick threshold; fast, higher risk
- Settings: `Speed`, `Hover` (hold altitude), `Auto Descend` on disable (smooth landing, no fall damage), sprint-boost integration
- Horizontal movement via auth-input motion + `SetEntityMotion`; vertical per-mode

### FreeCamModule (Visual) — true ghost camera

- Movement: forward/back/strafe from held keys (auth-input `inputData`), up/down via jump/sneak, yaw from player look
- `Speed` slider + sprint multiplier, optional height clamp
- Client-rendered only: ghost `MovePlayerPacket` goes to the client; server never sees it — real player stays at real position (zero detection surface)
- Reset on disable: camera snaps back to the real body position

## 5. Cleanup

- `PositionSpoofModule` / `TimingSpoofModule` **removed** — functions absorbed into the always-on ACB VarianceEngine. Old config entries ignored gracefully.
- Module names/keys unchanged — existing configs still load. No ModuleManager changes.

## 6. Verification

1. CI: `assembleDebug` green via GitHub Actions (standing verification gate — no local gradle builds).
2. Manual device test on a public server:
   - no rubber-banding / server position corrections (MoveGuard / TeleportProtector)
   - no kick messages ("movement too fast", "cheating detected")
   - freecam ghost moves in 3D while body stays put
   - fly jump mode stealthy; teleport mode smooth stepping
   - aura silent aim: target takes damage while camera stays still
3. If a server still flags something, `AcbConfig` constants are the tuning point — no UI changes.
4. Debug `Log.i("Acb", ...)` lines per layer activation.

## 7. Docs

- This design doc.
- CONTEXT.md updated: ACB section, module catalog notes, config tuning reference.
