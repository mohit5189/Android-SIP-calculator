package com.appshub.sipcalculator_financeplanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.appshub.sipcalculator_financeplanner.data.repository.GoalRepository
import com.appshub.sipcalculator_financeplanner.data.entity.*
import java.util.*

data class GoalUiState(
    val goals: List<Goal> = emptyList(),
    val goalsProgress: Map<String, GoalProgress> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedGoal: Goal? = null,
    val goalProgress: GoalProgress? = null
)

class GoalViewModel(private val repository: GoalRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()
    
    init {
        loadActiveGoals()
    }
    
    private fun loadActiveGoals() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getAllGoals().collect { goals ->
                    // Load progress for each goal
                    val progressMap = mutableMapOf<String, GoalProgress>()
                    goals.forEach { goal ->
                        repository.getGoalProgress(goal.goalId)?.let { progress ->
                            progressMap[goal.goalId] = progress
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        goals = goals,
                        goalsProgress = progressMap,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
    
    fun selectGoal(goalId: String) {
        viewModelScope.launch {
            try {
                val goal = repository.getGoalById(goalId)
                val progress = repository.getGoalProgress(goalId)
                _uiState.value = _uiState.value.copy(
                    selectedGoal = goal,
                    goalProgress = progress
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    suspend fun createGoal(
        goalName: String,
        targetAmount: Double,
        targetDate: Date,
        monthlySipNeeded: Double,
        goalType: GoalType,
        description: String = ""
    ): String {
        return try {
            val goal = Goal(
                goalId = UUID.randomUUID().toString(),
                goalName = goalName,
                targetAmount = targetAmount,
                targetDate = targetDate,
                monthlySipNeeded = monthlySipNeeded,
                createdDate = Date(),
                status = GoalStatus.ACTIVE,
                goalType = goalType,
                description = description
            )
            repository.insertGoal(goal)
            _uiState.value = _uiState.value.copy(
                selectedGoal = goal,
                error = null
            )
            goal.goalId
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
            throw e
        }
    }
    
    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            try {
                repository.updateGoal(goal)
                _uiState.value = _uiState.value.copy(error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun updateGoalStatus(goalId: String, status: GoalStatus) {
        viewModelScope.launch {
            try {
                repository.updateGoalStatus(goalId, status)
                _uiState.value = _uiState.value.copy(error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            try {
                repository.deleteGoal(goal)
                _uiState.value = _uiState.value.copy(
                    selectedGoal = null,
                    goalProgress = null,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun recordMonthlyProgress(goalId: String) {
        viewModelScope.launch {
            try {
                repository.recordMonthlyProgress(goalId)
                // Refresh goal progress
                selectGoal(goalId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            selectedGoal = null,
            goalProgress = null
        )
    }
}