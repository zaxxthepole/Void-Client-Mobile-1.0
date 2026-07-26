package com.voidclient.client.game.module.misc

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.math.vector.Vector3i
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.*

class AutoFishModule : Module("AutoFish", ModuleCategory.Misc) {

    private val castDelay by floatValue("Cast Delay", 0.5f, 0.1f..3.0f)
    private val reelDelay by floatValue("Reel Delay", 0.3f, 0.0f..2.0f)
    private val autoRecast by boolValue("Auto Recast", true)
    private val range by floatValue("Range", 3.0f, 1.0f..8.0f)
    private val randomize by boolValue("Randomize Timing", true)

    private enum class State { IDLE, CASTING, FISHING, BITING, REELING }

    private var state = State.IDLE
    private var stateTimer = 0L
    private var bobberEntityId = -1L
    private var castCount = 0

    override fun onEnabled() {
        super.onEnabled()
        state = State.IDLE
        stateTimer = 0L
        bobberEntityId = -1L
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet

        when (packet) {
            is AddEntityPacket -> onAddEntity(packet)
            is EntityEventPacket -> onEntityEvent(packet)
            is RemoveEntityPacket -> onRemoveEntity(packet)
            is InventoryTransactionPacket -> {
                if (packet.transactionType == InventoryTransactionType.ITEM_USE) {
                    state = State.FISHING
                }
            }
        }
    }

    private fun onAddEntity(packet: AddEntityPacket) {
        if (packet.identifier != "minecraft:fishing_hook") return
        bobberEntityId = packet.runtimeEntityId
        state = State.FISHING
    }

    private fun onEntityEvent(packet: EntityEventPacket) {
        if (!isSessionCreated) return

        when (packet.type) {
            EntityEventType.FISH_HOOK_BUBBLE,
            EntityEventType.FISH_HOOK_POSITION,
            EntityEventType.FISH_HOOK_TIME,
            EntityEventType.FISH_HOOK_TEASE -> {
                val playerId = session.localPlayer.runtimeEntityId
                if (packet.runtimeEntityId == playerId) {
                    state = State.BITING
                    scheduleReel()
                }
            }
            else -> {}
        }
    }

    private fun onRemoveEntity(packet: RemoveEntityPacket) {
        if (packet.uniqueEntityId == bobberEntityId) {
            bobberEntityId = -1L
            if (state == State.FISHING || state == State.BITING) {
                state = State.IDLE
                scheduleCast()
            }
        }
    }

    private fun scheduleReel() {
        val delay = if (randomize) reelDelay + (Math.random().toFloat() * 0.2f) else reelDelay
        val delayTicks = (delay * 20).toLong()
        stateTimer = System.currentTimeMillis() + (delayTicks * 50)
        state = State.REELING
        reel()
    }

    private fun scheduleCast() {
        if (!autoRecast) return
        val delay = if (randomize) castDelay + (Math.random().toFloat() * 0.3f) else castDelay
        val delayTicks = (delay * 20).toLong()
        stateTimer = System.currentTimeMillis() + (delayTicks * 50)
        state = State.CASTING
    }

    override fun afterPacketBound(packet: BedrockPacket) {
        if (!isEnabled || !isSessionCreated) return
        val now = System.currentTimeMillis()

        if (state == State.CASTING && now >= stateTimer) {
            tryCast()
        }

        if (state == State.IDLE && hasRod() && autoRecast) {
            scheduleCast()
        }
    }

    private fun tryCast() {
        if (!hasRod()) return

        val player = session.localPlayer
        val pkt = InventoryTransactionPacket().apply {
            transactionType = InventoryTransactionType.ITEM_USE
            actionType = 2
            blockPosition = Vector3i.ZERO
            blockFace = 255
            hotbarSlot = player.inventory.heldItemSlot
            itemInHand = player.inventory.hand
            playerPosition = player.vec3Position
            clickPosition = Vector3f.ZERO
        }
        session.serverBound(pkt)
        state = State.FISHING
        castCount++
    }

    private fun reel() {
        if (!hasRod()) return

        val player = session.localPlayer
        val pkt = InventoryTransactionPacket().apply {
            transactionType = InventoryTransactionType.ITEM_USE
            actionType = 2
            blockPosition = Vector3i.ZERO
            blockFace = 255
            hotbarSlot = player.inventory.heldItemSlot
            itemInHand = player.inventory.hand
            playerPosition = player.vec3Position
            clickPosition = Vector3f.ZERO
        }
        session.serverBound(pkt)
        bobberEntityId = -1L
        state = State.IDLE
    }

    private fun hasRod(): Boolean {
        if (!isSessionCreated) return false
        val hand = session.localPlayer.inventory.hand
        return hand != ItemData.AIR && hand.definition?.identifier == "minecraft:fishing_rod"
    }
}
