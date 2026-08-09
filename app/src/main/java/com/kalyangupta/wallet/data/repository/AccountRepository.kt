package com.kalyangupta.wallet.data.repository

import com.kalyangupta.wallet.data.remote.ApiService
import com.kalyangupta.wallet.data.remote.dto.AccountDto
import com.kalyangupta.wallet.util.NetworkUtils
import com.kalyangupta.wallet.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAccounts(): Resource<List<AccountDto>> {
        return try {
            NetworkUtils.handleResponse(apiService.getAccounts())
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun getAccount(id: Int): Resource<AccountDto> {
        return try {
            NetworkUtils.handleResponse(apiService.getAccount(id))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun createAccount(account: AccountDto): Resource<AccountDto> {
        return try {
            NetworkUtils.handleResponse(apiService.createAccount(account))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun updateAccount(id: Int, account: AccountDto): Resource<AccountDto> {
        return try {
            NetworkUtils.handleResponse(apiService.updateAccount(id, account))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun deleteAccount(id: Int): Resource<Unit> {
        return try {
            NetworkUtils.handleResponse(apiService.deleteAccount(id))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }
}
