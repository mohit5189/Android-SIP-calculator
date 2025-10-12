package com.appshub.sipcalculator_financeplanner.utils

import com.appshub.sipcalculator_financeplanner.data.models.*
import kotlin.math.pow

object FinancialCalculator {
    
    fun calculateSIP(
        monthlyInvestment: Double,
        annualReturnRate: Double,
        durationInYears: Int,
        stepUpPercentage: Double = 0.0
    ): SIPResult {
        val monthlyReturnRate = annualReturnRate / 12 / 100
        val yearWiseData = mutableListOf<YearWiseData>()
        
        var currentBalance = 0.0
        var totalInvested = 0.0
        var currentMonthlyAmount = monthlyInvestment
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                               "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        
        for (year in 1..durationInYears) {
            val yearOpeningBalance = currentBalance
            var yearlyInvested = 0.0
            var yearlyInterest = 0.0
            val monthlyDataList = mutableListOf<MonthlyData>()
            
            for (month in 1..12) {
                val monthOpeningBalance = currentBalance
                
                // Add monthly investment
                currentBalance += currentMonthlyAmount
                yearlyInvested += currentMonthlyAmount
                totalInvested += currentMonthlyAmount
                
                // Calculate interest on the balance
                val interestEarned = currentBalance * monthlyReturnRate
                currentBalance += interestEarned
                yearlyInterest += interestEarned
                
                monthlyDataList.add(
                    MonthlyData(
                        month = month,
                        monthName = monthNames[month - 1],
                        openingBalance = monthOpeningBalance,
                        sipAmount = currentMonthlyAmount,
                        interestEarned = interestEarned,
                        closingBalance = currentBalance
                    )
                )
            }
            
            val cumulativeInterest = currentBalance - totalInvested
            
            yearWiseData.add(
                YearWiseData(
                    year = year,
                    openingBalance = yearOpeningBalance,
                    totalInvestedThisYear = yearlyInvested,
                    interestEarnedThisYear = yearlyInterest,
                    closingBalance = currentBalance,
                    cumulativeInvested = totalInvested,
                    cumulativeInterest = cumulativeInterest,
                    monthlyAmount = currentMonthlyAmount,
                    monthlyData = monthlyDataList
                )
            )
            
            // Apply step-up for next year
            if (stepUpPercentage > 0) {
                currentMonthlyAmount *= (1 + stepUpPercentage / 100)
            }
        }
        
        return SIPResult(
            monthlyInvestment = monthlyInvestment,
            totalInvested = totalInvested,
            maturityAmount = currentBalance,
            totalGains = currentBalance - totalInvested,
            annualReturnRate = annualReturnRate,
            durationInYears = durationInYears,
            stepUpPercentage = stepUpPercentage,
            yearWiseData = yearWiseData
        )
    }
    
    fun calculateSWP(
        initialCorpus: Double,
        monthlyWithdrawal: Double,
        annualReturnRate: Double,
        durationInYears: Int,
        stepUpPercentage: Double = 0.0
    ): SWPResult {
        val monthlyReturnRate = annualReturnRate / 12 / 100
        val yearWiseData = mutableListOf<YearWiseData>()
        
        var currentBalance = initialCorpus
        var totalWithdrawn = 0.0
        var currentMonthlyWithdrawal = monthlyWithdrawal
        var corpusExhausted = false
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                               "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        
        for (year in 1..durationInYears) {
            val yearOpeningBalance = currentBalance
            var yearlyWithdrawn = 0.0
            var yearlyInterest = 0.0
            val monthlyDataList = mutableListOf<MonthlyData>()
            
            for (month in 1..12) {
                val monthOpeningBalance = currentBalance
                
                if (currentBalance <= currentMonthlyWithdrawal) {
                    corpusExhausted = true
                    break
                }
                
                // Withdraw monthly amount
                currentBalance -= currentMonthlyWithdrawal
                yearlyWithdrawn += currentMonthlyWithdrawal
                totalWithdrawn += currentMonthlyWithdrawal
                
                // Calculate interest on remaining balance
                val interestEarned = currentBalance * monthlyReturnRate
                currentBalance += interestEarned
                yearlyInterest += interestEarned
                
                monthlyDataList.add(
                    MonthlyData(
                        month = month,
                        monthName = monthNames[month - 1],
                        openingBalance = monthOpeningBalance,
                        sipAmount = -currentMonthlyWithdrawal, // Negative for withdrawal
                        interestEarned = interestEarned,
                        closingBalance = currentBalance
                    )
                )
            }
            
            yearWiseData.add(
                YearWiseData(
                    year = year,
                    openingBalance = yearOpeningBalance,
                    totalInvestedThisYear = -yearlyWithdrawn, // Negative for withdrawal
                    interestEarnedThisYear = yearlyInterest,
                    closingBalance = currentBalance,
                    cumulativeInvested = -totalWithdrawn, // Negative for withdrawal
                    cumulativeInterest = currentBalance + totalWithdrawn - initialCorpus,
                    monthlyAmount = currentMonthlyWithdrawal,
                    monthlyData = monthlyDataList
                )
            )
            
            // Apply step-up for next year
            if (stepUpPercentage > 0) {
                currentMonthlyWithdrawal *= (1 + stepUpPercentage / 100)
            }
            
            if (corpusExhausted) break
        }
        
        return SWPResult(
            initialCorpus = initialCorpus,
            monthlyWithdrawal = monthlyWithdrawal,
            remainingCorpus = currentBalance,
            totalWithdrawn = totalWithdrawn,
            annualReturnRate = annualReturnRate,
            durationInYears = durationInYears,
            stepUpPercentage = stepUpPercentage,
            yearWiseData = yearWiseData,
            corpusExhaustionWarning = corpusExhausted
        )
    }
    
