package com.voidclient.client.game.module.combat

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket

class CriticalsModule : Module("Criticals", ModuleCategory.Combat) {

    private val critHeight by floatValue("Crit Height", 0.11f, 0.02f..0.4f)
    private val interval by floatValue("Interval (ms)", 250f, 50f..1000f)
    private val packetCrit by boolValue("Packet Crit", true)

    private var lastCritTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return
        val packet = interceptablePacket.packet
        if (packet !is InventoryTransactionPacket) return
        if (packet.transactionType != InventoryTransactionType.ITEM_USE_ON_ENTITY) return

        val now = System.currentTimeMillis()
        if (now - lastCritTime < interval) return
        lastCritTime = now

        val player = session.localPlayer
        val pos = player.vec3Position

        if (packetCrit) {
            packet.playerPosition = Vector3f.from(pos.x, pos.y + critHeight, pos.z)
            packet.setHeadPosition(pos.add(0f, 1.62f + critHeight, 0f))
        }

        val movePacket = MovePlayerPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            position = Vector3f.from(pos.x, pos.y + critHeight, pos.z)
            rotation = player.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL
            setOnGround(false)
            tick = player.tickExists
        }
        session.serverBound(movePacket)
        session.clientBound(movePacket)
    }
}
