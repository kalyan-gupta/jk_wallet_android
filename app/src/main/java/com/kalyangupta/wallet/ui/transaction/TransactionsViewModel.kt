package com.kalyangupta.wallet.ui.transaction

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalyangupta.wallet.data.remote.dto.TransactionDto
import com.kalyangupta.wallet.data.repository.TransactionRepository
import com.kalyangupta.wallet.util.RefreshEventBus
import com.kalyangupta.wallet.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val refreshEventBus: RefreshEventBus
) : ViewModel() {

    private val _transactionsState = mutableStateOf<TransactionsState>(TransactionsState.Loading)
    val transactionsState: State<TransactionsState> = _transactionsState

    init {
        loadTransactions()
        observeRefreshEvents()
    }

    private fun observeRefreshEvents() {
        viewModelScope.launch {
            refreshEventBus.events.collectLatest { event ->
                if (event == RefreshEventBus.RefreshEvent.TRANSACTIONS || event == RefreshEventBus.RefreshEvent.ALL) {
                    loadTransactions()
                }
            }
        }
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _transactionsState.value = TransactionsState.Loading
            val result = transactionRepository.getTransactions()
            when (result) {
                is Resource.Success -> {
                    _transactionsState.value = TransactionsState.Success(result.data ?: emptyList())
                }
                is Resource.Error -> {
                    _transactionsState.value = TransactionsState.Error(result.message ?: "Unknown error")
                }
                else -> {}
            }
        }
    }

    fun deleteTransaction(id: Int) {
        val currentState = _transactionsState.value
        if (currentState is TransactionsState.Success) {
            val updatedList = currentState.transactions.filter { it.id != id }
            _transactionsState.value = TransactionsState.Success(updatedList)
        }

        viewModelScope.launch {
            val result = transactionRepository.deleteTransaction(id)
            if (result is Resource.Success) {
                refreshEventBus.publish(RefreshEventBus.RefreshEvent.TRANSACTIONS)
                refreshEventBus.publish(RefreshEventBus.RefreshEvent.ANALYTICS)
            } else {
                loadTransactions()
            }
        }
    }

    sealed class TransactionsState {
        object Loading : TransactionsState()
        data class Success(val transactions: List<TransactionDto>) : TransactionsState()
        data class Error(val message: String) : TransactionsState()
    }
}
