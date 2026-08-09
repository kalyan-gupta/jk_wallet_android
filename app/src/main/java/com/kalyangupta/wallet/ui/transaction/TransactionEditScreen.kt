package com.kalyangupta.wallet.ui.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kalyangupta.wallet.ui.components.DatePickerField
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditScreen(
    onBackClick: () -> Unit,
    viewModel: TransactionEditViewModel = hiltViewModel()
) {
    val transactionType by viewModel.transactionType
    val category by viewModel.category
    val amount by viewModel.amount
    val date by viewModel.date
    val sourceAccountId by viewModel.sourceAccountId
    val destinationAccountId by viewModel.destinationAccountId
    val recipientName by viewModel.recipientName
    val description by viewModel.description
    val accounts by viewModel.accounts
    val isLoading by viewModel.isLoading

    var typeMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var sourceMenuExpanded by remember { mutableStateOf(false) }
    var destinationMenuExpanded by remember { mutableStateOf(false) }

    val transactionTypes = listOf(
        "EXPENSE" to "Expense / Purchase",
        "INCOME" to "Income / Salary",
        "TRANSFER" to "Internal Self Transfer",
        "PAY_PEOPLE" to "Paid to Person / External",
        "CARD_PAYMENT" to "Credit Card Bill Payment",
        "DEMAT_DEPOSIT" to "Investment into Demat",
        "DEMAT_WITHDRAWAL" to "Withdrawal from Demat"
    )

    val categories = listOf(
        "FOOD" to "Food & Dining",
        "SHOPPING" to "Shopping & Electronics",
        "BILLS" to "Utilities & Bills",
        "SALARY" to "Salary & Income",
        "RENT" to "Rent & Housing",
        "INVESTMENT" to "Investments & Mutual Funds",
        "TRANSFER" to "Account Transfer",
        "CARD_BILL" to "Credit Card Bill",
        "ENTERTAINMENT" to "Entertainment & Subscriptions",
        "OTHERS" to "Others / Misc"
    )

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is TransactionEditViewModel.UiEvent.Success -> onBackClick()
                is TransactionEditViewModel.UiEvent.Error -> { /* Show error */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Transaction") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isLoading) {
                        IconButton(onClick = viewModel::saveTransaction) {
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
                ExposedDropdownMenuBox(
                    expanded = typeMenuExpanded,
                    onExpandedChange = { typeMenuExpanded = !typeMenuExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = transactionTypes.find { it.first == transactionType }?.second ?: transactionType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Transaction Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false }
                    ) {
                        transactionTypes.forEach { (code, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.onTransactionTypeChange(code)
                                    typeMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = categories.find { it.first == category }?.second ?: category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category / Reason") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        categories.forEach { (code, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.onCategoryChange(code)
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = amount,
                    onValueChange = viewModel::onAmountChange,
                    label = { Text("Amount (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                DatePickerField(
                    label = "Date",
                    selectedDate = date,
                    onDateSelected = viewModel::onDateChange
                )
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = sourceMenuExpanded,
                    onExpandedChange = { sourceMenuExpanded = !sourceMenuExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = accounts.find { it.id == sourceAccountId }?.name ?: "-- Select Source Account --",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Source Account / Wallet") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceMenuExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = sourceMenuExpanded,
                        onDismissRequest = { sourceMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("-- None --") },
                            onClick = {
                                viewModel.onSourceAccountChange(null)
                                sourceMenuExpanded = false
                            }
                        )
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.name) },
                                onClick = {
                                    viewModel.onSourceAccountChange(account.id)
                                    sourceMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = destinationMenuExpanded,
                    onExpandedChange = { destinationMenuExpanded = !destinationMenuExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = accounts.find { it.id == destinationAccountId }?.name ?: "-- Select Destination Account --",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Destination Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destinationMenuExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = destinationMenuExpanded,
                        onDismissRequest = { destinationMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("-- None --") },
                            onClick = {
                                viewModel.onDestinationAccountChange(null)
                                destinationMenuExpanded = false
                            }
                        )
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.name) },
                                onClick = {
                                    viewModel.onDestinationAccountChange(account.id)
                                    destinationMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = recipientName,
                    onValueChange = viewModel::onRecipientNameChange,
                    label = { Text("Recipient / Person Name (Optional)") },
                    placeholder = { Text("e.g. John, Amazon, Electricity Board") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Description / Notes") },
                    placeholder = { Text("Reason for transaction") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            item {
                Button(
                    onClick = viewModel::saveTransaction,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    enabled = !isLoading
                ) {
                    Text("Save Transaction")
                }
            }
        }
    }
}
