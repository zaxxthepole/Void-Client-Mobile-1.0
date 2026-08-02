package com.voidclient.client.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidclient.client.R
import com.voidclient.client.ui.theme.WColors
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val McFont = FontFamily(Font(R.font.minecraft))

private data class NebulaBlob(
    val color: Color,
    val radius: Float,
    val phaseX: Float,
    val phaseY: Float,
    val speed: Float,
    val alpha: Float
)

private fun generateNebulaBlobs(): List<NebulaBlob> {
    val random = Random(4242)
    val colors = listOf(WColors.Primary, WColors.Secondary, WColors.SecondaryLight)
    return colors.map { color ->
        NebulaBlob(
            color = color,
            radius = 0.45f + random.nextFloat() * 0.2f,
            phaseX = random.nextFloat() * 6.2832f,
            phaseY = random.nextFloat() * 6.2832f,
            speed = 0.8f + random.nextFloat() * 0.5f,
            alpha = 0.1f + random.nextFloat() * 0.06f
        )
    }
}

private data class OrbitParticle(
    val phase: Float,
    val radiusFrac: Float,
    val speed: Float
)

private fun generateOrbitParticles(count: Int): List<OrbitParticle> {
    val random = Random(777)
    return (0 until count).map { i ->
        val frac = i.toFloat() / (count - 1)
        OrbitParticle(
            phase = random.nextFloat() * 360f,
            radiusFrac = 0.18f + 0.82f * frac,
            speed = 1.6f - 1.1f * frac
        )
    }
}

private val RING_COLORS = listOf(
    WColors.SecondaryLight,
    WColors.PrimaryLight,
    WColors.Primary,
    WColors.Secondary,
    WColors.PrimaryDark,
    WColors.SecondaryVariant,
    WColors.PrimaryDark
)

