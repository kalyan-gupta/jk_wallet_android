package com.kalyangupta.wallet.ui.budget

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalyangupta.wallet.data.remote.dto.BudgetDto
import com.kalyangupta.wallet.data.repository.BudgetRepository
import com.kalyangupta.wallet.util.RefreshEventBus
import com.kalyangupta.wallet.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val refreshEventBus: RefreshEventBus
) : ViewModel() {

    private val _budgetsState = mutableStateOf<BudgetsState>(BudgetsState.Loading)
    val budgetsState: State<BudgetsState> = _budgetsState

    init {
        loadBudgets()
        observeRefreshEvents()
    }

    private fun observeRefreshEvents() {
        viewModelScope.launch {
            refreshEventBus.events.collectLatest { event ->
                if (event == RefreshEventBus.RefreshEvent.BUDGETS || event == RefreshEventBus.RefreshEvent.ALL) {
                    loadBudgets()
                }
            }
        }
    }

    fun loadBudgets() {
        viewModelScope.launch {
            _budgetsState.value = BudgetsState.Loading
            val result = budgetRepository.getBudgets()
            when (result) {
                is Resource.Success -> _budgetsState.value = BudgetsState.Success(result.data ?: emptyList())
                is Resource.Error -> _budgetsState.value = BudgetsState.Error(result.message ?: "Unknown error")
                else -> {}
            }
        }
    }

    fun deleteBudget(id: Int) {
        val currentState = _budgetsState.value
        if (currentState is BudgetsState.Success) {
            val updatedList = currentState.budgets.filter { it.id != id }
            _budgetsState.value = BudgetsState.Success(updatedList)
        }

        viewModelScope.launch {
            val result = budgetRepository.deleteBudget(id)
            if (result is Resource.Success) {
                refreshEventBus.publish(RefreshEventBus.RefreshEvent.BUDGETS)
            } else {
                loadBudgets()
            }
        }
    }

    sealed class BudgetsState {
        object Loading : BudgetsState()
        data class Success(val budgets: List<BudgetDto>) : BudgetsState()
        data class Error(val message: String) : BudgetsState()
    }
}
