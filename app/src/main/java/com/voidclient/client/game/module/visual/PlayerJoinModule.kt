package com.voidclient.client.game.module.visual

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket

class PlayerJoinModule : Module("PlayerJoin", ModuleCategory.Visual) {

    private val trackedPlayers = mutableMapOf<String, String>()

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerListPacket) {
            when (packet.action) {
                PlayerListPacket.Action.ADD -> {
                    for (entry in packet.entries) {
                        val uuid = entry.uuid.toString()
                        val name = entry.name?.toString() ?: "Unknown"
                        if (!trackedPlayers.containsKey(uuid)) {
                            trackedPlayers[uuid] = name
                            if (uuid != session.localPlayer.uuid.toString()) {
                                session.displayClientMessage("Â§a[+] Â§f$name Â§ajoined")
                            }
                        }
                    }
                }
                PlayerListPacket.Action.REMOVE -> {
                    for (entry in packet.entries) {
                        val uuid = entry.uuid.toString()
                        val name = trackedPlayers.remove(uuid)
                        if (name != null) {
                            session.displayClientMessage("Â§c[-] Â§f$name Â§cleft")
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    override fun onDisabled() {
        trackedPlayers.clear()
    }
}