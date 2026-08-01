package com.voidclient.client.game.module.visual

import android.graphics.Canvas
import android.graphics.Paint
import com.voidclient.client.game.InterceptablePacket
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import com.voidclient.client.render.RenderOverlayView
import org.cloudburstmc.math.matrix.Matrix4f
import org.cloudburstmc.math.vector.Vector2f
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.math.vector.Vector3i
import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket
import org.cloudburstmc.protocol.common.NamedDefinition
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class XRayModule : Module("XRay", ModuleCategory.Visual) {

    companion object {
        private var renderView: RenderOverlayView? = null
        fun setRenderView(view: RenderOverlayView) {
            renderView = view
        }
    }

    private val range by floatValue("Range", 32.0f, 8.0f..64.0f)
    private val showWireframe by boolValue("Wireframe", true)
    private val showTracers by boolValue("Tracers", true)
    private val fov by floatValue("FOV", 110f, 40f..110f)
    private val strokeWidth by floatValue("Stroke Width", 2.0f, 1f..5f)

    private val showDiamond by boolValue("Diamond", true)
    private val showEmerald by boolValue("Emerald", true)
    private val showGold by boolValue("Gold", true)
    private val showIron by boolValue("Iron", true)
    private val showCoal by boolValue("Coal", true)
    private val showRedstone by boolValue("Redstone", true)
    private val showLapis by boolValue("Lapis", true)
    private val showCopper by boolValue("Copper", true)
    private val showAncientDebris by boolValue("Ancient Debris", true)
    private val showQuartz by boolValue("Nether Quartz", true)

    private data class OreBlock(val pos: Vector3i, val x: Float, val y: Float, val z: Float, val ore: Ore)

    private enum class Ore(
        val color: Int,
        val blocks: List<String>,
        val show: (XRayModule) -> Boolean
    ) {
        DIAMOND(android.graphics.Color.rgb(80, 240, 255), listOf("minecraft:diamond_ore", "minecraft:deepslate_diamond_ore"), { it.showDiamond }),
        EMERALD(android.graphics.Color.rgb(0, 255, 120), listOf("minecraft:emerald_ore", "minecraft:deepslate_emerald_ore"), { it.showEmerald }),
        GOLD(android.graphics.Color.rgb(255, 220, 60), listOf("minecraft:gold_ore", "minecraft:deepslate_gold_ore", "minecraft:nether_gold_ore"), { it.showGold }),
        IRON(android.graphics.Color.rgb(255, 180, 140), listOf("minecraft:iron_ore", "minecraft:deepslate_iron_ore"), { it.showIron }),
        COAL(android.graphics.Color.rgb(90, 90, 90), listOf("minecraft:coal_ore", "minecraft:deepslate_coal_ore"), { it.showCoal }),
        REDSTONE(android.graphics.Color.rgb(255, 60, 60), listOf("minecraft:redstone_ore", "minecraft:deepslate_redstone_ore"), { it.showRedstone }),
        LAPIS(android.graphics.Color.rgb(60, 120, 255), listOf("minecraft:lapis_ore", "minecraft:deepslate_lapis_ore"), { it.showLapis }),
        COPPER(android.graphics.Color.rgb(255, 130, 60), listOf("minecraft:copper_ore", "minecraft:deepslate_copper_ore"), { it.showCopper }),
        ANCIENT_DEBRIS(android.graphics.Color.rgb(160, 90, 60), listOf("minecraft:ancient_debris"), { it.showAncientDebris }),
        QUARTZ(android.graphics.Color.rgb(240, 240, 255), listOf("minecraft:nether_quartz_ore"), { it.showQuartz });

        companion object {
            fun fromIdentifier(identifier: String): Ore? {
                return entries.firstOrNull { it.blocks.contains(identifier) }
            }
        }
    }

    private val oreBlocks = mutableMapOf<Long, OreBlock>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    fun render(canvas: Canvas) {
        if (!isEnabled || !isSessionCreated) return

        val localPlayer = session.localPlayer
        val viewProj = Matrix4f.createPerspective(fov, canvas.width.toFloat() / canvas.height, 0.1f, 128f)
            .mul(
                Matrix4f.createTranslation(localPlayer.vec3Position)
                    .mul(rotateY(-localPlayer.rotationYaw - 180f))
                    .mul(rotateX(-localPlayer.rotationPitch))
                    .invert()
            )

        val px = localPlayer.posX
        val py = localPlayer.posY
        val pz = localPlayer.posZ
        paint.strokeWidth = strokeWidth

        oreBlocks.values.forEach { block ->
            val dx = block.x - px
            val dy = block.y - py
            val dz = block.z - pz
            val dist = sqrt(dx * dx + dy * dy + dz * dz)
            if (dist > range) return@forEach
            if (!block.ore.show(this)) return@forEach

            paint.color = block.ore.color

            val wp = Vector3f.from(block.x + 0.5f, block.y, block.z + 0.5f)
            val verts = getBlockBoxVertices(wp)
            val screenPoints = verts.mapNotNull { worldToScreen(it, viewProj, canvas.width, canvas.height) }
            if (screenPoints.size < 8) return@forEach

            val minX = screenPoints.minOf { it.x }
            val maxX = screenPoints.maxOf { it.x }
            val minY = screenPoints.minOf { it.y }
            val maxY = screenPoints.maxOf { it.y }

            if (showWireframe) {
                draw3DBox(canvas, paint, screenPoints)
            }

            if (showTracers) {
                val cx = (minX + maxX) / 2f
                val cy = maxY
                canvas.drawLine(canvas.width / 2f, canvas.height.toFloat(), cx, cy, paint)
            }
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet
        if (packet !is UpdateBlockPacket) return

        val pos = packet.blockPosition ?: return
        val definition = packet.definition
        val key = blockPosKey(pos)

        val identifier = (definition as? NamedDefinition)?.identifier
        val ore = identifier?.let { Ore.fromIdentifier(it) }

        if (ore != null) {
            oreBlocks[key] = OreBlock(
                pos = pos,
                x = pos.x.toFloat(),
                y = pos.y.toFloat(),
                z = pos.z.toFloat(),
                ore = ore
            )
        } else {
            oreBlocks.remove(key)
        }
        renderView?.invalidate()
    }

    override fun onDisconnect(reason: String) {
        oreBlocks.clear()
    }

    private fun getBlockBoxVertices(center: Vector3f): Array<Vector3f> {
        val hw = 0.5f
        val h = 1.0f
        return arrayOf(
            Vector3f.from(center.x - hw, center.y, center.z - hw),
            Vector3f.from(center.x + hw, center.y, center.z - hw),
            Vector3f.from(center.x + hw, center.y + h, center.z - hw),
            Vector3f.from(center.x - hw, center.y + h, center.z - hw),
            Vector3f.from(center.x - hw, center.y, center.z + hw),
            Vector3f.from(center.x + hw, center.y, center.z + hw),
            Vector3f.from(center.x + hw, center.y + h, center.z + hw),
            Vector3f.from(center.x - hw, center.y + h, center.z + hw)
        )
    }

    private fun worldToScreen(pos: Vector3f, m: Matrix4f, w: Int, h: Int): Vector2f? {
        val rw = m.get(3, 0) * pos.x + m.get(3, 1) * pos.y + m.get(3, 2) * pos.z + m.get(3, 3)
        if (rw <= 0.01f) return null
        val inv = 1f / rw
        val x = w / 2f + (m.get(0, 0) * pos.x + m.get(0, 1) * pos.y + m.get(0, 2) * pos.z + m.get(0, 3)) * inv * w / 2f
        val y = h / 2f - (m.get(1, 0) * pos.x + m.get(1, 1) * pos.y + m.get(1, 2) * pos.z + m.get(1, 3)) * inv * h / 2f
        return Vector2f.from(x, y)
    }

    private fun draw3DBox(c: Canvas, p: Paint, pts: List<Vector2f>) {
        val edges = listOf(
            0 to 1, 1 to 2, 2 to 3, 3 to 0,
            4 to 5, 5 to 6, 6 to 7, 7 to 4,
            0 to 4, 1 to 5, 2 to 6, 3 to 7
        )
        edges.forEach { (a, b) ->
            c.drawLine(pts[a].x, pts[a].y, pts[b].x, pts[b].y, p)
        }
    }

    private fun rotateX(a: Float): Matrix4f {
        val r = Math.toRadians(a.toDouble())
        return Matrix4f.from(
            1f, 0f, 0f, 0f,
            0f, cos(r).toFloat(), -sin(r).toFloat(), 0f,
            0f, sin(r).toFloat(), cos(r).toFloat(), 0f,
            0f, 0f, 0f, 1f
        )
    }

    private fun rotateY(a: Float): Matrix4f {
        val r = Math.toRadians(a.toDouble())
        return Matrix4f.from(
            cos(r).toFloat(), 0f, sin(r).toFloat(), 0f,
            0f, 1f, 0f, 0f,
            -sin(r).toFloat(), 0f, cos(r).toFloat(), 0f,
            0f, 0f, 0f, 1f
        )
    }

    private fun blockPosKey(pos: Vector3i): Long {
        return (pos.x.toLong() shl 32) or (pos.z.toLong() and 0xFFFFFFFFL)
    }
}
