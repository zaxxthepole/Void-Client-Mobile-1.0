package com.voidclient.client.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.voidclient.client.R
import com.voidclient.client.game.BoolValue
import com.voidclient.client.game.EnumValue
import com.voidclient.client.game.FloatValue
import com.voidclient.client.game.IntValue
import com.voidclient.client.game.ListValue
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import com.voidclient.client.game.StringValue
import com.voidclient.client.overlay.OverlayManager
import com.voidclient.client.ui.theme.SpringBouncy
import com.voidclient.client.ui.theme.WColors
import com.voidclient.client.util.translatedSelf
import kotlin.math.roundToInt

private fun ModuleCategory.accentColor(): Color = when (this) {
    ModuleCategory.Combat -> Color(0xFFFF5D73)
    ModuleCategory.Motion -> Color(0xFF38BDF8)
    ModuleCategory.Visual -> Color(0xFF4ADE80)
    ModuleCategory.Misc -> Color(0xFFFBBF24)
    ModuleCategory.Config -> WColors.Primary
}

@Composable
fun VCModuleCard(
    module: Module,
    modifier: Modifier = Modifier
) {
    val values = module.values
    val accent = module.category.accentColor()

    val bg by animateColorAsState(
        targetValue = if (module.isExpanded) WColors.SurfaceVariant else WColors.Surface,
        animationSpec = tween(220),
        label = "moduleBg"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(
            1.dp,
            accent.copy(alpha = if (module.isEnabled) 0.35f else 0.12f)
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(accent.copy(alpha = if (module.isEnabled) 1f else 0.3f))
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(top = 10.dp, bottom = 10.dp, end = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClick = { module.isExpanded = !module.isExpanded },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        )
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        module.name.translatedSelf,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (module.isEnabled) accent else WColors.OnSurface
                    )
                    Spacer(Modifier.weight(1f))
                    VCToggleSwitch(
                        checked = module.isEnabled,
                        onCheckedChange = { module.isEnabled = it }
                    )
                }

                if (module.isExpanded) {
                    values.fastForEachIndexed { index, value ->
                        StaggeredValue(index) {
                            when (value) {
                                is BoolValue -> BoolValueContent(value)
                                is FloatValue -> FloatValueContent(value)
                                is IntValue -> IntValueContent(value)
                                is ListValue -> ChoiceValueContent(value)
                                is EnumValue<*> -> EnumValueContent(value)
                                is StringValue -> StringValueContent(value)
                            }
                        }
                    }
                    StaggeredValue(values.size) {
                        ShortcutContent(module)
                    }
                }
            }
        }
    }
}

@Composable
private fun StaggeredValue(
    index: Int,
    content: @Composable () -> Unit
) {
    val state = remember { MutableTransitionState<Boolean>(false) }
    state.targetState = true
    val delay = (index * 28).coerceAtMost(220)
    AnimatedVisibility(
        visibleState = state,
        enter = fadeIn(tween(200, delayMillis = delay)) + expandVertically(
            animationSpec = tween(220, delayMillis = delay)
        )
    ) {
        content()
    }
}

@Composable
private fun ChoiceValueContent(value: ListValue) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = WColors.OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            value.listItems.forEach { item ->
                ElevatedFilterChip(
                    selected = value.value == item,
                    onClick = { if (value.value != item) value.value = item },
                    label = { Text(item.name.translatedSelf) },
                    modifier = Modifier.height(32.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = WColors.Surface,
                        selectedContainerColor = WColors.Primary,
                        labelColor = WColors.OnSurfaceVariant,
                        selectedLabelColor = WColors.OnPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = value.value == item,
                        borderColor = WColors.Border,
                        selectedBorderColor = WColors.Primary
                    )
                )
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun FloatValueContent(value: FloatValue) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Row(modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                value.name.translatedSelf,
                style = MaterialTheme.typography.bodyMedium,
                color = WColors.OnSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            Text(
                String.format("%.2f", value.value),
                style = MaterialTheme.typography.bodyMedium,
                color = WColors.Primary
            )
        }

        val colors = SliderDefaults.colors(
            thumbColor = WColors.Primary,
            activeTrackColor = WColors.Primary,
            activeTickColor = WColors.Primary,
            inactiveTickColor = WColors.SliderTrack,
            inactiveTrackColor = WColors.SliderTrack,
            disabledThumbColor = WColors.OnSurfaceVariant,
            disabledActiveTrackColor = WColors.SliderTrack,
            disabledActiveTickColor = WColors.SliderTrack,
            disabledInactiveTrackColor = WColors.Surface,
            disabledInactiveTickColor = WColors.Surface
        )

        val animated by animateFloatAsState(
            targetValue = value.value,
            animationSpec = SpringBouncy,
            label = "floatSlider"
        )

        Slider(
            value = animated,
            onValueChange = {
                val rounded = ((it * 100.0).roundToInt() / 100.0).toFloat()
                if (value.value != rounded) value.value = rounded
            },
            valueRange = value.range,
            colors = colors
        )
    }
}

