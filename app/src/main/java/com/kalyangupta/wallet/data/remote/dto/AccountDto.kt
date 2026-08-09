package com.kalyangupta.wallet.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class AccountDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("account_type") val accountType: String,
    @SerializedName("institution_name") val institutionName: String?,
    @SerializedName("account_number_last4") val accountNumberLast4: String?,
    @SerializedName("current_balance") val currentBalance: BigDecimal,
    @SerializedName("credit_limit") val creditLimit: BigDecimal?,
    @SerializedName("card_due_date") val cardDueDate: Int?,
    @SerializedName("invested_amount") val investedAmount: BigDecimal?,
    @SerializedName("color_hex") val colorHex: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
