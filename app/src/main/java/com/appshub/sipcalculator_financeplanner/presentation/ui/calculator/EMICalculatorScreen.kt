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
import com.appshub.sipcalculator_financeplanner.data.models.EMIResult
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.InputCard
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.ResultCard
import com.appshub.sipcalculator_financeplanner.utils.AdManager
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency
import com.appshub.sipcalculator_financeplanner.utils.FinancialCalculator
import com.appshub.sipcalculator_financeplanner.utils.FirebaseAnalyticsManager
import com.appshub.sipcalculator_financeplanner.utils.CurrencyProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EMICalculatorScreen(
    onBackClick: () -> Unit = {},
    analyticsManager: FirebaseAnalyticsManager? = null,
    modifier: Modifier = Modifier
) {
    var loanAmount by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var loanTenure by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<EMIResult?>(null) }
    
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
                title = "Loan Details",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = loanAmount,
                        onValueChange = { loanAmount = it },
                        label = { Text("Loan Amount") },
                        placeholder = {
                            Text(
                                "${
                                    com.appshub.sipcalculator_financeplanner.data.preferences.CurrencyInfo.getCurrencyByCode(
                                        currencyCode
                                    )?.symbol ?: "₹"
                                }10,00,000"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = interestRate,
                        onValueChange = { interestRate = it },
                        label = { Text("Interest Rate (% per annum)") },
                        placeholder = { Text("8.5") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = loanTenure,
                        onValueChange = { loanTenure = it },
                        label = { Text("Loan Tenure (months)") },
                        placeholder = { Text("240") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }

            // Calculate Button
            Button(
                onClick = {
                    val loanAmountDouble = loanAmount.toDoubleOrNull()
                    val interestRateDouble = interestRate.toDoubleOrNull()
                    val loanTenureInt = loanTenure.toIntOrNull()

                    if (loanAmountDouble != null && interestRateDouble != null && loanTenureInt != null) {
                        if (loanAmountDouble > 0 && interestRateDouble >= 0 && loanTenureInt > 0) {
                            result = FinancialCalculator.calculateEMI(
                                loanAmount = loanAmountDouble,
                                interestRate = interestRateDouble,
                                loanTenure = loanTenureInt
                            )

                            // Track calculation
                            AdManager.getInstance().onCalculationPerformed(
                                context as android.app.Activity,
                                AdManager.Companion.CalculationType.EMI
                            )

                            analyticsManager?.logButtonClick("calculate_emi", "EMICalculatorScreen")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = loanAmount.isNotBlank() && interestRate.isNotBlank() && loanTenure.isNotBlank()
            ) {
                Text("Calculate EMI")
            }

            // Results Section
            result?.let { emiResult ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "EMI Calculation Results",
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
                                    text = "Monthly EMI:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = formatCurrency(emiResult.emi, currencyCode),
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
                                Text("Loan Amount:")
                                Text(formatCurrency(emiResult.loanAmount, currencyCode))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Amount Payable:")
                                Text(formatCurrency(emiResult.totalAmount, currencyCode))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Interest:")
                                Text(
                                    text = formatCurrency(emiResult.totalInterest, currencyCode),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Loan Tenure:")
                                Text("${emiResult.loanTenure} months (${emiResult.loanTenure / 12} years)")
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Interest Rate:")
                                Text("${emiResult.interestRate}% p.a.")
                            }
                        }
                    }
                }
            }
        }
    }
}