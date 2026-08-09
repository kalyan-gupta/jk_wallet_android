package com.kalyangupta.wallet.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefreshEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<RefreshEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    suspend fun publish(event: RefreshEvent) {
        _events.emit(event)
    }

    enum class RefreshEvent {
        ACCOUNTS, TRANSACTIONS, BUDGETS, DEBTS, ANALYTICS, ALL
    }
}
