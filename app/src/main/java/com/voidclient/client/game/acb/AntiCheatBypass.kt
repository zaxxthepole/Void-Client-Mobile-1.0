package com.voidclient.client.game.acb

import android.util.Log
import com.voidclient.client.game.GameSession
import com.voidclient.client.game.entity.Entity
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket

object Acb {

    val state = AcbState()
    val aim = RotationManager()
    val tickSync = TickSynchronizer()
    val ground = GroundValidator()
    val variance = VarianceEngine()
    val guard = PacketOrderGuard()
    val teleport = TeleportProtector()

    fun bind(session: GameSession) {
        state.session = session
        state.reset()
        state.session = session
        log("bound to session")
    }

    fun onDisconnect(reason: String) {
        state.reset()
        log("disconnected ($reason)")
    }

    // Called FIRST from GameSession.beforePacketBound, both directions. Return true = drop.
    fun onPacket(packet: BedrockPacket): Boolean {
        when (packet) {
            is PlayerAuthInputPacket -> {
                tickSync.sync(packet)
                teleport.step(packet)
                variance.apply(packet)
            }
            is MovePlayerPacket -> {
                tickSync.observeServerTick(packet.getTick())
                val lp = state.session?.localPlayer
                if (state.activeDesync && lp != null && packet.getRuntimeEntityId() == lp.runtimeEntityId) {
                    return true // suppress server rubber-band while freecam desync is intentional
                }
                ground.fix(packet)
            }
            is StartGamePacket -> state.reset()
        }
        return false
    }

    // The single choke point every combat module uses to attack.
    fun attack(target: Entity, packets: Int = AcbConfig.ATTACK_PACKETS_DEFAULT): Boolean {
        val session = state.session ?: return false
        if (!guard.acquire()) return false
        val player = session.localPlayer
        val look = aim.rotateTowards(target)
        val pos = player.vec3Position
        repeat(packets) {
            val pkt = InventoryTransactionPacket()
            pkt.setTransactionType(InventoryTransactionType.ITEM_USE_ON_ENTITY)
            pkt.setActionType(1)
            pkt.setRuntimeEntityId(target.runtimeEntityId)
            pkt.setHotbarSlot(player.inventory.heldItemSlot)
            pkt.setItemInHand(player.inventory.hand)
            pkt.setPlayerPosition(pos)
            pkt.setClickPosition(pos)
            aim.applyTo(pkt, look)
            session.serverBound(pkt)
        }
        log("attack ${target.runtimeEntityId} x$packets")
        return true
    }

    fun swing() {
        state.session?.localPlayer?.swing()
    }

    fun log(msg: String) {
        Log.i("Acb", msg)
    }
}
