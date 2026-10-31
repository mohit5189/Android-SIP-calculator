package com.appshub.sipcalculator_financeplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.appshub.sipcalculator_financeplanner.ui.theme.SIPCalculatorFInancePlannerTheme
import com.appshub.sipcalculator_financeplanner.utils.AdManager
import com.appshub.sipcalculator_financeplanner.utils.FirebaseAnalyticsManager
import com.appshub.sipcalculator_financeplanner.utils.LocalCalculationHandler
import com.appshub.sipcalculator_financeplanner.utils.RatingManager
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MobileAds.initialize(this) { }
        AdManager.getInstance().showAppOpenAdIfAvailable(this)
        setContent {
            SIPCalculatorFInancePlannerTheme {
                CompositionLocalProvider(LocalCalculationHandler provides { type ->
                    AdManager.getInstance().onCalculationPerformed(
                        this, AdManager.Companion.CalculationType.valueOf(type.name))
                }) {
                    SIPCalculatorApp(analyticsManager = FirebaseAnalyticsManager.getInstance(this))
                }
            }
        }
        initializeManagersAsync()
    }

    private fun initializeManagersAsync() {
        Thread {
            try {
                RatingManager.getInstance().initialize(this)
                runOnUiThread {
                    try {
                        AdManager.getInstance().initializeAds(this)
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Failed to initialize ads: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Failed to initialize managers: ${e.message}")
            }
        }.start()
    }
}
