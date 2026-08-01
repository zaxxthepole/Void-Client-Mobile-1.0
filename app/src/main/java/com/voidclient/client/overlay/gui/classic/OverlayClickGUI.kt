package com.voidclient.client.overlay.gui.classic

import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voidclient.client.game.ModuleCategory
import com.voidclient.client.game.ModuleManager
import com.voidclient.client.overlay.OverlayManager
import com.voidclient.client.overlay.OverlayWindow
import com.voidclient.client.ui.component.VCModuleCard
import com.voidclient.client.ui.component.VCSearchBar
import com.voidclient.client.ui.theme.Fade
import com.voidclient.client.ui.theme.Slide
import com.voidclient.client.ui.theme.WColors

class OverlayClickGUI : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            if (Build.VERSION.SDK_INT >= 31) blurBehindRadius = 20
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            dimAmount = 0.65f
            windowAnimations = android.R.style.Animation_Dialog
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var selectedModuleCategory by mutableStateOf<ModuleCategory?>(null)
    private var query by mutableStateOf("")

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    override fun Content() {
        val configuration = LocalConfiguration.current
        val maxPanelHeight = (configuration.screenHeightDp * 0.6f).dp
        val snackbarHostState = remember { SnackbarHostState() }
        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) { visible = true }

        val dimAlpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = Fade,
            label = "dimAlpha"
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f * dimAlpha))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    OverlayManager.dismissOverlayWindow(this@OverlayClickGUI)
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = Slide
                ) + fadeIn(Fade),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = Slide
                ) + fadeOut(Fade),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                OverlayPanel(
                    maxPanelHeight = maxPanelHeight,
                    snackbarHostState = snackbarHostState,
                    selectedCategory = selectedModuleCategory,
                    onCategorySelect = { selectedModuleCategory = it },
                    query = query,
                    onQueryChange = { query = it },
                    onClose = { OverlayManager.dismissOverlayWindow(this@OverlayClickGUI) }
                )
            }
        }
    }

    @Composable
    private fun OverlayPanel(
        maxPanelHeight: androidx.compose.ui.unit.Dp,
        snackbarHostState: SnackbarHostState,
        selectedCategory: ModuleCategory?,
        onCategorySelect: (ModuleCategory?) -> Unit,
        query: String,
        onQueryChange: (String) -> Unit,
        onClose: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .heightIn(max = maxPanelHeight)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            WColors.Surface,
                            WColors.SurfaceVariant
                        )
                    ),
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(WColors.BorderLight)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 6.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Voidclient",
                        style = MaterialTheme.typography.titleLarge,
                        color = WColors.PrimaryLight,
                        fontWeight = FontWeight.Bold
                    )
                    val activeCount = remember { ModuleManager.modules.count { it.isEnabled } }
                    Text(
                        "$activeCount module(s) active",
                        style = MaterialTheme.typography.bodySmall,
                        color = WColors.OnSurfaceVariant
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = WColors.OnSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VCSearchBar(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = "Search modules..."
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onCategorySelect(null) },
                        label = { Text("All") },
                        colors = chipColors(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ModuleCategory.entries.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategorySelect(category) },
                            label = { Text(category.displayName) },
                            colors = chipColors(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            val modules = remember {
                ModuleManager.modules.filter { !it.private }
            }
            val filtered = remember(modules, query, selectedCategory) {
                modules.filter { module ->
                    val categoryMatches = selectedCategory == null || module.category == selectedCategory
                    val queryMatches = query.isBlank() || module.name.contains(query, ignoreCase = true)
                    categoryMatches && queryMatches
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedCategory == ModuleCategory.Config) {
                    item {
                        ConfigurationScreen(snackbarHostState = snackbarHostState)
                    }
                } else {
                    items(filtered.size) { index ->
                        VCModuleCard(module = filtered[index])
                    }
                    if (filtered.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No modules found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WColors.OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tap a module to configure",
                    style = MaterialTheme.typography.labelSmall,
                    color = WColors.OnSurfaceVariant
                )
                val activeCount = remember { ModuleManager.modules.count { it.isEnabled } }
                Text(
                    "$activeCount active",
                    style = MaterialTheme.typography.labelSmall,
                    color = WColors.PrimaryLight,
                    fontWeight = FontWeight.SemiBold
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp)
            )
        }
    }

    @Composable
    private fun chipColors() = FilterChipDefaults.filterChipColors(
        containerColor = WColors.Surface,
        selectedContainerColor = WColors.Primary.copy(alpha = 0.2f),
        labelColor = WColors.OnSurfaceVariant,
        selectedLabelColor = WColors.PrimaryLight
    )
}
