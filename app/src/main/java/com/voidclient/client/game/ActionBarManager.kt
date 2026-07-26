package com.voidclient.client.game

import net.kyori.adventure.text.Component
import org.cloudburstmc.protocol.bedrock.packet.SetTitlePacket

object ActionBarManager {
    private val activeModules = mutableMapOf<String, String>()

    fun updateModule(moduleName: String, text: String) {
        if (text.isEmpty()) {
            activeModules.remove(moduleName)
        } else {
            activeModules[moduleName] = text
        }
    }

    fun removeModule(moduleName: String) {
        activeModules.remove(moduleName)
    }

    fun display(session: GameSession) {
        if (activeModules.isEmpty()) return

        val combinedText = activeModules.values.joinToString(" §7|§r ")

        val packet = SetTitlePacket()
        packet.type = SetTitlePacket.Type.ACTIONBAR
        packet.setText(combinedText)
        packet.fadeInTime = 0
        packet.fadeOutTime = 0
        packet.stayTime = 2
        packet.xuid = ""
        packet.platformOnlineId = ""
        session.clientBound(packet)
    }
}