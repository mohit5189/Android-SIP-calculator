package com.appshub.sipcalculator_financeplanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.appshub.sipcalculator_financeplanner.data.entity.*
import com.appshub.sipcalculator_financeplanner.data.repository.GoalRepository
import com.appshub.sipcalculator_financeplanner.data.dao.DebtsSummary

data class DebtUiState(
    val debts: List<Debt> = emptyList(),
    val debtsSummary: List<DebtsSummary> = emptyList(),
    val totalDebts: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val editingDebt: Debt? = null
)

class DebtViewModel(
    private val repository: GoalRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DebtUiState())
    val uiState: StateFlow<DebtUiState> = _uiState.asStateFlow()
    
    fun loadDebtsForGoal(goalId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getDebtsByGoal(goalId).collect { debts ->
                    val totalDebts = repository.getTotalDebtsForGoal(goalId)
                    val summary = repository.getDebtsSummaryByType(goalId)
                    
                    _uiState.value = _uiState.value.copy(
                        debts = debts,
                        debtsSummary = summary,
                        totalDebts = totalDebts,
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
    
    fun addDebt(debt: Debt) {
        viewModelScope.launch {
            try {
                repository.insertDebt(debt)
                loadDebtsForGoal(debt.goalId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun addDebt(
        goalId: String,
        debtType: DebtType,
        amount: Double,
        emiAmount: Double = 0.0,
        interestRate: Double = 0.0,
        customDebtName: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            try {
                val debt = Debt(
                    debtId = java.util.UUID.randomUUID().toString(),
                    goalId = goalId,
                    debtType = debtType,
                    customDebtName = customDebtName,
                    amount = amount,
                    emiAmount = emiAmount,
                    interestRate = interestRate,
                    notes = notes,
                    lastUpdated = java.util.Date(),
                    createdDate = java.util.Date()
                )
                repository.insertDebt(debt)
                loadDebtsForGoal(goalId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun updateDebt(debt: Debt) {
        viewModelScope.launch {
            try {
                repository.updateDebt(debt)
                loadDebtsForGoal(debt.goalId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            try {
                repository.deleteDebt(debt)
                loadDebtsForGoal(debt.goalId) // Refresh the list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun startEditingDebt(debt: Debt) {
        _uiState.value = _uiState.value.copy(editingDebt = debt)
    }
    
    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(editingDebt = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}