package com.kalyangupta.wallet.ui.debt

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalyangupta.wallet.data.remote.dto.AccountDto
import com.kalyangupta.wallet.data.remote.dto.DebtDto
import com.kalyangupta.wallet.data.repository.AccountRepository
import com.kalyangupta.wallet.data.repository.DebtRepository
import com.kalyangupta.wallet.util.RefreshEventBus
import com.kalyangupta.wallet.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DebtEditViewModel @Inject constructor(
    private val debtRepository: DebtRepository,
    private val accountRepository: AccountRepository,
    private val refreshEventBus: RefreshEventBus,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _personName = mutableStateOf("")
    val personName: State<String> = _personName

    private val _debtType = mutableStateOf("LENT")
    val debtType: State<String> = _debtType

    private val _amount = mutableStateOf("")
    val amount: State<String> = _amount

    private val _accountId = mutableStateOf<Int?>(null)
    val accountId: State<Int?> = _accountId

    private val _date = mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val date: State<String> = _date

    private val _description = mutableStateOf("")
    val description: State<String> = _description

    private val _accounts = mutableStateOf<List<AccountDto>>(emptyList())
    val accounts: State<List<AccountDto>> = _accounts

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentDebtId: Int = -1

    init {
        loadAccounts()
        savedStateHandle.get<Int>("debtId")?.let { id ->
            if (id != -1) {
                currentDebtId = id
                loadDebt(id)
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

    private fun loadDebt(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = debtRepository.getDebts()
            _isLoading.value = false
            if (result is Resource.Success) {
                val debt = result.data?.find { it.id == id }
                debt?.let {
                    _personName.value = it.personName
                    _debtType.value = it.debtType
                    _amount.value = it.amount.toString()
                    _accountId.value = it.accountId
                    _date.value = it.date ?: ""
                    _description.value = it.description ?: ""
                }
            }
        }
    }

    fun onPersonNameChange(value: String) { _personName.value = value }
    fun onDebtTypeChange(value: String) { _debtType.value = value }
    fun onAmountChange(value: String) { _amount.value = value }
    fun onAccountChange(id: Int?) { _accountId.value = id }
    fun onDateChange(value: String) { _date.value = value }
    fun onDescriptionChange(value: String) { _description.value = value }

    fun saveDebt() {
        val amountVal = _amount.value.toBigDecimalOrNull() ?: return
        
        _isLoading.value = true
        viewModelScope.launch {
            val debtDto = DebtDto(
                id = if (currentDebtId == -1) 0 else currentDebtId,
                personName = _personName.value,
                debtType = _debtType.value,
                amount = amountVal,
                accountId = _accountId.value,
                date = _date.value.ifBlank { null },
                description = _description.value.ifBlank { null },
                isSettled = false
            )
            
            val result = if (currentDebtId == -1) {
                debtRepository.createDebt(debtDto)
            } else {
                debtRepository.updateDebt(currentDebtId, debtDto)
            }
            
            _isLoading.value = false
            if (result is Resource.Success) {
                refreshEventBus.publish(RefreshEventBus.RefreshEvent.DEBTS)
                refreshEventBus.publish(RefreshEventBus.RefreshEvent.ANALYTICS)
                refreshEventBus.publish(RefreshEventBus.RefreshEvent.ACCOUNTS)
                _eventFlow.emit(UiEvent.Success)
            } else {
                _eventFlow.emit(UiEvent.Error(result.message ?: "Failed to save entry"))
            }
        }
    }

    sealed class UiEvent {
        object Success : UiEvent()
        data class Error(val message: String) : UiEvent()
    }
}
