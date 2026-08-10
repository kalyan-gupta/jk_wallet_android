package com.kalyangupta.wallet

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kalyangupta.wallet.data.local.SessionManager
import com.kalyangupta.wallet.ui.Screen
import com.kalyangupta.wallet.ui.account.AccountDetailScreen
import com.kalyangupta.wallet.ui.account.AccountEditScreen
import com.kalyangupta.wallet.ui.admin.AdminPanelScreen
import com.kalyangupta.wallet.ui.analytics.AnalyticsScreen
import com.kalyangupta.wallet.ui.auth.LoginScreen
import com.kalyangupta.wallet.ui.auth.ProfileScreen
import com.kalyangupta.wallet.ui.auth.RegisterScreen
import com.kalyangupta.wallet.ui.budget.BudgetEditScreen
import com.kalyangupta.wallet.ui.budget.BudgetsScreen
import com.kalyangupta.wallet.ui.components.BottomNavigationBar
import com.kalyangupta.wallet.ui.dashboard.DashboardScreen
import com.kalyangupta.wallet.ui.debt.DebtEditScreen
import com.kalyangupta.wallet.ui.debt.DebtsScreen
import com.kalyangupta.wallet.ui.theme.JKWalletTheme
import com.kalyangupta.wallet.ui.transaction.TransactionEditScreen
import com.kalyangupta.wallet.ui.transaction.TransactionsScreen
import com.kalyangupta.wallet.util.BiometricAuthenticator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val biometricAuthenticator = BiometricAuthenticator(this)

        setContent {
            JKWalletTheme {
                var isAppUnlocked by remember {
                    mutableStateOf(!sessionManager.isBiometricEnabled() || sessionManager.getAuthToken() == null)
                }

                if (isAppUnlocked) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val showBottomBar = currentRoute != Screen.Login.route && currentRoute != "register"

                    val startDestination = remember {
                        if (sessionManager.getAuthToken() != null) Screen.Dashboard.route else Screen.Login.route
                    }

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                BottomNavigationBar(navController = navController)
                            }
                        },
                        contentWindowInsets = WindowInsets(0, 0, 0, 0)
                    ) { padding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = padding.calculateBottomPadding())
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                NavHost(
                                    navController = navController,
                                    startDestination = startDestination
                                ) {
                                    composable(Screen.Login.route) {
                                        LoginScreen(
                                            onLoginSuccess = {
                                                navController.navigate(Screen.Dashboard.route) {
                                                    popUpTo(Screen.Login.route) { inclusive = true }
                                                }
                                            },
                                            onRegisterClick = {
                                                navController.navigate("register")
                                            }
                                        )
                                    }
                                    composable("register") {
                                        RegisterScreen(
                                            onRegisterSuccess = {
                                                navController.popBackStack()
                                            },
                                            onBackClick = {
                                                navController.popBackStack()
                                            }
                                        )
                                    }
                                    composable(Screen.Dashboard.route) {
                                        DashboardScreen(
                                            onAccountClick = { accountId ->
                                                navController.navigate(Screen.AccountDetail.createRoute(accountId))
                                            },
                                            onAddAccountClick = {
                                                navController.navigate(Screen.AccountEdit.createRoute(-1))
                                            },
                                            onAnalyticsClick = {
                                                navController.navigate(Screen.Analytics.route)
                                            },
                                            onAdminClick = {
                                                navController.navigate(Screen.AdminPanel.route)
                                            },
                                            onProfileClick = {
                                                navController.navigate(Screen.Profile.route)
                                            },
                                            onLogoutClick = {
                                                sessionManager.clearAuthToken()
                                                navController.navigate(Screen.Login.route) {
                                                    popUpTo(0)
                                                }
                                            }
                                        )
                                    }
                                    composable(Screen.Transactions.route) {
                                        TransactionsScreen(
                                            onAddTransactionClick = {
                                                navController.navigate(Screen.TransactionEdit.createRoute(-1))
                                            },
                                            onTransactionClick = { transactionId ->
                                                navController.navigate(Screen.TransactionEdit.createRoute(transactionId))
                                            }
                                        )
                                    }
                                    composable(
                                        route = Screen.TransactionEdit.route,
                                        arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
                                    ) {
                                        TransactionEditScreen(onBackClick = { navController.popBackStack() })
                                    }
                                    composable(Screen.Budgets.route) {
                                        BudgetsScreen(onAddBudgetClick = {
                                            navController.navigate(Screen.BudgetEdit.createRoute(-1))
                                        })
                                    }
                                    composable(
                                        route = Screen.BudgetEdit.route,
                                        arguments = listOf(navArgument("budgetId") { type = NavType.IntType })
                                    ) {
                                        BudgetEditScreen(onBackClick = { navController.popBackStack() })
                                    }
                                    composable(Screen.Debts.route) {
                                        DebtsScreen(onAddDebtClick = {
                                            navController.navigate(Screen.DebtEdit.createRoute(-1))
                                        })
                                    }
                                    composable(
                                        route = Screen.DebtEdit.route,
                                        arguments = listOf(navArgument("debtId") { type = NavType.IntType })
                                    ) {
                                        DebtEditScreen(onBackClick = { navController.popBackStack() })
                                    }
                                    composable(Screen.Analytics.route) {
                                        AnalyticsScreen()
                                    }
                                    composable(Screen.AdminPanel.route) {
                                        AdminPanelScreen()
                                    }
                                    composable(Screen.Profile.route) {
                                        ProfileScreen(onBackClick = { navController.popBackStack() })
                                    }
                                    composable(
                                        route = Screen.AccountDetail.route,
                                        arguments = listOf(navArgument("accountId") { type = NavType.IntType })
                                    ) {
                                        AccountDetailScreen(
                                            onBackClick = { navController.popBackStack() },
                                            onEditClick = { accountId ->
                                                navController.navigate(Screen.AccountEdit.createRoute(accountId))
                                            }
                                        )
                                    }
                                    composable(
                                        route = Screen.AccountEdit.route,
                                        arguments = listOf(navArgument("accountId") { type = NavType.IntType })
                                    ) {
                                        AccountEditScreen(
                                            onBackClick = { navController.popBackStack() },
                                            onDeleteSuccess = {
                                                navController.popBackStack(Screen.Dashboard.route, false)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LockScreen(
                        onUnlockClick = {
                            biometricAuthenticator.promptBiometricAuth(
                                title = "Unlock JK Wallet",
                                subtitle = "Authenticate to access your wallet",
                                negativeButtonText = "Cancel",
                                onAuthSuccess = {
                                    isAppUnlocked = true
                                },
                                onAuthError = { _: Int, _: CharSequence ->
                                    // Handle error
                                }
                            )
                        }
                    )

                    LaunchedEffect(Unit) {
                        biometricAuthenticator.promptBiometricAuth(
                            title = "Unlock JK Wallet",
                            subtitle = "Authenticate to access your wallet",
                            negativeButtonText = "Cancel",
                            onAuthSuccess = {
                                isAppUnlocked = true
                            },
                            onAuthError = { _: Int, _: CharSequence ->
                                // Handle error
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LockScreen(onUnlockClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "JK Wallet is Locked",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Please authenticate to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            androidx.compose.material3.Button(
                onClick = onUnlockClick,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Unlock App")
            }
        }
    }
}
