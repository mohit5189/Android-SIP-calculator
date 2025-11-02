package com.appshub.sipcalculator_financeplanner.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.appshub.sipcalculator_financeplanner.data.preferences.CurrencyInfo
import com.appshub.sipcalculator_financeplanner.data.preferences.CurrencyPreferences

class CurrencyManager(private val context: Context) {
    private val preferences = CurrencyPreferences(context)
    
    fun getCurrencyFlow() = preferences.selectedCurrencyCode
    
    suspend fun setCurrency(currencyCode: String) {
        preferences.setCurrencyCode(currencyCode)
    }
    
    companion object {
        @Volatile
        private var INSTANCE: CurrencyManager? = null
        
        fun getInstance(context: Context): CurrencyManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CurrencyManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

@Composable
fun formatCurrencyComposable(amount: Double): String {
    // This would require context injection which is complex
    // For now, keep the existing function signature
    return formatCurrency(amount)
}

// Extension function to format currency with current app currency
fun Double.formatWithCurrency(currencyCode: String = "INR"): String {
    return formatCurrency(this, currencyCode)
}