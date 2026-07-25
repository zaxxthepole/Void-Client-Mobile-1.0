package com.voidclient.client.game.module.combat


import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class AntiCrystalModule : Module("anti_crystal", ModuleCategory.Combat) {

    private var ylevel by floatValue("ylevel", 0.4f, 0.1f..1.61f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            packet.position.add(0.0, -ylevel.toDouble(), 0.0)
        }
    }

}