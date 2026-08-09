package com.kalyangupta.wallet.ui.analytics

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalyangupta.wallet.data.remote.dto.AnalyticsDto
import com.kalyangupta.wallet.data.repository.AnalyticsRepository
import com.kalyangupta.wallet.util.RefreshEventBus
import com.kalyangupta.wallet.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val refreshEventBus: RefreshEventBus
) : ViewModel() {

    private val _analyticsState = mutableStateOf<AnalyticsState>(AnalyticsState.Loading)
    val analyticsState: State<AnalyticsState> = _analyticsState

    init {
        loadAnalytics()
        observeRefreshEvents()
    }

    private fun observeRefreshEvents() {
        viewModelScope.launch {
            refreshEventBus.events.collectLatest { event ->
                if (event == RefreshEventBus.RefreshEvent.ANALYTICS || event == RefreshEventBus.RefreshEvent.ALL) {
                    loadAnalytics()
                }
            }
        }
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            _analyticsState.value = AnalyticsState.Loading
            val result = analyticsRepository.getAnalytics()
            when (result) {
                is Resource.Success -> _analyticsState.value = AnalyticsState.Success(result.data!!)
                is Resource.Error -> _analyticsState.value = AnalyticsState.Error(result.message ?: "Unknown error")
                else -> {}
            }
        }
    }

    sealed class AnalyticsState {
        object Loading : AnalyticsState()
        data class Success(val analytics: AnalyticsDto) : AnalyticsState()
        data class Error(val message: String) : AnalyticsState()
    }
}
