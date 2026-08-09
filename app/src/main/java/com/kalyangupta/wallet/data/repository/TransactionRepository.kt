package com.kalyangupta.wallet.data.repository

import com.kalyangupta.wallet.data.remote.ApiService
import com.kalyangupta.wallet.data.remote.dto.TransactionDto
import com.kalyangupta.wallet.util.NetworkUtils
import com.kalyangupta.wallet.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getTransactions(
        type: String? = null,
        category: String? = null,
        accountId: Int? = null,
        query: String? = null
    ): Resource<List<TransactionDto>> {
        return try {
            NetworkUtils.handleResponse(
                apiService.getTransactions(
                    type = type,
                    category = category,
                    accountId = accountId,
                    query = query
                )
            )
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun createTransaction(transaction: TransactionDto): Resource<TransactionDto> {
        return try {
            NetworkUtils.handleResponse(apiService.createTransaction(transaction))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun getTransaction(id: Int): Resource<TransactionDto> {
        return try {
            NetworkUtils.handleResponse(apiService.getTransaction(id))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun updateTransaction(id: Int, transaction: TransactionDto): Resource<TransactionDto> {
        return try {
            NetworkUtils.handleResponse(apiService.updateTransaction(id, transaction))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun deleteTransaction(id: Int): Resource<Unit> {
        return try {
            NetworkUtils.handleResponse(apiService.deleteTransaction(id))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }
}
