# Plan: Module Revamp (VAura, Killaura, Fly, FreeCam) + Hardcoded Anti-Cheat Bypass (ACB)

- **Goal:** Rebuild VAura, Killaura, Fly, and FreeCam into production-grade modules, and add a hardcoded, always-on anti-cheat bypass core (`game/acb/`) that makes all cheating traffic look like legit play on large public Bedrock servers with real anti-cheat.
- **Technologies:** Kotlin, CloudburstMC vendored protocol, Android app module (`:app`).
- **Principles:**
  1. The ACB is part of the client core, not a module. No toggle, always-on, registered FIRST in the packet pipeline.
  2. Legit-looking traffic at ALL times (during normal play too), with extra stealth layers only while a cheat module is active ("adaptive stealth").
  3. Module attacks route through the `Acb.attack(entity)` choke point — modules never craft packets by hand anymore.
  4. Server-authoritative movement is respected: desync (teleports, freecam) is done via sub-threshold stepping, never raw position jumps the server can flag.

## Background

Approved spec: `docs/superpowers/specs/2026-08-02-module-revamp-acb-design.md` (committed `7616db6`). User decision from brainstorming: the bypass is hardcoded into the core (no UI toggle, always-on cleanup + adaptive stealth), built as approach A (core ACB service + module API) borrowing deep packet-rewriting techniques from approach C. User targets **huge public servers with real anti-cheats** (not just vanilla). This plan implements that spec. All module names/keys stay unchanged so existing `UserConfig.json` files still load.

## Global Constraints

