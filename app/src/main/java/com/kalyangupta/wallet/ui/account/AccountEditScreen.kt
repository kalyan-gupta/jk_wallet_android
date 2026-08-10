package com.kalyangupta.wallet.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountEditScreen(
    onBackClick: () -> Unit,
    onDeleteSuccess: () -> Unit = onBackClick,
    viewModel: AccountEditViewModel = hiltViewModel()
) {
    val name by viewModel.name
    val accountType by viewModel.accountType
    val institutionName by viewModel.institutionName
    val currentBalance by viewModel.currentBalance
    val creditLimit by viewModel.creditLimit
    val cardDueDate by viewModel.cardDueDate
    val investedAmount by viewModel.investedAmount
    val notes by viewModel.notes
    val isLoading by viewModel.isLoading
    val isEditMode by viewModel.isEditMode

    var typeMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val accountTypes = listOf(
        "BANK" to "Bank Account",
        "CASH" to "Cash Wallet",
        "WALLET" to "Digital Wallet / UPI",
        "CREDIT_CARD" to "Credit Card",
        "DEMAT" to "Demat / Investment Account"
    )

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AccountEditViewModel.EditEvent.Saved -> onBackClick()
                is AccountEditViewModel.EditEvent.Deleted -> onDeleteSuccess()
                is AccountEditViewModel.EditEvent.Error -> { /* Show error snackbar or toast */ }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account permanently?") },
            text = { 
                Text("This action cannot be undone. All transactions associated with this account will also be deleted. Are you absolutely sure?") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Account" else "Add Account") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isLoading) {
                        IconButton(onClick = viewModel::saveAccount) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    } else {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp).size(24.dp))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Account Name") },
                    placeholder = { Text("e.g. HDFC Savings, Zerodha Demat") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = typeMenuExpanded,
                    onExpandedChange = { typeMenuExpanded = !typeMenuExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = accountTypes.find { it.first == accountType }?.second ?: accountType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Account Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false }
                    ) {
                        accountTypes.forEach { (code, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.onAccountTypeChange(code)
                                    typeMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = institutionName,
                    onValueChange = viewModel::onInstitutionNameChange,
                    label = { Text("Institution / Bank Name") },
                    placeholder = { Text("e.g. HDFC Bank, SBI, Paytm") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = currentBalance,
                    onValueChange = viewModel::onCurrentBalanceChange,
                    label = { Text("Current Cash Balance / Available Limit (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (accountType == "CREDIT_CARD") {
                item {
                    OutlinedTextField(
                        value = creditLimit,
                        onValueChange = viewModel::onCreditLimitChange,
                        label = { Text("Total Credit Limit (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = cardDueDate,
                        onValueChange = viewModel::onCardDueDateChange,
                        label = { Text("Bill Due Date (Day of Month, 1-31)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (accountType == "DEMAT") {
                item {
                    OutlinedTextField(
                        value = investedAmount,
                        onValueChange = viewModel::onInvestedAmountChange,
                        label = { Text("Total Invested Amount (Capital) (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = viewModel::onNotesChange,
                    label = { Text("Notes / Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            item {
                Button(
                    onClick = viewModel::saveAccount,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    enabled = !isLoading
                ) {
                    Text("Save Account")
                }
            }
            
            if (isEditMode) {
                item {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Account")
                    }
                }
            }
        }
    }
}
