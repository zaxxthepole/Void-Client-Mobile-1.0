package com.voidclient.client.game.module.combat

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import com.voidclient.client.game.entity.*
import com.voidclient.client.game.friend.FriendManager
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class WAuraModule : Module("WAura", ModuleCategory.Combat) {

    private var playersOnly by boolValue("players_only", true)
    private var mobsOnly by boolValue("mobs_only", false)

    private var rangeValue by floatValue("range", 50f, 2f..50f)
    private var cpsValue by intValue("cps", 25, 1..50)
    private var boost by intValue("packets", 2, 1..10)

    private var targetMode by intValue("Target Mode", 2, 0..2)
    private var switchDelay by intValue("Switch Delay", 100, 20..100)

    private var lastAttackNanoTime = 0L
    private var lastSwitchTime = 0L
    private var switchIndex = 0
    private var currentTarget: Entity? = null

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        if (interceptablePacket.packet !is PlayerAuthInputPacket) return

        val now = System.nanoTime()
        val nowMillis = System.currentTimeMillis()
        val attackDelay = 1_000_000_000L / cpsValue

        if ((now - lastAttackNanoTime) < attackDelay) return

        val targets = searchForTargets()
        if (targets.isEmpty()) {
            currentTarget = null
            return
        }

        val player = session.localPlayer

        when (targetMode) {
            0 -> {
                val target = currentTarget ?: targets.first()
                if (target.distance(player) <= rangeValue) {
                    repeat(boost) { player.attack(target) }
                    currentTarget = target
                    lastAttackNanoTime = now
                } else {
                    currentTarget = null
                }
            }

            1 -> {
                if ((nowMillis - lastSwitchTime) >= switchDelay) {
                    switchIndex = (switchIndex + 1) % targets.size
                    currentTarget = targets[switchIndex]
                    lastSwitchTime = nowMillis
                }
                currentTarget?.let { target ->
                    if (target.distance(player) <= rangeValue) {
                        repeat(boost) { player.attack(target) }
                        lastAttackNanoTime = now
                    }
                }
            }

            2 -> {
                for (entity in targets) {
                    if (entity.distance(player) <= rangeValue) {
                        repeat(boost) { player.attack(entity) }
                    }
                }
                lastAttackNanoTime = now
            }
        }
    }

    private fun searchForTargets(): List<Entity> {
        val player = session.localPlayer
        return session.level.entityMap.values
            .filter { it.distance(player) <= rangeValue }
            .filter { it.isTarget() }
            .filterNot { it is Player && FriendManager.isFriend(it.uuid) }
            .sortedBy { it.distance(player) }
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
