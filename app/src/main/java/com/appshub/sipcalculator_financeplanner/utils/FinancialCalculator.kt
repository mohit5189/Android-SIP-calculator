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
    
    fun calculateSimpleInterest(
        principal: Double,
        rate: Double,
        time: Double
    ): SimpleInterestResult {
        val simpleInterest = (principal * rate * time) / 100
        val maturityAmount = principal + simpleInterest
        
        val yearWiseData = mutableListOf<SimpleInterestYearData>()
        val yearlyInterest = simpleInterest / time
        
        for (year in 1..time.toInt()) {
            val cumulativeInterest = yearlyInterest * year
            val totalAmount = principal + cumulativeInterest
            
            yearWiseData.add(
                SimpleInterestYearData(
                    year = year,
                    yearlyInterest = yearlyInterest,
                    cumulativeInterest = cumulativeInterest,
                    totalAmount = totalAmount
                )
            )
        }
        
        return SimpleInterestResult(
            principal = principal,
            rate = rate,
            time = time,
            simpleInterest = simpleInterest,
            maturityAmount = maturityAmount,
            yearWiseData = yearWiseData
        )
    }
    
    fun calculateCompoundInterest(
        principal: Double,
        rate: Double,
        time: Double,
        compoundingFrequency: Int = 1 // 1=yearly, 4=quarterly, 12=monthly
    ): CompoundInterestResult {
        val ratePerPeriod = rate / (100 * compoundingFrequency)
        val totalPeriods = compoundingFrequency * time
        val maturityAmount = principal * (1 + ratePerPeriod).pow(totalPeriods)
        val compoundInterest = maturityAmount - principal
        
        val yearWiseData = mutableListOf<CompoundInterestYearData>()
        
        for (year in 1..time.toInt()) {
            val periodsElapsed = compoundingFrequency * year
            val openingAmount = if (year == 1) principal else {
                principal * (1 + ratePerPeriod).pow(compoundingFrequency * (year - 1))
            }
            val closingAmount = principal * (1 + ratePerPeriod).pow(periodsElapsed)
            val yearlyInterest = closingAmount - openingAmount
            val cumulativeInterest = closingAmount - principal
            
            yearWiseData.add(
                CompoundInterestYearData(
                    year = year,
                    openingAmount = openingAmount,
                    yearlyInterest = yearlyInterest,
                    closingAmount = closingAmount,
                    cumulativeInterest = cumulativeInterest
                )
            )
        }
        
        return CompoundInterestResult(
            principal = principal,
            rate = rate,
            time = time,
            compoundingFrequency = compoundingFrequency,
            compoundInterest = compoundInterest,
            maturityAmount = maturityAmount,
            yearWiseData = yearWiseData
        )
    }
    
    fun calculateEMI(
        loanAmount: Double,
        interestRate: Double,
        loanTenure: Int // in months
    ): EMIResult {
        val monthlyRate = interestRate / 12 / 100
        val emi = if (monthlyRate == 0.0) {
            loanAmount / loanTenure
        } else {
            loanAmount * monthlyRate * (1 + monthlyRate).pow(loanTenure) / ((1 + monthlyRate).pow(loanTenure) - 1)
        }
        
        val totalAmount = emi * loanTenure
        val totalInterest = totalAmount - loanAmount
        
        val yearWiseData = mutableListOf<EMIYearData>()
        var remainingBalance = loanAmount
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                               "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        
        val totalYears = (loanTenure + 11) / 12 // Round up to nearest year
        
        for (year in 1..totalYears) {
            val yearOpeningBalance = remainingBalance
            var yearlyPrincipal = 0.0
            var yearlyInterest = 0.0
            val monthlyDataList = mutableListOf<EMIMonthData>()
            
            val startMonth = (year - 1) * 12 + 1
            val endMonth = kotlin.math.min(year * 12, loanTenure)
            
            for (monthIndex in startMonth..endMonth) {
                val monthOpeningBalance = remainingBalance
                val interestPayment = remainingBalance * monthlyRate
                val principalPayment = emi - interestPayment
                
                remainingBalance -= principalPayment
                yearlyPrincipal += principalPayment
                yearlyInterest += interestPayment
                
                val monthInYear = ((monthIndex - 1) % 12) + 1
                monthlyDataList.add(
                    EMIMonthData(
                        month = monthInYear,
                        monthName = monthNames[monthInYear - 1],
                        openingBalance = monthOpeningBalance,
                        emi = emi,
                        principalPayment = principalPayment,
                        interestPayment = interestPayment,
                        closingBalance = remainingBalance
                    )
                )
            }
            
            yearWiseData.add(
                EMIYearData(
                    year = year,
                    openingBalance = yearOpeningBalance,
                    totalPayment = emi * monthlyDataList.size,
                    principalPayment = yearlyPrincipal,
                    interestPayment = yearlyInterest,
                    closingBalance = remainingBalance,
                    monthlyData = monthlyDataList
                )
            )
        }
        
        return EMIResult(
            loanAmount = loanAmount,
            interestRate = interestRate,
            loanTenure = loanTenure,
            emi = emi,
            totalAmount = totalAmount,
            totalInterest = totalInterest,
            yearWiseData = yearWiseData
        )
    }
    
    fun calculateRD(
        monthlyDeposit: Double,
        interestRate: Double,
        tenure: Int // in years
    ): RDResult {
        val monthlyRate = interestRate / 12 / 100
        val totalMonths = tenure * 12
        
        var maturityAmount = 0.0
        for (month in 1..totalMonths) {
            val monthsRemaining = totalMonths - month + 1
            maturityAmount += monthlyDeposit * (1 + monthlyRate).pow(monthsRemaining)
        }
        
        val totalDeposits = monthlyDeposit * totalMonths
        val totalInterest = maturityAmount - totalDeposits
        
        val yearWiseData = mutableListOf<RDYearData>()
        var currentBalance = 0.0
        var cumulativeDeposits = 0.0
        
        for (year in 1..tenure) {
            val yearOpeningBalance = currentBalance
            val yearlyDeposits = monthlyDeposit * 12
            cumulativeDeposits += yearlyDeposits
            
            // Calculate interest earned during the year
            var yearlyInterest = 0.0
            var tempBalance = yearOpeningBalance
            
            for (month in 1..12) {
                tempBalance += monthlyDeposit
                val monthlyInterestEarned = tempBalance * monthlyRate
                tempBalance += monthlyInterestEarned
                yearlyInterest += monthlyInterestEarned
            }
            
            currentBalance = tempBalance
            val cumulativeInterest = currentBalance - cumulativeDeposits
            
            yearWiseData.add(
                RDYearData(
                    year = year,
                    openingBalance = yearOpeningBalance,
                    totalDeposits = yearlyDeposits,
                    interestEarned = yearlyInterest,
                    closingBalance = currentBalance,
                    cumulativeDeposits = cumulativeDeposits,
                    cumulativeInterest = cumulativeInterest
                )
            )
        }
        
        return RDResult(
            monthlyDeposit = monthlyDeposit,
            interestRate = interestRate,
            tenure = tenure,
            maturityAmount = maturityAmount,
            totalDeposits = totalDeposits,
            totalInterest = totalInterest,
            yearWiseData = yearWiseData
        )
    }
    
    fun calculatePPF(
        yearlyDeposit: Double,
        interestRate: Double = 7.1
    ): PPFResult {
        val ppfRate = interestRate
        val tenure = 15 // PPF is always 15 years
        
        val yearWiseData = mutableListOf<PPFYearData>()
        var currentBalance = 0.0
        var cumulativeDeposits = 0.0
        
        for (year in 1..tenure) {
            val yearOpeningBalance = currentBalance
            currentBalance += yearlyDeposit
            cumulativeDeposits += yearlyDeposit
            
            // Calculate interest on year-end balance
            val interestEarned = currentBalance * ppfRate / 100
            currentBalance += interestEarned
            
            val cumulativeInterest = currentBalance - cumulativeDeposits
            
            yearWiseData.add(
                PPFYearData(
                    year = year,
                    openingBalance = yearOpeningBalance,
                    deposit = yearlyDeposit,
                    interestEarned = interestEarned,
                    closingBalance = currentBalance,
                    cumulativeDeposits = cumulativeDeposits,
                    cumulativeInterest = cumulativeInterest
                )
            )
        }
        
        val totalDeposits = yearlyDeposit * tenure
        val totalInterest = currentBalance - totalDeposits
        
        return PPFResult(
            yearlyDeposit = yearlyDeposit,
            interestRate = interestRate,
            tenure = tenure,
            maturityAmount = currentBalance,
            totalDeposits = totalDeposits,
            totalInterest = totalInterest,
            yearWiseData = yearWiseData
        )
    }
    
    fun calculateFD(
        principal: Double,
        interestRate: Double,
        tenure: Int, // in months
        isCompoundInterest: Boolean = true
    ): FDResult {
        val maturityAmount = if (isCompoundInterest) {
            // Quarterly compounding for FD
            val quarterlyRate = interestRate / 4 / 100
            val quarters = tenure / 3.0
            principal * (1 + quarterlyRate).pow(quarters)
        } else {
            // Simple interest
            principal * (1 + (interestRate * tenure / 12) / 100)
        }
        
        val totalInterest = maturityAmount - principal
        val monthlyInterest = totalInterest / tenure
        
        return FDResult(
            principal = principal,
            interestRate = interestRate,
            tenure = tenure,
            maturityAmount = maturityAmount,
            totalInterest = totalInterest,
            monthlyInterest = monthlyInterest,
            isCompoundInterest = isCompoundInterest
        )
    }
}