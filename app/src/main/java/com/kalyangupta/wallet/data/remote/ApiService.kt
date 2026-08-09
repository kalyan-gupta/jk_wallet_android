package com.kalyangupta.wallet.data.remote

import com.kalyangupta.wallet.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/v1/auth/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/v1/auth/register/")
    suspend fun register(@Body request: RegisterRequest): Response<UserDto>

    @GET("api/v1/auth/me/")
    suspend fun getMe(): Response<UserDto>

    @PATCH("api/v1/auth/me/")
    suspend fun updateMe(@Body user: UserDto): Response<UserDto>

    @GET("api/v1/auth/registration-status/")
    suspend fun getPublicRegistrationStatus(): Response<RegistrationToggleResponse>

    // Accounts
    @GET("api/v1/accounts/")
    suspend fun getAccounts(): Response<List<AccountDto>>

    @POST("api/v1/accounts/")
    suspend fun createAccount(@Body account: AccountDto): Response<AccountDto>

    @GET("api/v1/accounts/{id}/")
    suspend fun getAccount(@Path("id") id: Int): Response<AccountDto>

    @PUT("api/v1/accounts/{id}/")
    suspend fun updateAccount(@Path("id") id: Int, @Body account: AccountDto): Response<AccountDto>

    @DELETE("api/v1/accounts/{id}/")
    suspend fun deleteAccount(@Path("id") id: Int): Response<Unit>

    // Transactions
    @GET("api/v1/transactions/")
    suspend fun getTransactions(
        @Query("transaction_type") type: String? = null,
        @Query("category") category: String? = null,
        @Query("account") accountId: Int? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("q") query: String? = null
    ): Response<List<TransactionDto>>

    @POST("api/v1/transactions/")
    suspend fun createTransaction(@Body transaction: TransactionDto): Response<TransactionDto>

    @GET("api/v1/transactions/{id}/")
    suspend fun getTransaction(@Path("id") id: Int): Response<TransactionDto>

    @PUT("api/v1/transactions/{id}/")
    suspend fun updateTransaction(@Path("id") id: Int, @Body transaction: TransactionDto): Response<TransactionDto>

    @DELETE("api/v1/transactions/{id}/")
    suspend fun deleteTransaction(@Path("id") id: Int): Response<Unit>

    // Budgets
    @GET("api/v1/budgets/")
    suspend fun getBudgets(): Response<List<BudgetDto>>

    @POST("api/v1/budgets/")
    suspend fun createBudget(@Body budget: BudgetDto): Response<BudgetDto>

    @GET("api/v1/budgets/{id}/")
    suspend fun getBudget(@Path("id") id: Int): Response<BudgetDto>

    @PUT("api/v1/budgets/{id}/")
    suspend fun updateBudget(@Path("id") id: Int, @Body budget: BudgetDto): Response<BudgetDto>

    @DELETE("api/v1/budgets/{id}/")
    suspend fun deleteBudget(@Path("id") id: Int): Response<Unit>

    // Debts
    @GET("api/v1/debts/")
    suspend fun getDebts(): Response<List<DebtDto>>

    @POST("api/v1/debts/")
    suspend fun createDebt(@Body debt: DebtDto): Response<DebtDto>

    @GET("api/v1/debts/{id}/")
    suspend fun getDebt(@Path("id") id: Int): Response<DebtDto>

    @PUT("api/v1/debts/{id}/")
    suspend fun updateDebt(@Path("id") id: Int, @Body debt: DebtDto): Response<DebtDto>

    @DELETE("api/v1/debts/{id}/")
    suspend fun deleteDebt(@Path("id") id: Int): Response<Unit>

    @POST("api/v1/debts/{id}/settle/")
    suspend fun settleDebt(@Path("id") id: Int, @Body request: SettleDebtRequest): Response<DebtDto>

    @POST("api/v1/debts/{id}/unsettle/")
    suspend fun unsettleDebt(@Path("id") id: Int): Response<DebtDto>

    // Categories
    @GET("api/v1/categories/")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @POST("api/v1/categories/")
    suspend fun createCategory(@Body category: CategoryDto): Response<CategoryDto>

    @PUT("api/v1/categories/{id}/")
    suspend fun updateCategory(@Path("id") id: Int, @Body category: CategoryDto): Response<CategoryDto>

    @DELETE("api/v1/categories/{id}/")
    suspend fun deleteCategory(@Path("id") id: Int): Response<Unit>

    // Analytics
    @GET("api/v1/analytics/")
    suspend fun getAnalytics(): Response<AnalyticsDto>

    // Admin
    @GET("api/v1/admin/users/")
    suspend fun getAdminUsers(): Response<List<AdminUserDto>>

    @GET("api/v1/admin/toggle-registration/")
    suspend fun getRegistrationStatus(): Response<RegistrationToggleResponse>

    @POST("api/v1/admin/toggle-registration/")
    suspend fun toggleRegistration(): Response<RegistrationToggleResponse>

    @POST("api/v1/admin/toggle-user/{id}/")
    suspend fun toggleUserStatus(@Path("id") id: Int): Response<AdminUserDto>

    @DELETE("api/v1/admin/delete-user/{id}/")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>
}
