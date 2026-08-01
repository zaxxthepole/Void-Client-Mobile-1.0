package com.voidclient.client.game.module.combat

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import com.voidclient.client.game.entity.Player
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import kotlin.math.sqrt

class ReachModule : Module("Reach", ModuleCategory.Combat) {

    private val reach by floatValue("Reach", 4.0f, 1.0f..6.0f)
    private val hitboxSize by floatValue("Hitbox Size", 0.3f, 0.0f..1.0f)
    private val onlyPlayers by boolValue("Only Players", false)
    private val vertical by boolValue("Vertical Reach", true)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return
        val packet = interceptablePacket.packet
        if (packet !is InventoryTransactionPacket) return
        if (packet.transactionType != InventoryTransactionType.ITEM_USE_ON_ENTITY) return

        val target = session.level.entityMap[packet.runtimeEntityId] ?: return
        if (onlyPlayers && target !is Player) return

        val playerPosition = packet.playerPosition ?: return
        val targetPos = target.vec3Position
        val player = session.localPlayer

        val dx = targetPos.x - playerPosition.x
        val dy = if (vertical) targetPos.y - playerPosition.y else 0f
        val dz = targetPos.z - playerPosition.z
        val distance = sqrt(dx * dx + dy * dy + dz * dz)
        val maxReach = reach + hitboxSize
        if (distance <= maxReach) return

        val scale = maxReach / distance
        packet.playerPosition = Vector3f.from(
            playerPosition.x + dx * scale,
            playerPosition.y + dy * scale,
            playerPosition.z + dz * scale
        )
        packet.setHeadPosition(player.vec3Position.add(0f, 1.62f, 0f))
    }
}
