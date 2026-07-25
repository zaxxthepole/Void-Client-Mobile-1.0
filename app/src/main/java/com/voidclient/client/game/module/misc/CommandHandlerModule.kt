package com.voidclient.client.game.module.misc

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import com.voidclient.client.game.ModuleManager
import com.voidclient.client.game.friend.FriendManager
import com.voidclient.client.game.entity.Player
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

class CommandHandlerModule : Module("command_handler", ModuleCategory.Misc, true) {

    private val prefix = "."

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet !is TextPacket) return

        val message = packet.message.toString()
        if (!message.startsWith(prefix)) return

        interceptablePacket.intercept()

        val args = message.substring(prefix.length).split(" ")
        val command = args[0].lowercase()

        when (command) {

            "help" -> displayHelp(args.getOrNull(1))

            "friend" -> handleFriendCommand(args)

            "replay" -> handleReplayCommand(args)

            else -> toggleModule(command)
        }
    }


    private fun handleFriendCommand(args: List<String>) {

        if (args.size < 2) {
            usage()
            return
        }

        when (args[1].lowercase()) {

            "add" -> {
                val name = args.getOrNull(2) ?: return usage(".friend add <name>")
                val entry = session.level.playerMap.entries.find {
                    it.value.name.toString().equals(name, ignoreCase = true)

                } ?: return notFound(name)

                FriendManager.addFriend(entry.key)
                session.displayClientMessage("§aAdded friend: §f$name")
            }

            "remove" -> {
                val name = args.getOrNull(2) ?: return usage(".friend remove <name>")
                val entry = session.level.playerMap.entries.find {
                    it.value.name.toString().equals(name, ignoreCase = true)

                } ?: return notFound(name)

                FriendManager.removeFriend(entry.key)
                session.displayClientMessage("§eRemoved friend: §f$name")
            }

            "list" -> {
                val friends = FriendManager.getFriends()
                if (friends.isEmpty()) {
                    session.displayClientMessage("§7Friend list is empty")
                    return
                }

                session.displayClientMessage("§l§bFriends:")
                friends.forEach { uuid ->
                    val name = session.level.playerMap[uuid]?.name ?: uuid.toString()
                    session.displayClientMessage("§7- §f$name")
                }
            }

            "clear" -> {
                FriendManager.clear()
                session.displayClientMessage("§cFriend list cleared")
            }

            else -> usage()
        }
    }


    private fun displayHelp(category: String?) {
        session.displayClientMessage(
            """
            §l§c[WClient] §r§7Commands:
            §f.help <category> §7- Show modules
            §f.friend §7- Manage friends
            §f.<module> §7- Toggle module
            """.trimIndent()
        )

        if (category == null) {
            ModuleCategory.entries.forEach { displayCategory(it) }
            return
        }

        try {
            displayCategory(ModuleCategory.valueOf(category.uppercase()))
        } catch (_: Exception) {
            session.displayClientMessage("§cInvalid category")
        }
    }

    private fun displayCategory(category: ModuleCategory) {
        val modules = ModuleManager.modules.filter {
            it.category == category && !it.private
        }

        if (modules.isEmpty()) return

        session.displayClientMessage("§l§b${category.name}:")
        modules.chunked(3).forEach { row ->
            session.displayClientMessage(
                row.joinToString("   ") {
                    val s = if (it.isEnabled) "§a✔" else "§c✘"
                    "$s §f${it.name}"
                }
            )
        }
    }

    private fun handleReplayCommand(args: List<String>) {
        val replay = ModuleManager.modules.find { it is ReplayModule } as? ReplayModule
            ?: return session.displayClientMessage("§cReplay module not found")

        when (args.getOrNull(1)?.lowercase()) {
            "record" -> replay.startRecording()
            "play" -> replay.startPlayback()
            "stop" -> {
                replay.stopRecording()
                replay.stopPlayback()
            }
            "save" -> replay.saveReplay(args.getOrNull(2) ?: return)
            "load" -> replay.loadReplay(args.getOrNull(2) ?: return)
            else -> session.displayClientMessage("§7.replay record | play | stop | save | load")
        }
    }

    private fun toggleModule(name: String) {
        val module = ModuleManager.modules.find {
            it.name.equals(name, true)
        }

        if (module != null && !module.private) {
            module.isEnabled = !module.isEnabled
        } else {
            session.displayClientMessage("§cModule not found: §f$name")
        }
    }

    private fun usage(msg: String = ".friend add <name> | remove <name> | list | clear") {
        session.displayClientMessage("§cUsage: $msg")
    }

    private fun notFound(name: String) {
        session.displayClientMessage("§cPlayer not found: §f$name")
    }
}
