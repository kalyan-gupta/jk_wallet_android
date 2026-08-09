package com.kalyangupta.wallet.util

import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkUtils {
    fun <T> handleException(e: Exception): Resource<T> {
        return when (e) {
            is UnknownHostException -> Resource.Error("Server is unreachable. Please check your internet connection or try again later.")
            is ConnectException -> Resource.Error("Could not connect to the server. The server might be starting up...")
            is SocketTimeoutException -> Resource.Error("Connection timed out. The server is taking too long to respond.")
            else -> Resource.Error("An unexpected error occurred: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    fun <T> handleResponse(response: Response<T>): Resource<T> {
        return if (response.isSuccessful && response.body() != null) {
            Resource.Success(response.body()!!)
        } else {
            val errorMsg = when (response.code()) {
                401 -> "Unauthorized. Please login again."
                403 -> "You don't have permission to perform this action."
                404 -> "Requested resource not found."
                500 -> "Server error. Please try again later."
                503 -> "Server is temporarily unavailable. It might be spinning up..."
                else -> "Error: ${response.message()}"
            }
            Resource.Error(errorMsg)
        }
    }
}
