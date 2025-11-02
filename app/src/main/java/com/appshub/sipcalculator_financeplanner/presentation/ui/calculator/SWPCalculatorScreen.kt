package com.appshub.sipcalculator_financeplanner.presentation.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.*
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.SWPCalculatorViewModel
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.SWPCalculatorUiState
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency
import com.appshub.sipcalculator_financeplanner.utils.formatPercentage
import com.appshub.sipcalculator_financeplanner.utils.AdManager
import com.appshub.sipcalculator_financeplanner.utils.CurrencyProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SWPCalculatorScreen(
    analyticsManager: com.appshub.sipcalculator_financeplanner.utils.FirebaseAnalyticsManager? = null,
    modifier: Modifier = Modifier,
    viewModel: SWPCalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    if (uiState.showDetailedBreakdown && uiState.result != null) {
        CurrencyProvider { currencyCode ->
            SWPDetailedBreakdownScreen(
                swpResult = uiState.result!!,
                onBackClick = { viewModel.hideDetailedBreakdown() },
                currencyCode = currencyCode
            )
        }
    } else {
        SWPCalculatorMainScreen(
            uiState = uiState,
            viewModel = viewModel,
            analyticsManager = analyticsManager,
            modifier = modifier
        )
    }
}

@Composable
fun SWPCalculatorMainScreen(
    uiState: SWPCalculatorUiState,
    viewModel: SWPCalculatorViewModel,
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
        InputCard(title = "Withdrawal Details") {
            // Initial Corpus
            SuggestiveNumberInputField(
                value = uiState.initialCorpus,
                onValueChange = viewModel::updateInitialCorpus,
                label = "Initial Investment Corpus",
                prefix = com.appshub.sipcalculator_financeplanner.data.preferences.CurrencyInfo.getCurrencyByCode(currencyCode)?.symbol ?: "₹",
                suggestions = SuggestionData.initialCorpus,
                helperText = "Total amount you have invested initially",
                isError = uiState.error != null && uiState.initialCorpus.isEmpty()
            )
            
            // Monthly Withdrawal
            SuggestiveNumberInputField(
                value = uiState.monthlyWithdrawal,
                onValueChange = viewModel::updateMonthlyWithdrawal,
                label = "Monthly Withdrawal",
                prefix = com.appshub.sipcalculator_financeplanner.data.preferences.CurrencyInfo.getCurrencyByCode(currencyCode)?.symbol ?: "₹",
                suggestions = SuggestionData.withdrawalAmounts,
                helperText = "Amount you want to withdraw each month",
                isError = uiState.error != null && uiState.monthlyWithdrawal.isEmpty()
            )
            
            // Duration
            SuggestiveNumberInputField(
                value = uiState.durationInYears,
                onValueChange = viewModel::updateDurationInYears,
                label = "Withdrawal Duration",
                suffix = "years",
                suggestions = SuggestionData.durations,
                helperText = "How long do you want to withdraw?",
                isError = uiState.error != null && uiState.durationInYears.isEmpty()
            )
            
            // Expected Return Rate
            SuggestiveNumberInputField(
                value = uiState.annualReturnRate,
                onValueChange = viewModel::updateAnnualReturnRate,
                label = "Expected Annual Return",
                suffix = "%",
                suggestions = SuggestionData.returnRates,
                helperText = "Expected yearly return on remaining corpus",
                isError = uiState.error != null && uiState.annualReturnRate.isEmpty()
            )
            
            // Step-up SWP Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Step-up SWP",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Increase withdrawal amount annually",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = uiState.isStepUpEnabled,
                    onCheckedChange = { viewModel.toggleStepUp() }
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
                    helperText = "Increase withdrawal amount by this % every year"
                )
            }
        }
        
        val context = LocalContext.current
        
        // Calculate Button
        Button(
            onClick = { 
                viewModel.calculateSWP() 
                
                // Trigger ad logic after calculation
                if (context is android.app.Activity) {
                    AdManager.getInstance().onCalculationPerformed(context, AdManager.Companion.CalculationType.SWP)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = uiState.initialCorpus.isNotEmpty() && 
                     uiState.monthlyWithdrawal.isNotEmpty() &&
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
                text = if (uiState.isCalculating) "Calculating..." else "Calculate SWP",
                style = MaterialTheme.typography.titleMedium
            )
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SWPDetailedBreakdownScreen(
    swpResult: com.appshub.sipcalculator_financeplanner.data.models.SWPResult,
    onBackClick: () -> Unit,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
            // Back button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Summary Card
            item {
                SWPSummaryCard(swpResult, currencyCode)
            }
            
            // Warning if corpus will be exhausted
            if (swpResult.corpusExhaustionWarning) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "⚠️ Warning: Your corpus may get exhausted before the planned duration. Consider reducing withdrawal amount or increasing expected returns.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Year-wise breakdown
            item {
                Text(
                    text = "Year-wise Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            itemsIndexed(swpResult.yearWiseData) { _, yearData ->
                SWPYearCard(yearData = yearData, currencyCode = currencyCode)
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SWPSummaryCard(swpResult: com.appshub.sipcalculator_financeplanner.data.models.SWPResult, currencyCode: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            com.appshub.sipcalculator_financeplanner.ui.theme.GradientStart, 
                            com.appshub.sipcalculator_financeplanner.ui.theme.GradientEnd
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "SWP Summary",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SWPSummaryItem(
                        title = "Initial Corpus",
                        amount = swpResult.initialCorpus,
                        currencyCode = currencyCode,
                        modifier = Modifier.weight(1f)
                    )
                    
                    SWPSummaryItem(
                        title = "Monthly Withdrawal",
                        amount = swpResult.monthlyWithdrawal,
                        currencyCode = currencyCode,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                androidx.compose.material3.Divider(color = Color.White.copy(alpha = 0.3f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SWPSummaryItem(
                        title = "Total Withdrawn",
                        amount = swpResult.totalWithdrawn,
                        currencyCode = currencyCode,
                        modifier = Modifier.weight(1f)
                    )
                    
                    SWPSummaryItem(
                        title = "Remaining Corpus",
                        amount = swpResult.remainingCorpus,
                        currencyCode = currencyCode,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SWPSummaryItem(
    title: String,
    amount: Double,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f)
        )
        
        Text(
            text = formatCurrency(amount, currencyCode),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SWPYearCard(
    yearData: com.appshub.sipcalculator_financeplanner.data.models.YearWiseData,
    currencyCode: String
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Year Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Year ${yearData.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Year Summary for SWP (showing withdrawals as positive)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SWPYearSummaryItem("Opening", yearData.openingBalance, currencyCode)
                SWPYearSummaryItem("Withdrawn", kotlin.math.abs(yearData.totalInvestedThisYear), currencyCode)
                SWPYearSummaryItem("Interest", yearData.interestEarnedThisYear, currencyCode)
                SWPYearSummaryItem("Closing", yearData.closingBalance, currencyCode)
            }
            
            // Monthly breakdown (shown when expanded)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Divider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Monthly Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                yearData.monthlyData.forEach { monthlyData ->
                    SWPMonthlyBreakdownRow(monthlyData, currencyCode)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun SWPYearSummaryItem(label: String, amount: Double, currencyCode: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatCurrency(amount, currencyCode),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
}

@Composable
fun SWPMonthlyBreakdownRow(monthlyData: com.appshub.sipcalculator_financeplanner.data.models.MonthlyData, currencyCode: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = monthlyData.monthName,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = formatCurrency(kotlin.math.abs(monthlyData.sipAmount), currencyCode), // Show withdrawal as positive
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = formatCurrency(monthlyData.interestEarned, currencyCode),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = formatCurrency(monthlyData.closingBalance, currencyCode),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}