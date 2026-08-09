package com.kalyangupta.wallet.data.repository

import com.kalyangupta.wallet.data.remote.ApiService
import com.kalyangupta.wallet.data.remote.dto.BudgetDto
import com.kalyangupta.wallet.util.NetworkUtils
import com.kalyangupta.wallet.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getBudgets(): Resource<List<BudgetDto>> {
        return try {
            NetworkUtils.handleResponse(apiService.getBudgets())
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun createBudget(budget: BudgetDto): Resource<BudgetDto> {
        return try {
            NetworkUtils.handleResponse(apiService.createBudget(budget))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun updateBudget(id: Int, budget: BudgetDto): Resource<BudgetDto> {
        return try {
            NetworkUtils.handleResponse(apiService.updateBudget(id, budget))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun deleteBudget(id: Int): Resource<Unit> {
        return try {
            NetworkUtils.handleResponse(apiService.deleteBudget(id))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }
}
