package com.voidclient.client.overlay.gui.classic

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidclient.client.game.ModuleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


private val CardBackgroundExpanded = Color(0xFF1A1212)
private val AccentPrimary = Color(0xFFE63946)
private val TextPrimary = Color(0xFFE8E8E8)
private val TextTertiary = Color(0xFF888888)
private val ButtonBackground = Color(0xFF251A1A)

@Composable
fun ConfigurationScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (ModuleManager.importConfigFromFile(context, it)) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("✅ Config imported successfully")
                }
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("❌ Failed to import config")
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ConfigurationHeader()
        ConfigurationActions(
            onImportClick = { filePickerLauncher.launch("application/json") },
            onExportClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    val fileName = generateUniqueFileName()
                    val success = exportConfigToVoidclientFolder(fileName)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            val configsDir = ModuleManager.getVoidclientConfigsDirectory()
                            val path = configsDir?.let { File(it, fileName).absolutePath } ?: "Voidclient/configs/$fileName"
                            snackbarHostState.showSnackbar("✅ Exported to: $path")
                        } else {
                            snackbarHostState.showSnackbar("❌ Failed to export config")
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun ConfigurationHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Configuration",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Manage your configurations with ease",
            color = TextTertiary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ConfigurationActions(
    onImportClick: () -> Unit,
    onExportClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(0.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = CardBackgroundExpanded)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ConfigActionButton(
                text = "Import Config",
                icon = Icons.Rounded.Upload,
                onClick = onImportClick
            )

            ConfigActionButton(
                text = "Export Config",
                icon = Icons.Rounded.SaveAlt,
                onClick = onExportClick
            )
        }
    }
}

@Composable
private fun ConfigActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ButtonBackground,
            contentColor = TextPrimary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = AccentPrimary
        )
        Spacer(Modifier.width(10.dp))
        Text(text = text, fontSize = 14.sp)
    }
}



private fun generateUniqueFileName(): String {
    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    val timestamp = dateFormat.format(Date())
    return "config_$timestamp.json"
}

private fun exportConfigToVoidclientFolder(fileName: String): Boolean {
    return try {
        val configsDir = ModuleManager.getVoidclientConfigsDirectory() ?: return false
        val file = File(configsDir, fileName)

        val configContent = ModuleManager.exportConfig()
        file.writeText(configContent)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}