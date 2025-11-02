package com.appshub.sipcalculator_financeplanner.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import com.appshub.sipcalculator_financeplanner.data.dao.*
import com.appshub.sipcalculator_financeplanner.data.entity.*
import java.util.*
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class GoalRepository(
    private val goalDao: GoalDao,
    private val savingDao: SavingDao,
    private val debtDao: DebtDao,
    private val goalHistoryDao: GoalHistoryDao,
    private val appSettingsDao: AppSettingsDao
) {
    
    fun getAllGoals(): Flow<List<Goal>> = goalDao.getAllGoals()
    
    fun getActiveGoals(): Flow<List<Goal>> = goalDao.getGoalsByStatus(GoalStatus.ACTIVE)
    
    suspend fun getGoalById(goalId: String): Goal? = goalDao.getGoalById(goalId)
    
    suspend fun insertGoal(goal: Goal) = goalDao.insertGoal(goal)
    
    suspend fun updateGoal(goal: Goal) = goalDao.updateGoal(goal)
    
    suspend fun deleteGoal(goal: Goal) {
        goalDao.deleteGoal(goal)
        savingDao.deleteSavingsForGoal(goal.goalId)
        debtDao.deleteDebtsForGoal(goal.goalId)
        goalHistoryDao.deleteHistoryForGoal(goal.goalId)
    }
    
    suspend fun updateGoalStatus(goalId: String, status: GoalStatus) = 
        goalDao.updateGoalStatus(goalId, status)
    
    // Savings operations
    fun getSavingsByGoal(goalId: String): Flow<List<Saving>> = savingDao.getSavingsByGoal(goalId)
    
    suspend fun insertSaving(saving: Saving) = savingDao.insertSaving(saving)
    
    suspend fun updateSaving(saving: Saving) = savingDao.updateSaving(saving)
    
    suspend fun deleteSaving(saving: Saving) = savingDao.deleteSaving(saving)
    
    suspend fun getTotalSavingsForGoal(goalId: String): Double = 
        savingDao.getTotalSavingsForGoal(goalId) ?: 0.0
    
    suspend fun getSavingsSummaryByCategory(goalId: String): List<SavingsSummary> = 
        savingDao.getSavingsSummaryByCategory(goalId)
    
    // Debts operations
    fun getDebtsByGoal(goalId: String): Flow<List<Debt>> = debtDao.getDebtsByGoal(goalId)
    
    suspend fun insertDebt(debt: Debt) = debtDao.insertDebt(debt)
    
    suspend fun updateDebt(debt: Debt) = debtDao.updateDebt(debt)
    
    suspend fun deleteDebt(debt: Debt) = debtDao.deleteDebt(debt)
    
    suspend fun getTotalDebtsForGoal(goalId: String): Double = 
        debtDao.getTotalDebtsForGoal(goalId) ?: 0.0
    
    suspend fun getDebtsSummaryByType(goalId: String): List<DebtsSummary> = 
        debtDao.getDebtsSummaryByType(goalId)
    
    // History operations
    fun getHistoryForGoal(goalId: String): Flow<List<GoalHistory>> = 
        goalHistoryDao.getHistoryForGoal(goalId)
    
    suspend fun getRecentHistoryForGoal(goalId: String): List<GoalHistory> = 
        goalHistoryDao.getRecentHistoryForGoal(goalId)
    
    suspend fun recordMonthlyProgress(goalId: String) {
        val goal = getGoalById(goalId) ?: return
        val totalSavings = getTotalSavingsForGoal(goalId)
        val totalDebts = getTotalDebtsForGoal(goalId)
        val netWorth = totalSavings - totalDebts
        val progressPercentage = (netWorth / goal.targetAmount) * 100
        
        val calendar = Calendar.getInstance()
        val monthYear = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(calendar.time)
        
        val history = GoalHistory(
            historyId = UUID.randomUUID().toString(),
            goalId = goalId,
            monthYear = monthYear,
            totalSavings = totalSavings,
            totalDebt = totalDebts,
            netWorth = netWorth,
            progressPercentage = progressPercentage,
            recordedDate = Date()
        )
        
        // Delete existing record for this month if any
        goalHistoryDao.deleteHistoryForMonth(goalId, monthYear)
        goalHistoryDao.insertHistory(history)
        
        // Update goal's current amount
        val updatedGoal = goal.copy(
            currentAmount = netWorth,
            lastUpdated = Date()
        )
        updateGoal(updatedGoal)
    }
    
    // Combined data operations
    suspend fun getGoalProgress(goalId: String): GoalProgress? {
        val goal = getGoalById(goalId) ?: return null
        val totalSavings = getTotalSavingsForGoal(goalId)
        val totalDebts = getTotalDebtsForGoal(goalId)
        val netWorth = totalSavings - totalDebts
        val progressPercentage = (netWorth / goal.targetAmount) * 100
        val remainingAmount = goal.targetAmount - netWorth
        
        // Calculate months to goal
        val today = Date()
        val diffInMillis = goal.targetDate.time - today.time
        val monthsToGo = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt() / 30
        
        // Calculate if on track
        val expectedProgress = if (monthsToGo > 0) {
            val totalMonths = TimeUnit.MILLISECONDS.toDays(goal.targetDate.time - goal.createdDate.time).toInt() / 30
            val elapsedMonths = totalMonths - monthsToGo
            (elapsedMonths.toDouble() / totalMonths) * 100
        } else 100.0
        
        val isOnTrack = progressPercentage >= expectedProgress
        val monthlyProgressNeeded = if (monthsToGo > 0) remainingAmount / monthsToGo else 0.0
        
        return GoalProgress(
            goal = goal,
            totalSavings = totalSavings,
            totalDebts = totalDebts,
            netWorth = netWorth,
            progressPercentage = progressPercentage,
            remainingAmount = remainingAmount,
            monthsToGo = maxOf(0, monthsToGo),
            isOnTrack = isOnTrack,
            monthlyProgressNeeded = monthlyProgressNeeded
        )
    }
    
    fun getGoalProgressFlow(goalId: String): Flow<GoalProgress?> {
        return combine(
            savingDao.getSavingsByGoal(goalId),
            debtDao.getDebtsByGoal(goalId)
        ) { savings, debts ->
            getGoalProgress(goalId)
        }
    }
    
    // App Settings operations
    suspend fun getAppSettings(): AppSettings? = appSettingsDao.getSettings()
    
    suspend fun insertAppSettings(settings: AppSettings) = appSettingsDao.insertSettings(settings)
    
    suspend fun updateAppSettings(settings: AppSettings) = appSettingsDao.updateSettings(settings)
    
    suspend fun updatePinSettings(enabled: Boolean, pinHash: String) = 
        appSettingsDao.updatePinSettings(enabled, pinHash, Date())
    
    suspend fun disablePin() = appSettingsDao.disablePin(Date())
}