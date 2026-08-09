package com.kalyangupta.wallet.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class DebtDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("user") val userId: Int? = null,
    @SerializedName("person_name") val personName: String,
    @SerializedName("debt_type") val debtType: String,
    @SerializedName("amount") val amount: BigDecimal,
    @SerializedName("account") val accountId: Int?,
    @SerializedName("date") val date: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("is_settled") val isSettled: Boolean,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class SettleDebtRequest(
    @SerializedName("settle_account") val settleAccountId: Int
)
