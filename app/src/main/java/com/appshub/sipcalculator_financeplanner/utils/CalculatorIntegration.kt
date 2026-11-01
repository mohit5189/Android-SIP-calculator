package com.appshub.sipcalculator_financeplanner.utils

import androidx.compose.runtime.staticCompositionLocalOf

enum class CalculationType {
    SIP, SWP, GOAL, SIMPLE_INTEREST, COMPOUND_INTEREST, EMI, RD, PPF, FD
}

// Hosts explicitly opt into calculation events; embedded calculators do not load ads.
val LocalCalculationHandler = staticCompositionLocalOf<(CalculationType) -> Unit> { {} }
val LocalCurrencyManager = staticCompositionLocalOf<CurrencyManager?> { null }
val LocalHostCurrencyCode = staticCompositionLocalOf<String?> { null }
