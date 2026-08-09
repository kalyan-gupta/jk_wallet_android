package com.kalyangupta.wallet.ui.debt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kalyangupta.wallet.data.remote.dto.DebtDto
import com.kalyangupta.wallet.ui.components.ErrorView
import com.kalyangupta.wallet.ui.components.WalletPullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(
    onAddDebtClick: () -> Unit,
    viewModel: DebtsViewModel = hiltViewModel()
) {
    val state = viewModel.debtsState.value
    val accounts = viewModel.accounts.value
    var selectedTab by remember { mutableIntStateOf(0) }
    
    var showSettleDialog by remember { mutableStateOf(false) }
    var debtToSettle by remember { mutableStateOf<DebtDto?>(null) }
    var selectedAccountId by remember { mutableStateOf<Int?>(null) }

    if (showSettleDialog && debtToSettle != null) {
        AlertDialog(
            onDismissRequest = { showSettleDialog = false; debtToSettle = null },
            title = { Text("Settle Entry") },
            text = {
                Column {
                    Text("Select the account to record this settlement:")
                    Spacer(modifier = Modifier.height(16.dp))
                    accounts.forEach { account ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedAccountId == account.id,
                                onClick = { selectedAccountId = account.id }
                            )
                            Text(
                                text = account.name,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedAccountId?.let { accId ->
                            viewModel.settleDebt(debtToSettle!!.id!!, accId)
                        }
                        showSettleDialog = false
                        debtToSettle = null
                    },
                    enabled = selectedAccountId != null
                ) {
                    Text("Settle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettleDialog = false; debtToSettle = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("Owed (IOUs)") })
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Active") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Settled") })
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDebtClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { padding ->
        WalletPullToRefreshBox(
            isRefreshing = state is DebtsViewModel.DebtsState.Loading,
            onRefresh = { viewModel.loadDebts() },
            modifier = Modifier.padding(padding)
        ) {
            when (state) {
                is DebtsViewModel.DebtsState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DebtsViewModel.DebtsState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadDebts() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is DebtsViewModel.DebtsState.Success -> {
                    val filteredDebts = if (selectedTab == 0) {
                        state.debts.filter { !it.isSettled }
                    } else {
                        state.debts.filter { it.isSettled }
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (filteredDebts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No active or settled entries found")
                                }
                            }
                        } else {
                            items(filteredDebts) { debt ->
                                DebtItem(
                                    debt = debt,
                                    onSettleClick = { 
                                        debtToSettle = debt
                                        selectedAccountId = debt.accountId // Pre-select linked account if exists
                                        showSettleDialog = true
                                    },
                                    onUnsettleClick = { viewModel.unsettleDebt(debt.id!!) },
                                    onDelete = { viewModel.deleteDebt(debt.id!!) }
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
fun DebtItem(debt: DebtDto, onSettleClick: () -> Unit, onUnsettleClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        ListItem(
            headlineContent = { Text(debt.personName, fontWeight = FontWeight.Bold) },
            supportingContent = { 
                Column {
                    Text(
                        text = if (debt.debtType == "LENT") "Lent to them" else "Borrowed from them", 
                        style = MaterialTheme.typography.labelSmall,
                        color = if (debt.debtType == "LENT") Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    if (debt.description != null) {
                        Text(debt.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            trailingContent = { 
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${debt.amount}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (debt.debtType == "LENT") Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete, 
                                contentDescription = "Delete Entry",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (!debt.isSettled) {
                        TextButton(onClick = onSettleClick) {
                            Text("Settle", style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        TextButton(onClick = onUnsettleClick) {
                            Text("Unsettle", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}
