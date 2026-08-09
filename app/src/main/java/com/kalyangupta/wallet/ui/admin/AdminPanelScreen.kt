package com.kalyangupta.wallet.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kalyangupta.wallet.data.remote.dto.AdminUserDto
import com.kalyangupta.wallet.data.remote.dto.CategoryDto
import com.kalyangupta.wallet.ui.components.ErrorView
import com.kalyangupta.wallet.ui.components.WalletPullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: AdminViewModel = hiltViewModel()
) {
    val usersState = viewModel.usersState.value
    val categoriesState = viewModel.categoriesState.value
    val regEnabled = viewModel.registrationEnabled.value

    val isRefreshing = usersState is AdminViewModel.AdminState.Loading || 
            categoriesState is AdminViewModel.AdminState.Loading

    var showCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryDto?>(null) }
    var categoryName by remember { mutableStateOf("") }
    var categoryCode by remember { mutableStateOf("") }

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false; editingCategory = null },
            title = { Text(if (editingCategory == null) "Add Category" else "Edit Category") },
            text = {
                Column {
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        label = { Text("Name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = categoryCode,
                        onValueChange = { categoryCode = it },
                        label = { Text("Code") },
                        enabled = editingCategory == null
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (editingCategory == null) {
                        viewModel.addCategory(categoryName, categoryCode)
                    } else {
                        viewModel.updateCategory(editingCategory!!.id!!, categoryName, categoryCode)
                    }
                    showCategoryDialog = false
                    editingCategory = null
                    categoryName = ""
                    categoryCode = ""
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false; editingCategory = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Admin Control Panel") })
        }
    ) { padding ->
        WalletPullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadAdminData() },
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    val users = (usersState as? AdminViewModel.AdminState.Success)?.data ?: emptyList()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AdminStatCard(title = "Total Users", value = users.size.toString(), modifier = Modifier.weight(1f))
                        AdminStatCard(title = "Total Accounts", value = users.sumOf { it.accountCount }.toString(), modifier = Modifier.weight(1f))
                        AdminStatCard(title = "Total Trans", value = users.sumOf { it.transactionCount }.toString(), modifier = Modifier.weight(1f))
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Global Registration", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = if (regEnabled) "Enabled" else "Disabled",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (regEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                            Switch(checked = regEnabled, onCheckedChange = { viewModel.toggleRegistration() })
                        }
                    }
                }

                item {
                    Text(
                        text = "Users Management",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                when (usersState) {
                    is AdminViewModel.AdminState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is AdminViewModel.AdminState.Error -> {
                        item {
                            ErrorView(
                                message = usersState.error ?: "Error loading users",
                                onRetry = { viewModel.loadAdminData() }
                            )
                        }
                    }
                    is AdminViewModel.AdminState.Success -> {
                        val users = usersState.data ?: emptyList()
                        items(users) { user ->
                            AdminUserItem(
                                user = user,
                                onToggleStatus = { viewModel.toggleUserStatus(user.id) },
                                onDelete = { viewModel.deleteUser(user.id) }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transaction Categories",
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(onClick = {
                            editingCategory = null
                            categoryName = ""
                            categoryCode = ""
                            showCategoryDialog = true
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Category")
                        }
                    }
                }

                when (categoriesState) {
                    is AdminViewModel.AdminState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is AdminViewModel.AdminState.Error -> {
                        item {
                            ErrorView(
                                message = categoriesState.error ?: "Error loading categories",
                                onRetry = { viewModel.loadAdminData() }
                            )
                        }
                    }
                    is AdminViewModel.AdminState.Success -> {
                        val categories = categoriesState.data ?: emptyList()
                        items(categories) { category ->
                            CategoryItem(
                                category = category,
                                onEdit = {
                                    editingCategory = category
                                    categoryName = category.name
                                    categoryCode = category.code
                                    showCategoryDialog = true
                                },
                                onDelete = { viewModel.deleteCategory(category.id!!) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminUserItem(user: AdminUserDto, onToggleStatus: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(user.username) },
        supportingContent = { 
            Text("${user.email} • Accounts: ${user.accountCount} • Trans: ${user.transactionCount}") 
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = user.isStaff,
                    onClick = onToggleStatus,
                    label = { Text(if (user.isStaff) "Staff" else "User") }
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete, 
                        contentDescription = "Delete User",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        leadingContent = {
            Icon(Icons.Default.Person, contentDescription = null)
        }
    )
}

@Composable
fun CategoryItem(category: CategoryDto, onEdit: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(category.name) },
        supportingContent = { Text(category.code) },
        trailingContent = {
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}
