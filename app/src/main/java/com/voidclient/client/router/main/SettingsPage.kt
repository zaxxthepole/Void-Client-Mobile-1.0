package com.voidclient.client.router.main

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.rounded.Dangerous
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import com.voidclient.client.BuildConfig
import com.voidclient.client.R
import com.voidclient.client.game.ModuleManager
import com.voidclient.client.overlay.OverlayManager
import com.voidclient.client.ui.component.VCCard
import com.voidclient.client.ui.component.VCPrimaryButton
import com.voidclient.client.ui.component.VCSectionHeader
import com.voidclient.client.ui.component.VCSettingsRow
import com.voidclient.client.ui.theme.WColors
import com.voidclient.client.util.LocalSnackbarHostState
import com.voidclient.client.util.SnackbarHostStateScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingsPageContent() {
    var showAbout by rememberSaveable { mutableStateOf(false) }

    AnimatedContent(
        targetState = showAbout,
        transitionSpec = {
            slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it / 3 } + fadeOut()
        },
        label = "settings_about"
    ) { about ->
        if (about) {
            AboutSettingsContent(onBack = { showAbout = false })
        } else {
            SettingsMainContent(onOpenAbout = { showAbout = true })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsMainContent(onOpenAbout: () -> Unit) {
    SnackbarHostStateScope {
        val context = LocalContext.current
        val snackbarHostState = LocalSnackbarHostState.current
        val scope = rememberCoroutineScope()
        val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

        var showOpacityDialog by rememberSaveable { mutableStateOf(false) }
        var showFileNameDialog by rememberSaveable { mutableStateOf(false) }
        var showSelfDestructDialog by rememberSaveable { mutableStateOf(false) }
        var configFileName by rememberSaveable { mutableStateOf("") }
        var isExporting by remember { mutableStateOf(false) }

        var overlayOpacity by remember {
            mutableFloatStateOf(prefs.getFloat("overlay_opacity", 1f))
        }
        var shortcutOpacity by remember {
            mutableFloatStateOf(prefs.getFloat("shortcut_opacity", 1f))
        }

        val importConfigPicker = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                val ok = ModuleManager.importConfigFromFile(context, it)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (ok) "Config imported successfully" else "Failed to import config"
                    )
                }
            }
        }

        suspend fun exportConfig(fileName: String): Result<String> = withContext(Dispatchers.IO) {
            try {
                val configsDir = ModuleManager.getVoidclientConfigsDirectory()
                    ?: return@withContext Result.failure(Exception("Failed to create Voidclient directory"))

                val finalFileName = if (fileName.isBlank()) {
                    val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
                    "config_$timestamp"
                } else {
                    fileName
                }

                val configFile = File(configsDir, "$finalFileName.json")

                val success = ModuleManager.exportConfigToFile(context, configFile.absolutePath)

                if (success) {
                    Result.success(configFile.absolutePath)
                } else {
                    Result.failure(Exception("Failed to export configuration"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.settings),
                            style = MaterialTheme.typography.headlineMedium,
                            color = WColors.OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = WColors.OnSurface
                    )
                )
            },
            bottomBar = { SnackbarHost(LocalSnackbarHostState.current) },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                VCSectionHeader(
                    title = "General",
                    subtitle = "Overlay appearance"
                )

                VCCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                        VCSettingsRow(
                            icon = Icons.Rounded.Opacity,
                            title = stringResource(R.string.overlay_opacity_settings),
                            subtitle = stringResource(R.string.overlay_opacity_description),
                            onClick = { showOpacityDialog = true },
                            trailing = {
                                Icon(
                                    Icons.Rounded.Settings,
                                    contentDescription = null,
                                    tint = WColors.OnSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        HorizontalDivider(color = WColors.Border.copy(alpha = 0.4f))
                        VCSettingsRow(
                            icon = Icons.Rounded.Palette,
                            title = "GUI Theme",
                            subtitle = "Classic overlay theme (default)",
                            tint = WColors.Secondary,
                            trailing = {
                                Text(
                                    "Classic",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WColors.Secondary
                                )
                            }
                        )
                    }
                }

                VCSectionHeader(
                    title = "Config",
                    subtitle = "Save and load module configurations"
                )

                VCCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        VCPrimaryButton(
                            onClick = { importConfigPicker.launch("application/json") },
                            modifier = Modifier.fillMaxWidth(),
                            color = WColors.SurfaceVariant,
                            contentColor = WColors.OnSurface
                        ) {
                            Icon(Icons.Rounded.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Import Config")
                        }
                        VCPrimaryButton(
                            onClick = { showFileNameDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isExporting
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = WColors.OnPrimary
                                )
                            } else {
                                Icon(Icons.Rounded.SaveAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(if (isExporting) "Exporting..." else "Export Config")
                        }
                    }
                }

                VCSectionHeader(
                    title = "About",
                    subtitle = "App info, license and links"
                )

                VCCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenAbout) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                        VCSettingsRow(
                            icon = Icons.Rounded.Info,
                            title = "About Voidclient",
                            subtitle = "Version ${BuildConfig.VERSION_NAME}",
                            trailing = {
                                Icon(
                                    Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    tint = WColors.OnSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }

                VCSectionHeader(
                    title = "Danger Zone",
                    subtitle = "Emergency actions"
                )

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = Color(0xFF2A0E0E),
                        contentColor = Color(0xFFFF6B6B)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF6B1A1A)),
                    onClick = { showSelfDestructDialog = true },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Row(
                        Modifier.padding(15.dp),
                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Dangerous, contentDescription = null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(24.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Self Destruct",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFFFF6B6B),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Instantly disable all active modules",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCC8888)
                            )
                        }
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = Color(0xFFCC8888),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        if (showFileNameDialog) {
            BasicAlertDialog(
                onDismissRequest = {
                    if (!isExporting) {
                        showFileNameDialog = false
                        configFileName = ""
                    }
                },
                modifier = Modifier.padding(24.dp)
            ) {
                Surface(
                    shape = AlertDialogDefaults.shape,
                    tonalElevation = AlertDialogDefaults.TonalElevation
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Export Configuration",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Text(
                            "Config will be saved to: Documents/Voidclient/configs/",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = configFileName,
                            onValueChange = { configFileName = it },
                            label = { Text("Configuration Name (Optional)") },
                            placeholder = { Text("my-config") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isExporting,
                            supportingText = {
                                Text(
                                    if (configFileName.isBlank())
                                        "Leave empty to use timestamp"
                                    else
                                        "File: $configFileName.json"
                                )
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    showFileNameDialog = false
                                    configFileName = ""
                                },
                                enabled = !isExporting
                            ) {
                                Text("Cancel")
                            }
                            Spacer(Modifier.width(8.dp))
                            FilledTonalButton(
                                onClick = {
                                    isExporting = true
                                    scope.launch {
                                        val result = exportConfig(configFileName)
                                        isExporting = false

                                        result.onSuccess { filePath ->
                                            snackbarHostState.showSnackbar(
                                                message = "Config saved successfully!\nLocation: $filePath",
                                                duration = SnackbarDuration.Long
                                            )
                                        }.onFailure { error ->
                                            snackbarHostState.showSnackbar(
                                                message = "Export failed: ${error.message}",
                                                duration = SnackbarDuration.Long
                                            )
                                        }

                                        showFileNameDialog = false
                                        configFileName = ""
                                    }
                                },
                                enabled = !isExporting
                            ) {
                                if (isExporting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(if (isExporting) "Saving..." else "Export")
                            }
                        }
                    }
                }
            }
        }

        if (showSelfDestructDialog) {
            BasicAlertDialog(
                onDismissRequest = { showSelfDestructDialog = false },
                modifier = Modifier.padding(24.dp)
            ) {
                Surface(
                    shape = AlertDialogDefaults.shape,
                    tonalElevation = AlertDialogDefaults.TonalElevation
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Dangerous,
                                contentDescription = null,
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                "Self Destruct",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color(0xFFFF6B6B)
                            )
                        }

                        Text(
                            "Warning! This will instantly disable ALL active modules and save the configuration.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            "You will need to manually re-enable any modules you want to use again.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showSelfDestructDialog = false }
                            ) {
                                Text("Cancel")
                            }
                            Spacer(Modifier.width(8.dp))
                            FilledTonalButton(
                                onClick = {
                                    var count = 0
                                    ModuleManager.modules.forEach {
                                        if (it.isEnabled && !it.private) {
                                            it.isEnabled = false
                                            count++
                                        }
                                    }
                                    ModuleManager.saveConfig()
                                    showSelfDestructDialog = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Self Destruct: disabled $count module(s)",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF6B1A1A),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Disable All")
                            }
                        }
                    }
                }
            }
        }

        if (showOpacityDialog) {
            BasicAlertDialog(
                onDismissRequest = { showOpacityDialog = false },
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                Surface(
                    shape = AlertDialogDefaults.shape,
                    tonalElevation = AlertDialogDefaults.TonalElevation
                ) {
                    Column(
                        Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.overlay_opacity_settings),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column {
                                Text(
                                    stringResource(R.string.overlay_opacity),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Slider(
                                    value = overlayOpacity,
                                    onValueChange = {
                                        overlayOpacity = it
                                        prefs.edit { putFloat("overlay_opacity", it) }
                                        OverlayManager.updateOverlayOpacity(it)
                                    },
                                    valueRange = 0f..1f
                                )
                            }
                            Column {
                                Text(
                                    stringResource(R.string.shortcut_opacity),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Slider(
                                    value = shortcutOpacity,
                                    onValueChange = {
                                        shortcutOpacity = it
                                        prefs.edit { putFloat("shortcut_opacity", it) }
                                        OverlayManager.updateShortcutOpacity(it)
                                    },
                                    valueRange = 0f..1f
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutSettingsContent(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.about),
                        style = MaterialTheme.typography.headlineMedium,
                        color = WColors.OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WColors.OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = WColors.OnSurface
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            VCCard(modifier = Modifier.fillMaxWidth(), accent = WColors.Primary) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Voidclient",
                        style = MaterialTheme.typography.displaySmall,
                        color = WColors.Primary
                    )
                    Text(
                        text = "by DoTo.dev",
                        style = MaterialTheme.typography.titleMedium,
                        color = WColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = WColors.Primary.copy(alpha = 0.12f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Version ${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.labelLarge,
                            color = WColors.Primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            VCCard(
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://discord.gg/AM3ZpaXHW5".toUri()
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                accent = WColors.Secondary
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Discord",
                            style = MaterialTheme.typography.titleMedium,
                            color = WColors.OnSurface
                        )
                        Text(
                            text = "discord.gg/AM3ZpaXHW5",
                            style = MaterialTheme.typography.bodySmall,
                            color = WColors.OnSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open Discord",
                        tint = WColors.Secondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            VCCard(modifier = Modifier.fillMaxWidth(), accent = WColors.Secondary) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "License",
                        style = MaterialTheme.typography.headlineSmall,
                        color = WColors.OnSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(
                        color = WColors.OnSurfaceVariant.copy(alpha = 0.2f)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Permitted Uses",
                            style = MaterialTheme.typography.titleMedium,
                            color = WColors.Accent
                        )
                        Surface(
                            color = WColors.Accent.copy(alpha = 0.08f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BulletPoint("Personal use and modification")
                                BulletPoint("Creating content using Voidclient")
                                BulletPoint("Redistributing source code with GPLv3 license")
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Prohibited Uses",
                            style = MaterialTheme.typography.titleMedium,
                            color = WColors.Error
                        )
                        Surface(
                            color = WColors.Error.copy(alpha = 0.08f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BulletPoint("Distributing without source code and license")
                                BulletPoint("Claiming ownership of original source code")
                            }
                        }
                    }
                }
            }

            VCCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Legal",
                        style = MaterialTheme.typography.headlineSmall,
                        color = WColors.OnSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(
                        color = WColors.OnSurfaceVariant.copy(alpha = 0.2f)
                    )

                    LegalItem(
                        title = "Disclaimer of Warranty",
                        content = "This program is distributed under the GNU General Public License v3 (GPLv3). It is provided \"AS IS\", without any warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose, and noninfringement."
                    )

                    LegalItem(
                        title = "Limitation of Liability",
                        content = "In no event shall the author(s) or copyright holder(s) be liable for any claim, damages, or other liability, whether in an action of contract, tort, or otherwise, arising from, out of, or in connection with the software or the use or other dealings in the software."
                    )

                    LegalItem(
                        title = "Intended Use",
                        content = "This software is intended solely for educational and research purposes. Any use of this program that violates applicable laws, terms of service, or causes harm to others is strictly unintended and the responsibility of the user."
                    )
                }
            }

            VCCard(
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/zaxxthepole/VoidClient".toUri()
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                accent = WColors.Secondary
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Source Code",
                            style = MaterialTheme.typography.titleMedium,
                            color = WColors.OnSurface
                        )
                        Text(
                            text = "View on GitHub",
                            style = MaterialTheme.typography.bodySmall,
                            color = WColors.OnSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open GitHub",
                        tint = WColors.Secondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "\u2022",
            style = MaterialTheme.typography.bodyMedium,
            color = WColors.OnSurface
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = WColors.OnSurface
        )
    }
}

@Composable
private fun LegalItem(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = WColors.OnSurface
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = WColors.OnSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodySmall.lineHeight.times(1.5f)
        )
    }
}
