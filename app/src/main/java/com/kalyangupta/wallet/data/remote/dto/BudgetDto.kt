package com.kalyangupta.wallet.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class BudgetDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("user") val userId: Int? = null,
    @SerializedName("category") val category: String,
    @SerializedName("amount_limit") val amountLimit: BigDecimal,
    @SerializedName("month") val month: Int,
    @SerializedName("year") val year: Int,
    @SerializedName("category_display") val categoryDisplay: String? = null
)
