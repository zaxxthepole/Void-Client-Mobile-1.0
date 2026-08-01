package com.voidclient.client.game.module.visual

import android.graphics.Canvas
import android.graphics.Paint
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import com.voidclient.client.render.RenderOverlayView
import org.cloudburstmc.math.matrix.Matrix4f
import org.cloudburstmc.math.vector.Vector2f
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class BreadcrumbsModule : Module("Breadcrumbs", ModuleCategory.Visual) {

    companion object {
        private var renderView: RenderOverlayView? = null
        fun setRenderView(view: RenderOverlayView) {
            renderView = view
        }
    }

    private val trailLength by intValue("Trail Length", 150, 20..500)
    private val sampleInterval by intValue("Sample Interval (ms)", 50, 10..500)
    private val minDistance by floatValue("Min Distance", 0.4f, 0.1f..2.0f)
    private val lineWidth by floatValue("Line Width", 2.5f, 1f..8f)
    private val colorR by intValue("Color R", 192, 0..255)
    private val colorG by intValue("Color G", 132, 0..255)
    private val colorB by intValue("Color B", 252, 0..255)
    private val fade by boolValue("Fade", true)

    private val points = ArrayDeque<Vector3f>()
    private var lastSample = 0L
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    override fun onEnabled() {
        super.onEnabled()
        points.clear()
        lastSample = System.currentTimeMillis()
    }

    override fun afterPacketBound(packet: BedrockPacket) {
        if (!isEnabled || !isSessionCreated) return
        val now = System.currentTimeMillis()
        if (now - lastSample < sampleInterval) return
        lastSample = now

        val pos = session.localPlayer.vec3Position

        val last = points.lastOrNull()
        if (last != null) {
            val dx = pos.x - last.x
            val dy = pos.y - last.y
            val dz = pos.z - last.z
            if (sqrt(dx * dx + dy * dy + dz * dz) < minDistance) return
        }

        points.addLast(pos)
        while (points.size > trailLength) {
            points.removeFirst()
        }
        renderView?.invalidate()
    }

    fun render(canvas: Canvas) {
        if (!isEnabled || !isSessionCreated) return
        if (points.size < 2) return

        val localPlayer = session.localPlayer
        val viewProj = Matrix4f.createPerspective(110f, canvas.width.toFloat() / canvas.height, 0.1f, 128f)
            .mul(
                Matrix4f.createTranslation(localPlayer.vec3Position)
                    .mul(rotateY(-localPlayer.rotationYaw - 180f))
                    .mul(rotateX(-localPlayer.rotationPitch))
                    .invert()
            )

        paint.strokeWidth = lineWidth

        val screenPoints = points.mapNotNull { worldToScreen(it, viewProj, canvas.width, canvas.height) }
        if (screenPoints.size < 2) return

        val alphaStep = if (fade) 255f / screenPoints.size else 0f
        for (i in 0 until screenPoints.size - 1) {
            val alpha = if (fade) (255 - i * alphaStep).toInt().coerceIn(30, 255) else 255
            paint.color = (alpha shl 24) or (colorR shl 16) or (colorG shl 8) or colorB
            canvas.drawLine(
                screenPoints[i].x, screenPoints[i].y,
                screenPoints[i + 1].x, screenPoints[i + 1].y,
                paint
            )
        }
    }

    override fun onDisconnect(reason: String) {
        points.clear()
    }

    private fun worldToScreen(pos: Vector3f, m: Matrix4f, w: Int, h: Int): Vector2f? {
        val rw = m.get(3, 0) * pos.x + m.get(3, 1) * pos.y + m.get(3, 2) * pos.z + m.get(3, 3)
        if (rw <= 0.01f) return null
        val inv = 1f / rw
        val x = w / 2f + (m.get(0, 0) * pos.x + m.get(0, 1) * pos.y + m.get(0, 2) * pos.z + m.get(0, 3)) * inv * w / 2f
        val y = h / 2f - (m.get(1, 0) * pos.x + m.get(1, 1) * pos.y + m.get(1, 2) * pos.z + m.get(1, 3)) * inv * h / 2f
        return Vector2f.from(x, y)
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
}
