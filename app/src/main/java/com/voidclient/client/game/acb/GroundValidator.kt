package com.voidclient.client.game.acb

import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class GroundValidator {

    // Local player's own stream: track grounded-ness from vertical position deltas.
    fun fixAuth(packet: PlayerAuthInputPacket) {
        val deltaY = packet.getPosition().getY() - Acb.state.lastAuthY
        Acb.state.lastAuthGrounded = kotlin.math.abs(deltaY) < AcbConfig.GROUND_EPSILON
        Acb.state.lastAuthY = packet.getPosition().getY()
    }

    // Exposed for modules (e.g. Fly JUMP mode): was the player grounded on the last auth input?
    fun isGrounded(): Boolean = Acb.state.lastAuthGrounded

    // Local-player MovePlayerPackets only: plausibility fix on the onGround flag.
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
