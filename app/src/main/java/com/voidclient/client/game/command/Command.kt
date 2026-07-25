package com.voidclient.client.game.command

interface Command {

    val name: String
    val description: String
    val usage: String

    fun execute(args: List<String>)
}
