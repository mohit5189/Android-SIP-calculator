package com.appshub.sipcalculator_financeplanner.utils

import com.ibm.icu.text.NumberFormat
import java.util.*

fun formatCurrency(amount: Double, currency: String = "₹"): String {
    val formatter = NumberFormat.getInstance(Locale("en", "IN"))
    return "$currency ${formatter.format(amount.toLong())}"
}

fun formatPercentage(percentage: Double): String {
    return "${String.format("%.1f", percentage)}%"
}

fun formatNumber(number: Double): String {
    val formatter = NumberFormat.getInstance(Locale("en", "IN"))
    return formatter.format(number.toLong())
}