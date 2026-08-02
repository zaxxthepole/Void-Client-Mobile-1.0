package com.voidclient.client.game.acb

import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket

class GroundValidator {
    fun fix(packet: MovePlayerPacket) {
        val deltaY = packet.getPosition().getY() - Acb.state.lastAuthY
        val ground = packet.getOnGround()
        if (ground && deltaY > AcbConfig.GROUND_JUMP_THRESHOLD) packet.setOnGround(false)
        else if (!ground && kotlin.math.abs(deltaY) < AcbConfig.GROUND_EPSILON) packet.setOnGround(true)
        Acb.state.lastAuthY = packet.getPosition().getY()
    }
}
