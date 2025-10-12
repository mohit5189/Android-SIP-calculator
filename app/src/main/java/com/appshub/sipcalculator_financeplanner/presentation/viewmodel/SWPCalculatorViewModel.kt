package com.appshub.sipcalculator_financeplanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appshub.sipcalculator_financeplanner.data.models.SWPResult
import com.appshub.sipcalculator_financeplanner.utils.FinancialCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SWPCalculatorUiState(
    val initialCorpus: String = "1000000",
    val monthlyWithdrawal: String = "25000",
    val annualReturnRate: String = "12",
    val durationInYears: String = "10",
    val stepUpPercentage: String = "5",
    val isStepUpEnabled: Boolean = false,
    val result: SWPResult? = null,
    val isCalculating: Boolean = false,
    val error: String? = null,
    val showDetailedBreakdown: Boolean = false
)

class SWPCalculatorViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(SWPCalculatorUiState())
    val uiState: StateFlow<SWPCalculatorUiState> = _uiState.asStateFlow()
    
    fun updateInitialCorpus(value: String) {
        val filteredValue = value.filter { it.isDigit() || it == '.' }
        _uiState.value = _uiState.value.copy(
            initialCorpus = filteredValue,
            error = null,
            result = null
        )
    }
    
    fun updateMonthlyWithdrawal(value: String) {
        val filteredValue = value.filter { it.isDigit() || it == '.' }
        _uiState.value = _uiState.value.copy(
            monthlyWithdrawal = filteredValue,
            error = null,
            result = null
        )
    }
    
    fun updateAnnualReturnRate(value: String) {
        val filteredValue = value.filter { it.isDigit() || it == '.' }
        if (filteredValue.isEmpty() || filteredValue.toDoubleOrNull()?.let { it <= 50 } == true) {
            _uiState.value = _uiState.value.copy(
                annualReturnRate = filteredValue,
                error = null,
                result = null
            )
        }
    }
    
    fun updateDurationInYears(value: String) {
        val filteredValue = value.filter { it.isDigit() }
        if (filteredValue.isEmpty() || filteredValue.toIntOrNull()?.let { it <= 50 } == true) {
            _uiState.value = _uiState.value.copy(
                durationInYears = filteredValue,
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
    
    fun calculateSWP() {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            try {
                val initialAmount = currentState.initialCorpus.toDoubleOrNull()
                val withdrawalAmount = currentState.monthlyWithdrawal.toDoubleOrNull()
                val returnRate = currentState.annualReturnRate.toDoubleOrNull()
                val duration = currentState.durationInYears.toIntOrNull()
                val stepUp = if (currentState.isStepUpEnabled) 
                    currentState.stepUpPercentage.toDoubleOrNull() ?: 0.0 
                else 0.0
                
                if (initialAmount != null && withdrawalAmount != null && returnRate != null && 
                    duration != null && initialAmount > 0 && withdrawalAmount > 0 && 
                    returnRate > 0 && duration > 0) {
                    
                    _uiState.value = currentState.copy(isCalculating = true)
                    
                    val result = FinancialCalculator.calculateSWP(
                        initialCorpus = initialAmount,
                        monthlyWithdrawal = withdrawalAmount,
                        annualReturnRate = returnRate,
                        durationInYears = duration,
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