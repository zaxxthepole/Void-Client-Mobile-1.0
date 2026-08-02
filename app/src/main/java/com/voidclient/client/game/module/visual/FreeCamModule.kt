package com.voidclient.client.game.module.visual

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import com.voidclient.client.game.acb.Acb
import org.cloudburstmc.math.vector.Vector2f
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.cos
import kotlin.math.sin

class FreeCamModule : Module("FreeCam", ModuleCategory.Visual) {

    private val speedValue by floatValue("Speed", 1.2f, 0.1f..5f)

    private var offset = Vector3f.from(0f, 0f, 0f)
    private var lastTick = 0L

    override fun onEnabled() {
        super.onEnabled()
        Acb.state.activeDesync = true
        offset = Vector3f.from(0f, 0f, 0f)
        lastTick = 0L
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return
        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val tick = packet.getTick()
        if (lastTick == 0L) {
            lastTick = tick
            return
        }
        val dt = ((tick - lastTick) / 20f).coerceIn(0.01f, 0.1f)
        lastTick = tick

        val yaw = Math.toRadians(packet.getRotation().getX().toDouble()).toFloat()
        val move = packet.getRawMoveVector() ?: packet.getAnalogMoveVector() ?: Vector2f.from(0f, 0f)
        val dirX = move.getX() * -cos(yaw) + move.getY() * -sin(yaw)
        val dirZ = move.getX() * -sin(yaw) + move.getY() * cos(yaw)
        val up = if (packet.getInputData().contains(PlayerAuthInputData.JUMPING)) 1f else 0f
        val down = if (packet.getInputData().contains(PlayerAuthInputData.SNEAKING)) 1f else 0f

        val step = speedValue * dt
        offset = Vector3f.from(
            offset.getX() + dirX * step,
            offset.getY() + (up - down) * step,
            offset.getZ() + dirZ * step
        )

        val player = session.localPlayer
        val bodyPos = packet.getPosition() ?: player.vec3Position
        session.clientBound(
            MovePlayerPacket().apply {
                setRuntimeEntityId(player.runtimeEntityId)
                setPosition(
                    Vector3f.from(
                        bodyPos.getX() + offset.getX(),
                        bodyPos.getY() + offset.getY(),
                        bodyPos.getZ() + offset.getZ()
                    )
                )
                setRotation(packet.getRotation())
                setMode(MovePlayerPacket.Mode.NORMAL)
                setOnGround(false)
                setTick(tick)
            }
        )
    }

    override fun onDisabled() {
        Acb.state.activeDesync = false
        if (isSessionCreated) {
            val player = session.localPlayer
            session.clientBound(
                MovePlayerPacket().apply {
                    setRuntimeEntityId(player.runtimeEntityId)
                    setPosition(player.vec3Position)
                    setRotation(player.vec3Rotation)
                    setMode(MovePlayerPacket.Mode.NORMAL)
                    setOnGround(true)
                    setTick(player.tickExists)
                }
            )
        }
        offset = Vector3f.from(0f, 0f, 0f)
        lastTick = 0L
        super.onDisabled()
    }

    override fun onDisconnect(reason: String) {
        offset = Vector3f.from(0f, 0f, 0f)
        lastTick = 0L
        Acb.state.activeDesync = false
        super.onDisconnect(reason)
    }
}
