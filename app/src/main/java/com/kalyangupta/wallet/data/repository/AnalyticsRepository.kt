package com.kalyangupta.wallet.data.repository

import com.kalyangupta.wallet.data.remote.ApiService
import com.kalyangupta.wallet.data.remote.dto.AnalyticsDto
import com.kalyangupta.wallet.util.NetworkUtils
import com.kalyangupta.wallet.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAnalytics(): Resource<AnalyticsDto> {
        return try {
            NetworkUtils.handleResponse(apiService.getAnalytics())
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }
}
