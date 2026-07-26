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
import org.cloudburstmc.protocol.bedrock.packet.BlockEntityDataPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket
import kotlin.math.cos
import kotlin.math.sin

class StorageESPModule : Module("StorageESP", ModuleCategory.Visual) {

    companion object {
        private var renderView: RenderOverlayView? = null
        fun setRenderView(view: RenderOverlayView) {
            renderView = view
        }
    }

    private val range by floatValue("Range", 24.0f, 8.0f..64.0f)
    private val chestColorR by intValue("Chest Color R", 255, 0..255)
    private val chestColorG by intValue("Chest Color G", 200, 0..255)
    private val chestColorB by intValue("Chest Color B", 50, 0..255)
    private val shulkerColorR by intValue("Shulker Color R", 200, 0..255)
    private val shulkerColorG by intValue("Shulker Color G", 50, 0..255)
    private val shulkerColorB by intValue("Shulker Color B", 255, 0..255)
    private val furnaceColorR by intValue("Furnace Color R", 150, 0..255)
    private val furnaceColorG by intValue("Furnace Color G", 150, 0..255)
    private val furnaceColorB by intValue("Furnace Color B", 150, 0..255)
    private val otherColorR by intValue("Other Color R", 100, 0..255)
    private val otherColorG by intValue("Other Color G", 200, 0..255)
    private val otherColorB by intValue("Other Color B", 255, 0..255)
    private val showTracers by boolValue("Tracers", true)
    private val showWireframe by boolValue("Wireframe", true)
    private val fov by floatValue("FOV", 110f, 40f..110f)
    private val strokeWidth by floatValue("Stroke Width", 2.0f, 1f..5f)
    private val showChests by boolValue("Show Chests", true)
    private val showShulkers by boolValue("Show Shulkers", true)
    private val showFurnaces by boolValue("Show Furnaces", true)
    private val showOthers by boolValue("Show Others", true)

    private data class StoredBlock(
        val pos: Vector3i,
        val type: String,
        val x: Float, val y: Float, val z: Float
    )

    private val storedBlocks = mutableMapOf<Long, StoredBlock>()
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

        storedBlocks.values.forEach { block ->
            val dx = block.x - px
            val dy = block.y - py
            val dz = block.z - pz
            val dist = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
            if (dist > range) return@forEach
            if (!shouldShowType(block.type)) return@forEach

            paint.color = getColorForType(block.type)

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

    private fun shouldShowType(type: String): Boolean {
        return when {
            isChest(type) && showChests -> true
            isShulker(type) && showShulkers -> true
            isFurnace(type) && showFurnaces -> true
            showOthers -> true
            else -> false
        }
    }

    private fun isChest(type: String) = type == "Chest" || type == "TrappedChest" || type == "EnderChest"
    private fun isShulker(type: String) = type.startsWith("ShulkerBox") || type.contains("Shulker")
    private fun isFurnace(type: String) = type == "Furnace" || type == "BlastFurnace" || type == "Smoker"

    private fun getColorForType(type: String): Int {
        return when {
            isChest(type) -> android.graphics.Color.rgb(chestColorR, chestColorG, chestColorB)
            isShulker(type) -> android.graphics.Color.rgb(shulkerColorR, shulkerColorG, shulkerColorB)
            isFurnace(type) -> android.graphics.Color.rgb(furnaceColorR, furnaceColorG, furnaceColorB)
            else -> android.graphics.Color.rgb(otherColorR, otherColorG, otherColorB)
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet

        when (packet) {
            is BlockEntityDataPacket -> handleBlockEntityData(packet)
            is UpdateBlockPacket -> handleUpdateBlock(packet)
        }
    }

    private fun handleBlockEntityData(packet: BlockEntityDataPacket) {
        val pos = packet.blockPosition
        if (pos == null) return
        val tag = packet.data
        if (tag == null) return

        val id = tag.getString("id") ?: return

        val key = blockPosKey(pos)
        storedBlocks[key] = StoredBlock(
            pos = pos,
            type = id,
            x = pos.x.toFloat(),
            y = pos.y.toFloat(),
            z = pos.z.toFloat()
        )
        renderView?.invalidate()
    }

    private fun handleUpdateBlock(packet: UpdateBlockPacket) {
        val pos = packet.blockPosition
        if (pos == null) return
        val key = blockPosKey(pos)
        storedBlocks.remove(key)
        renderView?.invalidate()
    }

    override fun onDisconnect(reason: String) {
        storedBlocks.clear()
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
