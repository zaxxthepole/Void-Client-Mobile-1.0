package com.voidclient.client.game.module.misc

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory

class ToggleSoundModule : Module(
    name = "toggle_sounds",
    category = ModuleCategory.Misc,
    defaultEnabled = false
) {
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
    }
}
