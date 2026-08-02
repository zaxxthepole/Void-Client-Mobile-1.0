package com.voidclient.client.game.module.combat

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import com.voidclient.client.game.acb.Acb
import com.voidclient.client.game.entity.*
import com.voidclient.client.game.friend.FriendManager
import com.voidclient.client.game.utils.math.Rotation
import com.voidclient.client.game.utils.math.getRotationDifference
import com.voidclient.client.game.utils.math.toRotation
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.math.cos
import kotlin.math.sin

class KillauraModule : Module("killaura", ModuleCategory.Combat) {

    private val rangeValue by floatValue("Range", 6f, 1f..12f)
    private val cpsValue by intValue("CPS", 12, 1..30)
    private val angleValue by floatValue("Angle", 45f, 5f..180f)
    private val smoothValue by boolValue("Smooth Rotations", true)
    private val tpAuraValue by boolValue("TP Aura", false)
    private val tpDistanceValue by floatValue("TP Distance", 1.5f, 0.5f..5f)
    private val strafeValue by boolValue("Strafe", true)

    override fun onEnabled() {
        super.onEnabled()
        Acb.state.stealthActive = true
    }

    override fun onDisabled() {
        super.onDisabled()
        Acb.state.stealthActive = false
        Acb.teleport.cancel()
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        if (interceptablePacket.packet !is PlayerAuthInputPacket) return

        val player = session.localPlayer
        val playerRot = Rotation(player.vec3Rotation.getY(), player.vec3Rotation.getX())

        val target = session.level.entityMap.values
            .filterIsInstance<Player>()
            .filter { it !is LocalPlayer }
            .filter { it.distance(player) <= rangeValue }
            .filterNot { FriendManager.isFriend(it.uuid) }
            .filterNot { it.isBot() }
            .map {
                it to getRotationDifference(
                    toRotation(player.vec3Position, it.vec3Position),
                    playerRot
                )
            }
            .filter { it.second <= angleValue }
            .minByOrNull { it.second }
            ?.first
            ?: return

        if (!Acb.guard.acquire()) return

        val playerPos = player.vec3Position

        if (tpAuraValue && target.distance(playerPos) > tpDistanceValue) {
            val targetPos = target.vec3Position
            val dir = targetPos.sub(playerPos).normalize()
            val dest = targetPos.sub(dir.mul(tpDistanceValue))

            Acb.teleport.teleportTo(dest)

            session.clientBound(
                MovePlayerPacket().apply {
                    setRuntimeEntityId(player.runtimeEntityId)
                    setPosition(dest)
                    setRotation(player.vec3Rotation)
                    setMode(MovePlayerPacket.Mode.NORMAL)
                    setOnGround(true)
                    setTick(Acb.state.lastServerTick)
                }
            )
        }

        if (Acb.attack(target, packets = 1)) {
            Acb.swing()

            if (strafeValue) {
                val yaw = Math.toRadians(player.rotationYaw.toDouble())
                session.clientBound(
                    SetEntityMotionPacket().apply {
                        setRuntimeEntityId(player.runtimeEntityId)
                        setMotion(
                            Vector3f.from(
                                (-cos(yaw) * 0.15).toFloat(),
                                0f,
                                (-sin(yaw) * 0.15).toFloat()
                            )
                        )
                    }
                )
            }
        }
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val playerList = session.level.playerMap[this.uuid] ?: return false
        return playerList.name.isBlank()
    }
}
