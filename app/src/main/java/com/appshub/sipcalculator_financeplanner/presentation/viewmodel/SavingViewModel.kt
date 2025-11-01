package com.appshub.sipcalculator_financeplanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.appshub.sipcalculator_financeplanner.data.repository.GoalRepository
import com.appshub.sipcalculator_financeplanner.data.entity.*
import com.appshub.sipcalculator_financeplanner.data.dao.SavingsSummary
import java.util.*

data class SavingUiState(
    val savings: List<Saving> = emptyList(),
    val savingsSummary: List<SavingsSummary> = emptyList(),
    val totalSavings: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val editingSaving: Saving? = null
)

class SavingViewModel(private val repository: GoalRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SavingUiState())
    val uiState: StateFlow<SavingUiState> = _uiState.asStateFlow()
    
    private var currentGoalId: String? = null
    
    fun loadSavingsForGoal(goalId: String) {
        currentGoalId = goalId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getSavingsByGoal(goalId).collect { savings ->
                    val summary = repository.getSavingsSummaryByCategory(goalId)
                    val total = repository.getTotalSavingsForGoal(goalId)
                    
                    _uiState.value = _uiState.value.copy(
                        savings = savings,
                        savingsSummary = summary,
                        totalSavings = total,
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
    
    fun addSaving(
        goalId: String,
        category: SavingCategory,
        amount: Double,
        customCategoryName: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            try {
                val saving = Saving(
                    savingId = UUID.randomUUID().toString(),
                    goalId = goalId,
                    category = category,
                    customCategoryName = customCategoryName,
                    amount = amount,
                    notes = notes,
                    lastUpdated = Date(),
                    createdDate = Date()
                )
                repository.insertSaving(saving)
                loadSavingsForGoal(goalId)
                _uiState.value = _uiState.value.copy(error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun addSaving(
        category: SavingCategory,
        amount: Double,
        customCategoryName: String = "",
        notes: String = ""
    ) {
        val goalId = currentGoalId ?: return
        addSaving(goalId, category, amount, customCategoryName, notes)
    }
    
    fun updateSaving(saving: Saving) {
        viewModelScope.launch {
            try {
                val updatedSaving = saving.copy(lastUpdated = Date())
                repository.updateSaving(updatedSaving)
                currentGoalId?.let { loadSavingsForGoal(it) } // Refresh the list
                _uiState.value = _uiState.value.copy(
                    editingSaving = null,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun deleteSaving(saving: Saving) {
        viewModelScope.launch {
            try {
                repository.deleteSaving(saving)
                currentGoalId?.let { loadSavingsForGoal(it) } // Refresh the list
                _uiState.value = _uiState.value.copy(error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun startEditingSaving(saving: Saving) {
        _uiState.value = _uiState.value.copy(editingSaving = saving)
    }
    
    fun cancelEditing() {
        _uiState.value = _uiState.value.copy(editingSaving = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}