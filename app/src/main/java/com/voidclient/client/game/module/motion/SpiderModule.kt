package com.voidclient.client.game.module.motion

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket

class SpiderModule : Module("spider", ModuleCategory.Motion) {

    private val climbSpeed by floatValue("Climb Speed", 0.5f, 0.1f..2.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            if (packet.inputData.contains(PlayerAuthInputData.HORIZONTAL_COLLISION)) {
                val motionPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    motion = Vector3f.from(
                        session.localPlayer.motionX,
                        climbSpeed,
                        session.localPlayer.motionZ
                    )
                }
                session.clientBound(motionPacket)
            }
        }
    }
}