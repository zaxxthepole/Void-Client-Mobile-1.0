package com.voidclient.client.game.module.misc

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import com.voidclient.client.overlay.hud.WaterMarkOverlay
import kotlinx.coroutines.*

class WaterMarkModule : Module("watermark", ModuleCategory.Misc) {

    private var scope: CoroutineScope? = null

    private val customText by stringValue("Text", "WClient", listOf())
    private val showVersion by boolValue("Show Version", true)
    private val position by enumValue("Position", Position.TOP_LEFT, Position::class.java)
    private val fontSize by intValue("Font Size", 24, 12..36)
    private val mode by enumValue("Mode", WatermarkMode.RGB, WatermarkMode::class.java)
    private val fontStyle by enumValue("Font", FontStyle.MINECRAFT, FontStyle::class.java)

    override fun onEnabled() {
        super.onEnabled()
        if (!isSessionCreated) return

        WaterMarkOverlay.setOverlayEnabled(true)
        applySettings()

        scope = CoroutineScope(Dispatchers.Main + SupervisorJob()).apply {
            launch {
                while (isActive && isEnabled && isSessionCreated) {
                    applySettings()
                    delay(500L)
                }
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        scope?.cancel()
        scope = null

        if (isSessionCreated) {
            WaterMarkOverlay.setOverlayEnabled(false)
        }
    }

    override fun onDisconnect(reason: String) {
        scope?.cancel()
        scope = null
        WaterMarkOverlay.setOverlayEnabled(false)
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {

    }

    private fun applySettings() {
        WaterMarkOverlay.setCustomText(customText)
        WaterMarkOverlay.setShowVersion(showVersion)
        WaterMarkOverlay.setPosition(position)
        WaterMarkOverlay.setFontSize(fontSize)
        WaterMarkOverlay.setMode(mode)
        WaterMarkOverlay.setFontStyle(fontStyle)
    }

    enum class Position {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        CENTER_LEFT, CENTER, CENTER_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    enum class WatermarkMode {
        RGB

    }

    enum class FontStyle {
        DEFAULT,
        MINECRAFT
    }
}