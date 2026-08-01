package com.voidclient.client.game.module.visual

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class FreeCamModule : Module("FreeCam", ModuleCategory.Visual) {

    private val floatSpeed by floatValue("Float Speed", 1.0f, 0.0f..5.0f)
    private val heightLimit by floatValue("Height Limit", 64f, 4f..256f)
    private val resetOnDisable by boolValue("Reset On Disable", true)

    private var ghostY = 0.0f
    private var lastSample = 0L
    private var offsetApplied = false

    override fun onEnabled() {
        super.onEnabled()
        ghostY = 0.0f
        lastSample = System.currentTimeMillis()
        offsetApplied = false
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return
        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val now = System.currentTimeMillis()
        val dt = (now - lastSample).coerceAtLeast(0L) / 1000.0f
        lastSample = now

        val nextY = ghostY + floatSpeed * dt
        ghostY = if (nextY > heightLimit) heightLimit else nextY
        offsetApplied = true

        val player = session.localPlayer
        val pos = packet.position ?: player.vec3Position

        session.clientBound(
            MovePlayerPacket().apply {
                runtimeEntityId = player.runtimeEntityId
                position = Vector3f.from(pos.x, pos.y + ghostY, pos.z)
                rotation = packet.rotation
                mode = MovePlayerPacket.Mode.NORMAL
                setOnGround(false)
                tick = player.tickExists
            }
        )
    }

    override fun onDisabled() {
        if (isSessionCreated && resetOnDisable && offsetApplied) {
            val player = session.localPlayer
            session.clientBound(
                MovePlayerPacket().apply {
                    runtimeEntityId = player.runtimeEntityId
                    position = player.vec3Position
                    rotation = player.vec3Rotation
                    mode = MovePlayerPacket.Mode.NORMAL
                    setOnGround(true)
                    tick = player.tickExists
                }
            )
        }
        ghostY = 0.0f
        offsetApplied = false
        super.onDisabled()
    }

    override fun onDisconnect(reason: String) {
        ghostY = 0.0f
        offsetApplied = false
    }
}
