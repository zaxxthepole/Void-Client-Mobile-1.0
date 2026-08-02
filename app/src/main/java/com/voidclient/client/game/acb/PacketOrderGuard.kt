package com.voidclient.client.game.acb

import kotlin.random.Random

class PacketOrderGuard {
    fun acquire(): Boolean {
        val now = System.currentTimeMillis()
        if (now < Acb.state.attackLockUntil) return false
        Acb.state.attackLockUntil = now + AcbConfig.PACKET_GAP_BASE + Random.nextLong(AcbConfig.PACKET_GAP_JITTER)
        return true
    }
}
