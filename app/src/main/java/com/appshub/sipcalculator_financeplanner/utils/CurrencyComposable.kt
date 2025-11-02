package com.appshub.sipcalculator_financeplanner.utils

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun formatCurrencyWithContext(amount: Double): String {
    val context = LocalContext.current
    val currencyManager = remember { CurrencyManager.getInstance(context) }
    val selectedCurrency by currencyManager.getCurrencyFlow().collectAsStateWithLifecycle(initialValue = "INR")
    
    return formatCurrency(amount, selectedCurrency)
}

// Create a composable that provides currency context to its children
@Composable
fun CurrencyProvider(content: @Composable (String) -> Unit) {
    val context = LocalContext.current
    val currencyManager = remember { CurrencyManager.getInstance(context) }
    val selectedCurrency by currencyManager.getCurrencyFlow().collectAsStateWithLifecycle(initialValue = "INR")
    
    content(selectedCurrency)
}

// Extension function to make it easier to use
@Composable
fun Double.toCurrencyString(): String = formatCurrencyWithContext(this)