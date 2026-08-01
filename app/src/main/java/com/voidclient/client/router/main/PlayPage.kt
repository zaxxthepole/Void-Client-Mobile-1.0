package com.voidclient.client.router.main

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voidclient.client.R
import com.voidclient.client.game.AccountManager
import com.voidclient.client.service.RealmsManager
import com.voidclient.client.ui.component.AuthWebView
import com.voidclient.client.ui.component.VCCard
import com.voidclient.client.ui.component.VCPrimaryButton
import com.voidclient.client.ui.component.VCSectionHeader
import com.voidclient.client.ui.component.VCServerCard
import com.voidclient.client.ui.component.WRealmsSection
import com.voidclient.client.ui.theme.WColors
import com.voidclient.client.util.LocalSnackbarHostState
import com.voidclient.client.util.SnackbarHostStateScope
import com.voidclient.client.util.getActivityWindow
import com.voidclient.client.util.getDialogWindow
import com.voidclient.client.util.windowFullScreen
import com.voidclient.client.viewmodel.MainScreenViewModel
import kotlinx.coroutines.launch
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession.FullBedrockSession

data class ServerInfo(
    val name: String,
    val iconRes: Int,
    val address: String,
    val port: Int,
    val hasSubServers: Boolean = false,
    val subServers: List<SubServerInfo> = emptyList()
)

data class SubServerInfo(
    val name: String,
    val region: String,
    val address: String,
    val port: Int,
    val isSelected: Boolean = false
)

val servers = listOf(
    ServerInfo(
        name = "Lifeboat",
        iconRes = R.drawable.lifeboat_icon,
        address = "play.lbsg.net",
        port = 19132
    ),
    ServerInfo(
        name = "CubeCraft",
        iconRes = R.drawable.cubecraft_icon,
        address = "play.cubecraft.net",
        port = 19132
    ),
    ServerInfo(
        name = "NetherGames",
        iconRes = R.drawable.nethergames,
        address = "play.nethergames.org",
        port = 19132
    ),
    ServerInfo(
        name = "DonutSMP",
        iconRes = R.drawable.donutsmp_icon,
        address = "donutsmp.net",
        port = 19132
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayPageContent() {
    SnackbarHostStateScope {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = LocalSnackbarHostState.current
        val mainScreenViewModel: MainScreenViewModel = viewModel()
        val captureModeModel by mainScreenViewModel.captureModeModel.collectAsStateWithLifecycle()
        val realmsState by RealmsManager.realmsState.collectAsStateWithLifecycle()

        var showAddAccountDropDownMenu by remember { mutableStateOf(false) }
        var selectedAccountAction: FullBedrockSession? by remember { mutableStateOf(null) }
        var login by remember { mutableStateOf(false) }

        LaunchedEffectRealmsRefresh()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Play",
                            style = MaterialTheme.typography.headlineMedium,
                            color = WColors.OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    actions = {
                        IconButton(onClick = { showAddAccountDropDownMenu = true }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add account", tint = WColors.PrimaryLight)
                        }
                        AddAccountDropDownMenu(
                            expanded = showAddAccountDropDownMenu,
                            onClick = {
                                showAddAccountDropDownMenu = false
                                login = true
                            }
                        ) {
                            showAddAccountDropDownMenu = false
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = WColors.OnSurface
                    )
                )
            },
            bottomBar = {
                SnackbarHost(
                    snackbarHostState,
                    modifier = Modifier.animateContentSize()
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    VCSectionHeader(
                        title = "Account",
                        subtitle = "Microsoft account for multiplayer & Realms"
                    )
                }
                item {
                    AccountSection(
                        selectedAccountAction = selectedAccountAction,
                        onAccountAction = { selectedAccountAction = it },
                        onAddAccount = {
                            showAddAccountDropDownMenu = true
                        }
                    )
                }
                item {
                    VCSectionHeader(
                        title = "Servers",
                        subtitle = "Tap a server to set as active"
                    )
                }
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        userScrollEnabled = false
                    ) {
                        items(servers) { server ->
                            val isSelected =
                                captureModeModel.serverHostName.equals(server.address, ignoreCase = true) &&
                                        captureModeModel.serverPort == server.port
                            VCServerCard(
                                name = server.name,
                                address = "${server.address}:${server.port}",
                                selected = isSelected,
                                onClick = {
                                    mainScreenViewModel.selectCaptureModeModel(
                                        mainScreenViewModel.captureModeModel.value.copy(
                                            serverHostName = server.address,
                                            serverPort = server.port
                                        )
                                    )
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(server.iconRes),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            )
                        }
                    }
                }
                item {
                    VCSectionHeader(
                        title = "Realms",
                        subtitle = "Connect to your owned Realms"
                    )
                }
                item {
                    WRealmsSection(
                        realmsState = realmsState,
                        onRealmSelect = { host, port ->
                            val portInt = port.toIntOrNull() ?: 19132
                            mainScreenViewModel.selectCaptureModeModel(
                                mainScreenViewModel.captureModeModel.value.copy(
                                    serverHostName = host,
                                    serverPort = portInt
                                ).withAutoDetectedServerConfig()
                            )
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Realm selected: $host:$portInt")
                            }
                        },
                        onRefresh = {
                            RealmsManager.refreshRealms()
                        }
                    )
                }
                item {
                    Box(Modifier.height(8.dp))
                }
            }
        }

        if (login) {
            AccountDialog { throwable: Throwable? ->
                login = false
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    throwable?.let {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.fetch_account_failed, it.message)
                        )
                    } ?: snackbarHostState.showSnackbar(
                        context.getString(R.string.fetch_account_successfully)
                    )
                }
            }
        }
    }
}

