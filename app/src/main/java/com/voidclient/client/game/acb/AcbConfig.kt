package com.voidclient.client.game.acb

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
