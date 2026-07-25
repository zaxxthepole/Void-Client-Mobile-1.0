package com.voidclient.client.overlay.hud

import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp
import com.voidclient.client.BuildConfig
import com.voidclient.client.R
import com.voidclient.client.overlay.OverlayManager
import com.voidclient.client.overlay.OverlayWindow
import com.voidclient.client.game.module.misc.WaterMarkModule
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.math.PI

class WaterMarkOverlay : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 20
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var customText by mutableStateOf("Voidclient")
    private var showVersion by mutableStateOf(true)
    private var position by mutableStateOf(WaterMarkModule.Position.TOP_LEFT)
    private var fontSize by mutableStateOf(20)
    private var mode by mutableStateOf(WaterMarkModule.WatermarkMode.RGB)
    private var fontStyle by mutableStateOf(WaterMarkModule.FontStyle.MINECRAFT)

    companion object {
        val overlayInstance by lazy { WaterMarkOverlay() }
        private var shouldShowOverlay = false

        fun setOverlayEnabled(enabled: Boolean) {
            shouldShowOverlay = enabled
            try {
                if (enabled) OverlayManager.showOverlayWindow(overlayInstance)
                else OverlayManager.dismissOverlayWindow(overlayInstance)
            } catch (_: Exception) {}
        }

        fun isOverlayEnabled(): Boolean = shouldShowOverlay

        fun setCustomText(text: String) {
            overlayInstance.customText = text
        }

        fun setShowVersion(show: Boolean) {
            overlayInstance.showVersion = show
        }

        fun setPosition(pos: WaterMarkModule.Position) {
            overlayInstance.position = pos
            overlayInstance.updateLayoutParams()
        }

        fun setFontSize(size: Int) {
            overlayInstance.fontSize = size
        }

        fun setMode(newMode: WaterMarkModule.WatermarkMode) {
            overlayInstance.mode = newMode
        }

        fun setFontStyle(style: WaterMarkModule.FontStyle) {
            overlayInstance.fontStyle = style
        }
    }

    private fun updateLayoutParams() {
        _layoutParams.gravity = when (position) {
            WaterMarkModule.Position.TOP_LEFT -> Gravity.TOP or Gravity.START
            WaterMarkModule.Position.TOP_CENTER -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
            WaterMarkModule.Position.TOP_RIGHT -> Gravity.TOP or Gravity.END
            WaterMarkModule.Position.CENTER_LEFT -> Gravity.CENTER_VERTICAL or Gravity.START
            WaterMarkModule.Position.CENTER -> Gravity.CENTER
            WaterMarkModule.Position.CENTER_RIGHT -> Gravity.CENTER_VERTICAL or Gravity.END
            WaterMarkModule.Position.BOTTOM_LEFT -> Gravity.BOTTOM or Gravity.START
            WaterMarkModule.Position.BOTTOM_CENTER -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            WaterMarkModule.Position.BOTTOM_RIGHT -> Gravity.BOTTOM or Gravity.END
        }

        try {
            windowManager.updateViewLayout(composeView, _layoutParams)
        } catch (_: Exception) {}
    }

    @Composable
    override fun Content() {
        if (!shouldShowOverlay) return

        var time by remember { mutableStateOf(0f) }

        LaunchedEffect(Unit) {
            while (true) {
                time += 0.016f
                delay(16)
            }
        }

        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectDragGestures { _, drag ->
                        _layoutParams.x += drag.x.toInt()
                        _layoutParams.y += drag.y.toInt()
                        try {
                            windowManager.updateViewLayout(composeView, _layoutParams)
                        } catch (_: Exception) {}
                    }
                }
        ) {
            when (mode) {
                WaterMarkModule.WatermarkMode.RGB -> RGBWatermark(time)
            }
        }
    }

    @Composable
    private fun RGBWatermark(time: Float) {
        val colors = listOf(
            Color(0xFFFF0000), Color(0xFFFF7F00), Color(0xFFFFFF00),
            Color(0xFF00FF00), Color(0xFF0000FF), Color(0xFF4B0082),
            Color(0xFF9400D3), Color(0xFFFF0000)
        )

        val smoothGradient = remember(time) {
            val gradientColors = mutableListOf<Color>()
            for (i in 0 until 100) {
                val position = (i / 100f + time * 0.15f) % 1f
                val scaledPos = position * (colors.size - 1)
                val index = scaledPos.toInt()
                val fraction = scaledPos - index
                val color1 = colors[index]
                val color2 = colors[index + 1]
                gradientColors.add(blendColors(color1, color2, fraction))
            }
            gradientColors
        }

        val gradientBrush = Brush.linearGradient(
            colors = smoothGradient,
            start = Offset.Zero,
            end = Offset.Infinite
        )

        val fontFamily = getFontFamily()

        Text(
            text = buildWatermarkText(gradientBrush),
            style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.4f),
                    offset = Offset(1f, 1f),
                    blurRadius = 3f
                )
            )
        )
    }

    @Composable
    private fun GlowWatermark(time: Float) {

        val baseRed = Color(0xFFFF0033)
        val darkRed = Color(0xFFCC0028)
        val brightRed = Color(0xFFFF3355)


        val glowIntensity = (sin(time * 1.2f) * 0.5f + 0.5f) * 0.4f + 0.6f


        val redGradient = Brush.horizontalGradient(
            colors = listOf(
                darkRed,
                baseRed,
                brightRed,
                baseRed,
                darkRed
            )
        )

        val fontFamily = getFontFamily()


        Box(
            modifier = Modifier.drawBehind {
                drawIntoCanvas { canvas ->

                    val outerGlow = Paint().asFrameworkPaint().apply {
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(
                            35f * glowIntensity,
                            0f,
                            0f,
                            baseRed.copy(alpha = 0.6f * glowIntensity).toArgb()
                        )
                    }
                    canvas.nativeCanvas.drawRect(
                        -15f, -10f, size.width + 15f, size.height + 10f, outerGlow
                    )


                    val middleGlow = Paint().asFrameworkPaint().apply {
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(
                            20f * glowIntensity,
                            0f,
                            0f,
                            brightRed.copy(alpha = 0.7f * glowIntensity).toArgb()
                        )
                    }
                    canvas.nativeCanvas.drawRect(
                        -10f, -5f, size.width + 10f, size.height + 5f, middleGlow
                    )


                    val innerGlow = Paint().asFrameworkPaint().apply {
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(
                            10f * glowIntensity,
                            0f,
                            0f,
                            Color(0xFFFF6677).copy(alpha = 0.9f * glowIntensity).toArgb()
                        )
                    }
                    canvas.nativeCanvas.drawRect(
                        -5f, -2f, size.width + 5f, size.height + 2f, innerGlow
                    )
                }
            }
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontSize = fontSize.sp,
                            fontWeight = FontWeight.Bold,
                            brush = redGradient,
                            shadow = Shadow(
                                color = baseRed.copy(alpha = 0.8f * glowIntensity),
                                offset = Offset(0f, 0f),
                                blurRadius = 15f * glowIntensity
                            )
                        )
                    ) {
                        append(customText)
                    }

                    if (showVersion) {
                        withStyle(
                            SpanStyle(
                                fontSize = (fontSize * 0.55f).sp,
                                fontWeight = FontWeight.SemiBold,
                                baselineShift = BaselineShift.Superscript,
                                brush = redGradient,
                                shadow = Shadow(
                                    color = baseRed.copy(alpha = 0.7f * glowIntensity),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 12f * glowIntensity
                                )
                            )
                        ) {
                            append(" v${BuildConfig.VERSION_NAME}")
                        }
                    }
                },
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }

    @Composable
    private fun buildWatermarkText(brush: Brush): AnnotatedString {
        return buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    brush = brush
                )
            ) {
                append(customText)
            }

            if (showVersion) {
                withStyle(
                    SpanStyle(
                        fontSize = (fontSize * 0.55f).sp,
                        fontWeight = FontWeight.SemiBold,
                        baselineShift = BaselineShift.Superscript,
                        brush = brush
                    )
                ) {
                    append(" v${BuildConfig.VERSION_NAME}")
                }
            }
        }
    }

    @Composable
    private fun getFontFamily(): FontFamily {
        return when (fontStyle) {
            WaterMarkModule.FontStyle.MINECRAFT -> try {
                FontFamily(Font(R.font.minecraft))
            } catch (e: Exception) {
                FontFamily.Monospace
            }
            WaterMarkModule.FontStyle.DEFAULT -> FontFamily.Default
        }
    }

    private fun blendColors(color1: Color, color2: Color, ratio: Float): Color {
        val clampedRatio = ratio.coerceIn(0f, 1f)
        return Color(
            red = color1.red * (1 - clampedRatio) + color2.red * clampedRatio,
            green = color1.green * (1 - clampedRatio) + color2.green * clampedRatio,
            blue = color1.blue * (1 - clampedRatio) + color2.blue * clampedRatio,
            alpha = color1.alpha * (1 - clampedRatio) + color2.alpha * clampedRatio
        )
    }
}