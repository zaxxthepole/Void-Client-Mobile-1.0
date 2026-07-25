package com.voidclient.client.game.command.impl

import com.voidclient.client.game.command.Command
import com.voidclient.client.game.command.manager.CommandManager

object HelpCommand : Command {

    override val name = "help"
    override val description = "Show all commands"
    override val usage = ".help"

    override fun execute(args: List<String>) {
        send("Commands:")
        CommandManager.commands.forEach {
            send(".${it.name} - ${it.description}")
        }
    }

    private fun send(msg: String) {
        println("[Help] $msg")
    }
}
