package com.appshub.sipcalculator_financeplanner.utils

import com.ibm.icu.text.NumberFormat
import com.appshub.sipcalculator_financeplanner.data.preferences.CurrencyInfo
import java.util.*

fun formatCurrency(amount: Double, currencyCode: String = "INR"): String {
    val currency = CurrencyInfo.getCurrencyByCode(currencyCode) ?: CurrencyInfo.getCurrencyByCode("INR")!!
    
    // Use consistent Indian locale formatting regardless of currency
    // This ensures amounts are displayed as-is with consistent formatting
    val locale = Locale("en", "IN")
    val formatter = NumberFormat.getInstance(locale)
    val formattedAmount = formatter.format(amount.toLong())
    
    return "${currency.symbol} $formattedAmount"
}

fun formatPercentage(percentage: Double): String {
    return "${String.format("%.1f", percentage)}%"
}

fun formatNumber(number: Double): String {
    val formatter = NumberFormat.getInstance(Locale("en", "IN"))
    return formatter.format(number.toLong())
}