package com.voidclient.client.game.acb

import com.voidclient.client.game.entity.Entity
import com.voidclient.client.game.utils.math.Rotation
import com.voidclient.client.game.utils.math.getAngleDifference
import com.voidclient.client.game.utils.math.toRotation
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket

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
        packet.setHeadPosition(Vector3f.from(look.yaw, look.pitch, 0f))
    }
}
