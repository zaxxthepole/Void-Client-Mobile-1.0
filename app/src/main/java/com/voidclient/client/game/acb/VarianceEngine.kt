package com.voidclient.client.game.acb

import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.random.Random

class VarianceEngine {
    fun apply(packet: PlayerAuthInputPacket) {
        val r = packet.getRotation()
        val yawJ = (Random.nextFloat() * 2f - 1f) * AcbConfig.VARIANCE_YAW
        val pitchJ = (Random.nextFloat() * 2f - 1f) * AcbConfig.VARIANCE_PITCH * (if (Acb.state.stealthActive) 1f else 0.25f)
        packet.setRotation(Vector3f.from(r.getX() + yawJ, r.getY() + pitchJ, r.getZ()))
    }
}
