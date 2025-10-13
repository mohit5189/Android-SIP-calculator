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

data class SimpleInterestResult(
    val principal: Double,
    val rate: Double,
    val time: Double,
    val simpleInterest: Double,
    val maturityAmount: Double,
    val yearWiseData: List<SimpleInterestYearData>
)

data class CompoundInterestResult(
    val principal: Double,
    val rate: Double,
    val time: Double,
    val compoundingFrequency: Int, // 1=yearly, 4=quarterly, 12=monthly
    val compoundInterest: Double,
    val maturityAmount: Double,
    val yearWiseData: List<CompoundInterestYearData>
)

data class SimpleInterestYearData(
    val year: Int,
    val yearlyInterest: Double,
    val cumulativeInterest: Double,
    val totalAmount: Double
)

data class CompoundInterestYearData(
    val year: Int,
    val openingAmount: Double,
    val yearlyInterest: Double,
    val closingAmount: Double,
    val cumulativeInterest: Double
)

enum class CalculationType {
    SIP, SWP, GOAL, SIMPLE_INTEREST, COMPOUND_INTEREST
}