- **No local Gradle builds.** The standing rule: `.\gradlew.bat` is never run in-session (slow on this machine). Verification = push to `main` + GitHub Actions `assembleDebug` (workflow `.github/workflows/build.yml`) + manual device test.
- **Lombok ↔ Kotlin:** vendored packet classes use Lombok `@Data` — Kotlin MUST use explicit `getX()`/`setX()` calls, never property syntax (this is the #1 compile failure mode).
- **PowerShell 5.1 only** on this machine; `workdir` for all git/gradle commands = `C:\Users\mitra\OneDrive\Desktop\Gonzo\Voidclient`.
- No code comments unless surrounding code has them.
- 4-space indentation; Kotlin 1.9.x; Java 17 toolchain.
- Commit messages: conventional (`feat:`, `fix:`, `chore:`, `docs:`, `refactor:`).
- Never commit `CONTEXT.md` (gitignored) or `compile_log.txt` (gitignored).
- The Gonzo workspace root is a DIFFERENT git repo — never run git there.

## Repo-Specific Workflow

1. Write code, review for Lombok getters, commit, push (`git push origin main`).
2. Watch the GitHub Actions run for the pushed commit; it must be green before the next task.
3. If CI fails: read the failure (usually a compile error), fix, amend is NOT allowed — new commit, push again.
4. PowerShell renders native stderr red on success — check the actual result, not the color.
5. Manual device test only after the full plan is done (fresh CI artifact APK).

## Assumptions

- `Entity` API (verified): `runtimeEntityId`, `uniqueEntityId`, `posX/posY/posZ` (+ `prevPosX/Y/Z`), `vec3Position`, `vec3Rotation`, `attributes: MutableMap<String, AttributeData>`, `identifier`, `distance(other)`.
- `AttributeData` (vendored, Lombok `@Value`): Kotlin-visible properties `name`, `value`, `minimum`, `maximum` — health = `attributes["minecraft:health"]?.value` (defaults to 20.0f when absent).
- `session.level.playerMap` is a `ConcurrentHashMap<UUID, PlayerListPacket.Entry>`; `entry.name` is a nullable `TextHolder`, `entry.xuid` is nullable String (proven pattern in VAuraModule `isBot()`).
- `LocalPlayer`: `vec3Position`, `vec3Rotation`, `runtimeEntityId`, `inventory.heldItemSlot`, `inventory.hand`, `swing()`, `attack(entity)`.
- `GameSession`: `serverBound(packet)` / `clientBound(packet)` wrap `wRelaySession.serverBound/clientBound`; `beforePacketBound(packet): Boolean` (true = drop packet); `onDisconnect(reason)`; `displayClientMessage(message)`.
- `ModuleValues` API: `boolValue(name, default)`, `floatValue(name, default, range)`, `intValue(name, default, range)`, `enumValue(name, default, enumClass)` (enum pattern: `CoordinatesModule.kt:24-30` — nested `enum class` + `enumValue("Position", Position.TOP_LEFT, Position::class.java)`).
- `RotationUtils` (`game/utils/math/RotationUtils.kt`): `toRotation(from: Vector3f, to: Vector3f): Rotation`, `getRotationDifference(a, b)`, `getAngleDifference(a, b)` — reuse, do not re-implement.
- Packet API (verified against vendored sources):
  - `PlayerAuthInputPacket` (CORRECTED): `getRotation(): Vector3f` (x=yaw, y=pitch, z=roll), `getPosition(): Vector3f`, `getMotion(): Vector2f`, `getTick(): Long`, `getInputData(): EnumSet<PlayerAuthInputData>`, `getInteractRotation(): Vector2f` / `setInteractRotation(Vector2f)`; **NO `onGround` field exists on this packet** — grounded-ness comes from `Acb.ground.isGrounded()` (vertical position-delta tracking done by the ACB).
  - `InventoryTransactionPacket` (CORRECTED): `setTransactionType(InventoryTransactionType.ITEM_USE_ON_ENTITY)`, `setActionType(1)` — `actionType` is an **int**, there is NO `InventoryTransactionActionType` enum; `setRuntimeEntityId(Long)`, `setHotbarSlot(Int)`, `setItemInHand(ItemData?)`, `setPlayerPosition(Vector3f)`, `setClickPosition(Vector3f)` (clicked point — target position for entity attacks), `setHeadPosition(Vector3f)` (player's HEAD WORLD POSITION ~ +1.62 y — NOT angles); **no `interactRotation` field on this packet**.
  - `MovePlayerPacket`: `setRuntimeEntityId(Long)`, `setPosition(Vector3f)`, `setRotation(Vector3f)`, `setMode(MovePlayerPacket.Mode.NORMAL)`, `setOnGround(Boolean)`, `setTick(Long)`.
  - `SetEntityMotionPacket`: `setRuntimeEntityId(Long)`, `setMotion(Vector3f)`.
  - `StartGamePacket`: client-bound session start.
  - `PlayerAuthInputData` enum members used: `JUMPING`, `SNEAKING`, `SPRINTING`, `ASCEND`, `DESCEND` (verified present).
  - `MovePlayerPacket.Mode` enum: `NORMAL`, `TELEPORT`, etc. (use NORMAL for render sync).
  - Directional movement in auth inputs comes from `getMovementVector()` (Vector3f) when available; `inputData` flags used for jump/sneak/descend detection.
- `Vector2f(x, y)` / `Vector3f.from(x, y, z)` from `org.cloudburstmc.math`.
- Transaction enums live in `org.cloudburstmc.protocol.bedrock.data.inventory.transaction.*` (copy exact import paths from existing `KillauraModule.kt` — it already crafts these packets).

## Requirements (Acceptance Criteria)

1. New package `app/src/main/java/com/voidclient/client/game/acb/` with: `AcbConfig.kt`, `AcbState.kt`, `AntiCheatBypass.kt` (object `Acb`), `RotationManager.kt`, `TickSynchronizer.kt`, `GroundValidator.kt`, `TeleportProtector.kt`, `PacketOrderGuard.kt`, `VarianceEngine.kt`.
2. `Acb.onPacket(packet)` runs FIRST in `GameSession.beforePacketBound` (before localPlayer/level state update and before the module loop, both directions); `Acb.bind(this)` in `GameSession` init; `Acb.onDisconnect(reason)` in `GameSession.onDisconnect`.
3. Always-on behaviors even with zero modules enabled: auth-input tick continuity, onGround plausibility, tiny rotation variance.
4. Module attacks go exclusively through `Acb.attack(entity, packets)`; no module constructs `InventoryTransactionPacket` directly.
5. VAura: settings Range (default 8), CPS (default 20, 1–50), Max Targets (default 4, 1–8), Packets (default 2, 1–5), Target Sort (DISTANCE/HEALTH/ANGLE/RANDOM), Attack Mode (SINGLE_LOCK/SEQUENTIAL/HIT_ALL), Silent Aim (bool, default on), Rotation Smoothing (bool, default on). Targets players + mobs, skips friends and bots.
6. Killaura: surgical single-target; settings Range (default 6), CPS (default 12, 1–30), Angle (default 45°, 5–180), Smooth Rotations (bool), TP Aura (bool), TP Distance (default 1.5, 0.5–5), Strafe (bool).
7. Fly: Mode dropdown (VANILLA / JUMP / TELEPORT), Speed (default 0.4, 0.1–2), Hover (bool, default on), Auto Descend (bool, default off). Names/keys unchanged.
8. FreeCam: true 3D ghost camera; server never sees the ghost; body keeps moving normally via untouched auth inputs; snap-back on disable. Speed setting (default 1.2, 0.1–5).
9. `PositionSpoofModule.kt` and `TimingSpoofModule.kt` deleted from disk and from `ModuleManager.kt` registration (their functionality is absorbed into the ACB). Module count in README + CONTEXT.md updated.
10. All pushes green on CI; manual device test checklist passed on a fresh CI artifact.

## Implementation Tasks

---

### Task 1 — ACB core package + GameSession wiring

Files:
- CREATE `app/src/main/java/com/voidclient/client/game/acb/AcbConfig.kt`
- CREATE `app/src/main/java/com/voidclient/client/game/acb/AcbState.kt`
- CREATE `app/src/main/java/com/voidclient/client/game/acb/AntiCheatBypass.kt`
- CREATE `app/src/main/java/com/voidclient/client/game/acb/RotationManager.kt`
- CREATE `app/src/main/java/com/voidclient/client/game/acb/TickSynchronizer.kt`
- CREATE `app/src/main/java/com/voidclient/client/game/acb/GroundValidator.kt`
- CREATE `app/src/main/java/com/voidclient/client/game/acb/TeleportProtector.kt`
- CREATE `app/src/main/java/com/voidclient/client/game/acb/PacketOrderGuard.kt`
- CREATE `app/src/main/java/com/voidclient/client/game/acb/VarianceEngine.kt`
- MODIFY `app/src/main/java/com/voidclient/client/game/GameSession.kt`

Interfaces:

```kotlin
// AcbConfig.kt — hardcoded tuning constants (the only place to tune stealth)
object AcbConfig {
    const val TICK_DRIFT_THRESHOLD = 5L        // max tick gap we silently heal
    const val GROUND_EPSILON = 0.3f            // vertical delta considered "standing"
    const val GROUND_JUMP_THRESHOLD = 0.6f     // vertical delta above which onGround=true is implausible
    const val VARIANCE_YAW = 0.12f             // degrees, max jitter applied to auth yaw
    const val VARIANCE_PITCH = 0.06f           // degrees, max jitter applied to auth pitch
    const val TELEPORT_MAX_STEP = 2.0f         // blocks per tick while desyncing
    const val TELEPORT_EPSILON = 0.01f
    const val PACKET_GAP_BASE = 20L            // ms between attack bursts
    const val PACKET_GAP_JITTER = 60L          // +random 0..N ms
    const val AIM_SMOOTH_RATE = 0.25f          // per-tick blend toward target rotation
    const val ATTACK_PACKETS_DEFAULT = 2
}

// AcbState.kt — per-session mutable state
class AcbState {
    var session: GameSession? = null
    var lastServerTick = 0L
    var lastAuthY = 0f
    var attackLockUntil = 0L
    var pendingTeleport: Vector3f? = null
    var stealthActive = false          // set true by cheat modules while enabled
    var activeDesync = false           // set true while freecam/tp desync is intentional
    fun reset() { session = null; lastServerTick = 0L; lastAuthY = 0f; attackLockUntil = 0L; pendingTeleport = null; stealthActive = false; activeDesync = false }
}
```

Step-by-step:

1. Write `AcbConfig` and `AcbState` exactly as above (imports: `org.cloudburstmc.math.vector.Vector3f`).
2. Write the five small components:

```kotlin
// TickSynchronizer.kt
class TickSynchronizer {
    fun sync(packet: PlayerAuthInputPacket) {
        val t = packet.getTick()
        val expected = Acb.state.lastServerTick + 1
        val fixed = when {
            Acb.state.lastServerTick == 0L -> t
            t == Acb.state.lastServerTick -> expected                 // duplicate tick
            t in (expected + 1)..(expected + AcbConfig.TICK_DRIFT_THRESHOLD) -> expected // small drift
            else -> t                                                  // big reset: re-baseline
        }
        if (fixed != t) packet.setTick(fixed)
        Acb.state.lastServerTick = packet.getTick()
    }
    fun observeServerTick(tick: Long) { if (tick != 0L) Acb.state.lastServerTick = tick }
}

// GroundValidator.kt
class GroundValidator {
    fun fix(packet: PlayerAuthInputPacket) {
        val deltaY = packet.getPosition().getY() - Acb.state.lastAuthY
        val ground = packet.getOnGround()
        if (ground && deltaY > AcbConfig.GROUND_JUMP_THRESHOLD) packet.setOnGround(false)
        else if (!ground && kotlin.math.abs(deltaY) < AcbConfig.GROUND_EPSILON) packet.setOnGround(true)
        Acb.state.lastAuthY = packet.getPosition().getY()
    }
}

// VarianceEngine.kt — always-on micro-jitter (only grows when stealthActive)
class VarianceEngine {
    fun apply(packet: PlayerAuthInputPacket) {
        val r = packet.getRotation()
        val yawJ = (Random().nextFloat() * 2f - 1f) * AcbConfig.VARIANCE_YAW
        val pitchJ = (Random().nextFloat() * 2f - 1f) * AcbConfig.VARIANCE_PITCH * (if (Acb.state.stealthActive) 1f else 0.25f)
        packet.setRotation(Vector3f.from(r.getX() + yawJ, r.getY() + pitchJ, r.getZ()))
    }
}

// PacketOrderGuard.kt — burst spacing so attacks never fire at fixed intervals
class PacketOrderGuard {
    fun acquire(): Boolean {
        val now = System.currentTimeMillis()
        if (now < Acb.state.attackLockUntil) return false
        Acb.state.attackLockUntil = now + AcbConfig.PACKET_GAP_BASE + Random().nextLong(AcbConfig.PACKET_GAP_JITTER)
        return true
    }
}

// RotationManager.kt — silent aim: smoothed rotation target, applied ONLY to attack packets
class RotationManager {
    private var smoothed = Rotation(0f, 0f)
    fun rotateTowards(target: Entity): Rotation {
        val player = Acb.state.session?.localPlayer ?: return smoothed
        val desired = toRotation(player.vec3Position, target.vec3Position)
        val rate = AcbConfig.AIM_SMOOTH_RATE
        smoothed = Rotation(
            smoothed.yaw + getAngleDifference(desired.yaw, smoothed.yaw) * rate,
            smoothed.pitch + (desired.pitch - smoothed.pitch) * rate
        )
        return smoothed
    }
    fun applyTo(packet: InventoryTransactionPacket, look: Rotation) {
        packet.setInteractRotation(Vector2f.from(look.yaw, look.pitch))
        packet.setHeadPosition(Vector3f.from(look.yaw, look.pitch, 0f))
    }
}

// TeleportProtector.kt — sub-threshold teleport: server sees stepped positions in auth inputs
class TeleportProtector {
    fun teleportTo(target: Vector3f) { Acb.state.pendingTeleport = target }
    fun cancel() { Acb.state.pendingTeleport = null }
    fun step(packet: PlayerAuthInputPacket) {
        val target = Acb.state.pendingTeleport ?: return
        val pos = packet.getPosition()
        val dx = target.getX() - pos.getX(); val dy = target.getY() - pos.getY(); val dz = target.getZ() - pos.getZ()
        val dist = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
        if (dist <= AcbConfig.TELEPORT_EPSILON) { Acb.state.pendingTeleport = null; return }
        val t = kotlin.math.min(AcbConfig.TELEPORT_MAX_STEP, dist) / dist
        packet.setPosition(Vector3f.from(pos.getX() + dx * t, pos.getY() + dy * t, pos.getZ() + dz * t))
    }
}
```

3. Write `AntiCheatBypass.kt` — the orchestrator object:

```kotlin
object Acb {

    val state = AcbState()
    val aim = RotationManager()
    val tickSync = TickSynchronizer()
    val ground = GroundValidator()
    val variance = VarianceEngine()
    val guard = PacketOrderGuard()
    val teleport = TeleportProtector()

    fun bind(session: GameSession) {
        state.session = session
        state.reset()
        state.session = session
        log("bound to session")
    }

    fun onDisconnect(reason: String) {
        state.reset()
        log("disconnected ($reason)")
    }

    // Called FIRST from GameSession.beforePacketBound, both directions. Return true = drop.
    fun onPacket(packet: BedrockPacket): Boolean {
        when (packet) {
            is PlayerAuthInputPacket -> {
                tickSync.sync(packet)
                ground.fix(packet)
                teleport.step(packet)
                variance.apply(packet)
            }
            is MovePlayerPacket -> {
                tickSync.observeServerTick(packet.getTick())
                val lp = state.session?.localPlayer
                if (state.activeDesync && lp != null && packet.getRuntimeEntityId() == lp.runtimeEntityId) {
                    return true // suppress server rubber-band while freecam desync is intentional
                }
            }
            is StartGamePacket -> state.reset()
        }
        return false
    }

    // The single choke point every combat module uses to attack.
    fun attack(target: Entity, packets: Int = AcbConfig.ATTACK_PACKETS_DEFAULT): Boolean {
        val session = state.session ?: return false
        if (!guard.acquire()) return false
        val player = session.localPlayer
        val look = aim.rotateTowards(target)
        val pos = player.vec3Position
        repeat(packets) {
            val pkt = InventoryTransactionPacket()
            pkt.setTransactionType(InventoryTransactionType.ITEM_USE)
            pkt.setActionType(InventoryTransactionActionType.ATTACK)
            pkt.setRuntimeEntityId(target.runtimeEntityId)
            pkt.setHotbarSlot(player.inventory.heldItemSlot)
            pkt.setItemInHand(player.inventory.hand)
            pkt.setPlayerPosition(pos)
            pkt.setClickPosition(pos)
            aim.applyTo(pkt, look)
            session.serverBound(pkt)
        }
        log("attack ${target.runtimeEntityId} x$packets")
        return true
    }

    fun swing() {
        state.session?.localPlayer?.swing()
    }

    fun log(msg: String) {
        Log.i("Acb", msg)
    }
}
```

   NOTE: verify against existing `KillauraModule.kt` and copy its exact import paths for `InventoryTransactionPacket`, `InventoryTransactionType`, `InventoryTransactionActionType` (they live under `org.cloudburstmc.protocol.bedrock.data.inventory.transaction.*` in the vendored 3.0 tree). Verify `player.inventory.heldItemSlot` / `player.inventory.hand` are the correct accessors on the LocalPlayer's inventory (they are used by the existing modules).

4. Wire into `GameSession.kt` (three small edits):

```kotlin
class GameSession(val wRelaySession: WRelaySession) : ComposedPacketHandler {

    init { Acb.bind(this) }
    ...
    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        if (Acb.onPacket(packet)) return true        // FIRST line, before the when
        ...
    }
    ...
    override fun onDisconnect(reason: String) {
        Acb.onDisconnect(reason)                     // FIRST line
        ...
    }
}
```

5. Review every packet accessor in the new files against the Lombok rule (getX/setX).
6. Commit `feat(acb): hardcoded always-on anti-cheat bypass core + GameSession wiring`, push, watch CI.
7. Optional mid-check: `git show --stat HEAD` to confirm only intended files changed.

Verify: CI green. Manual (later): `Log.i("Acb", ...)` lines visible in logcat after connecting ("bound to session").

> IMPLEMENTATION NOTE (2026-08-02, committed as `2c395b3` + `aab437b`): the committed code deviates from the snippets above per code review: GroundValidator is split into `fixAuth(packet)` (tracks `lastAuthGrounded`/`lastAuthY` from the local player's auth stream, exposes `isGrounded()`) and `fixMove(packet)` (local-player MovePlayerPackets only); `AcbState.reset(keepSession: Boolean = false)` — the StartGamePacket branch calls it with `keepSession = true`; `RotationManager.applyTo(packet, target, look)` stamps `headPosition` = head world coords and `clickPosition` = target position, and a pending `interactRotation` (Vector2f yaw/pitch) is stamped once on the next auth input; the attack uses `setActionType(1)` (int) with `transactionType = ITEM_USE_ON_ENTITY`; `kotlin.random.Random` is used; no comments in new files. Tasks 2–5 must code against THIS committed API (reference block below).

---

### Task 2 — Rebuild VAuraModule

Files:
- REWRITE `app/src/main/java/com/voidclient/client/game/module/combat/VAuraModule.kt`

Interface (same class name, same key — config compat):

```kotlin
class VAuraModule : Module("VAura", ModuleCategory.Combat, defaultEnabled = true) {
    private val rangeValue = floatValue("Range", 8f, 1f..16f)
    private val cpsValue = intValue("CPS", 20, 1..50)
    private val targetsValue = intValue("Max Targets", 4, 1..8)
    private val packetsValue = intValue("Packets", 2, 1..5)
    private val sortValue = enumValue("Target Sort", SortMode.DISTANCE, SortMode::class.java)
    private val modeValue = enumValue("Attack Mode", AttackMode.SINGLE_LOCK, AttackMode::class.java)
    private val silentValue = boolValue("Silent Aim", true)
    private val smoothValue = boolValue("Rotation Smoothing", true)

    enum class SortMode { DISTANCE, HEALTH, ANGLE, RANDOM }
    enum class AttackMode { SINGLE_LOCK, SEQUENTIAL, HIT_ALL }
    ...
}
```

Algorithm (mirror the existing module's proven structure — packet-driven on `PlayerAuthInputPacket`, keep `onEnabled`/`onDisabled` toggling `Acb.state.stealthActive`):

1. `onEnabled()` → `Acb.state.stealthActive = true` (super.onEnabled() first for sound/message).
2. `onDisabled()` → `Acb.state.stealthActive = false` + cancel pending teleport.
3. In `beforePacketBound`, trigger on `PlayerAuthInputPacket` only.
4. Build candidates (keep the existing `getEntitiesInRange` pattern, verified in current file):
   - `session.level.entityMap.values.filter { it.distance(player) <= rangeValue }.filter { it.isTarget() }.filterNot { it is Player && FriendManager.isFriend(it.uuid) }`
   - keep `isTarget()` (players always; mobs only when a mobs-only setting is on — keep existing `mobsOnly` behavior if present, else target both) and the existing `Player.isBot()` check via `session.level.playerMap[this.uuid]`.
5. Sort per `sortValue`: DISTANCE (nearest first), HEALTH (lowest `attributes["minecraft:health"]?.value ?: 20f` first), ANGLE (smallest `getRotationDifference(toRotation(playerPos, targetPos), playerRotation)` first), RANDOM (shuffled with `kotlin.random.Random.Default`).
6. Take `max(targetsValue, 1)` targets, clamp to list size.
7. Attack per `modeValue`:
   - SINGLE_LOCK: attack ONLY the first target each cycle (re-picked each cycle — effectively "lock until out of range").
   - SEQUENTIAL: one attack per cycle, cycling index into the sorted list.
   - HIT_ALL: `Acb.attack(t, packets)` for every target in the window.
8. Cooldown via `Acb.guard.acquire()`; each call fires `packetsValue` packets (default 2) — with CPS 20 and gap jitter the burst spacing stays organic.
9. Swing animation: call `Acb.swing()` per attack cycle (mirrors current behavior).
10. If `silentValue` is false, ALSO send a client-bound render rotation? No — silent aim never moves the camera; when silent is OFF, follow the old behavior of visually snapping (send `SetEntityMotion` no…). SIMPLIFICATION: silent=false keeps the existing "visually rotate" behavior ONLY if the old module had it — otherwise treat silent as an always-on ACB feature and log. (Implementation note: check the current file; if it already never rotates the camera, silent is purely cosmetic metadata on the attack packet — keep `aim.applyTo` always.)

Verify: CI green. Reuse of the current file's working pieces (candidate filter, bot check, packet trigger) is expected — this is a rebuild, not a from-scratch.

---

### Task 3 — Rebuild KillauraModule

Files:
- REWRITE `app/src/main/java/com/voidclient/client/game/module/combat/KillauraModule.kt`

Interface:

```kotlin
class KillauraModule : Module("killaura", ModuleCategory.Combat, defaultEnabled = true) {
    private val rangeValue = floatValue("Range", 6f, 1f..12f)
    private val cpsValue = intValue("CPS", 12, 1..30)
    private val angleValue = floatValue("Angle", 45f, 5f..180f)
    private val smoothValue = boolValue("Smooth Rotations", true)
    private val tpAuraValue = boolValue("TP Aura", false)
    private val tpDistanceValue = floatValue("TP Distance", 1.5f, 0.5f..5f)
    private val strafeValue = boolValue("Strafe", true)
    ...
}
```

Algorithm:

1. Enable/disable toggles `Acb.state.stealthActive` (same as VAura).
2. Trigger on `PlayerAuthInputPacket`.
3. Target selection — the surgical part:
   - Candidates: players only (skip mobs) within `rangeValue`, not friends, not bots (reuse the same helpers as VAura).
   - Filter by crosshair angle: `getRotationDifference(toRotation(playerPos, targetPos), playerRotation) <= angleValue` — wait, `getRotationDifference` returns a hypot of (yaw diff, pitch diff) which is not a true angle; use it as the existing code does (it's the established metric) OR compute true angle via `acos(dot(dirToTarget, cameraDir))`. IMPLEMENTATION NOTE: try the `toRotation`/`getRotationDifference` metric first (matches existing codebase math); fall back to true angle if behavior feels wrong in testing.
   - Pick the target with the SMALLEST angle difference (surgical single-target).
4. Attack via `Acb.attack(target, packets=1)` gated by `Acb.guard.acquire()` — CPS 12 default.
5. TP Aura: if `tpAuraValue` and target distance > `tpDistanceValue`:
   - `Acb.teleport.teleportTo(targetPos - normalized(dirToTarget) * tpDistanceValue)` (i.e., one block before the target),
   - ALSO send a clientBound `MovePlayerPacket` (runtimeEntityId = local player, mode NORMAL, onGround=true) at the destination so the client renders the snap — the server only ever sees the stepped auth-input path (sub-threshold). The design's "step-interpolated tp_aura" is exactly this.
6. Strafe: when `strafeValue` and a target is held, between cooldown windows send a small clientBound `SetEntityMotionPacket(runtimeEntityId = local, motion = Vector3f.from(sideways, 0f, forward))` (direction from player yaw, magnitude ~0.1–0.2) so the local view visibly side-steps — the client then reports that motion in auth inputs, which the ACB forwards unchanged (looks like legit strafing).
7. Swing: `Acb.swing()` per hit.

Verify: CI green.

---

### Task 4 — Rebuild FlyModule

Files:
- REWRITE `app/src/main/java/com/voidclient/client/game/module/motion/FlyModule.kt`

Interface:

```kotlin
class FlyModule : Module("fly", ModuleCategory.Motion, defaultEnabled = false) {
    private val modeValue = enumValue("Mode", FlyMode.VANILLA, FlyMode::class.java)
    private val speedValue = floatValue("Speed", 0.4f, 0.1f..2f)
    private val hoverValue = boolValue("Hover", true)
    private val descendValue = boolValue("Auto Descend", false)

    enum class FlyMode { VANILLA, JUMP, TELEPORT }
    ...
}
```

Algorithm:

1. Enable/disable toggles `Acb.state.stealthActive`.
2. Keep the existing VANILLA implementation intact (it already works — UpdateAbilities + local-player flags), just route through the new values:
   - Speed maps onto the existing horizontal speed handling.
   - Hover = existing keep-y behavior; Auto Descend = existing descend behavior (verify they exist in the current file and preserve them).
3. JUMP mode — the "legit" chain-jump fly (looks like a normal player holding jump):
   - On each `PlayerAuthInputPacket` where `Acb.ground.isGrounded()` is true (grounded-ness is tracked by the ACB from vertical position deltas — auth inputs have no onGround field): send clientBound `SetEntityMotionPacket(runtimeEntityId = local, motion = Vector3f.from(0f, 0.42f, 0f))` — a normal jump impulse. The client jumps; auth inputs follow real jump physics; the server sees only legit jumps. Holding jump chains them (spam-jump fly). Horizontal: when airborne and holding forward, `speedValue` scales a small forward motion boost (cap ≈ 0.1 blocks/tick so it stays under sprint-like velocities).
   - Hover / Auto Descend apply in VANILLA mode only (preserve the current file's existing behavior for them); JUMP mode deliberately does not counteract gravity (server plausibility).
4. TELEPORT mode:
   - When the jump key (`inputData` contains `JUMPING`) is held: `Acb.teleport.teleportTo(playerPos + up)` per tick (steps of ≤ `TELEPORT_MAX_STEP` blocks/tick via the auth-input rewriting) + clientBound render `MovePlayerPacket` at the current step position (so the client camera follows).
   - Descend with the descend/sneak key via `inputData` `SNEAKING`/`DESCEND` → `teleportTo(playerPos - up)`.
   - This is the high-speed mode; TP aura and this share the same protector.
5. When disabled: `Acb.teleport.cancel()`, `Acb.state.stealthActive = false`.

Verify: CI green.

---

### Task 5 — Rebuild FreeCamModule

Files:
- REWRITE `app/src/main/java/com/voidclient/client/game/module/visual/FreeCamModule.kt`

Interface:

```kotlin
class FreeCamModule : Module("FreeCam", ModuleCategory.Visual, defaultEnabled = false) {
    private val speedValue = floatValue("Speed", 1.2f, 0.1f..5f)
    private val offset = Vector3f.from(0f, 0f, 0f)
    private var lastTick = 0L
    ...
}
```

Algorithm:

1. `onEnabled()`: `Acb.state.activeDesync = true`; store nothing else — offset starts at 0.
2. `onDisabled()`: `Acb.state.activeDesync = false`; send one clientBound `MovePlayerPacket` (local player, body position, mode NORMAL) to snap the camera back; reset offset.
3. The ghost is CLIENT-RENDERED ONLY — this is the design's "true 3D ghost camera": the server never receives any FreeCam packet. The body's real position keeps updating from the player's own (untouched) auth inputs.
4. On each `PlayerAuthInputPacket` (server-bound, but we only READ it — never rewrite):
   - `dt = (packet.getTick() - lastTick) / 20f` (clamp 0.01..0.1), update `lastTick`.
   - Direction from `packet.getRotation().getX()` (yaw) + movement flags: read `getInputData()` for `JUMPING` (up) / `SNEAKING` (down) and use the movement vector for forward/back/left/right (fallback: none when absent).
   - `offset += dir * speedValue * dt`.
   - Send clientBound `MovePlayerPacket(local, bodyPos + offset, rotation = packet rotation, mode NORMAL, onGround = false, tick = packet.getTick())` — the camera follows the ghost.
5. The ACB's `activeDesync` flag suppresses server rubber-band corrections to the local player while FreeCam is on (Task 1 hook).
6. On disconnect: reset offset (guard against stale state).

Verify: CI green.

---

### Task 6 — Remove PositionSpoof + TimingSpoof

Files:
- DELETE `app/src/main/java/com/voidclient/client/game/module/misc/PositionSpoofModule.kt`
- DELETE `app/src/main/java/com/voidclient/client/game/module/misc/TimingSpoofModule.kt`
- MODIFY `app/src/main/java/com/voidclient/client/game/ModuleManager.kt` (remove both registrations from the Misc section)

Steps:
1. Remove the two `moduleList.add(...)` lines in `ModuleManager.kt` under the Misc category.
2. Delete both files.
3. Grep the repo for `PositionSpoof` / `TimingSpoof` references (overlay, README, other modules) and fix any dangling references.
4. Commit `refactor: remove PositionSpoof/TimingSpoof modules (absorbed into ACB core)`, push, CI.
5. Config compat: old config entries for these two keys are silently ignored by the loader (by-name lookup) — verify this in `ModuleManager.loadConfig` during implementation and note if anything breaks.

Verify: CI green + config with old "PositionSpoof" key still loads without crash.

---

### Task 7 — Docs & catalog update

Files:
- MODIFY `CONTEXT.md` (gitignored — edit only, never commit)
- MODIFY `README.md` (committed — module catalog, counts 55 → 53)

Steps:
1. CONTEXT.md: add an ACB section under Module System (package path, `Acb.attack` choke point, always-on cleanup, `AcbConfig` as the tuning point, `activeDesync`/`stealthActive` flags), update Module Catalog (remove PositionSpoof/TimingSpoof rows, note VAura/Killaura/Fly/FreeCam rebuilds with new settings), bump module count 55 → 53, add a Recent Session Notes entry.
2. README.md: update the module reference accordingly (same removals + rebuild notes).
3. Commit `docs: update module catalog for ACB rebuild (55 -> 53 modules)`, push, CI.
4. Design docs index: mark the ACB design doc status Implemented in CONTEXT.md only.

---

## Risks

| Risk | Mitigation |
|------|-----------|
| Lombok getter/setter mistakes (most likely compile failure) | Copy import paths + accessor patterns from existing `KillauraModule.kt`/`VAuraModule.kt`; review every packet touch before push |
| `InventoryTransactionType`/`ActionType` import path drift in vendored 3.0 | Copy from the existing module that already compiles against these classes |
| Rotation/angle semantics (yaw sign, angle metric) | Reuse `RotationUtils` helpers (`toRotation`, `getRotationDifference`) exactly as existing modules do |
| Server rubber-bands during teleport modes | Sub-threshold stepping via `TeleportProtector` (2 blocks/tick cap); FreeCam suppresses corrections via `activeDesync` |
| `playerMap`/`entityMap` concurrent modification while iterating | Use existing iteration patterns from VAuraModule (they already handle this) |
| Old config file with removed module keys | Verify `fromJson` ignores unknown keys (by-name lookup) |
| CI slower than a compile-error loop would like | Push once per task; each task is self-contained and compile-reviewable before push |
| User tests stale APK | Manual test checklist requires fresh CI artifact |

## Verification Plan

1. Per-task: push → GitHub Actions `assembleDebug` green (watch the run for the exact commit).
2. Final: logcat check for `Acb` tag ("bound to session" after connect; attack lines while modules active).
3. Manual device test (fresh CI artifact):
   - Connect to a vanilla server first: connect, move, jump — no kicks, no rubber-bands (always-on cleanup working).
   - VAura: default settings; verify multi-target hits, no camera snap (silent aim), CPS feels like 20.
   - Killaura: angle-priority targeting; TP Aura on an anarchy server — observe no teleport-lock; Strafe visible in local view.
   - Fly: VANILLA (old behavior preserved), JUMP (server sees jump-chain — check no fall damage spam), TELEPORT (smooth climb, no rubber-band).
   - FreeCam: camera flies free, body walks, ghost independent, disable snaps back; no server-side effect (position on another client unchanged).
   - Reboot app, old config loads, `killaura`/`fly`/`vaura` settings retained (keys unchanged).
4. Success = all CI green + the manual checklist passes on the fresh APK.
