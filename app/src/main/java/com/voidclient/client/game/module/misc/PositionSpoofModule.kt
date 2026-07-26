package com.voidclient.client.game.module.misc

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.random.Random

class PositionSpoofModule : Module("PositionSpoof", ModuleCategory.Misc) {

    private val jitterAmount by floatValue("Jitter", 0.003f, 0.001f..0.01f)
    private val jitterVertical by floatValue("Vertical Jitter", 0.001f, 0.0001f..0.005f)
    private val groundSpoof by boolValue("Ground Spoof", true)
    private val spoofChance by floatValue("Spoof Chance", 0.7f, 0.1f..1.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        when (packet) {
            is MovePlayerPacket -> spoofMovePlayer(packet)
            is PlayerAuthInputPacket -> spoofAuthInput(packet)
        }
    }

    private fun spoofMovePlayer(packet: MovePlayerPacket) {
        if (Random.nextFloat() > spoofChance) return

        val pos = packet.position
        if (pos != null) {
            val jitterX = (Random.nextFloat() - 0.5f) * 2f * jitterAmount
            val jitterY = (Random.nextFloat() - 0.5f) * 2f * jitterVertical
            val jitterZ = (Random.nextFloat() - 0.5f) * 2f * jitterAmount
            packet.position = org.cloudburstmc.math.vector.Vector3f.from(
                pos.x + jitterX,
                pos.y + jitterY,
                pos.z + jitterZ
            )
        }

        if (groundSpoof) {
            packet.setOnGround(Random.nextBoolean())
        }
    }

    private fun spoofAuthInput(packet: PlayerAuthInputPacket) {
        if (Random.nextFloat() > spoofChance) return

        val pos = packet.position
        if (pos != null) {
            val jitterX = (Random.nextFloat() - 0.5f) * 2f * jitterAmount
            val jitterY = (Random.nextFloat() - 0.5f) * 2f * jitterVertical
            val jitterZ = (Random.nextFloat() - 0.5f) * 2f * jitterAmount
            packet.position = org.cloudburstmc.math.vector.Vector3f.from(
                pos.x + jitterX,
                pos.y + jitterY,
                pos.z + jitterZ
            )
        }
    }
}
