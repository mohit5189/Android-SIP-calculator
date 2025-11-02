package com.appshub.sipcalculator_financeplanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.appshub.sipcalculator_financeplanner.data.repository.GoalRepository
import com.appshub.sipcalculator_financeplanner.data.entity.AppSettings
import com.appshub.sipcalculator_financeplanner.utils.PinManager
import java.util.*

data class PinUiState(
    val isPinEnabled: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val actualPin: String? = null // For development/recovery
)

class PinViewModel(private val repository: GoalRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PinUiState())
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()
    
    init {
        checkPinStatus()
    }
    
    private fun checkPinStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val settings = repository.getAppSettings()
                _uiState.value = _uiState.value.copy(
                    isPinEnabled = settings?.isPinEnabled ?: false,
                    actualPin = settings?.pinPlaintext,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
    
    fun authenticatePin(pin: String) {
        viewModelScope.launch {
            try {
                val settings = repository.getAppSettings()
                if (settings != null && settings.isPinEnabled) {
                    val isValid = PinManager.validatePin(pin, settings.pinHash)
                    if (isValid) {
                        _uiState.value = _uiState.value.copy(
                            isAuthenticated = true,
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "Invalid PIN. Please try again.",
                            isAuthenticated = false
                        )
                    }
                } else {
                    // No PIN set, allow access
                    _uiState.value = _uiState.value.copy(isAuthenticated = true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isAuthenticated = false
                )
            }
        }
    }
    
    fun setupPin(pin: String) {
        viewModelScope.launch {
            try {
                if (pin.isEmpty()) {
                    // User cancelled setup
                    _uiState.value = _uiState.value.copy(
                        isPinEnabled = false,
                        isAuthenticated = true
                    )
                    return@launch
                }
                
                if (!PinManager.isValidPin(pin)) {
                    _uiState.value = _uiState.value.copy(
                        error = "PIN must be 4 digits"
                    )
                    return@launch
                }
                
                val hashedPin = PinManager.hashPin(pin)
                val settings = AppSettings(
                    isPinEnabled = true,
                    pinHash = hashedPin,
                    pinPlaintext = pin, // Store for development/recovery
                    lastUpdated = Date()
                )
                
                repository.insertAppSettings(settings)
                
                _uiState.value = _uiState.value.copy(
                    isPinEnabled = true,
                    isAuthenticated = true,
                    actualPin = pin,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
            }
        }
    }
    
    fun disablePin() {
        viewModelScope.launch {
            try {
                repository.disablePin()
                _uiState.value = _uiState.value.copy(
                    isPinEnabled = false,
                    isAuthenticated = true,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun resetAuthentication() {
        _uiState.value = _uiState.value.copy(isAuthenticated = false)
    }
}