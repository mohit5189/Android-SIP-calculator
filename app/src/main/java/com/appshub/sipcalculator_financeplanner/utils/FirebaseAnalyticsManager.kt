package com.appshub.sipcalculator_financeplanner.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsManager private constructor(private val context: Context) {
    
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    private val isAnalyticsEnabled = !isDebugBuild()
    
    init {
        // Disable analytics collection for debug builds
        firebaseAnalytics.setAnalyticsCollectionEnabled(isAnalyticsEnabled)
        
        if (!isAnalyticsEnabled) {
            // Additional settings to ensure no data is sent in debug builds
            firebaseAnalytics.setSessionTimeoutDuration(0)
            firebaseAnalytics.setUserId(null)
            firebaseAnalytics.resetAnalyticsData()
            Log.d(TAG, "Firebase Analytics DISABLED for debug build")
        } else {
            Log.d(TAG, "Firebase Analytics ENABLED for release build")
        }
    }
    
    private fun isDebugBuild(): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
    
    companion object {
        private const val TAG = "FirebaseAnalytics"
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
        if (!isAnalyticsEnabled) {
            Log.d(TAG, "Analytics disabled - Skipping screen view: $screenName")
            return
        }
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
        if (!isAnalyticsEnabled) return
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
        if (!isAnalyticsEnabled) return
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
        if (!isAnalyticsEnabled) return
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
        if (!isAnalyticsEnabled) {
            Log.d(TAG, "Analytics disabled - Skipping button click: $buttonName on $screenName")
            return
        }
        val bundle = Bundle().apply {
            putString("button_name", buttonName)
            putString("screen_name", screenName)
        }
        firebaseAnalytics.logEvent("button_click", bundle)
    }
    
    // Navigation Events
    fun logNavigation(fromScreen: String, toScreen: String) {
        if (!isAnalyticsEnabled) return
        val bundle = Bundle().apply {
            putString("from_screen", fromScreen)
            putString("to_screen", toScreen)
        }
        firebaseAnalytics.logEvent("navigation", bundle)
    }
    
    // Generic Custom Event
    fun logCustomEvent(eventName: String, parameters: Map<String, Any>) {
        if (!isAnalyticsEnabled) return
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
    
    // Goal Management Events
    fun logGoalCreated(goalType: String, targetAmount: Double) {
        if (!isAnalyticsEnabled) return
        val bundle = Bundle().apply {
            putString("goal_type", goalType)
            putDouble("target_amount", targetAmount)
        }
        firebaseAnalytics.logEvent("goal_created", bundle)
    }
    
    fun logGoalDeleted(goalType: String, targetAmount: Double) {
        if (!isAnalyticsEnabled) return
        val bundle = Bundle().apply {
            putString("goal_type", goalType)
            putDouble("target_amount", targetAmount)
        }
        firebaseAnalytics.logEvent("goal_deleted", bundle)
    }
    
    // Investment Management Events
    fun logInvestmentAdded(goalId: String, amount: Double, investmentType: String) {
        if (!isAnalyticsEnabled) return
        val bundle = Bundle().apply {
            putString("goal_id", goalId)
            putDouble("amount", amount)
            putString("investment_type", investmentType)
        }
        firebaseAnalytics.logEvent("investment_added", bundle)
    }
    
    fun logInvestmentScreenView() {
        if (!isAnalyticsEnabled) return
        logScreenView("add_investment", "InvestmentScreen")
    }
    
    fun logGoalListScreenView() {
        if (!isAnalyticsEnabled) return
        logScreenView("goal_list", "GoalsListScreen")
    }
    
    fun logGoalDetailScreenView(goalId: String) {
        if (!isAnalyticsEnabled) return
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, "goal_detail")
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "GoalDashboardScreen")
            putString("goal_id", goalId)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
    
    // Debug utility to check if analytics is enabled
    fun getAnalyticsStatus(): Boolean {
        return isAnalyticsEnabled
    }
}