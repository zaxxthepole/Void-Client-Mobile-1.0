package com.voidclient.client.game.acb

import com.voidclient.client.game.entity.Entity
import com.voidclient.client.game.utils.math.Rotation
import com.voidclient.client.game.utils.math.getAngleDifference
import com.voidclient.client.game.utils.math.toRotation
import org.cloudburstmc.math.vector.Vector2f
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket

class RotationManager {
    private val HEAD_HEIGHT = 1.62f
    private var smoothed = Rotation(0f, 0f)
    var pendingInteractRotation: Vector2f? = null

    fun rotateTowards(target: Entity): Rotation {
        val player = Acb.state.session?.localPlayer ?: return smoothed
        val desired = toRotation(player.vec3Position, target.vec3Position)
        val rate = AcbConfig.AIM_SMOOTH_RATE
        smoothed = Rotation(
            smoothed.yaw + getAngleDifference(desired.yaw, smoothed.yaw) * rate,
            smoothed.pitch + (desired.pitch - smoothed.pitch) * rate
        )
        pendingInteractRotation = Vector2f.from(smoothed.yaw, smoothed.pitch)
        return smoothed
    }

    fun applyTo(packet: InventoryTransactionPacket, target: Entity, look: Rotation) {
        val player = Acb.state.session?.localPlayer ?: return
        val pos = player.vec3Position
        packet.setHeadPosition(Vector3f.from(pos.getX(), pos.getY() + HEAD_HEIGHT, pos.getZ()))
        packet.setClickPosition(target.vec3Position)
    }

    fun reset() {
        smoothed = Rotation(0f, 0f)
        pendingInteractRotation = null
    }
}
