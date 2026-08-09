package com.kalyangupta.wallet.data.repository

import com.kalyangupta.wallet.data.remote.ApiService
import com.kalyangupta.wallet.data.remote.dto.AdminUserDto
import com.kalyangupta.wallet.data.remote.dto.RegistrationToggleResponse
import com.kalyangupta.wallet.util.NetworkUtils
import com.kalyangupta.wallet.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAdminUsers(): Resource<List<AdminUserDto>> {
        return try {
            NetworkUtils.handleResponse(apiService.getAdminUsers())
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun getRegistrationStatus(): Resource<Boolean> {
        return try {
            val resource = NetworkUtils.handleResponse(apiService.getRegistrationStatus())
            when (resource) {
                is Resource.Success -> Resource.Success(resource.data!!.registrationEnabled)
                is Resource.Error -> Resource.Error(resource.message ?: "Failed to fetch status")
                else -> Resource.Error("Unexpected state")
            }
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun toggleRegistration(): Resource<Boolean> {
        return try {
            val resource = NetworkUtils.handleResponse(apiService.toggleRegistration())
            when (resource) {
                is Resource.Success -> Resource.Success(resource.data!!.registrationEnabled)
                is Resource.Error -> Resource.Error(resource.message ?: "Failed to toggle registration")
                else -> Resource.Error("Unexpected state")
            }
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun toggleUserStatus(id: Int): Resource<AdminUserDto> {
        return try {
            NetworkUtils.handleResponse(apiService.toggleUserStatus(id))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun deleteUser(id: Int): Resource<Unit> {
        return try {
            NetworkUtils.handleResponse(apiService.deleteUser(id))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }
}
