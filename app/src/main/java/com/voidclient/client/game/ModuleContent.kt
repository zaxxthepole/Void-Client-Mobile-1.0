package com.voidclient.client.game

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.voidclient.client.R
import com.voidclient.client.overlay.OverlayManager
import com.voidclient.client.util.translatedSelf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt


private val DarkBackground = Color(0xFF0E0A1A)
private val CardBackground = Color(0xFF121220)
private val CardBackgroundExpanded = Color(0xFF1A1228)
private val AccentPrimary = Color(0xFF7B2FBE)
private val AccentSecondary = Color(0xFFA855F7)
private val AccentDark = Color(0xFF5B1F8E)
private val TextPrimary = Color(0xFFE8E8E8)
private val TextSecondary = Color(0xFFB0B0B0)
private val BorderColor = Color(0xFF2A1A30)
private val BorderColorActive = Color(0xFF3A2244)
private val ErrorRed = Color(0xFFCF222E)

private val moduleCache = HashMap<ModuleCategory, List<Module>>()

private fun fetchCachedModules(category: ModuleCategory): List<Module> {
    val cached = moduleCache[category] ?: ModuleManager.modules
        .filter { !it.private && it.category === category }
    moduleCache[category] = cached
    return cached
}

@Composable
fun ModuleContent(moduleCategory: ModuleCategory) {
    var modules: List<Module>? by remember(moduleCategory) { mutableStateOf(moduleCache[moduleCategory]) }

    LaunchedEffect(modules) {
        if (modules == null) withContext(Dispatchers.IO) { modules = fetchCachedModules(moduleCategory) }
    }

    Crossfade(
        targetState = modules,
        animationSpec = tween(durationMillis = 300)
    ) { list ->
        if (list != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(list.size) { i -> ModuleCard(list[i]) }
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentPrimary
                )
            }
        }
    }
}

@Composable
private fun ModuleCard(module: Module) {
    val values = module.values
    val bg by animateColorAsState(
        targetValue = if (module.isExpanded) CardBackgroundExpanded else CardBackground,
        animationSpec = tween(durationMillis = 300),
        label = "moduleBg"
    )

    val elevation by animateFloatAsState(
        targetValue = if (module.isExpanded) 4f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardElevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { module.isExpanded = !module.isExpanded },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    module.name.translatedSelf,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (module.isEnabled) AccentPrimary else TextPrimary
                )
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = module.isEnabled,
                    onCheckedChange = { module.isEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentPrimary,
                        checkedTrackColor = AccentPrimary.copy(alpha = 0.3f),
                        checkedBorderColor = Color.Transparent,
                        uncheckedThumbColor = Color(0xFF353545),
                        uncheckedTrackColor = Color(0xFF1A1A2E),
                        uncheckedBorderColor = BorderColor
                    ),
                    modifier = Modifier
                        .width(52.dp)
                        .height(32.dp)
                )
            }

            if (module.isExpanded) {
                values.fastForEach {
                    when (it) {
                        is BoolValue -> BoolValueContent(it)
                        is FloatValue -> FloatValueContent(it)
                        is IntValue -> IntValueContent(it)
                        is ListValue -> ChoiceValueContent(it)
                        is EnumValue<*> -> EnumValueContent(it)
                        is StringValue -> StringValueContent(it)
                    }
                }
                ShortcutContent(module)
            }
        }
    }
}

@Composable
private fun ChoiceValueContent(value: ListValue) {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            value.listItems.forEach { item ->
                ElevatedFilterChip(
                    selected = value.value == item,
                    onClick = { if (value.value != item) value.value = item },
                    label = { Text(item.name.translatedSelf) },
                    modifier = Modifier.height(32.dp),
                    enabled = true,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF1A1A2E),
                        selectedContainerColor = AccentPrimary,
                        labelColor = TextSecondary,
                        selectedLabelColor = Color.White,
                        disabledContainerColor = Color(0xFF121212),
                        disabledLabelColor = Color(0xFF706080)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = value.value == item,
                        borderColor = BorderColor,
                        selectedBorderColor = AccentPrimary,
                        disabledBorderColor = Color(0xFF1A1A2E),
                        disabledSelectedBorderColor = Color(0xFF1A1A2E)
                    )
                )
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun FloatValueContent(value: FloatValue) {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
        Row(modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                value.name.translatedSelf,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.weight(1f))
            Text(
                String.format("%.2f", value.value),
                style = MaterialTheme.typography.bodyMedium,
                color = AccentPrimary
            )
        }

        val colors = SliderDefaults.colors(
            thumbColor = AccentPrimary,
            activeTrackColor = AccentPrimary,
            activeTickColor = AccentPrimary,
            inactiveTickColor = Color(0xFF2A1A30),
            inactiveTrackColor = Color(0xFF2A1A30),
            disabledThumbColor = Color(0xFF353545),
            disabledActiveTrackColor = Color(0xFF2A1A30),
            disabledActiveTickColor = Color(0xFF2A1A30),
            disabledInactiveTrackColor = Color(0xFF1A1A2E),
            disabledInactiveTickColor = Color(0xFF1A1A2E)
        )

        val animated by animateFloatAsState(
            targetValue = value.value,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "floatSlider"
        )

        Slider(
            value = animated,
            onValueChange = {
                val rounded = ((it * 100.0).roundToInt() / 100.0).toFloat()
                if (value.value != rounded) value.value = rounded
            },
            valueRange = value.range,
            colors = colors,
            enabled = true
        )
    }
}

