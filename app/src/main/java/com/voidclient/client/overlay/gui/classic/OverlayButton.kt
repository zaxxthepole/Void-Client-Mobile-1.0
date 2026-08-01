package com.voidclient.client.overlay.gui.classic

import android.content.res.Configuration
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.voidclient.client.R
import com.voidclient.client.overlay.OverlayManager
import com.voidclient.client.overlay.OverlayWindow
import com.voidclient.client.ui.theme.WColors
import kotlin.math.min

class OverlayButton : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            windowAnimations = android.R.style.Animation_Toast
            x = 0
            y = 100
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private val overlayClickGUI by lazy { OverlayClickGUI() }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val width = context.resources.displayMetrics.widthPixels
        val height = context.resources.displayMetrics.heightPixels
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        LaunchedEffect(isLandscape) {
            _layoutParams.x = min(width, _layoutParams.x)
            _layoutParams.y = min(height, _layoutParams.y)
            windowManager.updateViewLayout(composeView, _layoutParams)
        }

        Box(
            modifier = Modifier
                .padding(5.dp)
                .size(54.dp)
                .pointerInput(Unit) {
                    detectDragGestures { _, drag ->
                        _layoutParams.x += drag.x.toInt()
                        _layoutParams.y += drag.y.toInt()
                        windowManager.updateViewLayout(composeView, _layoutParams)
                    }
                }
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = WColors.Primary.copy(alpha = 0.4f),
                    spotColor = WColors.Primary.copy(alpha = 0.5f)
                )
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            WColors.SurfaceVariant,
                            WColors.Surface
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            WColors.Primary.copy(alpha = 0.7f),
                            WColors.Accent.copy(alpha = 0.3f),
                            WColors.Primary.copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(
                    onClick = { OverlayManager.showOverlayWindow(overlayClickGUI) },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.v_overlay_icon),
                contentDescription = "Overlay Button Icon",
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
