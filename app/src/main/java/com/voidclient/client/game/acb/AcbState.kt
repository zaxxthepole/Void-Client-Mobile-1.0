package com.voidclient.client.game.acb

import com.voidclient.client.game.GameSession
import org.cloudburstmc.math.vector.Vector3f

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
