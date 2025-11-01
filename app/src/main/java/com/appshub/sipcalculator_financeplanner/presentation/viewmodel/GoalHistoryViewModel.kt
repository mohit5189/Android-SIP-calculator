package com.appshub.sipcalculator_financeplanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.appshub.sipcalculator_financeplanner.data.entity.*
import com.appshub.sipcalculator_financeplanner.data.repository.GoalRepository

data class GoalHistoryUiState(
    val historyList: List<GoalHistory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GoalHistoryViewModel(
    private val repository: GoalRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GoalHistoryUiState())
    val uiState: StateFlow<GoalHistoryUiState> = _uiState.asStateFlow()
    
    fun loadHistoryForGoal(goalId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getHistoryForGoal(goalId).collect { history ->
                    _uiState.value = _uiState.value.copy(
                        historyList = history,
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
}