@Composable
fun LoadingScreen(onDone: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 50, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    LaunchedEffect(Unit) {
        val loadingSteps = listOf(
            0f to 15f, 15f to 35f, 35f to 65f, 65f to 85f, 85f to 100f
        )
        for ((start, end) in loadingSteps) {
            val steps = (end - start).toInt()
            val delayTime = when {
                start < 15f -> 45L
                start < 65f -> 25L
                else -> 60L
            }
            repeat(steps) {
                delay(delayTime)
                progress = start + it + 1f
            }
        }
        delay(800)
        onDone()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "InfiniteAnimations")

    val vortexTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(9000, easing = LinearEasing)),
        label = "VortexTime"
    )

    val nebulaDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(14000, easing = LinearEasing)),
        label = "NebulaDrift"
    )

    val glow by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow"
    )

    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CorePulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WColors.Background)
    ) {
        NebulaCanvas(drift = nebulaDrift)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(Color.Transparent, WColors.Background.copy(alpha = 0.55f))
                    )
                )
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.drawBehind {
                    drawCircle(
                        color = WColors.Primary.copy(alpha = glow * 0.35f),
                        radius = size.width * 0.55f,
                        center = center
                    )
                    drawCircle(
                        color = WColors.Secondary.copy(alpha = glow * 0.2f),
                        radius = size.width * 0.85f,
                        center = center
                    )
                }
            ) {
                McText(
                    text = "VOIDCLIENT",
                    fontSize = 30.sp,
                    color = Color(0xFFF3ECFF)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            VortexCanvas(
                time = vortexTime,
                pulse = corePulse,
                modifier = Modifier
                    .width(290.dp)
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            XpBar(progress = animatedProgress)

            Spacer(modifier = Modifier.height(16.dp))

            val displayProgress by animateIntAsState(
                targetValue = animatedProgress.toInt(),
                animationSpec = tween(100, easing = FastOutSlowInEasing),
                label = "ProgressCounter"
            )

            val loadingText = when {
                displayProgress < 15 -> "Initializing..."
                displayProgress < 35 -> "Loading assets..."
                displayProgress < 65 -> "Processing..."
                displayProgress < 85 -> "Finalizing..."
                displayProgress < 100 -> "Almost ready..."
                else -> "Complete!"
            }

            McText(
                text = "$displayProgress%",
                fontSize = 13.sp,
                color = WColors.XpGreen.copy(alpha = 0.95f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            McText(
                text = loadingText,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun NebulaCanvas(drift: Float, modifier: Modifier = Modifier) {
    val blobs = remember { generateNebulaBlobs() }
    Canvas(modifier = modifier.fillMaxSize()) {
        blobs.forEach { blob ->
            val cx = size.width * (0.5f + 0.35f * sin(drift * 2f * PI.toFloat() * blob.speed + blob.phaseX))
            val cy = size.height * (0.5f + 0.28f * cos(drift * 2f * PI.toFloat() * blob.speed * 0.8f + blob.phaseY))
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(blob.color.copy(alpha = blob.alpha), Color.Transparent)
                ),
                radius = size.minDimension * blob.radius,
                center = Offset(cx, cy)
            )
        }
    }
}

@Composable
private fun VortexCanvas(
    time: Float,
    pulse: Float,
    modifier: Modifier = Modifier
) {
    val particles = remember { generateOrbitParticles(34) }
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseR = size.minDimension * 0.34f

        drawCircle(
            brush = Brush.radialGradient(
                listOf(WColors.Primary.copy(alpha = 0.2f * pulse), Color.Transparent)
            ),
            radius = baseR * 1.05f,
            center = center
        )

        RING_COLORS.forEachIndexed { i, color ->
            val r = baseR * (1f + i * 0.16f)
            val rot = time * 360f * (1.25f - i * 0.11f) + i * 24f
            val alpha = (0.5f - i * 0.05f).coerceAtLeast(0.18f)
            val strokeWidth = (2.5f - i * 0.2f).coerceAtLeast(1.2f).dp.toPx()
            rotate(rot, center) {
                drawOval(
                    color = color.copy(alpha = alpha),
                    topLeft = Offset(center.x - r, center.y - r * 0.62f),
                    size = Size(r * 2f, r * 1.24f),
                    style = Stroke(width = strokeWidth)
                )
            }
        }

        drawCircle(
            color = WColors.SecondaryLight.copy(alpha = 0.85f * pulse),
            radius = 3.dp.toPx(),
            center = center
        )
        drawCircle(
            color = WColors.PrimaryLight.copy(alpha = 0.4f * pulse),
            radius = 8.dp.toPx(),
            center = center
        )

        particles.forEach { p ->
            val r = baseR * 1.35f * p.radiusFrac
            val angle = (time * 360f * p.speed + p.phase) * PI.toFloat() / 180f
            val pos = Offset(
                center.x + cos(angle) * r,
                center.y + sin(angle) * r
            )
            val inward = 1f - p.radiusFrac
            drawCircle(
                color = WColors.SecondaryLight.copy(alpha = 0.35f + 0.4f * inward),
                radius = (1.5f + 1.5f * inward).dp.toPx(),
                center = pos
            )
        }
    }
}

@Composable
private fun McText(
    text: String,
    fontSize: TextUnit = 14.sp,
    color: Color = Color.White,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    outlineColor: Color = Color(0xFF0B0714)
) {
    val density = LocalDensity.current
    val outlinePx = with(density) { 1.6.dp.toPx() }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val offsets = listOf(
            Offset(-outlinePx, 0f),
            Offset(outlinePx, 0f),
            Offset(0f, -outlinePx),
            Offset(0f, outlinePx)
        )
        offsets.forEach { o ->
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = McFont,
                    fontSize = fontSize,
                    color = outlineColor,
                    textAlign = textAlign
                ),
                modifier = Modifier.offset(
                    x = with(density) { o.x.toDp() },
                    y = with(density) { o.y.toDp() }
                )
            )
        }
        Text(
            text = text,
            style = TextStyle(
                fontFamily = McFont,
                fontSize = fontSize,
                color = color,
                textAlign = textAlign
            )
        )
    }
}

@Composable
private fun XpBar(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.width(240.dp).height(12.dp)) {
        val w = size.width
        val h = size.height
        val radius = CornerRadius(2.dp.toPx())
        drawRoundRect(
            color = WColors.XpTrack,
            size = Size(w, h),
            cornerRadius = radius
        )
        val fill = (progress / 100f).coerceIn(0f, 1f)
        val fillW = w * fill
        if (fillW > 0f) {
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(WColors.XpGreen, WColors.XpGreenDark),
                    startY = 0f,
                    endY = h
                ),
                size = Size(fillW, h),
                cornerRadius = radius
            )
            drawRect(
                color = Color.White.copy(alpha = 0.3f),
                size = Size(fillW, h * 0.3f)
            )
            for (i in 1 until 10) {
                val x = w * i / 10f
                if (x <= fillW) {
                    drawLine(
                        color = WColors.XpTrack.copy(alpha = 0.7f),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1f
                    )
                }
            }
        }
    }
}
