package com.voidclient.client.game.module.combat

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import com.voidclient.client.game.entity.Entity
import com.voidclient.client.game.entity.EntityUnknown
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class CrystalSmashModule : Module("crystal_smash", ModuleCategory.Combat) {

    private var rangeValue by floatValue("range", 4.0f, 2f..7f)
    private var attackInterval by intValue("delay", 5, 1..20)
    private var cpsValue by intValue("cps", 10, 1..20)
    private var packets by intValue("packets", 1, 1..10)

    private var lastAttackTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()
            val minAttackDelay = 1000L / cpsValue

            if (packet.tick % attackInterval == 0L && (currentTime - lastAttackTime) >= minAttackDelay) {
                val crystals = searchForCrystals()

                if (crystals.isNotEmpty()) {
                    crystals.forEach { crystal ->
                        repeat(packets) {
                            session.localPlayer.attack(crystal)
                        }
                    }
                    lastAttackTime = currentTime
                }
            }
        }
    }

    private fun searchForCrystals(): List<Entity> {
        return session.level.entityMap.values
            .filter { entity ->
                entity.distance(session.localPlayer) < rangeValue &&
                        entity is EntityUnknown &&
                        entity.identifier == "minecraft:end_crystal"
            }
    }
}