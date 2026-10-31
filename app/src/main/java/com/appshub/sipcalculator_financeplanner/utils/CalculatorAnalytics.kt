package com.appshub.sipcalculator_financeplanner.utils

interface CalculatorAnalytics {
    fun logScreenView(screenName: String, screenClass: String? = null)
    fun logSIPCalculation(monthlyInvestment: Double, expectedReturn: Double, timePeriod: Int, maturityAmount: Double)
    fun logSWPCalculation(initialInvestment: Double, monthlyWithdrawal: Double, expectedReturn: Double, timePeriod: Int)
    fun logGoalCalculation(goalAmount: Double, currentAge: Int, retirementAge: Int, expectedReturn: Double, monthlyInvestmentRequired: Double)
    fun logButtonClick(buttonName: String, screenName: String)
    fun logNavigation(fromScreen: String, toScreen: String)
    fun logCustomEvent(eventName: String, parameters: Map<String, Any>)
    fun logGoalCreated(goalType: String, targetAmount: Double)
    fun logGoalDeleted(goalType: String, targetAmount: Double)
    fun logInvestmentAdded(goalId: String, amount: Double, investmentType: String)
    fun logInvestmentScreenView()
    fun logGoalListScreenView()
    fun logGoalDetailScreenView(goalId: String)
    fun getAnalyticsStatus(): Boolean
}
