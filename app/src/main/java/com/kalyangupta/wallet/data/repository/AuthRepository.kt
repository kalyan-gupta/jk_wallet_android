package com.kalyangupta.wallet.data.repository

import com.kalyangupta.wallet.data.local.SessionManager
import com.kalyangupta.wallet.data.remote.ApiService
import com.kalyangupta.wallet.data.remote.dto.LoginRequest
import com.kalyangupta.wallet.data.remote.dto.RegisterRequest
import com.kalyangupta.wallet.data.remote.dto.UserDto
import com.kalyangupta.wallet.util.NetworkUtils
import com.kalyangupta.wallet.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun login(username: String, password: String): Resource<String> {
        return try {
            val resource = NetworkUtils.handleResponse(apiService.login(LoginRequest(username, password)))
            when (resource) {
                is Resource.Success -> {
                    val token = resource.data!!.token
                    sessionManager.saveAuthToken(token)

                    // Fetch user info to know if staff
                    val userResponse = apiService.getMe()
                    if (userResponse.isSuccessful && userResponse.body() != null) {
                        sessionManager.saveIsStaff(userResponse.body()!!.isStaff)
                    }

                    Resource.Success(token)
                }
                is Resource.Error -> Resource.Error(resource.message ?: "Login failed")
                else -> Resource.Error("Unexpected state")
            }
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun register(username: String, email: String, password: String): Resource<UserDto> {
        return try {
            NetworkUtils.handleResponse(apiService.register(RegisterRequest(username, email, password)))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun getRegistrationStatus(): Resource<Boolean> {
        return try {
            val resource = NetworkUtils.handleResponse(apiService.getPublicRegistrationStatus())
            when (resource) {
                is Resource.Success -> Resource.Success(resource.data!!.registrationEnabled)
                is Resource.Error -> Resource.Error(resource.message ?: "Failed to fetch status")
                else -> Resource.Error("Unexpected state")
            }
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun getMe(): Resource<UserDto> {
        return try {
            NetworkUtils.handleResponse(apiService.getMe())
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    suspend fun updateMe(user: UserDto): Resource<UserDto> {
        return try {
            NetworkUtils.handleResponse(apiService.updateMe(user))
        } catch (e: Exception) {
            NetworkUtils.handleException(e)
        }
    }

    fun logout() {
        sessionManager.clearAuthToken()
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.getAuthToken() != null
    }
}
