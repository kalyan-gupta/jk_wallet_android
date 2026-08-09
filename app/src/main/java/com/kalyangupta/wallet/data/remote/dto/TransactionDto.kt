package com.kalyangupta.wallet.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class TransactionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("transaction_type") val transactionType: String,
    @SerializedName("category") val category: String,
    @SerializedName("amount") val amount: BigDecimal,
    @SerializedName("source_account") val sourceAccountId: Int?,
    @SerializedName("destination_account") val destinationAccountId: Int?,
    @SerializedName("recipient_name") val recipientName: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("date") val date: String,
    @SerializedName("created_at") val createdAt: String
)
