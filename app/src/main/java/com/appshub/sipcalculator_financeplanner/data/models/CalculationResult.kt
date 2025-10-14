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

data class EMIResult(
    val loanAmount: Double,
    val interestRate: Double,
    val loanTenure: Int, // in months
    val emi: Double,
    val totalAmount: Double,
    val totalInterest: Double,
    val yearWiseData: List<EMIYearData>
)

data class EMIYearData(
    val year: Int,
    val openingBalance: Double,
    val totalPayment: Double,
    val principalPayment: Double,
    val interestPayment: Double,
    val closingBalance: Double,
    val monthlyData: List<EMIMonthData>
)

data class EMIMonthData(
    val month: Int,
    val monthName: String,
    val openingBalance: Double,
    val emi: Double,
    val principalPayment: Double,
    val interestPayment: Double,
    val closingBalance: Double
)

data class RDResult(
    val monthlyDeposit: Double,
    val interestRate: Double,
    val tenure: Int, // in years
    val maturityAmount: Double,
    val totalDeposits: Double,
    val totalInterest: Double,
    val yearWiseData: List<RDYearData>
)

data class RDYearData(
    val year: Int,
    val openingBalance: Double,
    val totalDeposits: Double,
    val interestEarned: Double,
    val closingBalance: Double,
    val cumulativeDeposits: Double,
    val cumulativeInterest: Double
)

data class PPFResult(
    val yearlyDeposit: Double,
    val interestRate: Double,
    val tenure: Int, // fixed 15 years for PPF
    val maturityAmount: Double,
    val totalDeposits: Double,
    val totalInterest: Double,
    val yearWiseData: List<PPFYearData>
)

data class PPFYearData(
    val year: Int,
    val openingBalance: Double,
    val deposit: Double,
    val interestEarned: Double,
    val closingBalance: Double,
    val cumulativeDeposits: Double,
    val cumulativeInterest: Double
)

data class FDResult(
    val principal: Double,
    val interestRate: Double,
    val tenure: Int, // in months
    val maturityAmount: Double,
    val totalInterest: Double,
    val monthlyInterest: Double,
    val isCompoundInterest: Boolean
)

enum class CalculationType {
    SIP, SWP, GOAL, SIMPLE_INTEREST, COMPOUND_INTEREST, EMI, RD, PPF, FD
}