@Composable
private fun IntValueContent(value: IntValue) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Row(modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                value.name.translatedSelf,
                style = MaterialTheme.typography.bodyMedium,
                color = WColors.OnSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            Text(
                value.value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = WColors.Primary
            )
        }

        val colors = SliderDefaults.colors(
            thumbColor = WColors.Primary,
            activeTrackColor = WColors.Primary,
            activeTickColor = WColors.Primary,
            inactiveTickColor = WColors.SliderTrack,
            inactiveTrackColor = WColors.SliderTrack,
            disabledThumbColor = WColors.OnSurfaceVariant,
            disabledActiveTrackColor = WColors.SliderTrack,
            disabledActiveTickColor = WColors.SliderTrack,
            disabledInactiveTrackColor = WColors.Surface,
            disabledInactiveTickColor = WColors.Surface
        )

        val animated by animateFloatAsState(
            targetValue = value.value.toFloat(),
            animationSpec = SpringBouncy,
            label = "intSlider"
        )

        Slider(
            value = animated,
            onValueChange = {
                val next = it.roundToInt()
                if (value.value != next) value.value = next
            },
            valueRange = value.range.toFloatRange(),
            colors = colors
        )
    }
}

@Composable
private fun BoolValueContent(value: BoolValue) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(
                value = value.value,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = true
            ) { value.value = it }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = WColors.OnSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = value.value,
            onCheckedChange = null,
            enabled = true,
            colors = CheckboxDefaults.colors(
                uncheckedColor = WColors.OnSurfaceVariant,
                checkedColor = WColors.Primary,
                checkmarkColor = WColors.OnPrimary,
                disabledCheckedColor = WColors.OnSurfaceVariant,
                disabledUncheckedColor = WColors.SurfaceVariant,
                disabledIndeterminateColor = WColors.OnSurfaceVariant
            )
        )
    }
}

@Composable
private fun ShortcutContent(module: Module) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(
                value = module.isShortcutDisplayed,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = true
            ) {
                module.isShortcutDisplayed = it
                if (it) OverlayManager.showOverlayWindow(module.overlayShortcutButton)
                else OverlayManager.dismissOverlayWindow(module.overlayShortcutButton)
            }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.shortcut),
            style = MaterialTheme.typography.bodyMedium,
            color = WColors.OnSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = module.isShortcutDisplayed,
            onCheckedChange = null,
            enabled = true,
            colors = CheckboxDefaults.colors(
                uncheckedColor = WColors.OnSurfaceVariant,
                checkedColor = WColors.Primary,
                checkmarkColor = WColors.OnPrimary,
                disabledCheckedColor = WColors.OnSurfaceVariant,
                disabledUncheckedColor = WColors.SurfaceVariant,
                disabledIndeterminateColor = WColors.OnSurfaceVariant
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : Enum<T>> EnumValueContent(value: EnumValue<T>) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = WColors.OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            value.enumClass.enumConstants?.forEach { option ->
                ElevatedFilterChip(
                    selected = value.value == option,
                    onClick = { if (value.value != option) value.value = option },
                    label = { Text(option.name.translatedSelf) },
                    modifier = Modifier.height(32.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = WColors.Surface,
                        selectedContainerColor = WColors.Primary,
                        labelColor = WColors.OnSurfaceVariant,
                        selectedLabelColor = WColors.OnPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = value.value == option,
                        borderColor = WColors.Border,
                        selectedBorderColor = WColors.Primary
                    )
                )
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StringValueContent(value: StringValue) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = WColors.OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value.value,
            onValueChange = { value.value = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WColors.Primary,
                unfocusedBorderColor = WColors.Border,
                focusedTextColor = WColors.OnSurface,
                unfocusedTextColor = WColors.OnSurface,
                cursorColor = WColors.Primary,
                disabledBorderColor = WColors.SurfaceVariant,
                disabledTextColor = WColors.OnSurfaceVariant,
                errorBorderColor = WColors.Error,
                errorTextColor = WColors.OnSurface,
                errorCursorColor = WColors.Error
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

private fun IntRange.toFloatRange() = first.toFloat()..last.toFloat()
