package com.kalyangupta.wallet.ui.transaction

import androidx.compose.foundation.clickable
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
import com.kalyangupta.wallet.data.remote.dto.TransactionDto
import com.kalyangupta.wallet.ui.components.ErrorView
import com.kalyangupta.wallet.ui.components.WalletPullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (Int) -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val state = viewModel.transactionsState.value

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Transactions") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransactionClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        WalletPullToRefreshBox(
            isRefreshing = state is TransactionsViewModel.TransactionsState.Loading,
            onRefresh = { viewModel.loadTransactions() },
            modifier = Modifier.padding(padding)
        ) {
            when (state) {
                is TransactionsViewModel.TransactionsState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is TransactionsViewModel.TransactionsState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadTransactions() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is TransactionsViewModel.TransactionsState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (state.transactions.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No transactions found")
                                }
                            }
                        } else {
                            items(state.transactions) { transaction ->
                                TransactionItem(
                                    transaction = transaction,
                                    onClick = { onTransactionClick(transaction.id) },
                                    onDelete = { viewModel.deleteTransaction(transaction.id) }
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
fun TransactionItem(transaction: TransactionDto, onClick: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(transaction.category, fontWeight = FontWeight.Bold) },
        supportingContent = { Text(transaction.date, style = MaterialTheme.typography.labelSmall) },
        trailingContent = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = (if (transaction.transactionType == "INCOME") "+" else "-") + "₹${transaction.amount}",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (transaction.transactionType == "INCOME") Color(0xFF4CAF50) else Color(0xFFF44336)
                ) 
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Delete Transaction",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable { onClick() }
    )
}
