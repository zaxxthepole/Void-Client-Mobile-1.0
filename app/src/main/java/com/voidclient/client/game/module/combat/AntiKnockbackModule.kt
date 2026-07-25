package com.voidclient.client.game.module.combat

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket

class AntiKnockbackModule : Module("anti_knockback", ModuleCategory.Combat) {

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is SetEntityMotionPacket) {


            if (packet.runtimeEntityId == session.localPlayer.runtimeEntityId) {
                interceptablePacket.intercept()
            }
        }
    }
}
