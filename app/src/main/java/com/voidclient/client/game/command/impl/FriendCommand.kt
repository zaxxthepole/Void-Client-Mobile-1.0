package com.voidclient.client.game.command.impl

import com.voidclient.client.game.command.Command
import com.voidclient.client.game.friend.FriendManager
import com.voidclient.client.game.player.PlayerCache

object FriendCommand : Command {

    override val name = "friend"
    override val description = "Manage friends"
    override val usage = ".friend add <name> | remove <name> | list | clear"

    override fun execute(args: List<String>) {

        if (args.isEmpty()) {
            send("Usage: $usage")
            return
        }

        when (args[0].lowercase()) {

            "add" -> {
                if (args.size < 2) {
                    send("Usage: .friend add <name>")
                    return
                }

                val name = args[1]
                val entry = PlayerCache.players.entries.find {
                    it.value.equals(name, ignoreCase = true)
                }

                if (entry == null) {
                    send("Player not found: $name")
                    return
                }

                FriendManager.addFriend(entry.key)
                send("Added $name to friends")
            }

            "remove" -> {
                if (args.size < 2) {
                    send("Usage: .friend remove <name>")
                    return
                }

                val name = args[1]
                val entry = PlayerCache.players.entries.find {
                    it.value.equals(name, ignoreCase = true)
                }

                if (entry == null) {
                    send("Player not found: $name")
                    return
                }

                FriendManager.removeFriend(entry.key)
                send("Removed $name from friends")
            }

            "list" -> {
                if (FriendManager.getFriends().isEmpty()) {
                    send("Friend list is empty")
                    return
                }

                send("Friends:")
                FriendManager.getFriends().forEach { uuid ->
                    val name = PlayerCache.players[uuid] ?: uuid.toString()
                    send("- $name")
                }
            }

            "clear" -> {
                FriendManager.clear()
                send("Friend list cleared")
            }

            else -> send("Usage: $usage")
        }
    }

    private fun send(msg: String) {
        println("[Friend] $msg") // replace with client chat method
    }
}
