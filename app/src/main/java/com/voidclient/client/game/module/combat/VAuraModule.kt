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
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.random.Random

class VAuraModule : Module("VAura", ModuleCategory.Combat) {

    private val playersOnly by boolValue("players_only", true)
    private val mobsOnly by boolValue("mobs_only", false)

    private val rangeValue by floatValue("Range", 8f, 1f..16f)
    private val cpsValue by intValue("CPS", 20, 1..50)
    private val targetsValue by intValue("Max Targets", 4, 1..8)
    private val packetsValue by intValue("Packets", 2, 1..5)
    private val sortValue by enumValue("Target Sort", SortMode.DISTANCE, SortMode::class.java)
    private val modeValue by enumValue("Attack Mode", AttackMode.SINGLE_LOCK, AttackMode::class.java)
    private val silentValue by boolValue("Silent Aim", true)
    private val smoothValue by boolValue("Rotation Smoothing", true)

    enum class SortMode { DISTANCE, HEALTH, ANGLE, RANDOM }

    enum class AttackMode { SINGLE_LOCK, SEQUENTIAL, HIT_ALL }

    private var cycleIndex = 0

    override fun onEnabled() {
        super.onEnabled()
        Acb.state.stealthActive = true
    }

    override fun onDisabled() {
        super.onDisabled()
        Acb.state.stealthActive = false
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        if (interceptablePacket.packet !is PlayerAuthInputPacket) return

        val player = session.localPlayer

        val candidates = session.level.entityMap.values
            .filter { it.distance(player) <= rangeValue }
            .filter { it.isTarget() }
            .filterNot { it is Player && FriendManager.isFriend(it.uuid) }

        val sorted = when (sortValue) {
            SortMode.DISTANCE -> candidates.sortedBy { it.distance(player) }
            SortMode.HEALTH -> candidates.sortedBy { it.attributes["minecraft:health"]?.value ?: 20f }
            SortMode.ANGLE -> {
                val playerRot = Rotation(player.vec3Rotation.getY(), player.vec3Rotation.getX())
                candidates.sortedBy {
                    getRotationDifference(toRotation(player.vec3Position, it.vec3Position), playerRot)
                }
            }
            SortMode.RANDOM -> candidates.shuffled(Random.Default)
        }

        val window = sorted.take(maxOf(targetsValue, 1))
        if (window.isEmpty()) {
            cycleIndex = 0
            return
        }

        if (!Acb.guard.acquire()) return

        var attacked = false
        when (modeValue) {
            AttackMode.SINGLE_LOCK -> attacked = Acb.attack(window[0], packetsValue)
            AttackMode.SEQUENTIAL -> {
                attacked = Acb.attack(window[cycleIndex % window.size], packetsValue)
                cycleIndex = (cycleIndex + 1) % window.size
            }
            AttackMode.HIT_ALL -> for (target in window) {
                if (Acb.attack(target, packetsValue)) attacked = true
            }
        }

        if (attacked) Acb.swing()
    }

    private fun Entity.isTarget(): Boolean = when (this) {
        is LocalPlayer -> false
        is Player -> !mobsOnly && !isBot()
        is EntityUnknown -> {
            if (mobsOnly) identifier in MobList.mobTypes
            else !playersOnly
        }
        else -> false
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val playerList = session.level.playerMap[this.uuid] ?: return false
        return playerList.name.isBlank()
    }
}
