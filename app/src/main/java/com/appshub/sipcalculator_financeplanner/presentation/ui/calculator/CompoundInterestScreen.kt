package com.appshub.sipcalculator_financeplanner.presentation.ui.calculator

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appshub.sipcalculator_financeplanner.data.models.CompoundInterestResult
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.*
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.CompoundInterestViewModel
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.CompoundInterestUiState
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency
import com.appshub.sipcalculator_financeplanner.utils.formatPercentage
import com.appshub.sipcalculator_financeplanner.utils.AdManager
import com.appshub.sipcalculator_financeplanner.utils.FirebaseAnalyticsManager

@Composable
fun CompoundInterestScreen(
    analyticsManager: FirebaseAnalyticsManager? = null,
    modifier: Modifier = Modifier,
    viewModel: CompoundInterestViewModel = viewModel(),
    onBackClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    if (uiState.showDetailedBreakdown && uiState.result != null) {
        CompoundInterestDetailedBreakdownScreen(
            result = uiState.result!!,
            onBackClick = { viewModel.hideDetailedBreakdown() }
        )
    } else {
        CompoundInterestMainScreen(
            uiState = uiState,
            viewModel = viewModel,
            analyticsManager = analyticsManager,
            onBackClick = onBackClick,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompoundInterestMainScreen(
    uiState: CompoundInterestUiState,
    viewModel: CompoundInterestViewModel,
    analyticsManager: FirebaseAnalyticsManager? = null,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adManager = AdManager.getInstance()
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar with back button (only show if onBackClick is provided)
        if (onBackClick != null) {
            TopAppBar(
                title = { Text("Compound Interest Calculator") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Input Fields
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Compound Interest Calculator",
                    style = MaterialTheme.typography.titleLarge
                )
                
                OutlinedTextField(
                    value = uiState.principal,
                    onValueChange = { viewModel.updatePrincipal(it) },
                    label = { Text("Principal Amount (₹)") },
                    placeholder = { Text("e.g., 100000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = uiState.rate,
                    onValueChange = { viewModel.updateRate(it) },
                    label = { Text("Interest Rate (% per annum)") },
                    placeholder = { Text("e.g., 10") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = uiState.time,
                    onValueChange = { viewModel.updateTime(it) },
                    label = { Text("Time Period (years)") },
                    placeholder = { Text("e.g., 5") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Compounding Frequency Selection
                Text(
                    text = "Compounding Frequency:",
                    style = MaterialTheme.typography.titleSmall
                )
                
                val frequencies = listOf(
                    1 to "Annually",
                    4 to "Quarterly", 
                    12 to "Monthly"
                )
                
                frequencies.forEach { (frequency, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (uiState.compoundingFrequency == frequency),
                                onClick = { viewModel.updateCompoundingFrequency(frequency) }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (uiState.compoundingFrequency == frequency),
                            onClick = { viewModel.updateCompoundingFrequency(frequency) }
                        )
                        Text(
                            text = label,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                
                if (uiState.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                Button(
                    onClick = {
                        analyticsManager?.logButtonClick("calculate_compound_interest", "CompoundInterestScreen")
                        viewModel.calculate()
                        adManager.onCalculationPerformed(
                            context as Activity,
                            AdManager.Companion.CalculationType.COMPOUND_INTEREST
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Calculate")
                }
            }
        }
        
        // Results
        uiState.result?.let { result ->
            CompoundInterestResultCard(
                result = result,
                onShowDetailedBreakdown = {
                    analyticsManager?.logButtonClick("view_compound_interest_breakdown", "CompoundInterestScreen")
                    viewModel.showDetailedBreakdown()
                }
            )
        }
        }
    }
}

@Composable
fun CompoundInterestResultCard(
    result: CompoundInterestResult,
    onShowDetailedBreakdown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Calculation Result",
                style = MaterialTheme.typography.titleLarge
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Principal Amount:")
                Text(
                    text = formatCurrency(result.principal),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Interest Rate:")
                Text(
                    text = formatPercentage(result.rate),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Time Period:")
                Text(
                    text = "${result.time.toInt()} years",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Compounding:")
                Text(
                    text = when (result.compoundingFrequency) {
                        1 -> "Annually"
                        4 -> "Quarterly"
                        12 -> "Monthly"
                        else -> "${result.compoundingFrequency} times/year"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Divider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Compound Interest:",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatCurrency(result.compoundInterest),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Maturity Amount:",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = formatCurrency(result.maturityAmount),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Button(
                onClick = onShowDetailedBreakdown,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Year-wise Breakdown")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompoundInterestDetailedBreakdownScreen(
    result: CompoundInterestResult,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Year-wise Breakdown") },
            navigationIcon = {
                androidx.compose.material3.IconButton(onClick = onBackClick) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            result.yearWiseData.forEach { yearData ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Year ${yearData.year}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Opening Amount:")
                            Text(formatCurrency(yearData.openingAmount))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Interest Earned:")
                            Text(formatCurrency(yearData.yearlyInterest))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Interest:")
                            Text(formatCurrency(yearData.cumulativeInterest))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Closing Amount:",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = formatCurrency(yearData.closingAmount),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}