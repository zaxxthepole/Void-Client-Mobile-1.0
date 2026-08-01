package com.voidclient.client.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

val SpringSoft: SpringSpec<Float> = spring(
    dampingRatio = 0.8f,
    stiffness = Spring.StiffnessMedium
)

val SpringBouncy: SpringSpec<Float> = spring(
    dampingRatio = 0.5f,
    stiffness = Spring.StiffnessMedium
)

val Fade: TweenSpec<Float> = tween(durationMillis = 220)

val Slide: TweenSpec<Float> = tween(
    durationMillis = 320,
    easing = FastOutSlowInEasing
)
