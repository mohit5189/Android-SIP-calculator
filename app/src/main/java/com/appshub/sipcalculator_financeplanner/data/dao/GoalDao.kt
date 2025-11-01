package com.appshub.sipcalculator_financeplanner.data.dao

import androidx.room.*
import com.appshub.sipcalculator_financeplanner.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    
    @Query("SELECT * FROM goals WHERE status = :status ORDER BY createdDate DESC")
    fun getGoalsByStatus(status: GoalStatus): Flow<List<Goal>>
    
    @Query("SELECT * FROM goals ORDER BY createdDate DESC")
    fun getAllGoals(): Flow<List<Goal>>
    
    @Query("SELECT * FROM goals WHERE goalId = :goalId")
    suspend fun getGoalById(goalId: String): Goal?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)
    
    @Update
    suspend fun updateGoal(goal: Goal)
    
    @Delete
    suspend fun deleteGoal(goal: Goal)
    
    @Query("UPDATE goals SET status = :status WHERE goalId = :goalId")
    suspend fun updateGoalStatus(goalId: String, status: GoalStatus)
    
    @Query("SELECT COUNT(*) FROM goals WHERE status = 'ACTIVE'")
    suspend fun getActiveGoalsCount(): Int
}

@Dao
interface SavingDao {
    
    @Query("SELECT * FROM savings WHERE goalId = :goalId ORDER BY createdDate DESC")
    fun getSavingsByGoal(goalId: String): Flow<List<Saving>>
    
    @Query("SELECT * FROM savings WHERE savingId = :savingId")
    suspend fun getSavingById(savingId: String): Saving?
    
    @Query("SELECT SUM(amount) FROM savings WHERE goalId = :goalId")
    suspend fun getTotalSavingsForGoal(goalId: String): Double?
    
    @Query("SELECT category, SUM(amount) as total FROM savings WHERE goalId = :goalId GROUP BY category")
    suspend fun getSavingsSummaryByCategory(goalId: String): List<SavingsSummary>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaving(saving: Saving)
    
    @Update
    suspend fun updateSaving(saving: Saving)
    
    @Delete
    suspend fun deleteSaving(saving: Saving)
    
    @Query("DELETE FROM savings WHERE goalId = :goalId")
    suspend fun deleteSavingsForGoal(goalId: String)
}

@Dao
interface DebtDao {
    
    @Query("SELECT * FROM debts WHERE goalId = :goalId ORDER BY createdDate DESC")
    fun getDebtsByGoal(goalId: String): Flow<List<Debt>>
    
    @Query("SELECT * FROM debts WHERE debtId = :debtId")
    suspend fun getDebtById(debtId: String): Debt?
    
    @Query("SELECT SUM(amount) FROM debts WHERE goalId = :goalId")
    suspend fun getTotalDebtsForGoal(goalId: String): Double?
    
    @Query("SELECT debtType, SUM(amount) as total FROM debts WHERE goalId = :goalId GROUP BY debtType")
    suspend fun getDebtsSummaryByType(goalId: String): List<DebtsSummary>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: Debt)
    
    @Update
    suspend fun updateDebt(debt: Debt)
    
    @Delete
    suspend fun deleteDebt(debt: Debt)
    
    @Query("DELETE FROM debts WHERE goalId = :goalId")
    suspend fun deleteDebtsForGoal(goalId: String)
}

@Dao
interface GoalHistoryDao {
    
    @Query("SELECT * FROM goal_history WHERE goalId = :goalId ORDER BY recordedDate DESC")
    fun getHistoryForGoal(goalId: String): Flow<List<GoalHistory>>
    
    @Query("SELECT * FROM goal_history WHERE goalId = :goalId ORDER BY recordedDate DESC LIMIT 12")
    suspend fun getRecentHistoryForGoal(goalId: String): List<GoalHistory>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: GoalHistory)
    
    @Query("DELETE FROM goal_history WHERE goalId = :goalId AND monthYear = :monthYear")
    suspend fun deleteHistoryForMonth(goalId: String, monthYear: String)
    
    @Query("DELETE FROM goal_history WHERE goalId = :goalId")
    suspend fun deleteHistoryForGoal(goalId: String)
}

data class SavingsSummary(
    val category: SavingCategory,
    val total: Double
)

data class DebtsSummary(
    val debtType: DebtType,
    val total: Double
)