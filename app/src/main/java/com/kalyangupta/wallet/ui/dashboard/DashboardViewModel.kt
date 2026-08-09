package com.kalyangupta.wallet.ui.dashboard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalyangupta.wallet.data.local.SessionManager
import com.kalyangupta.wallet.data.remote.dto.AccountDto
import com.kalyangupta.wallet.data.remote.dto.AnalyticsDto
import com.kalyangupta.wallet.data.remote.dto.TransactionDto
import com.kalyangupta.wallet.data.repository.AccountRepository
import com.kalyangupta.wallet.data.repository.AnalyticsRepository
import com.kalyangupta.wallet.data.repository.TransactionRepository
import com.kalyangupta.wallet.util.RefreshEventBus
import com.kalyangupta.wallet.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val sessionManager: SessionManager,
    private val refreshEventBus: RefreshEventBus
) : ViewModel() {

    private val _accountsState = mutableStateOf<DashboardState<List<AccountDto>>>(DashboardState.Loading())
    val accountsState: State<DashboardState<List<AccountDto>>> = _accountsState

    private val _transactionsState = mutableStateOf<DashboardState<List<TransactionDto>>>(DashboardState.Loading())
    val transactionsState: State<DashboardState<List<TransactionDto>>> = _transactionsState

    private val _analyticsState = mutableStateOf<DashboardState<AnalyticsDto>>(DashboardState.Loading())
    val analyticsState: State<DashboardState<AnalyticsDto>> = _analyticsState

    private val _isStaff = mutableStateOf(sessionManager.isStaff())
    val isStaff: State<Boolean> = _isStaff

    init {
        loadDashboard()
        observeRefreshEvents()
    }

    private fun observeRefreshEvents() {
        viewModelScope.launch {
            refreshEventBus.events.collectLatest { event ->
                when (event) {
                    RefreshEventBus.RefreshEvent.ALL -> loadDashboard()
                    RefreshEventBus.RefreshEvent.ACCOUNTS -> loadAccounts()
                    RefreshEventBus.RefreshEvent.TRANSACTIONS -> loadRecentTransactions()
                    RefreshEventBus.RefreshEvent.ANALYTICS -> loadAnalytics()
                    else -> {}
                }
            }
        }
    }

    fun loadDashboard() {
        loadAccounts()
        loadAnalytics()
        loadRecentTransactions()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            _accountsState.value = DashboardState.Loading()
            val result = accountRepository.getAccounts()
            when (result) {
                is Resource.Success -> _accountsState.value = DashboardState.Success(result.data ?: emptyList())
                is Resource.Error -> _accountsState.value = DashboardState.Error(result.message ?: "Unknown error")
                else -> {}
            }
        }
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            _analyticsState.value = DashboardState.Loading()
            val result = analyticsRepository.getAnalytics()
            when (result) {
                is Resource.Success -> _analyticsState.value = DashboardState.Success(result.data!!)
                is Resource.Error -> _analyticsState.value = DashboardState.Error(result.message ?: "Unknown error")
                else -> {}
            }
        }
    }

    private fun loadRecentTransactions() {
        viewModelScope.launch {
            _transactionsState.value = DashboardState.Loading()
            val result = transactionRepository.getTransactions()
            when (result) {
                is Resource.Success -> _transactionsState.value = DashboardState.Success(result.data ?: emptyList())
                is Resource.Error -> _transactionsState.value = DashboardState.Error(result.message ?: "Unknown error")
                else -> {}
            }
        }
    }

    sealed class DashboardState<T>(val data: T? = null, val error: String? = null) {
        class Loading<T> : DashboardState<T>()
        class Success<T>(data: T) : DashboardState<T>(data = data)
        class Error<T>(message: String) : DashboardState<T>(error = message)
    }
}
