package com.kalyangupta.wallet.ui.debt

import androidx.compose.foundation.layout.*
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
fun DebtEditScreen(
    onBackClick: () -> Unit,
    viewModel: DebtEditViewModel = hiltViewModel()
) {
    val personName by viewModel.personName
    val debtType by viewModel.debtType
    val amount by viewModel.amount
    val accountId by viewModel.accountId
    val date by viewModel.date
    val description by viewModel.description
    val accounts by viewModel.accounts
    val isLoading by viewModel.isLoading

    var typeMenuExpanded by remember { mutableStateOf(false) }
    var accountMenuExpanded by remember { mutableStateOf(false) }

    val debtTypes = listOf(
        "LENT" to "Lent to Someone",
        "BORROWED" to "Borrowed from Someone"
    )

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is DebtEditViewModel.UiEvent.Success -> onBackClick()
                is DebtEditViewModel.UiEvent.Error -> { /* Show error */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Lent or Borrowed Loan") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isLoading) {
                        IconButton(onClick = viewModel::saveDebt) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    } else {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp).size(24.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = personName,
                onValueChange = viewModel::onPersonNameChange,
                label = { Text("Person Name") },
                placeholder = { Text("e.g. John Doe, Sarah") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = typeMenuExpanded,
                onExpandedChange = { typeMenuExpanded = !typeMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = debtTypes.find { it.first == debtType }?.second ?: debtType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Relationship / Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false }
                ) {
                    debtTypes.forEach { (code, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                viewModel.onDebtTypeChange(code)
                                typeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Amount (₹)") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = accountMenuExpanded,
                onExpandedChange = { accountMenuExpanded = !accountMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = accounts.find { it.id == accountId }?.name ?: "-- Select Linked Account --",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Source / Destination Account") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountMenuExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = accountMenuExpanded,
                    onDismissRequest = { accountMenuExpanded = false }
                ) {
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account.name) },
                            onClick = {
                                viewModel.onAccountChange(account.id)
                                accountMenuExpanded = false
                            }
                        )
                    }
                }
            }

            DatePickerField(
                label = "Date",
                selectedDate = date,
                onDateSelected = viewModel::onDateChange
            )

            OutlinedTextField(
                value = description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description / Notes") },
                placeholder = { Text("e.g. Lunch split, trip expenses") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = viewModel::saveDebt,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading
            ) {
                Text("Save Entry")
            }
        }
    }
}
