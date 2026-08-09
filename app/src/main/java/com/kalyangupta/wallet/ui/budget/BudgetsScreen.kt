package com.kalyangupta.wallet.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kalyangupta.wallet.data.remote.dto.BudgetDto
import com.kalyangupta.wallet.ui.components.ErrorView
import com.kalyangupta.wallet.ui.components.WalletPullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    onAddBudgetClick: () -> Unit,
    viewModel: BudgetsViewModel = hiltViewModel()
) {
    val state = viewModel.budgetsState.value

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Monthly Budgets") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBudgetClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { padding ->
        WalletPullToRefreshBox(
            isRefreshing = state is BudgetsViewModel.BudgetsState.Loading,
            onRefresh = { viewModel.loadBudgets() },
            modifier = Modifier.padding(padding)
        ) {
            when (state) {
                is BudgetsViewModel.BudgetsState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is BudgetsViewModel.BudgetsState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadBudgets() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is BudgetsViewModel.BudgetsState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (state.budgets.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No budgets set for this month")
                                }
                            }
                        } else {
                            items(state.budgets) { budget ->
                                BudgetItem(
                                    budget = budget,
                                    onDelete = { budget.id?.let { viewModel.deleteBudget(it) } }
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
fun BudgetItem(budget: BudgetDto, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        ListItem(
            headlineContent = { Text(budget.categoryDisplay ?: budget.category, fontWeight = FontWeight.Bold) },
            supportingContent = { Text("Period: ${budget.month}/${budget.year}", style = MaterialTheme.typography.labelMedium) },
            trailingContent = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "₹${budget.amountLimit}", 
                        style = MaterialTheme.typography.titleMedium, 
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Delete Budget",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}