@Composable
private fun IntValueContent(value: IntValue) {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
        Row(modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                value.name.translatedSelf,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.weight(1f))
            Text(
                value.value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = AccentPrimary
            )
        }

        val colors = SliderDefaults.colors(
            thumbColor = AccentPrimary,
            activeTrackColor = AccentPrimary,
            activeTickColor = AccentPrimary,
            inactiveTickColor = Color(0xFF2A1A30),
            inactiveTrackColor = Color(0xFF2A1A30),
            disabledThumbColor = Color(0xFF353545),
            disabledActiveTrackColor = Color(0xFF2A1A30),
            disabledActiveTickColor = Color(0xFF2A1A30),
            disabledInactiveTrackColor = Color(0xFF1A1A2E),
            disabledInactiveTickColor = Color(0xFF1A1A2E)
        )

        val animated by animateFloatAsState(
            targetValue = value.value.toFloat(),
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "intSlider"
        )

        Slider(
            value = animated,
            onValueChange = {
                val next = it.roundToInt()
                if (value.value != next) value.value = next
            },
            valueRange = value.range.toFloatRange(),
            colors = colors,
            enabled = true
        )
    }
}

@Composable
private fun BoolValueContent(value: BoolValue) {
    Row(
        Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .fillMaxWidth()
            .toggleable(
                value = value.value,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = true
            ) { value.value = it },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = value.value,
            onCheckedChange = null,
            modifier = Modifier.padding(0.dp),
            enabled = true,
            colors = CheckboxDefaults.colors(
                uncheckedColor = Color(0xFF353545),
                checkedColor = AccentPrimary,
                checkmarkColor = Color.White,
                disabledCheckedColor = Color(0xFF353545),
                disabledUncheckedColor = Color(0xFF252535),
                disabledIndeterminateColor = Color(0xFF353545)
            )
        )
    }
}

@Composable
private fun ShortcutContent(module: Module) {
    Row(
        Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
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
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.shortcut),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(Modifier.weight(1f))
        Checkbox(
            checked = module.isShortcutDisplayed,
            onCheckedChange = null,
            modifier = Modifier.padding(0.dp),
            enabled = true,
            colors = CheckboxDefaults.colors(
                uncheckedColor = Color(0xFF353545),
                checkedColor = AccentPrimary,
                checkmarkColor = Color.White,
                disabledCheckedColor = Color(0xFF353545),
                disabledUncheckedColor = Color(0xFF252535),
                disabledIndeterminateColor = Color(0xFF353545)
            )
        )
    }
}

@Composable
private fun <T : Enum<T>> EnumValueContent(value: EnumValue<T>) {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            value.enumClass.enumConstants?.forEach { option ->
                ElevatedFilterChip(
                    selected = value.value == option,
                    onClick = { if (value.value != option) value.value = option },
                    label = { Text(option.name.translatedSelf) },
                    modifier = Modifier.height(32.dp),
                    enabled = true,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF1A1A2E),
                        selectedContainerColor = AccentPrimary,
                        labelColor = TextSecondary,
                        selectedLabelColor = Color.White,
                        disabledContainerColor = Color(0xFF121212),
                        disabledLabelColor = Color(0xFF706080)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = value.value == option,
                        borderColor = BorderColor,
                        selectedBorderColor = AccentPrimary,
                        disabledBorderColor = Color(0xFF1A1A2E),
                        disabledSelectedBorderColor = Color(0xFF1A1A2E)
                    )
                )
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun StringValueContent(value: StringValue) {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
        Text(
            value.name.translatedSelf,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value.value,
            onValueChange = { value.value = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPrimary,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = AccentPrimary,
                disabledBorderColor = Color(0xFF1A1A2E),
                disabledTextColor = Color(0xFF706080),
                errorBorderColor = ErrorRed,
                errorTextColor = TextPrimary,
                errorCursorColor = ErrorRed
            ),
            shape = MaterialTheme.shapes.small
        )
    }
}

private fun IntRange.toFloatRange() = first.toFloat()..last.toFloat()