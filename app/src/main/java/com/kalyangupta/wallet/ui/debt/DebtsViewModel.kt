package com.kalyangupta.wallet.ui.debt

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalyangupta.wallet.data.remote.dto.AccountDto
import com.kalyangupta.wallet.data.remote.dto.DebtDto
import com.kalyangupta.wallet.data.repository.AccountRepository
import com.kalyangupta.wallet.data.repository.DebtRepository
import com.kalyangupta.wallet.util.RefreshEventBus
import com.kalyangupta.wallet.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtsViewModel @Inject constructor(
    private val debtRepository: DebtRepository,
    private val accountRepository: AccountRepository,
    private val refreshEventBus: RefreshEventBus
) : ViewModel() {

    private val _debtsState = mutableStateOf<DebtsState>(DebtsState.Loading)
    val debtsState: State<DebtsState> = _debtsState

    private val _accounts = mutableStateOf<List<AccountDto>>(emptyList())
    val accounts: State<List<AccountDto>> = _accounts

    init {
        loadDebts()
        loadAccounts()
        observeRefreshEvents()
    }

    private fun observeRefreshEvents() {
        viewModelScope.launch {
            refreshEventBus.events.collectLatest { event ->
                if (event == RefreshEventBus.RefreshEvent.DEBTS || event == RefreshEventBus.RefreshEvent.ALL) {
                    loadDebts()
                }
            }
        }
    }

    fun loadDebts() {
        viewModelScope.launch {
            _debtsState.value = DebtsState.Loading
            val result = debtRepository.getDebts()
            when (result) {
                is Resource.Success -> _debtsState.value = DebtsState.Success(result.data ?: emptyList())
                is Resource.Error -> _debtsState.value = DebtsState.Error(result.message ?: "Unknown error")
                else -> {}
            }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            val result = accountRepository.getAccounts()
            if (result is Resource.Success) {
                _accounts.value = result.data ?: emptyList()
            }
        }
    }

    fun settleDebt(id: Int, accountId: Int) {
        viewModelScope.launch {
            val result = debtRepository.settleDebt(id, accountId)
            if (result is Resource.Success) {
                refreshEventBus.publish(RefreshEventBus.RefreshEvent.DEBTS)
                refreshEventBus.publish(RefreshEventBus.RefreshEvent.ANALYTICS)
                refreshEventBus.publish(RefreshEventBus.RefreshEvent.ACCOUNTS)
            }
        }
    }

    fun unsettleDebt(id: Int) {
        viewModelScope.launch {
            val result = debtRepository.unsettleDebt(id)
            if (result is Resource.Success) {
                refreshEventBus.publish(RefreshEventBus.RefreshEvent.DEBTS)
                refreshEventBus.publish(RefreshEventBus.RefreshEvent.ANALYTICS)
                refreshEventBus.publish(RefreshEventBus.RefreshEvent.ACCOUNTS)
            }
        }
    }

    fun deleteDebt(id: Int) {
        val currentState = _debtsState.value
        if (currentState is DebtsState.Success) {
            val updatedList = currentState.debts.filter { it.id != id }
            _debtsState.value = DebtsState.Success(updatedList)
        }

        viewModelScope.launch {
            val result = debtRepository.deleteDebt(id)
            if (result is Resource.Success) {
                refreshEventBus.publish(RefreshEventBus.RefreshEvent.DEBTS)
                refreshEventBus.publish(RefreshEventBus.RefreshEvent.ANALYTICS)
                refreshEventBus.publish(RefreshEventBus.RefreshEvent.ACCOUNTS)
            } else {
                loadDebts()
            }
        }
    }

    sealed class DebtsState {
        object Loading : DebtsState()
        data class Success(val debts: List<DebtDto>) : DebtsState()
        data class Error(val message: String) : DebtsState()
    }
}
