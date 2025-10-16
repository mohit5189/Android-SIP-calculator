package com.appshub.sipcalculator_financeplanner.presentation.ui.calculator

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.appshub.sipcalculator_financeplanner.data.models.SimpleInterestResult
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.*
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.SimpleInterestViewModel
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.SimpleInterestUiState
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency
import com.appshub.sipcalculator_financeplanner.utils.formatPercentage
import com.appshub.sipcalculator_financeplanner.utils.AdManager
import com.appshub.sipcalculator_financeplanner.utils.FirebaseAnalyticsManager

@Composable
fun SimpleInterestScreen(
    analyticsManager: FirebaseAnalyticsManager? = null,
    modifier: Modifier = Modifier,
    viewModel: SimpleInterestViewModel = viewModel(),
    onBackClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    if (uiState.showDetailedBreakdown && uiState.result != null) {
        SimpleInterestDetailedBreakdownScreen(
            result = uiState.result!!,
            onBackClick = { viewModel.hideDetailedBreakdown() }
        )
    } else {
        SimpleInterestMainScreen(
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
fun SimpleInterestMainScreen(
    uiState: SimpleInterestUiState,
    viewModel: SimpleInterestViewModel,
    analyticsManager: FirebaseAnalyticsManager? = null,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adManager = AdManager.getInstance()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back button
        if (onBackClick != null) {
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
                    text = "Simple Interest Calculator",
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
                        analyticsManager?.logButtonClick("calculate_simple_interest", "SimpleInterestScreen")
                        viewModel.calculate()
                        adManager.onCalculationPerformed(
                            context as Activity,
                            AdManager.Companion.CalculationType.SIMPLE_INTEREST
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
            SimpleInterestResultCard(
                result = result,
                onShowDetailedBreakdown = {
                    analyticsManager?.logButtonClick("view_simple_interest_breakdown", "SimpleInterestScreen")
                    viewModel.showDetailedBreakdown()
                }
            )
        }
    }
}

@Composable
fun SimpleInterestResultCard(
    result: SimpleInterestResult,
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
            
            Divider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Simple Interest:",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatCurrency(result.simpleInterest),
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
fun SimpleInterestDetailedBreakdownScreen(
    result: SimpleInterestResult,
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
                            Text("Yearly Interest:")
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
                                text = "Total Amount:",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = formatCurrency(yearData.totalAmount),
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