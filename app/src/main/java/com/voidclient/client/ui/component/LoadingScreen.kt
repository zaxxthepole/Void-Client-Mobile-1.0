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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
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

private val V_MASK = listOf(
    listOf(1, 0, 0, 0, 0, 0, 0, 0, 1),
    listOf(0, 1, 0, 0, 0, 0, 0, 1, 0),
    listOf(0, 0, 1, 0, 0, 0, 1, 0, 0),
    listOf(0, 0, 0, 1, 0, 1, 0, 0, 0),
    listOf(0, 0, 0, 0, 1, 0, 0, 0, 0),
    listOf(0, 0, 0, 1, 0, 1, 0, 0, 0),
    listOf(0, 0, 1, 0, 0, 0, 1, 0, 0)
)

private data class Star(val x: Float, val y: Float, val size: Float, val phase: Float, val brightness: Float)

private fun generateStars(count: Int): List<Star> {
    val random = Random(1337)
    return List(count) {
        Star(
            x = random.nextFloat(),
            y = random.nextFloat(),
            size = 1.5f + random.nextFloat() * 2f,
            phase = random.nextFloat() * 6.2832f,
            brightness = 0.3f + random.nextFloat() * 0.7f
        )
    }
}

private fun Color.darker(factor: Float): Color =
    Color(red * factor, green * factor, blue * factor, alpha)

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

    val starPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(5000, easing = LinearEasing)),
        label = "StarPhase"
    )

    val cubeAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(7000, easing = LinearEasing)),
        label = "CubeAngle"
    )

    val cubeBob by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CubeBob"
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

    val breathe by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Breathe"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WColors.Background)
    ) {
        PixelStarfield(phase = starPhase)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(Color.Transparent, WColors.Background.copy(alpha = 0.6f))
                    )
                )
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VoxelVLogo(
                progress = animatedProgress,
                breathe = breathe,
                modifier = Modifier
                    .width(220.dp)
                    .height(172.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))

            Box(
                modifier = Modifier.drawBehind {
                    drawCircle(
                        color = WColors.Primary.copy(alpha = glow * 0.35f),
                        radius = size.width * 0.5f,
                        center = center
                    )
                    drawCircle(
                        color = WColors.Secondary.copy(alpha = glow * 0.18f),
                        radius = size.width * 0.75f,
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

            Spacer(modifier = Modifier.height(26.dp))

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

        GrassCube(
            angleDeg = cubeAngle,
            bob = cubeBob,
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        )
    }
}

@Composable
private fun PixelStarfield(phase: Float, modifier: Modifier = Modifier) {
    val stars = remember { generateStars(45) }
    Canvas(modifier = modifier.fillMaxSize()) {
        stars.forEach { star ->
            val twinkle = 0.5f + 0.5f * sin(phase * 2f * PI.toFloat() * 0.8f + star.phase)
            val alpha = star.brightness * (0.3f + 0.7f * twinkle)
            drawRect(
                color = Color.White.copy(alpha = alpha),
                topLeft = Offset(star.x * size.width, star.y * size.height),
                size = Size(star.size, star.size)
            )
        }
    }
}

@Composable
private fun VoxelVLogo(
    progress: Float,
    breathe: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.scale(breathe)) {
        val cols = V_MASK[0].size
        val rows = V_MASK.size
        val marginX = size.width * 0.02f
        val cell = (size.width * 0.96f) / (cols + 0.1f)
        val blockW = cell * 0.78f
        val blockH = blockW * 1.05f
        val gap = cell - blockW
        val totalH = rows * cell
        val startY = (size.height - totalH) / 2f
        val assembly = ((progress - 5f) / 45f).coerceIn(0f, 1f)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (V_MASK[r][c] == 0) continue
                val idx = (rows - 1 - r) * cols + c
                val start = 0.8f * idx / 62f
                val local = ((assembly - start) / 0.2f).coerceIn(0f, 1f)
                if (local <= 0f) continue
                val scale = local + 0.35f * sin(local * PI.toFloat()) * (1f - local)
                drawVoxelBlock(
                    x = marginX + c * cell + gap / 2f,
                    y = startY + r * cell + gap / 2f,
                    w = blockW,
                    h = blockH,
                    scale = scale,
                    alpha = local
                )
            }
        }
    }
}

