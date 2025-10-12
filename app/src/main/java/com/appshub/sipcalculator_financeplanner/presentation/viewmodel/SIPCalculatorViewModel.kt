package com.appshub.sipcalculator_financeplanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appshub.sipcalculator_financeplanner.data.models.SIPResult
import com.appshub.sipcalculator_financeplanner.utils.FinancialCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SIPCalculatorUiState(
    val monthlyInvestment: String = "5000",
    val annualReturnRate: String = "12",
    val durationInYears: String = "10",
    val stepUpPercentage: String = "10",
    val isStepUpEnabled: Boolean = false,
    val result: SIPResult? = null,
    val isCalculating: Boolean = false,
    val error: String? = null,
    val showDetailedBreakdown: Boolean = false
)

class SIPCalculatorViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(SIPCalculatorUiState())
    val uiState: StateFlow<SIPCalculatorUiState> = _uiState.asStateFlow()
    
    fun updateMonthlyInvestment(value: String) {
        val filteredValue = value.filter { it.isDigit() || it == '.' }
        _uiState.value = _uiState.value.copy(
            monthlyInvestment = filteredValue,
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
    
    fun setPresetAmount(amount: Double) {
        _uiState.value = _uiState.value.copy(
            monthlyInvestment = amount.toInt().toString(),
            result = null
        )
    }
    
    fun setPresetDuration(years: Int) {
        _uiState.value = _uiState.value.copy(
            durationInYears = years.toString(),
            result = null
        )
    }
    
    fun setPresetReturnRate(rate: Double) {
        _uiState.value = _uiState.value.copy(
            annualReturnRate = rate.toString(),
            result = null
        )
    }
    
    fun calculateSIP() {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            try {
                val monthlyAmount = currentState.monthlyInvestment.toDoubleOrNull()
                val returnRate = currentState.annualReturnRate.toDoubleOrNull()
                val duration = currentState.durationInYears.toIntOrNull()
                val stepUp = if (currentState.isStepUpEnabled) 
                    currentState.stepUpPercentage.toDoubleOrNull() ?: 0.0 
                else 0.0
                
                if (monthlyAmount != null && returnRate != null && duration != null &&
                    monthlyAmount > 0 && returnRate > 0 && duration > 0) {
                    
                    _uiState.value = currentState.copy(isCalculating = true)
                    
                    val result = FinancialCalculator.calculateSIP(
                        monthlyInvestment = monthlyAmount,
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
    
    private fun isValidNumber(value: String): Boolean {
        return try {
            value.toDouble() > 0
        } catch (e: NumberFormatException) {
            false
        }
    }
}