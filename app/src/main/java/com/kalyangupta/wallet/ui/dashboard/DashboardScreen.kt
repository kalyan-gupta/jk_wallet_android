package com.kalyangupta.wallet.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kalyangupta.wallet.R
import com.kalyangupta.wallet.data.remote.dto.AccountDto
import com.kalyangupta.wallet.ui.components.ErrorView
import com.kalyangupta.wallet.ui.components.WalletPullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAccountClick: (Int) -> Unit,
    onAddAccountClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onAdminClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val accountsState = viewModel.accountsState.value
    val analyticsState = viewModel.analyticsState.value
    val isStaff = viewModel.isStaff.value
    var showMenu by remember { mutableStateOf(false) }

    val isRefreshing = accountsState is DashboardViewModel.DashboardState.Loading ||
            analyticsState is DashboardViewModel.DashboardState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "JKWallet",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboard() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onAnalyticsClick) {
                        Icon(Icons.Default.PieChart, contentDescription = "Analytics")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (isStaff) {
                                DropdownMenuItem(
                                    text = { Text("Admin Panel") },
                                    onClick = { showMenu = false; onAdminClick() },
                                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("My Profile") },
                                onClick = { showMenu = false; onProfileClick() },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = { showMenu = false; onLogoutClick() },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAccountClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Account")
            }
        }
    ) { padding ->
        WalletPullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadDashboard() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (accountsState is DashboardViewModel.DashboardState.Error &&
                analyticsState is DashboardViewModel.DashboardState.Error
            ) {
                ErrorView(
                    message = "Could not load dashboard data",
                    onRetry = { viewModel.loadDashboard() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        when (analyticsState) {
                            is DashboardViewModel.DashboardState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            is DashboardViewModel.DashboardState.Success -> {
                                val analytics = analyticsState.data!!
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Text(
                                            text = "Net Worth",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "₹${analytics.netWorth}",
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Savings Rate",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "${analytics.averageSavingRatePercent}%",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "Top Category",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = analytics.topCategory,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            is DashboardViewModel.DashboardState.Error -> {
                                ErrorView(
                                    message = analyticsState.error ?: "Error loading analytics",
                                    onRetry = { viewModel.loadDashboard() }
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Accounts",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    when (accountsState) {
                        is DashboardViewModel.DashboardState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        is DashboardViewModel.DashboardState.Error -> {
                            item {
                                ErrorView(
                                    message = accountsState.error ?: "Error loading accounts",
                                    onRetry = { viewModel.loadDashboard() }
                                )
                            }
                        }

                        is DashboardViewModel.DashboardState.Success -> {
                            val accounts = accountsState.data ?: emptyList()
                            items(accounts) { account ->
                                AccountItem(
                                    account = account,
                                    onClick = { onAccountClick(account.id) })
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountItem(account: AccountDto, onClick: () -> Unit) {
    ListItem(
        headlineContent = { 
            Text(
                account.name, 
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            ) 
        },
        supportingContent = { 
            Text(
                account.accountType, 
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ) 
        },
        trailingContent = { 
            Text(
                "₹${account.currentBalance}", 
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            ) 
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    )
}
