package com.voidclient.client.game

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.voidclient.client.R

enum class ModuleCategory(
    @DrawableRes val iconResId: Int,
    @StringRes val labelResId: Int,
    val displayName: String
) {
    Combat(
        iconResId = R.drawable.swords_24px,
        labelResId = R.string.combat,
        displayName = "Combat"
    ),
    Motion(
        iconResId = R.drawable.sprint_24px,
        labelResId = R.string.motion,
        displayName = "Motion"
    ),
    Visual(
        iconResId = R.drawable.view_in_ar_24px,
        labelResId = R.string.visual,
        displayName = "Visual"
    ),
    Config(
        iconResId = R.drawable.ic_settings,
        labelResId = R.string.config,
        displayName = "Config"
    ),
}
