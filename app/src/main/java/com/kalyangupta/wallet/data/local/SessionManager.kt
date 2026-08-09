package com.kalyangupta.wallet.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("jk_wallet_prefs", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun clearAuthToken() {
        prefs.edit().remove("auth_token").remove("is_staff").apply()
    }

    fun saveIsStaff(isStaff: Boolean) {
        prefs.edit().putBoolean("is_staff", isStaff).apply()
    }

    fun isStaff(): Boolean {
        return prefs.getBoolean("is_staff", false)
    }
}