    fun calculateGoalSIP(
        targetAmount: Double,
        timeHorizon: Int,
        initialAmount: Double = 0.0,
        expectedReturn: Double,
        stepUpPercentage: Double = 0.0
    ): GoalResult {
        val monthlyReturnRate = expectedReturn / 12 / 100
        val totalMonths = timeHorizon * 12
        
        // Calculate required monthly SIP
        val futureValueOfLumpSum = initialAmount * (1 + expectedReturn / 100).pow(timeHorizon)
        val remainingAmount = targetAmount - futureValueOfLumpSum
        
        val requiredMonthlySIP = if (stepUpPercentage == 0.0) {
            // Simple SIP calculation
            remainingAmount * monthlyReturnRate / ((1 + monthlyReturnRate).pow(totalMonths) - 1)
        } else {
            // Step-up SIP calculation (approximation)
            val stepUpFactor = (1 + stepUpPercentage / 100)
            val adjustedRate = monthlyReturnRate + stepUpPercentage / 100 / 12
            remainingAmount * adjustedRate / ((1 + adjustedRate).pow(totalMonths) - 1)
        }
        
        // Calculate milestones
        val milestones = listOf(25, 50, 75, 100).map { percentage ->
            val milestoneAmount = targetAmount * percentage / 100
            val timeToReach = calculateTimeToReachAmount(
                requiredMonthlySIP, expectedReturn, milestoneAmount, initialAmount, stepUpPercentage
            )
            Milestone(
                percentage = percentage,
                targetAmount = milestoneAmount,
                timeToReach = timeToReach,
                isAchieved = false
            )
        }
        
        // Calculate goal achievement probability (simplified)
        val goalProbability = calculateGoalProbability(expectedReturn, timeHorizon)
        
        return GoalResult(
            targetAmount = targetAmount,
            requiredMonthlySIP = requiredMonthlySIP,
            timeHorizon = timeHorizon,
            initialAmount = initialAmount,
            expectedReturn = expectedReturn,
            stepUpPercentage = stepUpPercentage,
            goalAchievementProbability = goalProbability,
            milestones = milestones
        )
    }
    
    private fun calculateSIPWithStepUp(
        monthlyInvestment: Double,
        annualReturnRate: Double,
        durationInYears: Int,
        stepUpPercentage: Double
    ): Double {
        val monthlyReturnRate = annualReturnRate / 12 / 100
        val totalMonths = durationInYears * 12
        var futureValue = 0.0
        var currentMonthlyAmount = monthlyInvestment
        
        for (year in 1..durationInYears) {
            for (month in 1..12) {
                val remainingMonths = totalMonths - ((year - 1) * 12 + month - 1)
                futureValue += currentMonthlyAmount * (1 + monthlyReturnRate).pow(remainingMonths)
            }
            // Apply step-up for next year
            currentMonthlyAmount *= (1 + stepUpPercentage / 100)
        }
        
        return futureValue
    }
    
    private fun calculatePortfolioValue(
        initialMonthlyAmount: Double,
        monthlyReturnRate: Double,
        years: Int,
        stepUpPercentage: Double
    ): Double {
        var portfolioValue = 0.0
        var currentMonthlyAmount = initialMonthlyAmount
        
        for (year in 1..years) {
            for (month in 1..12) {
                val monthsFromStart = (year - 1) * 12 + month
                val monthsToEnd = years * 12 - monthsFromStart
                portfolioValue += currentMonthlyAmount * (1 + monthlyReturnRate).pow(monthsToEnd)
            }
            if (stepUpPercentage > 0) {
                currentMonthlyAmount *= (1 + stepUpPercentage / 100)
            }
        }
        
        return portfolioValue
    }
    
    private fun calculateTimeToReachAmount(
        monthlySIP: Double,
        annualReturn: Double,
        targetAmount: Double,
        initialAmount: Double,
        stepUpPercentage: Double
    ): Int {
        // Simplified calculation for time to reach milestone
        val monthlyRate = annualReturn / 12 / 100
        var currentValue = initialAmount
        var currentSIP = monthlySIP
        var months = 0
        
        while (currentValue < targetAmount && months < 600) { // Max 50 years
            currentValue += currentSIP
            currentValue *= (1 + monthlyRate)
            months++
            
            // Apply annual step-up
            if (months % 12 == 0 && stepUpPercentage > 0) {
                currentSIP *= (1 + stepUpPercentage / 100)
            }
        }
        
        return months / 12
    }
    
    private fun calculateGoalProbability(expectedReturn: Double, timeHorizon: Int): Double {
        // Simplified probability calculation based on market volatility
        // Higher returns and longer time horizons generally have higher probability
        val baseProb = when {
            expectedReturn <= 8 -> 0.85
            expectedReturn <= 12 -> 0.75
            expectedReturn <= 15 -> 0.65
            else -> 0.55
        }
        
        val timeBonus = when {
            timeHorizon >= 15 -> 0.10
            timeHorizon >= 10 -> 0.05
            timeHorizon >= 5 -> 0.02
            else -> 0.0
        }
        
        return kotlin.math.min(0.95, baseProb + timeBonus)
    }
}