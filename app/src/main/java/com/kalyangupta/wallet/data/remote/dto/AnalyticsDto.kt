package com.kalyangupta.wallet.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class AnalyticsDto(
    @SerializedName("net_worth") val netWorth: BigDecimal,
    @SerializedName("total_six_month_income") val totalSixMonthIncome: BigDecimal,
    @SerializedName("total_six_month_expense") val totalSixMonthExpense: BigDecimal,
    @SerializedName("net_savings_margin") val netSavingsMargin: BigDecimal,
    @SerializedName("average_saving_rate_percent") val averageSavingRatePercent: Int,
    @SerializedName("top_category") val topCategory: String,
    @SerializedName("top_category_amount") val topCategoryAmount: BigDecimal,
    @SerializedName("monthly_income") val monthlyIncome: List<MonthlyData>,
    @SerializedName("monthly_expense") val monthlyExpense: List<MonthlyData>,
    @SerializedName("category_breakdown") val categoryBreakdown: List<CategoryBreakdown>
)

data class MonthlyData(
    @SerializedName("month") val month: String,
    @SerializedName("amount") val amount: BigDecimal
)

data class CategoryBreakdown(
    @SerializedName("category_code") val categoryCode: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("amount") val amount: BigDecimal
)
