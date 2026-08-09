package com.kalyangupta.wallet.data.repository

import com.kalyangupta.wallet.data.remote.ApiService
import com.kalyangupta.wallet.data.remote.dto.DebtDto
import com.kalyangupta.wallet.data.remote.dto.SettleDebtRequest
import com.kalyangupta.wallet.util.NetworkUtils
import com.kalyangupta.wallet.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebtRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getDebts(): Resource<List<DebtDto>> {
        return try {
            NetworkUtils.handleResponse(apiService.getDebts())
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun createDebt(debt: DebtDto): Resource<DebtDto> {
        return try {
            NetworkUtils.handleResponse(apiService.createDebt(debt))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun updateDebt(id: Int, debt: DebtDto): Resource<DebtDto> {
        return try {
            NetworkUtils.handleResponse(apiService.updateDebt(id, debt))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun settleDebt(id: Int, accountId: Int): Resource<DebtDto> {
        return try {
            NetworkUtils.handleResponse(apiService.settleDebt(id, SettleDebtRequest(accountId)))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun unsettleDebt(id: Int): Resource<DebtDto> {
        return try {
            NetworkUtils.handleResponse(apiService.unsettleDebt(id))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun deleteDebt(id: Int): Resource<Unit> {
        return try {
            NetworkUtils.handleResponse(apiService.deleteDebt(id))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }
}
