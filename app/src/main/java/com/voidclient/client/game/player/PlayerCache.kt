package com.voidclient.client.game.player

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object PlayerCache {

    /**
     * UUID -> Player Name
     */
    val players: MutableMap<UUID, String> = ConcurrentHashMap()

    fun add(uuid: UUID, name: String) {
        players[uuid] = name
    }

    fun remove(uuid: UUID) {
        players.remove(uuid)
    }

    fun clear() {
        players.clear()
    }

    fun getName(uuid: UUID): String? {
        return players[uuid]
    }
}
