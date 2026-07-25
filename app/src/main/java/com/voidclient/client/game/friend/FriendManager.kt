package com.voidclient.client.game.friend

import java.util.*

object FriendManager {

    private val friends = mutableSetOf<UUID>()

    fun addFriend(uuid: UUID) {
        friends.add(uuid)
    }

    fun removeFriend(uuid: UUID) {
        friends.remove(uuid)
    }

    fun isFriend(uuid: UUID): Boolean {
        return friends.contains(uuid)
    }

    fun getFriends(): Set<UUID> {
        return friends
    }

    fun clear() {
        friends.clear()
    }
}
