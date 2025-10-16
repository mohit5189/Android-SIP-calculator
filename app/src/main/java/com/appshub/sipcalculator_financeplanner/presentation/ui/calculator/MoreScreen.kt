package com.appshub.sipcalculator_financeplanner.presentation.ui.calculator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.appshub.sipcalculator_financeplanner.utils.FirebaseAnalyticsManager

enum class MoreCalculatorType {
    SIMPLE_INTEREST,
    COMPOUND_INTEREST,
    EMI,
    RD,
    PPF,
    FD
}

@Composable
fun MoreScreen(
    analyticsManager: FirebaseAnalyticsManager? = null,
    currentCalculator: MoreCalculatorType? = null,
    onCalculatorChanged: (MoreCalculatorType?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Use external state instead of local state
    val selectedCalculator = currentCalculator
    
    when (selectedCalculator) {
        MoreCalculatorType.SIMPLE_INTEREST -> {
            SimpleInterestScreen(
                analyticsManager = analyticsManager,
                modifier = modifier,
                onBackClick = {
                    onCalculatorChanged(null)
                    analyticsManager?.logButtonClick("back_to_more_screen", "SimpleInterestScreen")
                }
            )
        }
        MoreCalculatorType.COMPOUND_INTEREST -> {
            CompoundInterestScreen(
                analyticsManager = analyticsManager,
                modifier = modifier,
                onBackClick = {
                    onCalculatorChanged(null)
                    analyticsManager?.logButtonClick("back_to_more_screen", "CompoundInterestScreen")
                }
            )
        }
        MoreCalculatorType.EMI -> {
            EMICalculatorScreen(
                analyticsManager = analyticsManager,
                modifier = modifier,
                onBackClick = {
                    onCalculatorChanged(null)
                    analyticsManager?.logButtonClick("back_to_more_screen", "EMICalculatorScreen")
                }
            )
        }
        MoreCalculatorType.RD -> {
            RDCalculatorScreen(
                analyticsManager = analyticsManager,
                modifier = modifier,
                onBackClick = {
                    onCalculatorChanged(null)
                    analyticsManager?.logButtonClick("back_to_more_screen", "RDCalculatorScreen")
                }
            )
        }
        MoreCalculatorType.PPF -> {
            PPFCalculatorScreen(
                analyticsManager = analyticsManager,
                modifier = modifier,
                onBackClick = {
                    onCalculatorChanged(null)
                    analyticsManager?.logButtonClick("back_to_more_screen", "PPFCalculatorScreen")
                }
            )
        }
        MoreCalculatorType.FD -> {
            FDCalculatorScreen(
                analyticsManager = analyticsManager,
                modifier = modifier,
                onBackClick = {
                    onCalculatorChanged(null)
                    analyticsManager?.logButtonClick("back_to_more_screen", "FDCalculatorScreen")
                }
            )
        }
        null -> {
            MoreMainScreen(
                onCalculatorSelected = { type ->
                    onCalculatorChanged(type)
                    analyticsManager?.logScreenView(
                        when (type) {
                            MoreCalculatorType.SIMPLE_INTEREST -> "simple_interest_calculator"
                            MoreCalculatorType.COMPOUND_INTEREST -> "compound_interest_calculator"
                            MoreCalculatorType.EMI -> "emi_calculator"
                            MoreCalculatorType.RD -> "rd_calculator"
                            MoreCalculatorType.PPF -> "ppf_calculator"
                            MoreCalculatorType.FD -> "fd_calculator"
                        },
                        when (type) {
                            MoreCalculatorType.SIMPLE_INTEREST -> "SimpleInterestScreen"
                            MoreCalculatorType.COMPOUND_INTEREST -> "CompoundInterestScreen"
                            MoreCalculatorType.EMI -> "EMICalculatorScreen"
                            MoreCalculatorType.RD -> "RDCalculatorScreen"
                            MoreCalculatorType.PPF -> "PPFCalculatorScreen"
                            MoreCalculatorType.FD -> "FDCalculatorScreen"
                        }
                    )
                },
                analyticsManager = analyticsManager,
                modifier = modifier
            )
        }
    }
}

@Composable
fun MoreMainScreen(
    onCalculatorSelected: (MoreCalculatorType) -> Unit,
    analyticsManager: FirebaseAnalyticsManager? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        CalculatorOptionCard(
            title = "Simple Interest Calculator",
            description = "Calculate simple interest on your principal amount with fixed interest rate",
            icon = Icons.Default.Calculate,
            onClick = {
                analyticsManager?.logButtonClick("simple_interest_selected", "MoreScreen")
                onCalculatorSelected(MoreCalculatorType.SIMPLE_INTEREST)
            }
        )
        
        CalculatorOptionCard(
            title = "Compound Interest Calculator",
            description = "Calculate compound interest with various compounding frequencies (yearly, quarterly, monthly)",
            icon = Icons.Default.TrendingUp,
            onClick = {
                analyticsManager?.logButtonClick("compound_interest_selected", "MoreScreen")
                onCalculatorSelected(MoreCalculatorType.COMPOUND_INTEREST)
            }
        )
        
        CalculatorOptionCard(
            title = "EMI Calculator",
            description = "Calculate equated monthly installments for loans with detailed payment breakdown",
            icon = Icons.Default.Calculate,
            onClick = {
                analyticsManager?.logButtonClick("emi_selected", "MoreScreen")
                onCalculatorSelected(MoreCalculatorType.EMI)
            }
        )
        
        CalculatorOptionCard(
            title = "RD Calculator",
            description = "Calculate recurring deposit maturity amount with monthly deposit planning",
            icon = Icons.Default.TrendingUp,
            onClick = {
                analyticsManager?.logButtonClick("rd_selected", "MoreScreen")
                onCalculatorSelected(MoreCalculatorType.RD)
            }
        )
        
        CalculatorOptionCard(
            title = "PPF Calculator",
            description = "Calculate Public Provident Fund returns with 15-year tenure and tax benefits",
            icon = Icons.Default.Calculate,
            onClick = {
                analyticsManager?.logButtonClick("ppf_selected", "MoreScreen")
                onCalculatorSelected(MoreCalculatorType.PPF)
            }
        )
        
        CalculatorOptionCard(
            title = "FD Calculator",
            description = "Calculate fixed deposit maturity with simple or compound interest options",
            icon = Icons.Default.TrendingUp,
            onClick = {
                analyticsManager?.logButtonClick("fd_selected", "MoreScreen")
                onCalculatorSelected(MoreCalculatorType.FD)
            }
        )
    }
}

@Composable
fun CalculatorOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}