@Composable
private fun LaunchedEffectRealmsRefresh() {
    androidx.compose.runtime.LaunchedEffect(AccountManager.selectedAccount) {
        RealmsManager.updateSession(AccountManager.selectedAccount)
    }
}

@Composable
private fun AccountSection(
    selectedAccountAction: FullBedrockSession?,
    onAccountAction: (FullBedrockSession?) -> Unit,
    onAddAccount: () -> Unit
) {
    val accounts = AccountManager.accounts
    if (accounts.isEmpty()) {
        VCCard(
            modifier = Modifier.fillMaxWidth(),
            accent = WColors.Primary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Rounded.AccountCircle,
                    contentDescription = null,
                    tint = WColors.OnSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    "No account added",
                    style = MaterialTheme.typography.titleMedium,
                    color = WColors.OnSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Sign in with your Microsoft account to play on servers and Realms.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WColors.OnSurfaceVariant
                )
                VCPrimaryButton(onClick = onAddAccount) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Add Account", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    } else {
        accounts.forEach { account ->
            val isSelected = AccountManager.selectedAccount == account
            VCCard(
                modifier = Modifier.fillMaxWidth(),
                accent = if (isSelected) WColors.Success else WColors.Primary,
                onClick = {
                    if (isSelected) AccountManager.selectAccount(null)
                    else AccountManager.selectAccount(account)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.AccountCircle,
                            contentDescription = null,
                            tint = WColors.PrimaryLight,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            account.mcChain.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = WColors.OnSurface,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            if (isSelected) "Selected" else "Tap to select",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) WColors.Success else WColors.OnSurfaceVariant
                        )
                    }
                    if (isSelected) {
                        Icon(
                            Icons.Outlined.CheckBox,
                            contentDescription = null,
                            tint = WColors.Success,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { onAccountAction(account) }) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = "Account options",
                            tint = WColors.OnSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = selectedAccountAction == account,
                        onDismissRequest = { onAccountAction(null) }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (isSelected) stringResource(R.string.unselect) else stringResource(R.string.select)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    if (isSelected) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                if (AccountManager.selectedAccount != account) {
                                    AccountManager.selectAccount(account)
                                } else {
                                    AccountManager.selectAccount(null)
                                }
                                onAccountAction(null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete)) },
                            leadingIcon = {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                            },
                            onClick = {
                                AccountManager.removeAccount(account)
                                if (account == AccountManager.selectedAccount) {
                                    AccountManager.selectAccount(null)
                                }
                                onAccountAction(null)
                            }
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDialog(
    callback: (Throwable?) -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = true,
            decorFitsSystemWindows = false
        )
    ) {
        val activityWindow = getActivityWindow()
        val dialogWindow = getDialogWindow()

        SideEffect {
            windowFullScreen(activityWindow, dialogWindow)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.add_account))
                    }
                )
            }
        ) {
            Column(
                Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                AndroidView(
                    factory = { context ->
                        AuthWebView(context).also { authWebView ->
                            authWebView.callback = callback
                        }.also { authWebView ->
                            authWebView.addAccount()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun AddAccountDropDownMenu(
    expanded: Boolean,
    onClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            text = {
                val xboxDeviceCodeString = stringResource(R.string.xbox_device_code)
                Text(stringResource(R.string.login_in, xboxDeviceCodeString))
            },
            onClick = onClick
        )
    }
}
