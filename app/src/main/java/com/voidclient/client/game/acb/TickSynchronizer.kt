package com.voidclient.client.game.acb

import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

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
