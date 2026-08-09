package com.kalyangupta.wallet.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kalyangupta.wallet.data.remote.dto.CategoryBreakdown
import com.kalyangupta.wallet.data.remote.dto.MonthlyData
import com.kalyangupta.wallet.ui.components.ErrorView
import com.kalyangupta.wallet.ui.components.WalletPullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state = viewModel.analyticsState.value

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Financial Analytics") })
        }
    ) { padding ->
        WalletPullToRefreshBox(
            isRefreshing = state is AnalyticsViewModel.AnalyticsState.Loading,
            onRefresh = { viewModel.loadAnalytics() },
            modifier = Modifier.padding(padding)
        ) {
            when (state) {
                is AnalyticsViewModel.AnalyticsState.Loading -> {
                    // PullToRefreshBox will show its own indicator if isRefreshing is true,
                    // but we might still want the initial loader if there's no data.
                    // For now, let's just keep it simple.
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is AnalyticsViewModel.AnalyticsState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadAnalytics() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is AnalyticsViewModel.AnalyticsState.Success -> {
                    val analytics = state.analytics
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatCard(title = "Net Worth", value = "₹${analytics.netWorth}", modifier = Modifier.weight(1f))
                                StatCard(title = "Savings Rate", value = "${analytics.averageSavingRatePercent}%", modifier = Modifier.weight(1f))
                            }
                        }
                        item {
                            StatCard(title = "Top Expense Category", value = analytics.topCategory, subValue = "₹${analytics.topCategoryAmount}", modifier = Modifier.fillMaxWidth())
                        }
                        item {
                            Text(text = "Monthly Cashflow (Last 6 Months)", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            SimpleBarChart(data = analytics.monthlyIncome.map { it.amount.toFloat() to it.amount.toFloat() }) // Simplified for now
                        }
                        item {
                            Text(text = "Category Expenditure", style = MaterialTheme.typography.titleMedium)
                        }
                        items(analytics.categoryBreakdown) { breakdown ->
                            CategoryExpenseItem(breakdown = breakdown)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, subValue: String? = null, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Text(
                text = value, 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subValue != null) {
                Text(text = subValue, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SimpleBarChart(data: List<Pair<Float, Float>>) {
    // Basic placeholder for a bar chart using Row and Boxes
    Row(
        modifier = Modifier.fillMaxWidth().height(150.dp).padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val maxVal = data.maxOfOrNull { it.first } ?: 1f
        data.forEach { (income, expense) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(income / maxVal)
                    .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
            )
        }
    }
}

@Composable
fun CategoryExpenseItem(breakdown: CategoryBreakdown) {
    ListItem(
        headlineContent = { Text(breakdown.categoryName) },
        trailingContent = { Text("₹${breakdown.amount}", fontWeight = FontWeight.Bold) }
    )
}

@Composable
fun MonthlyTrendItem(income: MonthlyData, expense: MonthlyData?) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = income.month, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Income: ₹${income.amount}", color = MaterialTheme.colorScheme.primary)
                if (expense != null) {
                    Text(text = "Expense: ₹${expense.amount}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
