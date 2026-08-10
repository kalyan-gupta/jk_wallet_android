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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class AccountEditViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val refreshEventBus: RefreshEventBus,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _name = mutableStateOf("")
    val name: State<String> = _name

    private val _accountType = mutableStateOf("BANK")
    val accountType: State<String> = _accountType

    private val _institutionName = mutableStateOf("")
    val institutionName: State<String> = _institutionName

    private val _currentBalance = mutableStateOf("")
    val currentBalance: State<String> = _currentBalance

    private val _creditLimit = mutableStateOf("")
    val creditLimit: State<String> = _creditLimit

    private val _cardDueDate = mutableStateOf("")
    val cardDueDate: State<String> = _cardDueDate

    private val _investedAmount = mutableStateOf("")
    val investedAmount: State<String> = _investedAmount

    private val _notes = mutableStateOf("")
    val notes: State<String> = _notes

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isEditMode = mutableStateOf(false)
    val isEditMode: State<Boolean> = _isEditMode

    private val _eventFlow = MutableSharedFlow<EditEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentAccountId: Int? = null

    init {
        savedStateHandle.get<Int>("accountId")?.let { id ->
            if (id != -1) {
                currentAccountId = id
                _isEditMode.value = true
                loadAccount(id)
            }
        }
    }

    private fun loadAccount(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = accountRepository.getAccount(id)
            _isLoading.value = false
            if (result is Resource.Success) {
                result.data?.let {
                    _name.value = it.name
                    _accountType.value = it.accountType
                    _institutionName.value = it.institutionName ?: ""
                    _currentBalance.value = it.currentBalance.toString()
                    _creditLimit.value = it.creditLimit?.toString() ?: ""
                    _cardDueDate.value = it.cardDueDate?.toString() ?: ""
                    _investedAmount.value = it.investedAmount?.toString() ?: ""
                    _notes.value = it.notes ?: ""
                }
            }
        }
    }

    fun onNameChange(value: String) { _name.value = value }
    fun onAccountTypeChange(value: String) { _accountType.value = value }
    fun onInstitutionNameChange(value: String) { _institutionName.value = value }
    fun onCurrentBalanceChange(value: String) { _currentBalance.value = value }
    fun onCreditLimitChange(value: String) { _creditLimit.value = value }
    fun onCardDueDateChange(value: String) { _cardDueDate.value = value }
    fun onInvestedAmountChange(value: String) { _investedAmount.value = value }
    fun onNotesChange(value: String) { _notes.value = value }

    fun saveAccount() {
        val nameVal = _name.value
        val currentBalanceVal = _currentBalance.value.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val creditLimitVal = _creditLimit.value.toBigDecimalOrNull()
        val cardDueDateVal = _cardDueDate.value.toIntOrNull()
        val investedAmountVal = _investedAmount.value.toBigDecimalOrNull()
        
        if (nameVal.isBlank()) return
        
        _isLoading.value = true
        viewModelScope.launch {
            val accountDto = AccountDto(
                id = currentAccountId ?: 0,
                name = nameVal,
                accountType = _accountType.value,
                institutionName = _institutionName.value.ifBlank { null },
                accountNumberLast4 = null,
                currentBalance = currentBalanceVal,
                creditLimit = creditLimitVal,
                cardDueDate = cardDueDateVal,
                investedAmount = investedAmountVal,
                colorHex = "#6366f1", // Default color
                icon = when(_accountType.value) {
                    "BANK" -> "fa-university"
                    "CASH" -> "fa-wallet"
                    "WALLET" -> "fa-mobile-alt"
                    "CREDIT_CARD" -> "fa-credit-card"
                    "DEMAT" -> "fa-chart-line"
                    else -> "fa-wallet"
                },
                notes = _notes.value.ifBlank { null },
                isActive = true,
                createdAt = "",
                updatedAt = ""
            )

            val result = if (currentAccountId != null) {
                accountRepository.updateAccount(currentAccountId!!, accountDto)
            } else {
                accountRepository.createAccount(accountDto)
            }
            
            _isLoading.value = false
            when (result) {
                is Resource.Success -> {
                    refreshEventBus.publish(RefreshEventBus.RefreshEvent.ACCOUNTS)
                    refreshEventBus.publish(RefreshEventBus.RefreshEvent.ANALYTICS)
                    _eventFlow.emit(EditEvent.Saved)
                }
                is Resource.Error -> _eventFlow.emit(EditEvent.Error(result.message ?: "Save failed"))
                else -> {}
            }
        }
    }

    fun deleteAccount() {
        currentAccountId?.let { id ->
            _isLoading.value = true
            viewModelScope.launch {
                val result = accountRepository.deleteAccount(id)
                _isLoading.value = false
                if (result is Resource.Success) {
                    refreshEventBus.publish(RefreshEventBus.RefreshEvent.ACCOUNTS)
                    refreshEventBus.publish(RefreshEventBus.RefreshEvent.ANALYTICS)
                    _eventFlow.emit(EditEvent.Deleted)
                } else {
                    _eventFlow.emit(EditEvent.Error(result.message ?: "Delete failed"))
                }
            }
        }
    }

    sealed class EditEvent {
        object Saved : EditEvent()
        object Deleted : EditEvent()
        data class Error(val message: String) : EditEvent()
    }
}
