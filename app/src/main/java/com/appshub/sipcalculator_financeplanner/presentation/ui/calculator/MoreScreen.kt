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
    COMPOUND_INTEREST
}

@Composable
fun MoreScreen(
    analyticsManager: FirebaseAnalyticsManager? = null,
    modifier: Modifier = Modifier
) {
    var selectedCalculator by remember { mutableStateOf<MoreCalculatorType?>(null) }
    
    when (selectedCalculator) {
        MoreCalculatorType.SIMPLE_INTEREST -> {
            SimpleInterestScreen(
                analyticsManager = analyticsManager,
                modifier = modifier,
                onBackClick = {
                    selectedCalculator = null
                    analyticsManager?.logButtonClick("back_to_more_screen", "SimpleInterestScreen")
                }
            )
        }
        MoreCalculatorType.COMPOUND_INTEREST -> {
            CompoundInterestScreen(
                analyticsManager = analyticsManager,
                modifier = modifier,
                onBackClick = {
                    selectedCalculator = null
                    analyticsManager?.logButtonClick("back_to_more_screen", "CompoundInterestScreen")
                }
            )
        }
        null -> {
            MoreMainScreen(
                onCalculatorSelected = { type ->
                    selectedCalculator = type
                    analyticsManager?.logScreenView(
                        when (type) {
                            MoreCalculatorType.SIMPLE_INTEREST -> "simple_interest_calculator"
                            MoreCalculatorType.COMPOUND_INTEREST -> "compound_interest_calculator"
                        },
                        when (type) {
                            MoreCalculatorType.SIMPLE_INTEREST -> "SimpleInterestScreen"
                            MoreCalculatorType.COMPOUND_INTEREST -> "CompoundInterestScreen"
                        }
                    )
                },
                analyticsManager = analyticsManager,
                modifier = modifier
            )
        }
    }
    
    // Handle back navigation by resetting to main screen
    LaunchedEffect(selectedCalculator) {
        if (selectedCalculator != null) {
            // You can add back navigation logic here if needed
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
        Text(
            text = "More Calculators",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
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
        
        // Placeholder for future calculators
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "More calculators coming soon!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "We're working on adding more financial calculators to help with your planning needs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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