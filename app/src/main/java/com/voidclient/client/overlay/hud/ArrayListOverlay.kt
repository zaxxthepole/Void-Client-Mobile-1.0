package com.voidclient.client.overlay.hud

import android.view.Gravity
import com.voidclient.client.overlay.OverlayWindow
import com.voidclient.client.overlay.OverlayManager
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidclient.client.R
import com.voidclient.client.game.module.misc.ArrayListModule
import com.voidclient.client.ui.theme.WColors
import com.voidclient.client.util.translatedSelf
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class ArrayListOverlay : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.END
            x = 20
            y = 20
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var modules by mutableStateOf(listOf<ModuleInfo>())
    private var sortMode by mutableStateOf(ArrayListModule.SortMode.LENGTH)
    private var animationSpeed by mutableStateOf(300)
    private var showBackground by mutableStateOf(true)
    private var showBorder by mutableStateOf(true)
    private var borderStyle by mutableStateOf(ArrayListModule.BorderStyle.LEFT)
    private var colorMode by mutableStateOf(ArrayListModule.ColorMode.RAINBOW)
    private var rainbowSpeed by mutableStateOf(1.0f)
    private var fontSize by mutableStateOf(14)
    private var spacing by mutableStateOf(2)
    private var fadeAnimation by mutableStateOf(true)
    private var slideAnimation by mutableStateOf(true)
    private var fontStyle by mutableStateOf(ArrayListModule.FontStyle.NORMAL)
    private var gradientIntensity by mutableStateOf(1.0f)
    private var glowEffect by mutableStateOf(false)
    private var smoothGradient by mutableStateOf(true)

    data class ModuleInfo(
        val name: String,
        val category: String,
        val isEnabled: Boolean,
        val priority: Int
    )

    companion object {
        val overlayInstance by lazy { ArrayListOverlay() }
        private var shouldShowOverlay = false

        fun showOverlay() {
            if (shouldShowOverlay) {
                try {
                    OverlayManager.showOverlayWindow(overlayInstance)
                } catch (e: Exception) {}
            }
        }

        fun dismissOverlay() {
            try {
                OverlayManager.dismissOverlayWindow(overlayInstance)
            } catch (e: Exception) {}
        }

        fun setOverlayEnabled(enabled: Boolean) {
            shouldShowOverlay = enabled
            if (enabled) showOverlay() else dismissOverlay()
        }

        fun isOverlayEnabled(): Boolean = shouldShowOverlay

        fun setModules(moduleList: List<ModuleInfo>) {
            overlayInstance.modules = moduleList
        }

        fun setSortMode(mode: ArrayListModule.SortMode) {
            overlayInstance.sortMode = mode
        }

        fun setAnimationSpeed(speed: Int) {
            overlayInstance.animationSpeed = speed
        }

        fun setShowBackground(show: Boolean) {
            overlayInstance.showBackground = show
        }

        fun setShowBorder(show: Boolean) {
            overlayInstance.showBorder = show
        }

        fun setBorderStyle(style: ArrayListModule.BorderStyle) {
            overlayInstance.borderStyle = style
        }

        fun setColorMode(mode: ArrayListModule.ColorMode) {
            overlayInstance.colorMode = mode
        }

        fun setRainbowSpeed(speed: Float) {
            overlayInstance.rainbowSpeed = speed
        }

        fun setFontSize(size: Int) {
            overlayInstance.fontSize = size
        }

        fun setSpacing(space: Int) {
            overlayInstance.spacing = space
        }

        fun setFadeAnimation(fade: Boolean) {
            overlayInstance.fadeAnimation = fade
        }

        fun setSlideAnimation(slide: Boolean) {
            overlayInstance.slideAnimation = slide
        }

        fun setFontStyle(style: ArrayListModule.FontStyle) {
            overlayInstance.fontStyle = style
        }

        fun setGradientIntensity(intensity: Float) {
            overlayInstance.gradientIntensity = intensity
        }

        fun setGlowEffect(glow: Boolean) {
            overlayInstance.glowEffect = glow
        }

        fun setSmoothGradient(smooth: Boolean) {
            overlayInstance.smoothGradient = smooth
        }
    }

    @Composable
    override fun Content() {
        if (!isOverlayEnabled()) return

        var rainbowOffset by remember { mutableStateOf(0f) }

        val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
        val animatedOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (3000 / rainbowSpeed).toInt(),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "offset"
        )

        LaunchedEffect(animatedOffset) {
            rainbowOffset = animatedOffset
        }

        val sortedModules = when (sortMode) {
            ArrayListModule.SortMode.LENGTH -> modules.sortedByDescending { it.name.length }
            ArrayListModule.SortMode.ALPHABETICAL -> modules.sortedBy { it.name }
            ArrayListModule.SortMode.CATEGORY -> modules.sortedBy { it.category }
            ArrayListModule.SortMode.CUSTOM -> modules.sortedByDescending { it.priority }
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            sortedModules.forEachIndexed { index, module ->
                AnimatedVisibility(
                    visible = true,
                    enter = if (fadeAnimation) fadeIn(animationSpec = tween(animationSpeed, easing = FastOutSlowInEasing)) else fadeIn(tween(0)) +
                            if (slideAnimation) slideInHorizontally(animationSpec = tween(animationSpeed, easing = FastOutSlowInEasing)) { it } else slideInHorizontally(tween(0)) { 0 } +
                                    expandVertically(animationSpec = tween(animationSpeed, easing = FastOutSlowInEasing)),
                    exit = if (fadeAnimation) fadeOut(animationSpec = tween(animationSpeed, easing = FastOutSlowInEasing)) else fadeOut(tween(0)) +
                            if (slideAnimation) slideOutHorizontally(animationSpec = tween(animationSpeed, easing = FastOutSlowInEasing)) { it } else slideOutHorizontally(tween(0)) { 0 } +
                                    shrinkVertically(animationSpec = tween(animationSpeed, easing = FastOutSlowInEasing))
                ) {
                    ModuleItem(
                        module = module,
                        index = index,
                        rainbowOffset = rainbowOffset,
                        totalModules = sortedModules.size,
                        isLast = index == sortedModules.size - 1
                    )
                }
            }
        }
    }

    @Composable
    private fun ModuleItem(
        module: ModuleInfo,
        index: Int,
        rainbowOffset: Float,
        totalModules: Int,
        isLast: Boolean
    ) {
        val moduleColor = getModuleColor(index, rainbowOffset, totalModules)
        val borderWidth = if (showBorder) 2.dp else 0.dp

        val fontFamily = when (fontStyle) {
            ArrayListModule.FontStyle.MINECRAFT -> try {
                FontFamily(Font(R.font.minecraft))
            } catch (e: Exception) {
                FontFamily.Monospace
            }
            ArrayListModule.FontStyle.NORMAL -> FontFamily.Default
        }

        val textShadow = if (glowEffect) {
            Shadow(
                color = moduleColor.copy(alpha = 0.8f),
                offset = Offset(0f, 0f),
                blurRadius = 8f
            )
        } else {
            Shadow(
                color = Color.Black.copy(alpha = 0.5f),
                offset = Offset(2f, 2f),
                blurRadius = 4f
            )
        }

        Box(
            modifier = Modifier
                .padding(bottom = if (!isLast) spacing.dp else 0.dp)
                .let { modifier ->
                    if (smoothGradient && colorMode == ArrayListModule.ColorMode.SMOOTH_GRADIENT) {
                        val gradientColors = getSmoothGradientColors(index, rainbowOffset, totalModules)
                        modifier.background(
                            brush = Brush.horizontalGradient(gradientColors),
                            shape = RoundedCornerShape(4.dp)
                        )
                    } else if (showBackground) {
                        modifier
                            .background(
                                WColors.Surface.copy(alpha = 0.7f),
                                RoundedCornerShape(4.dp)
                            )
                    } else modifier
                }
                .clip(RoundedCornerShape(4.dp))
                .let { modifier ->
                    when (borderStyle) {
                        ArrayListModule.BorderStyle.LEFT -> {
                            if (smoothGradient && colorMode == ArrayListModule.ColorMode.SMOOTH_GRADIENT) {
                                val gradientColors = getSmoothGradientColors(index, rainbowOffset, totalModules)
                                modifier.drawBehind {
                                    drawRect(
                                        brush = Brush.verticalGradient(gradientColors),
                                        size = size.copy(width = borderWidth.toPx())
                                    )
                                }
                            } else {
                                modifier.border(
                                    width = borderWidth,
                                    color = moduleColor,
                                    shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                                )
                            }
                        }
                        ArrayListModule.BorderStyle.RIGHT -> {
                            if (smoothGradient && colorMode == ArrayListModule.ColorMode.SMOOTH_GRADIENT) {
                                val gradientColors = getSmoothGradientColors(index, rainbowOffset, totalModules)
                                modifier.drawBehind {
                                    drawRect(
                                        brush = Brush.verticalGradient(gradientColors),
                                        topLeft = Offset(size.width - borderWidth.toPx(), 0f),
                                        size = size.copy(width = borderWidth.toPx())
                                    )
                                }
                            } else {
                                modifier.border(
                                    width = borderWidth,
                                    color = moduleColor,
                                    shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                                )
                            }
                        }
                        ArrayListModule.BorderStyle.FULL -> {
                            if (smoothGradient && colorMode == ArrayListModule.ColorMode.SMOOTH_GRADIENT) {
                                val gradientColors = getSmoothGradientColors(index, rainbowOffset, totalModules)
                                modifier.border(
                                    width = borderWidth,
                                    brush = Brush.sweepGradient(gradientColors),
                                    shape = RoundedCornerShape(4.dp)
                                )
                            } else {
                                modifier.border(
                                    width = borderWidth,
                                    color = moduleColor,
                                    shape = RoundedCornerShape(4.dp)
                                )
                            }
                        }
                        else -> modifier
                    }
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = module.name.translatedSelf,
                color = if (smoothGradient && colorMode == ArrayListModule.ColorMode.SMOOTH_GRADIENT) Color.White else moduleColor,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = fontFamily,
                style = TextStyle(shadow = textShadow)
            )
        }
    }

    private fun getSmoothGradientColors(index: Int, offset: Float, totalModules: Int): List<Color> {
        val baseColors = listOf(
            Color(0xFFFF0080), // Hot Pink
            Color(0xFF8000FF), // Purple
            Color(0xFF0080FF), // Blue
            Color(0xFF00FF80), // Cyan
            Color(0xFF80FF00), // Lime
            Color(0xFFFFFF00), // Yellow
            Color(0xFFFF8000), // Orange
            Color(0xFFFF0000)  // Red
        )

        val smoothColors = mutableListOf<Color>()
        val segmentProgress = (offset + index.toFloat() / totalModules.coerceAtLeast(1)) % 1f
        val colorIndex = (segmentProgress * baseColors.size * gradientIntensity).toInt() % baseColors.size
        val nextColorIndex = (colorIndex + 1) % baseColors.size
        val localProgress = (segmentProgress * baseColors.size * gradientIntensity) % 1f

        // Create smooth transition between colors
        for (i in 0..2) {
            val currentIndex = (colorIndex + i) % baseColors.size
            val nextIndex = (currentIndex + 1) % baseColors.size
            smoothColors.add(lerpColor(baseColors[currentIndex], baseColors[nextIndex], localProgress))
        }

        return smoothColors
    }

    private fun getModuleColor(index: Int, rainbowOffset: Float, totalModules: Int): Color {
        return when (colorMode) {
            ArrayListModule.ColorMode.RAINBOW -> {
                val hue = (rainbowOffset + index.toFloat() / totalModules.coerceAtLeast(1) * gradientIntensity) % 1f
                hsvToRgb(hue, 0.9f, 1f)
            }
            ArrayListModule.ColorMode.SMOOTH_GRADIENT -> {
                val progress = (rainbowOffset + index.toFloat() / totalModules.coerceAtLeast(1)) % 1f
                val hue = progress * 360f
                hsvToRgb(hue / 360f, 0.85f, 1f)
            }
            ArrayListModule.ColorMode.GRADIENT -> {
                val progress = index.toFloat() / maxOf(totalModules - 1, 1)
                lerpColor(Color(0xFFFF0080), Color(0xFF00D4FF), progress)
            }
            ArrayListModule.ColorMode.WAVE -> {
                val wave = (sin((rainbowOffset * 2 * Math.PI + index * 0.3).toFloat()) * 0.5f + 0.5f)
                hsvToRgb(wave, 0.9f, 1f)
            }
            ArrayListModule.ColorMode.PULSE -> {
                val pulse = abs(sin((rainbowOffset * Math.PI + index * 0.2).toFloat()))
                Color.White.copy(alpha = 0.5f + pulse * 0.5f)
            }
            ArrayListModule.ColorMode.STATIC -> WColors.Accent
            ArrayListModule.ColorMode.CATEGORY_BASED -> when (modules.getOrNull(index)?.category) {
                "Combat" -> Color(0xFFFF3333)
                "Movement" -> Color(0xFF3399FF)
                "Visual" -> Color(0xFF33FF66)
                "Misc" -> Color(0xFFFFCC33)
                "World" -> Color(0xFF33FFFF)
                else -> WColors.Accent
            }
            ArrayListModule.ColorMode.RANDOM -> {
                val colors = listOf(
                    Color(0xFFFF0080), Color(0xFF0080FF),
                    Color(0xFF00FF80), Color(0xFFFF8000),
                    Color(0xFF8000FF), Color(0xFFFFFF00)
                )
                colors[index % colors.size]
            }
        }
    }

    private fun hsvToRgb(h: Float, s: Float, v: Float): Color {
        val hDegrees = h * 360f
        val c = v * s
        val x = c * (1 - abs((hDegrees / 60f) % 2 - 1))
        val m = v - c

        val (r, g, b) = when {
            hDegrees < 60 -> Triple(c, x, 0f)
            hDegrees < 120 -> Triple(x, c, 0f)
            hDegrees < 180 -> Triple(0f, c, x)
            hDegrees < 240 -> Triple(0f, x, c)
            hDegrees < 300 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Color(
            red = (r + m).coerceIn(0f, 1f),
            green = (g + m).coerceIn(0f, 1f),
            blue = (b + m).coerceIn(0f, 1f)
        )
    }

    private fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
        val f = fraction.coerceIn(0f, 1f)
        return Color(
            red = start.red + f * (stop.red - start.red),
            green = start.green + f * (stop.green - start.green),
            blue = start.blue + f * (stop.blue - start.blue),
            alpha = start.alpha + f * (stop.alpha - start.alpha)
        )
    }
}