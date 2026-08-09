package com.kalyangupta.wallet.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Accounts : Screen("accounts")
    object Transactions : Screen("transactions")
    object AccountDetail : Screen("account_detail/{accountId}") {
        fun createRoute(accountId: Int) = "account_detail/$accountId"
    }
    object AccountEdit : Screen("account_edit/{accountId}") {
        fun createRoute(accountId: Int) = "account_edit/$accountId"
    }
    object TransactionEdit : Screen("transaction_edit/{transactionId}") {
        fun createRoute(transactionId: Int) = "transaction_edit/$transactionId"
    }
    object Analytics : Screen("analytics")
    object Budgets : Screen("budgets")
    object Debts : Screen("debts")
    object AdminPanel : Screen("admin_panel")
    object Profile : Screen("profile")
    object BudgetEdit : Screen("budget_edit/{budgetId}") {
        fun createRoute(budgetId: Int) = "budget_edit/$budgetId"
    }
    object DebtEdit : Screen("debt_edit/{debtId}") {
        fun createRoute(debtId: Int) = "debt_edit/$debtId"
    }
}
