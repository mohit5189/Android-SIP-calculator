package com.appshub.sipcalculator_financeplanner.data.models

import kotlin.math.pow

data class SIPResult(
    val monthlyInvestment: Double,
    val totalInvested: Double,
    val maturityAmount: Double,
    val totalGains: Double,
    val annualReturnRate: Double,
    val durationInYears: Int,
    val stepUpPercentage: Double = 0.0,
    val yearWiseData: List<YearWiseData>
)

data class SWPResult(
    val initialCorpus: Double,
    val monthlyWithdrawal: Double,
    val remainingCorpus: Double,
    val totalWithdrawn: Double,
    val annualReturnRate: Double,
    val durationInYears: Int,
    val stepUpPercentage: Double = 0.0,
    val yearWiseData: List<YearWiseData>,
    val corpusExhaustionWarning: Boolean
)

data class GoalResult(
    val targetAmount: Double,
    val requiredMonthlySIP: Double,
    val timeHorizon: Int,
    val initialAmount: Double,
    val expectedReturn: Double,
    val stepUpPercentage: Double,
    val goalAchievementProbability: Double,
    val milestones: List<Milestone>
)

data class YearWiseData(
    val year: Int,
    val openingBalance: Double,
    val totalInvestedThisYear: Double,
    val interestEarnedThisYear: Double,
    val closingBalance: Double,
    val cumulativeInvested: Double,
    val cumulativeInterest: Double,
    val monthlyAmount: Double,
    val monthlyData: List<MonthlyData>
)

data class MonthlyData(
    val month: Int,
    val monthName: String,
    val openingBalance: Double,
    val sipAmount: Double,
    val interestEarned: Double,
    val closingBalance: Double
)

data class Milestone(
    val percentage: Int,
    val targetAmount: Double,
    val timeToReach: Int,
    val isAchieved: Boolean
)

enum class CalculationType {
    SIP, SWP, GOAL
}