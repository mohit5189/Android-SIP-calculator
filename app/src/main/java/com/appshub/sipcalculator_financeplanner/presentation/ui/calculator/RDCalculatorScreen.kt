package com.appshub.sipcalculator_financeplanner.presentation.ui.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.appshub.sipcalculator_financeplanner.data.models.RDResult
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.InputCard
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.ResultCard
import com.appshub.sipcalculator_financeplanner.utils.AdManager
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency
import com.appshub.sipcalculator_financeplanner.utils.FinancialCalculator
import com.appshub.sipcalculator_financeplanner.utils.FirebaseAnalyticsManager
import com.appshub.sipcalculator_financeplanner.utils.CurrencyProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RDCalculatorScreen(
    onBackClick: () -> Unit = {},
    analyticsManager: FirebaseAnalyticsManager? = null,
    modifier: Modifier = Modifier
) {
    var monthlyDeposit by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var tenure by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<RDResult?>(null) }
    
    val context = LocalContext.current
    
    CurrencyProvider { currencyCode ->
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
            title = "Recurring Deposit Details",
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = monthlyDeposit,
                    onValueChange = { monthlyDeposit = it },
                    label = { Text("Monthly Deposit") },
                    placeholder = { Text("${com.appshub.sipcalculator_financeplanner.data.preferences.CurrencyInfo.getCurrencyByCode(currencyCode)?.symbol ?: "₹"}5,000") },
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
                    label = { Text("Tenure (years)") },
                    placeholder = { Text("5") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
        
        // Calculate Button
        Button(
            onClick = {
                val monthlyDepositDouble = monthlyDeposit.toDoubleOrNull()
                val interestRateDouble = interestRate.toDoubleOrNull()
                val tenureInt = tenure.toIntOrNull()
                
                if (monthlyDepositDouble != null && interestRateDouble != null && tenureInt != null) {
                    if (monthlyDepositDouble > 0 && interestRateDouble >= 0 && tenureInt > 0) {
                        result = FinancialCalculator.calculateRD(
                            monthlyDeposit = monthlyDepositDouble,
                            interestRate = interestRateDouble,
                            tenure = tenureInt
                        )
                        
                        // Track calculation
                        AdManager.getInstance().onCalculationPerformed(
                            context as android.app.Activity,
                            AdManager.Companion.CalculationType.RD
                        )
                        
                        analyticsManager?.logButtonClick("calculate_rd", "RDCalculatorScreen")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = monthlyDeposit.isNotBlank() && interestRate.isNotBlank() && tenure.isNotBlank()
        ) {
            Text("Calculate RD")
        }
        
        // Results Section
        result?.let { rdResult ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "RD Calculation Results",
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
                            text = formatCurrency(rdResult.maturityAmount, currencyCode),
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
                        Text("Monthly Deposit:")
                        Text(formatCurrency(rdResult.monthlyDeposit, currencyCode))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Deposits:")
                        Text(formatCurrency(rdResult.totalDeposits, currencyCode))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Interest Earned:")
                        Text(
                            text = formatCurrency(rdResult.totalInterest, currencyCode),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tenure:")
                        Text("${rdResult.tenure} years")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Interest Rate:")
                        Text("${rdResult.interestRate}% p.a.")
                    }
                }
                }
            }
        }
    }
}}