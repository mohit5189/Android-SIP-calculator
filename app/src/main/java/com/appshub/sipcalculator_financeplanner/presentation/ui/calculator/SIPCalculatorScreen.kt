package com.appshub.sipcalculator_financeplanner.presentation.ui.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.*
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.SuggestiveNumberInputField
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.SuggestionData
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.SIPCalculatorViewModel
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.SIPCalculatorUiState
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency
import com.appshub.sipcalculator_financeplanner.utils.formatPercentage
import com.appshub.sipcalculator_financeplanner.utils.AdManager
import com.appshub.sipcalculator_financeplanner.utils.CurrencyProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SIPCalculatorScreen(
    analyticsManager: com.appshub.sipcalculator_financeplanner.utils.FirebaseAnalyticsManager? = null,
    modifier: Modifier = Modifier,
    viewModel: SIPCalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    if (uiState.showDetailedBreakdown && uiState.result != null) {
        SIPDetailedBreakdownScreen(
            sipResult = uiState.result!!,
            onBackClick = { viewModel.hideDetailedBreakdown() }
        )
    } else {
        SIPCalculatorMainScreen(
            uiState = uiState,
            viewModel = viewModel,
            analyticsManager = analyticsManager,
            modifier = modifier
        )
    }
}

@Composable
fun SIPCalculatorMainScreen(
    uiState: SIPCalculatorUiState,
    viewModel: SIPCalculatorViewModel,
    analyticsManager: com.appshub.sipcalculator_financeplanner.utils.FirebaseAnalyticsManager? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    CurrencyProvider { currencyCode ->
        Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // Input Card
        InputCard(title = "Investment Details") {
            // Monthly Investment Amount
            SuggestiveNumberInputField(
                value = uiState.monthlyInvestment,
                onValueChange = viewModel::updateMonthlyInvestment,
                label = "Monthly Investment",
                prefix = com.appshub.sipcalculator_financeplanner.data.preferences.CurrencyInfo.getCurrencyByCode(currencyCode)?.symbol ?: "₹",
                suggestions = SuggestionData.monthlyAmounts(com.appshub.sipcalculator_financeplanner.data.preferences.CurrencyInfo.getCurrencyByCode(currencyCode)?.symbol ?: "₹"),
                helperText = "Amount to invest every month",
                isError = uiState.error != null && uiState.monthlyInvestment.isEmpty()
            )
            
            // Duration
            SuggestiveNumberInputField(
                value = uiState.durationInYears,
                onValueChange = viewModel::updateDurationInYears,
                label = "Investment Duration",
                suffix = "years",
                suggestions = SuggestionData.durations,
                helperText = "How long do you want to invest?",
                isError = uiState.error != null && uiState.durationInYears.isEmpty()
            )
            
            // Expected Return Rate
            SuggestiveNumberInputField(
                value = uiState.annualReturnRate,
                onValueChange = viewModel::updateAnnualReturnRate,
                label = "Expected Annual Return",
                suffix = "%",
                suggestions = SuggestionData.returnRates,
                helperText = "Expected yearly return from your investment",
                isError = uiState.error != null && uiState.annualReturnRate.isEmpty()
            )
            
            // Step-up SIP Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Step-up SIP",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Increase investment amount annually",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = uiState.isStepUpEnabled,
                    onCheckedChange = { 
                        analyticsManager?.logButtonClick("stepup_toggle", "sip_calculator")
                        viewModel.toggleStepUp() 
                    }
                )
            }
            
            // Step-up percentage (shown only when enabled)
            if (uiState.isStepUpEnabled) {
                SuggestiveNumberInputField(
                    value = uiState.stepUpPercentage,
                    onValueChange = viewModel::updateStepUpPercentage,
                    label = "Annual Step-up",
                    suffix = "%",
                    suggestions = SuggestionData.stepUpPercentages,
                    helperText = "Increase investment amount by this % every year"
                )
            }
        }
        
        val context = LocalContext.current
        
        // Calculate Button
        Button(
            onClick = { 
                analyticsManager?.logButtonClick("calculate_sip", "sip_calculator")
                viewModel.calculateSIP()
                
                // Trigger ad logic after calculation
                if (context is android.app.Activity) {
                    AdManager.getInstance().onCalculationPerformed(context, AdManager.Companion.CalculationType.SIP)
                }
                
                // Log the calculation with parameters after calculation
                uiState.result?.let { result ->
                    analyticsManager?.logSIPCalculation(
                        monthlyInvestment = result.monthlyInvestment,
                        expectedReturn = result.annualReturnRate,
                        timePeriod = result.durationInYears,
                        maturityAmount = result.maturityAmount
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = uiState.monthlyInvestment.isNotEmpty() && 
                     uiState.annualReturnRate.isNotEmpty() && 
                     uiState.durationInYears.isNotEmpty()
        ) {
            if (uiState.isCalculating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (uiState.isCalculating) "Calculating..." else "Calculate SIP",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        // Results Card
        uiState.result?.let { result ->
            ResultCard(
                title = "SIP Calculation Results",
                totalInvested = result.totalInvested,
                maturityAmount = result.maturityAmount,
                totalGains = result.totalGains,
                isLoading = uiState.isCalculating
            ) {
                // Additional info for SIP
                if (result.stepUpPercentage > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "With ${formatPercentage(result.stepUpPercentage)} annual step-up",
                                style = MaterialTheme.typography.bodySmall,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
                
                // Final monthly investment amount (after step-ups)
                if (result.stepUpPercentage > 0) {
                    val finalMonthlyAmount = result.monthlyInvestment * 
                        Math.pow(1 + result.stepUpPercentage / 100, result.durationInYears.toDouble())
                    
                    Text(
                        text = "Final monthly SIP: ${formatCurrency(finalMonthlyAmount, currencyCode)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // View Details Button
                OutlinedButton(
                    onClick = { 
                        analyticsManager?.logButtonClick("view_detailed_breakdown", "sip_calculator")
                        viewModel.showDetailedBreakdown() 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, 
                        androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = "📊 View Detailed Breakdown",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
        
        // Error message
        uiState.error?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}}