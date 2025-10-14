package com.appshub.sipcalculator_financeplanner.presentation.ui.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appshub.sipcalculator_financeplanner.data.models.PPFResult
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.InputCard
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.ResultCard
import com.appshub.sipcalculator_financeplanner.utils.AdManager
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency
import com.appshub.sipcalculator_financeplanner.utils.FinancialCalculator
import com.appshub.sipcalculator_financeplanner.utils.FirebaseAnalyticsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PPFCalculatorScreen(
    onBackClick: () -> Unit = {},
    analyticsManager: FirebaseAnalyticsManager? = null,
    modifier: Modifier = Modifier
) {
    var yearlyDeposit by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<PPFResult?>(null) }
    
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "PPF Calculator",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // PPF Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = "PPF Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Fixed 15-year tenure\n• Minimum: ₹500, Maximum: ₹1.5L per year\n• Tax-free returns under Section 80C\n• Interest rate is set by Government (currently 7.1%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        
        // Input Section
        InputCard(
            title = "PPF Investment Details",
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = yearlyDeposit,
                    onValueChange = { yearlyDeposit = it },
                    label = { Text("Yearly Deposit (₹500 - ₹1,50,000)") },
                    placeholder = { Text("₹1,50,000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = interestRate,
                    onValueChange = { interestRate = it },
                    label = { Text("Interest Rate (% per annum)") },
                    placeholder = { Text("7.1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Text(
                    text = "PPF has a fixed tenure of 15 years. Interest rate changes as per Government notifications.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Calculate Button
        Button(
            onClick = {
                val yearlyDepositDouble = yearlyDeposit.toDoubleOrNull()
                val interestRateDouble = interestRate.toDoubleOrNull()
                
                if (yearlyDepositDouble != null && interestRateDouble != null) {
                    if (yearlyDepositDouble >= 500 && yearlyDepositDouble <= 150000 && interestRateDouble > 0) {
                        result = FinancialCalculator.calculatePPF(yearlyDepositDouble, interestRateDouble)
                        
                        // Track calculation
                        AdManager.getInstance().onCalculationPerformed(
                            context as android.app.Activity,
                            AdManager.Companion.CalculationType.PPF
                        )
                        
                        analyticsManager?.logButtonClick("calculate_ppf", "PPFCalculatorScreen")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = yearlyDeposit.isNotBlank() && interestRate.isNotBlank()
        ) {
            Text("Calculate PPF")
        }
        
        // Results Section
        result?.let { ppfResult ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "PPF Calculation Results",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Key Results
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Maturity Amount:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatCurrency(ppfResult.maturityAmount),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Divider()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Yearly Deposit:")
                        Text(formatCurrency(ppfResult.yearlyDeposit))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Deposits:")
                        Text(formatCurrency(ppfResult.totalDeposits))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Interest Earned:")
                        Text(
                            text = formatCurrency(ppfResult.totalInterest),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tenure:")
                        Text("${ppfResult.tenure} years (Fixed)")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Interest Rate:")
                        Text("${ppfResult.interestRate}% p.a.")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tax Benefit:")
                        Text(
                            text = "80C Deduction",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                }
            }
        }
    }
}