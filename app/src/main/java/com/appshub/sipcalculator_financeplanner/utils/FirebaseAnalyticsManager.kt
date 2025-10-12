package com.appshub.sipcalculator_financeplanner.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsManager private constructor(context: Context) {
    
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    
    companion object {
        @Volatile
        private var INSTANCE: FirebaseAnalyticsManager? = null
        
        fun getInstance(context: Context): FirebaseAnalyticsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseAnalyticsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Screen View Events
    fun logScreenView(screenName: String, screenClass: String? = null) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { putString(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
    
    // SIP Calculator Events
    fun logSIPCalculation(
        monthlyInvestment: Double,
        expectedReturn: Double,
        timePeriod: Int,
        maturityAmount: Double
    ) {
        val bundle = Bundle().apply {
            putDouble("monthly_investment", monthlyInvestment)
            putDouble("expected_return", expectedReturn)
            putInt("time_period_years", timePeriod)
            putDouble("maturity_amount", maturityAmount)
        }
        firebaseAnalytics.logEvent("sip_calculation", bundle)
    }
    
    // SWP Calculator Events
    fun logSWPCalculation(
        initialInvestment: Double,
        monthlyWithdrawal: Double,
        expectedReturn: Double,
        timePeriod: Int
    ) {
        val bundle = Bundle().apply {
            putDouble("initial_investment", initialInvestment)
            putDouble("monthly_withdrawal", monthlyWithdrawal)
            putDouble("expected_return", expectedReturn)
            putInt("time_period_years", timePeriod)
        }
        firebaseAnalytics.logEvent("swp_calculation", bundle)
    }
    
    // Goal Calculator Events
    fun logGoalCalculation(
        goalAmount: Double,
        currentAge: Int,
        retirementAge: Int,
        expectedReturn: Double,
        monthlyInvestmentRequired: Double
    ) {
        val bundle = Bundle().apply {
            putDouble("goal_amount", goalAmount)
            putInt("current_age", currentAge)
            putInt("retirement_age", retirementAge)
            putDouble("expected_return", expectedReturn)
            putDouble("monthly_investment_required", monthlyInvestmentRequired)
        }
        firebaseAnalytics.logEvent("goal_calculation", bundle)
    }
    
    // Button Click Events
    fun logButtonClick(buttonName: String, screenName: String) {
        val bundle = Bundle().apply {
            putString("button_name", buttonName)
            putString("screen_name", screenName)
        }
        firebaseAnalytics.logEvent("button_click", bundle)
    }
    
    // Navigation Events
    fun logNavigation(fromScreen: String, toScreen: String) {
        val bundle = Bundle().apply {
            putString("from_screen", fromScreen)
            putString("to_screen", toScreen)
        }
        firebaseAnalytics.logEvent("navigation", bundle)
    }
    
    // Generic Custom Event
    fun logCustomEvent(eventName: String, parameters: Map<String, Any>) {
        val bundle = Bundle().apply {
            parameters.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Double -> putDouble(key, value)
                    is Float -> putFloat(key, value)
                    is Long -> putLong(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
}