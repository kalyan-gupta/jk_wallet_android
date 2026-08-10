package com.kalyangupta.wallet.ui.account

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalyangupta.wallet.data.remote.dto.AccountDto
import com.kalyangupta.wallet.data.repository.AccountRepository
import com.kalyangupta.wallet.util.RefreshEventBus
import com.kalyangupta.wallet.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val refreshEventBus: RefreshEventBus,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _accountState = mutableStateOf<AccountDetailState>(AccountDetailState.Loading)
    val accountState: State<AccountDetailState> = _accountState

    private var currentAccountId: Int = -1

    init {
        savedStateHandle.get<Int>("accountId")?.let { id ->
            currentAccountId = id
            loadAccount(id)
        }
        observeRefreshEvents()
    }

    private fun observeRefreshEvents() {
        viewModelScope.launch {
            refreshEventBus.events.collectLatest { event ->
                if ((event == RefreshEventBus.RefreshEvent.ACCOUNTS || event == RefreshEventBus.RefreshEvent.ALL) && currentAccountId != -1) {
                    loadAccount(currentAccountId)
                }
            }
        }
    }

    private fun loadAccount(id: Int) {
        viewModelScope.launch {
            _accountState.value = AccountDetailState.Loading
            val result = accountRepository.getAccount(id)
            when (result) {
                is Resource.Success -> {
                    result.data?.let { account ->
                        _accountState.value = AccountDetailState.Success(account)
                    } ?: run {
                        _accountState.value = AccountDetailState.Error("Account not found")
                    }
                }
                is Resource.Error -> {
                    _accountState.value = AccountDetailState.Error(result.message ?: "Unknown error")
                }
                else -> {}
            }
        }
    }

    sealed class AccountDetailState {
        object Loading : AccountDetailState()
        data class Success(val account: AccountDto) : AccountDetailState()
        data class Error(val message: String) : AccountDetailState()
    }
}
