package com.appshub.sipcalculator_financeplanner.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "currency_preferences")

class CurrencyPreferences(context: Context, private val store: DataStore<Preferences> = context.dataStore) {
    
    companion object {
        private val CURRENCY_CODE_KEY = stringPreferencesKey("selected_currency_code")
        const val DEFAULT_CURRENCY = "INR"
    }
    
    val selectedCurrencyCode: Flow<String> = store.data
        .map { preferences ->
            preferences[CURRENCY_CODE_KEY] ?: DEFAULT_CURRENCY
        }
    
    suspend fun setCurrencyCode(currencyCode: String) {
        store.edit { preferences ->
            preferences[CURRENCY_CODE_KEY] = currencyCode
        }
    }
}

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val flag: String
) {
    companion object {
        fun getSupportedCurrencies(): List<CurrencyInfo> {
            return listOf(
                CurrencyInfo("INR", "Indian Rupee", "₹", "🇮🇳"),
                CurrencyInfo("USD", "US Dollar", "$", "🇺🇸"),
                CurrencyInfo("EUR", "Euro", "€", "🇪🇺"),
                CurrencyInfo("GBP", "British Pound", "£", "🇬🇧"),
                CurrencyInfo("AUD", "Australian Dollar", "A$", "🇦🇺"),
                CurrencyInfo("CAD", "Canadian Dollar", "C$", "🇨🇦"),
                CurrencyInfo("JPY", "Japanese Yen", "¥", "🇯🇵"),
                CurrencyInfo("CNY", "Chinese Yuan", "CN¥", "🇨🇳"),
                CurrencyInfo("SGD", "Singapore Dollar", "S$", "🇸🇬"),
                CurrencyInfo("AED", "UAE Dirham", "AED", "🇦🇪"),
                CurrencyInfo("SAR", "Saudi Riyal", "SAR", "🇸🇦"),
                CurrencyInfo("ZAR", "South African Rand", "ZAR", "🇿🇦"),
                CurrencyInfo("BRL", "Brazilian Real", "R$", "🇧🇷"),
                CurrencyInfo("MXN", "Mexican Peso", "MX$", "🇲🇽")
            )
        }
        
        fun getCurrencyByCode(code: String): CurrencyInfo? {
            return getSupportedCurrencies().find { it.code == code } ?: try {
                java.util.Currency.getInstance(code).let {
                    CurrencyInfo(it.currencyCode, it.getDisplayName(java.util.Locale.ENGLISH),
                        it.getSymbol(java.util.Locale.US), "")
                }
            } catch (_: IllegalArgumentException) { null }
        }
    }
}