private fun DrawScope.drawVoxelBlock(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    scale: Float,
    alpha: Float
) {
    val faceColor = WColors.Primary
    val topColor = WColors.PrimaryLight
    val sideColor = WColors.PrimaryDark
    val outlineColor = Color(0xFF1F0A38)

    val centerX = x + w / 2f
    val centerY = y + h / 2f
    val w2 = w * scale
    val h2 = h * scale
    val x0 = centerX - w2 / 2f
    val y0 = centerY - h2 / 2f
    val th = h2 * 0.22f
    val sd = w2 * 0.2f

    drawRect(
        color = topColor.copy(alpha = alpha),
        topLeft = Offset(x0, y0 - th),
        size = Size(w2, th)
    )
    val topHalfW = w2 / 2f
    val topHalfH = th / 2f
    drawRect(
        color = topColor.darker(0.88f).copy(alpha = alpha),
        topLeft = Offset(x0, y0 - th),
        size = Size(topHalfW, topHalfH)
    )
    drawRect(
        color = topColor.darker(0.88f).copy(alpha = alpha),
        topLeft = Offset(x0 + topHalfW, y0 - th + topHalfH),
        size = Size(topHalfW, topHalfH)
    )

    drawRect(
        color = sideColor.copy(alpha = alpha),
        topLeft = Offset(x0 - sd, y0),
        size = Size(sd, h2)
    )

    drawRect(
        color = faceColor.copy(alpha = alpha),
        topLeft = Offset(x0, y0),
        size = Size(w2, h2)
    )
    val faceHalfW = w2 / 2f
    val faceHalfH = h2 / 2f
    drawRect(
        color = faceColor.darker(0.92f).copy(alpha = alpha),
        topLeft = Offset(x0, y0),
        size = Size(faceHalfW, faceHalfH)
    )
    drawRect(
        color = faceColor.darker(0.92f).copy(alpha = alpha),
        topLeft = Offset(x0 + faceHalfW, y0 + faceHalfH),
        size = Size(faceHalfW, faceHalfH)
    )

    drawRect(
        color = sideColor.copy(alpha = alpha),
        topLeft = Offset(x0 + w2, y0),
        size = Size(sd, h2)
    )

    drawRect(
        color = outlineColor.copy(alpha = alpha * 0.8f),
        topLeft = Offset(x0 - 1.5f, y0 - th - 1.5f),
        size = Size(w2 + sd + 3f, h2 + th + 3f),
        style = Stroke(width = 1.5f)
    )
}

private data class CubeFace(
    val verts: List<FloatArray>,
    val normal: FloatArray,
    val isTop: Boolean
)

private val CUBE_FACES = listOf(
    CubeFace(
        verts = listOf(
            floatArrayOf(-1f, 1f, -1f),
            floatArrayOf(1f, 1f, -1f),
            floatArrayOf(1f, 1f, 1f),
            floatArrayOf(-1f, 1f, 1f)
        ),
        normal = floatArrayOf(0f, 1f, 0f),
        isTop = true
    ),
    CubeFace(
        verts = listOf(
            floatArrayOf(-1f, -1f, 1f),
            floatArrayOf(1f, -1f, 1f),
            floatArrayOf(1f, 1f, 1f),
            floatArrayOf(-1f, 1f, 1f)
        ),
        normal = floatArrayOf(0f, 0f, 1f),
        isTop = false
    ),
    CubeFace(
        verts = listOf(
            floatArrayOf(1f, -1f, -1f),
            floatArrayOf(1f, -1f, 1f),
            floatArrayOf(1f, 1f, 1f),
            floatArrayOf(1f, 1f, -1f)
        ),
        normal = floatArrayOf(1f, 0f, 0f),
        isTop = false
    ),
    CubeFace(
        verts = listOf(
            floatArrayOf(-1f, -1f, 1f),
            floatArrayOf(-1f, -1f, -1f),
            floatArrayOf(-1f, 1f, -1f),
            floatArrayOf(-1f, 1f, 1f)
        ),
        normal = floatArrayOf(-1f, 0f, 0f),
        isTop = false
    )
)

