package com.kalyangupta.wallet.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditScreen(
    onBackClick: () -> Unit,
    viewModel: BudgetEditViewModel = hiltViewModel()
) {
    val category by viewModel.category
    val amountLimit by viewModel.amountLimit
    val month by viewModel.month
    val year by viewModel.year
    val isLoading by viewModel.isLoading

    var categoryMenuExpanded by remember { mutableStateOf(false) }
    val categories = listOf(
        "FOOD" to "Food & Dining",
        "SHOPPING" to "Shopping & Electronics",
        "BILLS" to "Utilities & Bills",
        "SALARY" to "Salary & Income",
        "RENT" to "Rent & Housing",
        "INVESTMENT" to "Investments & Mutual Funds",
        "ENTERTAINMENT" to "Entertainment & Subscriptions",
        "OTHERS" to "Others / Misc"
    )

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is BudgetEditViewModel.UiEvent.Success -> onBackClick()
                is BudgetEditViewModel.UiEvent.Error -> { /* Show error */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Budget") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isLoading) {
                        IconButton(onClick = viewModel::saveBudget) {
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
            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = categories.find { it.first == category }?.second ?: category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Budget Category") },
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

            OutlinedTextField(
                value = amountLimit,
                onValueChange = viewModel::onAmountLimitChange,
                label = { Text("Monthly Limit (₹)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = month.toString(),
                    onValueChange = { val m = it.toIntOrNull(); if (m != null && m in 1..12) viewModel.onMonthChange(m) },
                    label = { Text("Month") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = year.toString(),
                    onValueChange = { val y = it.toIntOrNull(); if (y != null) viewModel.onYearChange(y) },
                    label = { Text("Year") },
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = viewModel::saveBudget,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading
            ) {
                Text("Save Budget")
            }
        }
    }
}
