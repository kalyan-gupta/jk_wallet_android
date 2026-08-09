package com.kalyangupta.wallet.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    onBackClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    viewModel: AccountDetailViewModel = hiltViewModel()
) {
    val state = viewModel.accountState.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state is AccountDetailViewModel.AccountDetailState.Success) {
                        IconButton(onClick = { onEditClick(state.account.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                is AccountDetailViewModel.AccountDetailState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AccountDetailViewModel.AccountDetailState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is AccountDetailViewModel.AccountDetailState.Success -> {
                    val account = state.account
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(text = account.name, style = MaterialTheme.typography.headlineMedium)
                        Text(text = account.accountType, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        DetailItem(label = "Current Balance", value = "₹${account.currentBalance}")
                        if (account.institutionName != null) {
                            DetailItem(label = "Institution", value = account.institutionName)
                        }

                        if (account.accountType == "CREDIT_CARD") {
                            DetailItem(label = "Credit Limit", value = "₹${account.creditLimit ?: 0}")
                            DetailItem(label = "Due Date", value = "Day ${account.cardDueDate ?: "-"}")
                        }

                        if (account.accountType == "DEMAT") {
                            DetailItem(label = "Invested Amount", value = "₹${account.investedAmount ?: 0}")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (!account.notes.isNullOrBlank()) {
                            Text(text = "Notes", style = MaterialTheme.typography.titleMedium)
                            Text(text = account.notes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.titleLarge)
    }
}
