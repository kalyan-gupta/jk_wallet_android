package com.kalyangupta.wallet.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String
)
