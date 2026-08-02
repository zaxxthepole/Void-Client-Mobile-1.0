package com.voidclient.client.game.acb

import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class GroundValidator {

    fun fixAuth(packet: PlayerAuthInputPacket) {
        val deltaY = packet.getPosition().getY() - Acb.state.lastAuthY
        Acb.state.lastAuthGrounded = kotlin.math.abs(deltaY) < AcbConfig.GROUND_EPSILON
        Acb.state.lastAuthY = packet.getPosition().getY()
    }

    fun isGrounded(): Boolean = Acb.state.lastAuthGrounded

    fun fixMove(packet: MovePlayerPacket) {
        val lp = Acb.state.session?.localPlayer ?: return
        if (packet.getRuntimeEntityId() != lp.runtimeEntityId) return
        val deltaY = packet.getPosition().getY() - Acb.state.lastAuthY
        val ground = packet.getOnGround()
        if (ground && deltaY > AcbConfig.GROUND_JUMP_THRESHOLD) packet.setOnGround(false)
        else if (!ground && kotlin.math.abs(deltaY) < AcbConfig.GROUND_EPSILON) packet.setOnGround(true)
        Acb.state.lastAuthY = packet.getPosition().getY()
    }
}
