package com.voidclient.client.router.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voidclient.client.ui.component.VCBottomNavBar
import com.voidclient.client.ui.component.VCTab
import com.voidclient.client.ui.component.VideoBackground
import com.voidclient.client.viewmodel.MainScreenViewModel

@Immutable
enum class AppTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Rounded.Home),
    Play("Play", Icons.Rounded.PlayArrow),
    Modules("Modules", Icons.Rounded.Extension),
    Settings("Settings", Icons.Rounded.Settings)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen() {
    val vm: MainScreenViewModel = viewModel()
    val selectedTab by vm.selectedPage.collectAsStateWithLifecycle()
    val tabs = remember { AppTab.entries.toList().map { VCTab(it.label, it.icon) } }

    VideoBackground(
        modifier = Modifier.fillMaxSize(),
        overlayColor = Color(0x4D000000)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        slideInVertically(
                            initialOffsetY = { it / 10 },
                            animationSpec = tween(320, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(220)) togetherWith
                                slideOutVertically(
                                    targetOffsetY = { -it / 12 },
                                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                                ) + fadeOut(tween(200))
                    },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        AppTab.Home -> HomePageContent()
                        AppTab.Play -> PlayPageContent()
                        AppTab.Modules -> ModulesPageContent()
                        AppTab.Settings -> SettingsPageContent()
                    }
                }
            }
            VCBottomNavBar(
                tabs = tabs,
                selected = tabs.first { it.label == selectedTab.label },
                onSelect = { tab ->
                    val page = AppTab.entries.firstOrNull { it.label == tab.label } ?: return@VCBottomNavBar
                    if (selectedTab != page) vm.selectPage(page)
                }
            )
        }
    }
}
