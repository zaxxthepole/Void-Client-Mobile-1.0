package com.voidclient.client.game.module.motion

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.math.cos
import kotlin.math.sin

class JetPackModule : Module("jetpack", ModuleCategory.Motion) {

    private val speed by floatValue("speed", 2.5f, 1.0f..10.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            // Convert angles to radians
            val yaw = Math.toRadians(packet.rotation.y.toDouble())
            val pitch = Math.toRadians(packet.rotation.x.toDouble())

            // Calculate direction vector based on where player is looking
            val motionX = -sin(yaw) * cos(pitch) * speed
            val motionY = -sin(pitch) * speed
            val motionZ = cos(yaw) * cos(pitch) * speed

            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = Vector3f.from(
                    motionX.toFloat(),
                    motionY.toFloat(),
                    motionZ.toFloat()
                )
            }
            session.clientBound(motionPacket)
        }
    }
}