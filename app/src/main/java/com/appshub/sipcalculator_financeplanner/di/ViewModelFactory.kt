package com.appshub.sipcalculator_financeplanner.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.appshub.sipcalculator_financeplanner.data.database.GoalDatabase
import com.appshub.sipcalculator_financeplanner.data.repository.GoalRepository
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.*

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    private val database by lazy { GoalDatabase.getDatabase(context) }
    private val repository by lazy { 
        GoalRepository(
            goalDao = database.goalDao(),
            savingDao = database.savingDao(),
            debtDao = database.debtDao(),
            goalHistoryDao = database.goalHistoryDao()
        )
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            GoalViewModel::class.java -> GoalViewModel(repository) as T
            SavingViewModel::class.java -> SavingViewModel(repository) as T
            DebtViewModel::class.java -> DebtViewModel(repository) as T
            GoalHistoryViewModel::class.java -> GoalHistoryViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}