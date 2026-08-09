package com.kalyangupta.wallet.ui.transaction

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalyangupta.wallet.data.remote.dto.AccountDto
import com.kalyangupta.wallet.data.remote.dto.TransactionDto
import com.kalyangupta.wallet.data.repository.AccountRepository
import com.kalyangupta.wallet.data.repository.TransactionRepository
import com.kalyangupta.wallet.util.RefreshEventBus
import com.kalyangupta.wallet.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*
import java.text.SimpleDateFormat
import javax.inject.Inject

@HiltViewModel
class TransactionEditViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val refreshEventBus: RefreshEventBus,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _transactionType = mutableStateOf("EXPENSE")
    val transactionType: State<String> = _transactionType

    private val _category = mutableStateOf("OTHERS")
    val category: State<String> = _category

    private val _amount = mutableStateOf("")
    val amount: State<String> = _amount

    private val _date = mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val date: State<String> = _date

    private val _sourceAccountId = mutableStateOf<Int?>(null)
    val sourceAccountId: State<Int?> = _sourceAccountId

    private val _destinationAccountId = mutableStateOf<Int?>(null)
    val destinationAccountId: State<Int?> = _destinationAccountId

    private val _recipientName = mutableStateOf("")
    val recipientName: State<String> = _recipientName

    private val _description = mutableStateOf("")
    val description: State<String> = _description

    private val _accounts = mutableStateOf<List<AccountDto>>(emptyList())
    val accounts: State<List<AccountDto>> = _accounts

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentTransactionId: Int = -1

    init {
        loadAccounts()
        savedStateHandle.get<Int>("transactionId")?.let { id ->
            if (id != -1) {
                currentTransactionId = id
                loadTransaction(id)
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

    private fun loadTransaction(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = transactionRepository.getTransaction(id)
            _isLoading.value = false
            if (result is Resource.Success<TransactionDto>) {
                result.data?.let { dto ->
                    _transactionType.value = dto.transactionType
                    _category.value = dto.category
                    _amount.value = dto.amount.toString()
                    _date.value = dto.date
                    _sourceAccountId.value = dto.sourceAccountId
                    _destinationAccountId.value = dto.destinationAccountId
                    _recipientName.value = dto.recipientName ?: ""
                    _description.value = dto.description ?: ""
                }
            }
        }
    }

    fun onTransactionTypeChange(value: String) { _transactionType.value = value }
    fun onCategoryChange(value: String) { _category.value = value }
    fun onAmountChange(value: String) { _amount.value = value }
    fun onDateChange(value: String) { _date.value = value }
    fun onSourceAccountChange(id: Int?) { _sourceAccountId.value = id }
    fun onDestinationAccountChange(id: Int?) { _destinationAccountId.value = id }
    fun onRecipientNameChange(value: String) { _recipientName.value = value }
    fun onDescriptionChange(value: String) { _description.value = value }

    fun saveTransaction() {
        val amountVal = _amount.value.toBigDecimalOrNull() ?: return
        
        _isLoading.value = true
        viewModelScope.launch {
            val transactionDto = TransactionDto(
                id = if (currentTransactionId == -1) 0 else currentTransactionId,
                transactionType = _transactionType.value,
                category = _category.value,
                amount = amountVal,
                sourceAccountId = _sourceAccountId.value,
                destinationAccountId = _destinationAccountId.value,
                recipientName = _recipientName.value.ifBlank { null },
                description = _description.value.ifBlank { null },
                date = _date.value,
                createdAt = ""
            )
            
            val result = if (currentTransactionId == -1) {
                transactionRepository.createTransaction(transactionDto)
            } else {
                transactionRepository.updateTransaction(currentTransactionId, transactionDto)
            }
            
            _isLoading.value = false
            when (result) {
                is Resource.Success -> {
                    refreshEventBus.publish(RefreshEventBus.RefreshEvent.TRANSACTIONS)
                    refreshEventBus.publish(RefreshEventBus.RefreshEvent.ACCOUNTS)
                    refreshEventBus.publish(RefreshEventBus.RefreshEvent.ANALYTICS)
                    _eventFlow.emit(UiEvent.Success)
                }
                is Resource.Error -> _eventFlow.emit(UiEvent.Error(result.message ?: "Failed"))
                else -> {}
            }
        }
    }

    sealed class UiEvent {
        object Success : UiEvent()
        data class Error(val message: String) : UiEvent()
    }
}
