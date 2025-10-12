package com.appshub.sipcalculator_financeplanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appshub.sipcalculator_financeplanner.data.models.GoalResult
import com.appshub.sipcalculator_financeplanner.utils.FinancialCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GoalCalculatorUiState(
    val targetAmount: String = "5000000",
    val timeHorizon: String = "15",
    val initialAmount: String = "100000",
    val expectedReturn: String = "12",
    val stepUpPercentage: String = "10",
    val isStepUpEnabled: Boolean = false,
    val result: GoalResult? = null,
    val isCalculating: Boolean = false,
    val error: String? = null,
    val showDetailedBreakdown: Boolean = false
)

class GoalCalculatorViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(GoalCalculatorUiState())
    val uiState: StateFlow<GoalCalculatorUiState> = _uiState.asStateFlow()
    
    fun updateTargetAmount(value: String) {
        val filteredValue = value.filter { it.isDigit() || it == '.' }
        _uiState.value = _uiState.value.copy(
            targetAmount = filteredValue,
            error = null,
            result = null
        )
    }
    
    fun updateTimeHorizon(value: String) {
        val filteredValue = value.filter { it.isDigit() }
        if (filteredValue.isEmpty() || filteredValue.toIntOrNull()?.let { it <= 50 } == true) {
            _uiState.value = _uiState.value.copy(
                timeHorizon = filteredValue,
                error = null,
                result = null
            )
        }
    }
    
    fun updateInitialAmount(value: String) {
        val filteredValue = value.filter { it.isDigit() || it == '.' }
        _uiState.value = _uiState.value.copy(
            initialAmount = filteredValue,
            error = null,
            result = null
        )
    }
    
    fun updateExpectedReturn(value: String) {
        val filteredValue = value.filter { it.isDigit() || it == '.' }
        if (filteredValue.isEmpty() || filteredValue.toDoubleOrNull()?.let { it <= 50 } == true) {
            _uiState.value = _uiState.value.copy(
                expectedReturn = filteredValue,
                error = null,
                result = null
            )
        }
    }
    
    fun updateStepUpPercentage(value: String) {
        val filteredValue = value.filter { it.isDigit() || it == '.' }
        if (filteredValue.isEmpty() || filteredValue.toDoubleOrNull()?.let { it <= 50 } == true) {
            _uiState.value = _uiState.value.copy(
                stepUpPercentage = filteredValue,
                error = null,
                result = null
            )
        }
    }
    
    fun toggleStepUp() {
        _uiState.value = _uiState.value.copy(
            isStepUpEnabled = !_uiState.value.isStepUpEnabled,
            result = null
        )
    }
    
    fun calculateGoal() {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            try {
                val targetAmount = currentState.targetAmount.toDoubleOrNull()
                val timeHorizon = currentState.timeHorizon.toIntOrNull()
                val initialAmount = currentState.initialAmount.toDoubleOrNull() ?: 0.0
                val expectedReturn = currentState.expectedReturn.toDoubleOrNull()
                val stepUp = if (currentState.isStepUpEnabled) 
                    currentState.stepUpPercentage.toDoubleOrNull() ?: 0.0 
                else 0.0
                
                if (targetAmount != null && timeHorizon != null && expectedReturn != null &&
                    targetAmount > 0 && timeHorizon > 0 && expectedReturn > 0) {
                    
                    _uiState.value = currentState.copy(isCalculating = true)
                    
                    val result = FinancialCalculator.calculateGoalSIP(
                        targetAmount = targetAmount,
                        timeHorizon = timeHorizon,
                        initialAmount = initialAmount,
                        expectedReturn = expectedReturn,
                        stepUpPercentage = stepUp
                    )
                    
                    _uiState.value = currentState.copy(
                        result = result,
                        isCalculating = false,
                        error = null,
                        showDetailedBreakdown = true
                    )
                } else {
                    _uiState.value = currentState.copy(
                        result = null,
                        isCalculating = false,
                        error = "Please enter valid values"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isCalculating = false,
                    error = "Calculation error: ${e.message}"
                )
            }
        }
    }
    
    fun showDetailedBreakdown() {
        _uiState.value = _uiState.value.copy(showDetailedBreakdown = true)
    }
    
    fun hideDetailedBreakdown() {
        _uiState.value = _uiState.value.copy(showDetailedBreakdown = false)
    }
}