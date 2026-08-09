package com.kalyangupta.wallet.data.repository

import com.kalyangupta.wallet.data.remote.ApiService
import com.kalyangupta.wallet.data.remote.dto.CategoryDto
import com.kalyangupta.wallet.util.NetworkUtils
import com.kalyangupta.wallet.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getCategories(): Resource<List<CategoryDto>> {
        return try {
            NetworkUtils.handleResponse(apiService.getCategories())
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun createCategory(category: CategoryDto): Resource<CategoryDto> {
        return try {
            NetworkUtils.handleResponse(apiService.createCategory(category))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun updateCategory(id: Int, category: CategoryDto): Resource<CategoryDto> {
        return try {
            NetworkUtils.handleResponse(apiService.updateCategory(id, category))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun deleteCategory(id: Int): Resource<Unit> {
        return try {
            NetworkUtils.handleResponse(apiService.deleteCategory(id))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }
}
