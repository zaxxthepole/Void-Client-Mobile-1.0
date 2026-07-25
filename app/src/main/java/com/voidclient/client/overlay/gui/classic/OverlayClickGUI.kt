package com.voidclient.client.overlay.gui.classic

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidclient.client.R
import com.voidclient.client.game.ModuleCategory
import com.voidclient.client.game.ModuleContent
import com.voidclient.client.overlay.OverlayManager
import com.voidclient.client.overlay.OverlayWindow

private val DarkBackground = Color(0xFF0A0A0A)
private val SidebarBackground = Color(0xFF1A1212)
private val HeaderBackground = Color(0xFF161212)
private val AccentPrimary = Color(0xFFE63946)
private val TextPrimary = Color(0xFFE8E8E8)
private val TextSecondary = Color(0xFFB0B0B0)
private val ButtonBackground = Color(0xFF251A1A)

class OverlayClickGUI : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            if (Build.VERSION.SDK_INT >= 31) blurBehindRadius = 20
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            dimAmount = 0.7f
            windowAnimations = android.R.style.Animation_Dialog
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var selectedModuleCategory by mutableStateOf(ModuleCategory.Combat)

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val snackbarHostState = remember { SnackbarHostState() }

        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xD0000000))
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) {
                    OverlayManager.dismissOverlayWindow(this)
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(600.dp)
                    .height(340.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0F0A0A),
                                Color(0xFF121010)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) {}
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HeaderBar(
                        onDiscord = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://discord.gg/N2Gejr8Fbp")
                                )
                            )
                        },
                        onWebsite = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://wclient.neocities.org/")
                                )
                            )
                        },
                        onClose = { OverlayManager.dismissOverlayWindow(this@OverlayClickGUI) }
                    )
                    MainArea(snackbarHostState)
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }

    @Composable
    private fun HeaderBar(
        onDiscord: () -> Unit,
        onWebsite: () -> Unit,
        onClose: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(HeaderBackground)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "WClient",
                    color = AccentPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onDiscord) {
                    Icon(
                        painter = painterResource(R.drawable.ic_discord),
                        contentDescription = "Discord",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onWebsite) {
                    Icon(
                        painter = painterResource(R.drawable.ic_web),
                        contentDescription = "Website",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun MainArea(snackbarHostState: SnackbarHostState) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategorySidebar()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                AnimatedContent(
                    targetState = selectedModuleCategory,
                    transitionSpec = {
                        fadeIn(tween(200)) + slideInHorizontally { it / 3 } togetherWith
                                fadeOut(tween(200)) + slideOutHorizontally { -it / 3 }
                    },
                    label = "CategoryContent"
                ) { category ->
                    if (category == ModuleCategory.Config) {
                        ConfigurationScreen(snackbarHostState = snackbarHostState)
                    } else {
                        ModuleContent(category)
                    }
                }
            }
        }
    }

    @Composable
    private fun CategorySidebar() {
        val categories = remember { ModuleCategory.entries }

        LazyColumn(
            modifier = Modifier
                .width(68.dp)
                .fillMaxHeight()
                .background(SidebarBackground, RoundedCornerShape(12.dp))
                .padding(vertical = 12.dp, horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(categories.size) { index ->
                val category = categories[index]
                CategoryIcon(
                    category = category,
                    isSelected = selectedModuleCategory == category,
                    onClick = { selectedModuleCategory = category }
                )
            }
        }
    }

    @Composable
    private fun CategoryIcon(
        category: ModuleCategory,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        val scale by animateFloatAsState(
            targetValue = if (isSelected) 1.05f else 1f,
            animationSpec = spring(dampingRatio = 0.7f),
            label = "catScale"
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.clickable { onClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .scale(scale)
                    .background(
                        if (isSelected) ButtonBackground else Color.Transparent,
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(category.iconResId),
                    contentDescription = category.name,
                    tint = if (isSelected) AccentPrimary else Color(0xFF666666),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = category.name,
                color = if (isSelected) AccentPrimary else Color(0xFF666666),
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}