package com.kalyangupta.wallet.ui.budget

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalyangupta.wallet.data.remote.dto.BudgetDto
import com.kalyangupta.wallet.data.repository.BudgetRepository
import com.kalyangupta.wallet.util.RefreshEventBus
import com.kalyangupta.wallet.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BudgetEditViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val refreshEventBus: RefreshEventBus,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _category = mutableStateOf("FOOD")
    val category: State<String> = _category

    private val _amountLimit = mutableStateOf("")
    val amountLimit: State<String> = _amountLimit

    private val _month = mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val month: State<Int> = _month

    private val _year = mutableStateOf(Calendar.getInstance().get(Calendar.YEAR))
    val year: State<Int> = _year

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentBudgetId: Int = -1

    init {
        savedStateHandle.get<Int>("budgetId")?.let { id ->
            if (id != -1) {
                currentBudgetId = id
                loadBudget(id)
            }
        }
    }

    private fun loadBudget(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = budgetRepository.getBudgets()
            _isLoading.value = false
            if (result is Resource.Success) {
                val budget = result.data?.find { it.id == id }
                budget?.let {
                    _category.value = it.category
                    _amountLimit.value = it.amountLimit.toString()
                    _month.value = it.month
                    _year.value = it.year
                }
            }
        }
    }

    fun onCategoryChange(value: String) { _category.value = value }
    fun onAmountLimitChange(value: String) { _amountLimit.value = value }
    fun onMonthChange(value: Int) { _month.value = value }
    fun onYearChange(value: Int) { _year.value = value }

    fun saveBudget() {
        val amountVal = _amountLimit.value.toBigDecimalOrNull() ?: return
        
        _isLoading.value = true
        viewModelScope.launch {
            val budgetDto = BudgetDto(
                id = if (currentBudgetId == -1) 0 else currentBudgetId,
                category = _category.value,
                amountLimit = amountVal,
                month = _month.value,
                year = _year.value
            )
            
            val result = if (currentBudgetId == -1) {
                budgetRepository.createBudget(budgetDto)
            } else {
                budgetRepository.updateBudget(currentBudgetId, budgetDto)
            }
            
            _isLoading.value = false
            when (result) {
                is Resource.Success -> {
                    refreshEventBus.publish(RefreshEventBus.RefreshEvent.BUDGETS)
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
