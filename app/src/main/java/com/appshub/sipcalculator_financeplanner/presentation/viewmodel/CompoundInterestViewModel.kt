package com.appshub.sipcalculator_financeplanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appshub.sipcalculator_financeplanner.data.models.CompoundInterestResult
import com.appshub.sipcalculator_financeplanner.utils.FinancialCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CompoundInterestUiState(
    val principal: String = "",
    val rate: String = "",
    val time: String = "",
    val compoundingFrequency: Int = 1, // 1=yearly, 4=quarterly, 12=monthly
    val result: CompoundInterestResult? = null,
    val showDetailedBreakdown: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CompoundInterestViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(CompoundInterestUiState())
    val uiState: StateFlow<CompoundInterestUiState> = _uiState.asStateFlow()
    
    fun updatePrincipal(principal: String) {
        _uiState.value = _uiState.value.copy(principal = principal, errorMessage = null)
    }
    
    fun updateRate(rate: String) {
        _uiState.value = _uiState.value.copy(rate = rate, errorMessage = null)
    }
    
    fun updateTime(time: String) {
        _uiState.value = _uiState.value.copy(time = time, errorMessage = null)
    }
    
    fun updateCompoundingFrequency(frequency: Int) {
        _uiState.value = _uiState.value.copy(compoundingFrequency = frequency, errorMessage = null)
    }
    
    fun calculate() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                val principal = _uiState.value.principal.replace(",", "").toDoubleOrNull()
                val rate = _uiState.value.rate.toDoubleOrNull()
                val time = _uiState.value.time.toDoubleOrNull()
                
                if (principal == null || principal <= 0) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Please enter a valid principal amount"
                    )
                    return@launch
                }
                
                if (rate == null || rate <= 0) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Please enter a valid interest rate"
                    )
                    return@launch
                }
                
                if (time == null || time <= 0) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Please enter a valid time period"
                    )
                    return@launch
                }
                
                val result = FinancialCalculator.calculateCompoundInterest(
                    principal = principal,
                    rate = rate,
                    time = time,
                    compoundingFrequency = _uiState.value.compoundingFrequency
                )
                
                _uiState.value = _uiState.value.copy(
                    result = result,
                    isLoading = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Calculation failed: ${e.message}"
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
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}