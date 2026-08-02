package com.voidclient.client.game.acb

import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

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
