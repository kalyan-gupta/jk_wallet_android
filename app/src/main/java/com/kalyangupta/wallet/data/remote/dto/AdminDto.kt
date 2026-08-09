package com.kalyangupta.wallet.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AdminUserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("is_staff") val isStaff: Boolean,
    @SerializedName("is_superuser") val isSuperuser: Boolean,
    @SerializedName("date_joined") val dateJoined: String,
    @SerializedName("account_count") val accountCount: Int,
    @SerializedName("transaction_count") val transactionCount: Int
)

data class RegistrationToggleResponse(
    @SerializedName("registration_enabled") val registrationEnabled: Boolean
)
