package com.voidclient.client.game.module.motion

import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import com.voidclient.client.game.acb.Acb
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.RequestAbilityPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class FlyModule : Module("fly", ModuleCategory.Motion) {

    enum class FlyMode { VANILLA, JUMP, TELEPORT }

    private val modeValue by enumValue("Mode", FlyMode.VANILLA, FlyMode::class.java)
    private val speedValue by floatValue("Speed", 0.4f, 0.1f..2f)
    private val hoverValue by boolValue("Hover", true)
    private val descendValue by boolValue("Auto Descend", false)

    private val enableFlyAbilitiesPacket = UpdateAbilitiesPacket().apply {
        setPlayerPermission(PlayerPermission.OPERATOR)
        setCommandPermission(CommandPermission.OWNER)
        getAbilityLayers().add(AbilityLayer().apply {
            setLayerType(AbilityLayer.Type.BASE)
            getAbilitiesSet().addAll(Ability.entries.toTypedArray())
            getAbilityValues().addAll(
                arrayOf(
                    Ability.BUILD,
                    Ability.MINE,
                    Ability.DOORS_AND_SWITCHES,
                    Ability.OPEN_CONTAINERS,
                    Ability.ATTACK_PLAYERS,
                    Ability.ATTACK_MOBS,
                    Ability.OPERATOR_COMMANDS,
                    Ability.MAY_FLY,
                    Ability.FLY_SPEED,
                    Ability.WALK_SPEED
                )
            )
            setWalkSpeed(0.1f)
            setFlySpeed(speedValue)
        })
    }

    private val disableFlyAbilitiesPacket = UpdateAbilitiesPacket().apply {
        setPlayerPermission(PlayerPermission.OPERATOR)
        setCommandPermission(CommandPermission.OWNER)
        getAbilityLayers().add(AbilityLayer().apply {
            setLayerType(AbilityLayer.Type.BASE)
            getAbilitiesSet().addAll(Ability.entries.toTypedArray())
            getAbilityValues().addAll(
                arrayOf(
                    Ability.BUILD,
                    Ability.MINE,
                    Ability.DOORS_AND_SWITCHES,
                    Ability.OPEN_CONTAINERS,
                    Ability.ATTACK_PLAYERS,
                    Ability.ATTACK_MOBS,
                    Ability.OPERATOR_COMMANDS,
                    Ability.FLY_SPEED,
                    Ability.WALK_SPEED
                )
            )
            setWalkSpeed(0.1f)
        })
    }

    private var canFly = false

    override fun onEnabled() {
        super.onEnabled()
        Acb.state.stealthActive = true
    }

    override fun onDisabled() {
        super.onDisabled()
        Acb.state.stealthActive = false
        Acb.teleport.cancel()
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet
        if (packet is RequestAbilityPacket && packet.getAbility() == Ability.FLYING) {
            interceptablePacket.intercept()
            return
        }

        if (packet is UpdateAbilitiesPacket) {
            interceptablePacket.intercept()
            return
        }

        if (packet !is PlayerAuthInputPacket) return

        // Enable/disable flying abilities
        if (!canFly && isEnabled) {
            enableFlyAbilitiesPacket.setUniqueEntityId(session.localPlayer.uniqueEntityId)
            session.clientBound(enableFlyAbilitiesPacket)
            canFly = true
        } else if (canFly && !isEnabled) {
            disableFlyAbilitiesPacket.setUniqueEntityId(session.localPlayer.uniqueEntityId)
            session.clientBound(disableFlyAbilitiesPacket)
            canFly = false
            return
        }

        if (!isEnabled) return

        when (modeValue) {
            FlyMode.VANILLA -> handleVanilla(packet)
            FlyMode.JUMP -> handleJump(packet)
            FlyMode.TELEPORT -> handleTeleport(packet)
        }
    }

    private fun handleVanilla(packet: PlayerAuthInputPacket) {
        val player = session.localPlayer

        // Space for up, Shift for down, otherwise hover/descend
        var verticalMotion = 0f
        when {
            packet.getInputData().contains(PlayerAuthInputData.JUMPING) ->
                verticalMotion = speedValue

            packet.getInputData().contains(PlayerAuthInputData.SNEAKING) ->
                verticalMotion = -speedValue

            !Acb.ground.isGrounded() && descendValue ->
                verticalMotion = -min(speedValue * 0.25f, 0.1f)

            !Acb.ground.isGrounded() && hoverValue -> verticalMotion = 0f

            else -> return
        }

        val motionPacket = SetEntityMotionPacket().apply {
            setRuntimeEntityId(player.runtimeEntityId)
            setMotion(Vector3f.from(player.motionX, verticalMotion, player.motionZ))
        }
        session.clientBound(motionPacket)
    }

    private fun handleJump(packet: PlayerAuthInputPacket) {
        val player = session.localPlayer

        if (Acb.ground.isGrounded()) {
            val jumpPacket = SetEntityMotionPacket().apply {
                setRuntimeEntityId(player.runtimeEntityId)
                setMotion(Vector3f.from(0f, 0.42f, 0f))
            }
            session.clientBound(jumpPacket)
        } else if (packet.getMotion().getY() > 0f) {
            val boost = min(speedValue * 0.25f, 0.1f)
            val yawRad = Math.toRadians(player.rotationYaw.toDouble()).toFloat()
            val motionPacket = SetEntityMotionPacket().apply {
                setRuntimeEntityId(player.runtimeEntityId)
                setMotion(Vector3f.from(-sin(yawRad) * boost, 0f, cos(yawRad) * boost))
            }
            session.clientBound(motionPacket)
        }
    }

    private fun handleTeleport(packet: PlayerAuthInputPacket) {
        val player = session.localPlayer
        val inputData = packet.getInputData()
        val ascending = inputData.contains(PlayerAuthInputData.JUMPING)
        val descending =
            inputData.contains(PlayerAuthInputData.SNEAKING) || inputData.contains(PlayerAuthInputData.DESCEND)
        if (!ascending && !descending) return

        val step = min(speedValue, 2.0f)
        val vertical = if (ascending) step else -step
        val pos = player.vec3Position
        val target = Vector3f.from(pos.getX(), pos.getY() + vertical, pos.getZ())

        Acb.teleport.teleportTo(target)

        val movePlayerPacket = MovePlayerPacket().apply {
            setRuntimeEntityId(player.runtimeEntityId)
            setPosition(target)
            setRotation(player.vec3Rotation)
            setMode(MovePlayerPacket.Mode.NORMAL)
            setOnGround(false)
            setTick(packet.getTick())
        }
        session.clientBound(movePlayerPacket)
    }
}
