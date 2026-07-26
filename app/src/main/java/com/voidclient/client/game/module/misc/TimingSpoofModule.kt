package com.voidclient.client.game.module.misc

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector2f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.random.Random

class TimingSpoofModule : Module("TimingSpoof", ModuleCategory.Misc) {

    private val tickJitter by intValue("Tick Jitter", 1, 0..3)
    private val motionNoise by floatValue("Motion Noise", 0.005f, 0.0f..0.05f)
    private val rotationJitter by floatValue("Rotation Jitter", 0.3f, 0.0f..2.0f)

    private var lastTick = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        when (packet) {
            is MovePlayerPacket -> spoofMovePacket(packet)
            is PlayerAuthInputPacket -> spoofAuthInput(packet)
        }
    }

    private fun spoofMovePacket(packet: MovePlayerPacket) {
        if (tickJitter > 0) {
            val jitter = Random.nextInt(-tickJitter, tickJitter + 1)
            packet.tick = packet.tick + jitter
        }

        val rot = packet.rotation
        if (rot != null && rotationJitter > 0f) {
            val yawNoise = (Random.nextFloat() - 0.5f) * 2f * rotationJitter
            val pitchNoise = (Random.nextFloat() - 0.5f) * 2f * rotationJitter
            packet.rotation = org.cloudburstmc.math.vector.Vector3f.from(
                rot.x + yawNoise,
                rot.y + pitchNoise,
                rot.z
            )
        }
    }

    private fun spoofAuthInput(packet: PlayerAuthInputPacket) {
        if (motionNoise > 0f) {
            val motion = packet.motion
            if (motion != null) {
                val noiseX = (Random.nextFloat() - 0.5f) * 2f * motionNoise
                val noiseZ = (Random.nextFloat() - 0.5f) * 2f * motionNoise
                packet.motion = Vector2f.from(
                    motion.x + noiseX,
                    motion.y + noiseZ
                )
            }
        }

        if (rotationJitter > 0f) {
            val rot = packet.rotation
            if (rot != null) {
                val yawNoise = (Random.nextFloat() - 0.5f) * 2f * rotationJitter
                val pitchNoise = (Random.nextFloat() - 0.5f) * 2f * rotationJitter
                packet.rotation = org.cloudburstmc.math.vector.Vector3f.from(
                    rot.x + yawNoise,
                    rot.y + pitchNoise,
                    rot.z
                )
            }
        }
    }
}
