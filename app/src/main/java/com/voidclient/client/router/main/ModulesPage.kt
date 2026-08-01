package com.voidclient.client.router.main

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voidclient.client.game.Module
import com.voidclient.client.game.ModuleCategory
import com.voidclient.client.game.ModuleManager
import com.voidclient.client.ui.component.VCModuleCard
import com.voidclient.client.ui.component.VCSearchBar
import com.voidclient.client.ui.component.VCSectionHeader
import com.voidclient.client.ui.theme.WColors
import com.voidclient.client.util.translatedSelf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesPageContent() {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf<ModuleCategory?>(null) }

    val allModules = remember { ModuleManager.modules.filter { !it.private } }

    val filtered = remember(allModules, query, selectedCategory) {
        allModules.filter { module ->
            val categoryMatches = selectedCategory == null || module.category == selectedCategory
            val queryMatches = query.isBlank() || module.name.translatedSelf.contains(query, ignoreCase = true)
            categoryMatches && queryMatches
        }
    }

    val enabledCount = remember(allModules) { allModules.count { it.isEnabled } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Modules",
                            style = MaterialTheme.typography.headlineMedium,
                            color = WColors.OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "$enabledCount enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = WColors.OnSurfaceVariant
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
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VCSearchBar(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Search modules..."
                )
                CategoryChips(
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    VCSectionHeader(
                        title = if (selectedCategory == null) "All Modules" else selectedCategory!!.displayName,
                        subtitle = "${filtered.size} module(s)"
                    )
                }
                items(filtered.size) { index ->
                    VCModuleCard(module = filtered[index])
                }
            }
        }
    }
}

@Composable
private fun CategoryChips(
    selected: ModuleCategory?,
    onSelect: (ModuleCategory?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("All") },
            colors = chipColors(),
            shape = RoundedCornerShape(10.dp)
        )
        ModuleCategory.entries.forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(category.displayName) },
                colors = chipColors(),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    containerColor = WColors.Surface,
    selectedContainerColor = WColors.Primary.copy(alpha = 0.2f),
    labelColor = WColors.OnSurfaceVariant,
    selectedLabelColor = WColors.PrimaryLight,
    iconColor = WColors.OnSurfaceVariant,
    selectedIconColor = WColors.PrimaryLight
)
