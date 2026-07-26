package com.voidclient.client.game.module.motion

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class NoSlowModule : Module("NoSlow", ModuleCategory.Motion) {

    private val itemMode by boolValue("Remove Item Flag", true)
    private val keepSprint by boolValue("Keep Sprint", true)
    private val speedMul by floatValue("Speed Multiplier", 1.0f, 0.1f..2.0f)
    private val onlyWhenMoving by boolValue("Only When Moving", false)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val isMoving = packet.inputData.any { it in MOVE_FLAGS }

        if (onlyWhenMoving && !isMoving) return

        if (itemMode) {
            packet.inputData.remove(PlayerAuthInputData.START_USING_ITEM)

            if (keepSprint && isMoving) {
                packet.inputData.add(PlayerAuthInputData.SPRINTING)
                packet.inputData.add(PlayerAuthInputData.START_SPRINTING)
            }

            if (speedMul > 0.5f) {
                packet.inputData.add(PlayerAuthInputData.WANT_UP)
            }
        }
    }

    companion object {
        private val MOVE_FLAGS = setOf(
            PlayerAuthInputData.UP,
            PlayerAuthInputData.DOWN,
            PlayerAuthInputData.LEFT,
            PlayerAuthInputData.RIGHT,
            PlayerAuthInputData.UP_LEFT,
            PlayerAuthInputData.UP_RIGHT,
            PlayerAuthInputData.DOWN_LEFT,
            PlayerAuthInputData.DOWN_RIGHT,
            PlayerAuthInputData.JUMPING,
            PlayerAuthInputData.SPRINTING,
            PlayerAuthInputData.SPRINT_DOWN
        )
    }
}
