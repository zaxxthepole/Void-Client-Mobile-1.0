package com.voidclient.client.game.module.combat

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.random.Random

class AntiAimModule : Module("AntiAim", ModuleCategory.Combat) {

    private val yawRange by floatValue("Yaw Range", 30f, 0f..180f)
    private val pitchRange by floatValue("Pitch Range", 20f, 0f..90f)
    private val chance by floatValue("Chance", 0.5f, 0.05f..1.0f)
    private val onlyWhileMoving by boolValue("Only While Moving", false)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return
        if (Random.nextFloat() > chance) return

        val packet = interceptablePacket.packet

        when (packet) {
            is PlayerAuthInputPacket -> {
                val inputData = packet.inputData
                if (onlyWhileMoving && !inputData.any { it in MOVE_FLAGS }) return
                packet.rotation = randomize(packet.rotation)
            }

            is MovePlayerPacket -> {
                packet.rotation = randomize(packet.rotation)
            }
        }
    }

    private fun randomize(rotation: Vector3f): Vector3f {
        val yawOffset = (Random.nextFloat() - 0.5f) * 2f * yawRange
        val pitchOffset = (Random.nextFloat() - 0.5f) * 2f * pitchRange
        return Vector3f.from(rotation.x + pitchOffset, rotation.y + yawOffset, rotation.z)
    }

    companion object {
        private val MOVE_FLAGS = setOf(
            org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData.UP,
            org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData.DOWN,
            org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData.LEFT,
            org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData.RIGHT,
            org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData.UP_LEFT,
            org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData.UP_RIGHT,
            org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData.DOWN_LEFT,
            org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData.DOWN_RIGHT,
            org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData.JUMPING,
            org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData.SPRINTING,
            org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData.SPRINT_DOWN
        )
    }
}