@Composable
private fun GrassCube(
    angleDeg: Float,
    bob: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val bobPx = sin(bob * 2f * PI.toFloat()) * size.minDimension * 0.05f
        drawGrassCube(
            angleRad = angleDeg * PI.toFloat() / 180f,
            bobPx = bobPx,
            size = size.minDimension
        )
    }
}

private fun DrawScope.drawGrassCube(angleRad: Float, bobPx: Float, size: Float) {
    val half = size * 0.30f
    val view = floatArrayOf(0.577f, 0.577f, 0.577f)
    val cosA = cos(angleRad.toDouble()).toFloat()
    val sinA = sin(angleRad.toDouble()).toFloat()

    fun rotate(v: FloatArray): FloatArray {
        val x = v[0] * cosA - v[2] * sinA
        val z = v[0] * sinA + v[2] * cosA
        return floatArrayOf(x, v[1], z)
    }

    data class VisibleFace(val face: CubeFace, val points: List<Offset>, val depth: Float)

    val visible = CUBE_FACES.mapNotNull { face ->
        val n = rotate(face.normal)
        val dot = n[0] * view[0] + n[1] * view[1] + n[2] * view[2]
        if (dot <= 0.01f) return@mapNotNull null
        val rotated = face.verts.map { rotate(it) }
        val depth = rotated.fold(0f) { acc, v -> acc + v[2] } / rotated.size
        val points = rotated.map { p ->
            val sx = (p[0] - p[2]) * 0.866f
            val sy = (p[0] + p[2]) * 0.5f - p[1]
            Offset(sx * half, sy * half + bobPx)
        }
        VisibleFace(face, points, depth)
    }.sortedByDescending { it.depth }

    visible.forEach { vf ->
        val path = Path().apply {
            moveTo(vf.points[0].x, vf.points[0].y)
            vf.points.forEach { lineTo(it.x, it.y) }
            close()
        }
        if (vf.face.isTop) {
            drawTexturedFace(
                path = path,
                base = WColors.Grass,
                dark = WColors.GrassDark,
                edge = Color(0xFF3E6B26),
                strip = null
            )
        } else {
            drawTexturedFace(
                path = path,
                base = WColors.Dirt,
                dark = WColors.DirtDark,
                edge = Color(0xFF3E2510),
                strip = WColors.Grass
            )
        }
        drawPath(path, Color.Black.copy(alpha = 0.35f), style = Stroke(width = 1.5f))
    }
}

private fun DrawScope.drawTexturedFace(
    path: Path,
    base: Color,
    dark: Color,
    edge: Color,
    strip: Color?
) {
    drawPath(path, base)
    clipPath(path) {
        val bounds = path.getBounds()
        val cellW = bounds.width / 2f
        val cellH = bounds.height / 2f
        for (r in 0 until 2) {
            for (c in 0 until 2) {
                if ((r + c) % 2 == 1) {
                    drawRect(
                        color = dark,
                        topLeft = Offset(bounds.left + c * cellW, bounds.top + r * cellH),
                        size = Size(cellW, cellH)
                    )
                }
            }
        }
        if (strip != null) {
            drawRect(
                color = strip,
                topLeft = Offset(bounds.left, bounds.top),
                size = Size(bounds.width, bounds.height * 0.16f)
            )
        }
        val midX = bounds.left + cellW
        val midY = bounds.top + cellH
        drawLine(edge, Offset(midX, bounds.top), Offset(midX, bounds.bottom), strokeWidth = 1f)
        drawLine(edge, Offset(bounds.left, midY), Offset(bounds.right, midY), strokeWidth = 1f)
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
