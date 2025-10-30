package com.appshub.sipcalculator_financeplanner.presentation.ui.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.appshub.sipcalculator_financeplanner.data.models.FDResult
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.InputCard
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.ResultCard
import com.appshub.sipcalculator_financeplanner.utils.AdManager
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency
import com.appshub.sipcalculator_financeplanner.utils.FinancialCalculator
import com.appshub.sipcalculator_financeplanner.utils.FirebaseAnalyticsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FDCalculatorScreen(
    onBackClick: () -> Unit = {},
    analyticsManager: FirebaseAnalyticsManager? = null,
    modifier: Modifier = Modifier
) {
    var principal by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var tenure by remember { mutableStateOf("") }
    var isCompoundInterest by remember { mutableStateOf(true) }
    var result by remember { mutableStateOf<FDResult?>(null) }
    
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back button
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
        
        // Input Section
        InputCard(
            title = "Fixed Deposit Details",
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = principal,
                    onValueChange = { principal = it },
                    label = { Text("Principal Amount") },
                    placeholder = { Text("₹1,00,000") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = interestRate,
                    onValueChange = { interestRate = it },
                    label = { Text("Interest Rate (% per annum)") },
                    placeholder = { Text("6.5") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = tenure,
                    onValueChange = { tenure = it },
                    label = { Text("Tenure (months)") },
                    placeholder = { Text("12") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                Text(
                    text = "Interest Type:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isCompoundInterest,
                                onClick = { isCompoundInterest = true }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isCompoundInterest,
                            onClick = { isCompoundInterest = true }
                        )
                        Text(
                            text = "Compound Interest (Quarterly compounding)",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = !isCompoundInterest,
                                onClick = { isCompoundInterest = false }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !isCompoundInterest,
                            onClick = { isCompoundInterest = false }
                        )
                        Text(
                            text = "Simple Interest",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
        
        // Calculate Button
        Button(
            onClick = {
                val principalDouble = principal.toDoubleOrNull()
                val interestRateDouble = interestRate.toDoubleOrNull()
                val tenureInt = tenure.toIntOrNull()
                
                if (principalDouble != null && interestRateDouble != null && tenureInt != null) {
                    if (principalDouble > 0 && interestRateDouble >= 0 && tenureInt > 0) {
                        result = FinancialCalculator.calculateFD(
                            principal = principalDouble,
                            interestRate = interestRateDouble,
                            tenure = tenureInt,
                            isCompoundInterest = isCompoundInterest
                        )
                        
                        // Track calculation
                        AdManager.getInstance().onCalculationPerformed(
                            context as android.app.Activity,
                            AdManager.Companion.CalculationType.FD
                        )
                        
                        analyticsManager?.logButtonClick("calculate_fd", "FDCalculatorScreen")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = principal.isNotBlank() && interestRate.isNotBlank() && tenure.isNotBlank()
        ) {
            Text("Calculate FD")
        }
        
        // Results Section
        result?.let { fdResult ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "FD Calculation Results",
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
                            text = formatCurrency(fdResult.maturityAmount),
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
                        Text("Principal Amount:")
                        Text(formatCurrency(fdResult.principal))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Interest Earned:")
                        Text(
                            text = formatCurrency(fdResult.totalInterest),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Average Monthly Interest:")
                        Text(formatCurrency(fdResult.monthlyInterest))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tenure:")
                        Text("${fdResult.tenure} months (${fdResult.tenure / 12.0} years)")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Interest Rate:")
                        Text("${fdResult.interestRate}% p.a.")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Interest Type:")
                        Text(if (fdResult.isCompoundInterest) "Compound (Quarterly)" else "Simple")
                    }
                }
                }
            }
        }
    }
}