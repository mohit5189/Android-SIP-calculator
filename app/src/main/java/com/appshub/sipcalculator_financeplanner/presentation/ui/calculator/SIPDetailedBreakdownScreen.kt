package com.appshub.sipcalculator_financeplanner.presentation.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appshub.sipcalculator_financeplanner.data.models.MonthlyData
import com.appshub.sipcalculator_financeplanner.data.models.SIPResult
import com.appshub.sipcalculator_financeplanner.data.models.YearWiseData
import com.appshub.sipcalculator_financeplanner.ui.theme.GradientEnd
import com.appshub.sipcalculator_financeplanner.ui.theme.GradientStart
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency
import com.appshub.sipcalculator_financeplanner.utils.formatPercentage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SIPDetailedBreakdownScreen(
    sipResult: SIPResult,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    
    LazyColumn(
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
                SummaryCard(sipResult)
            }
            
            // Year-wise breakdown
            item {
                Text(
                    text = "Year-wise Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            itemsIndexed(sipResult.yearWiseData) { index, yearData ->
                YearCard(
                    yearData = yearData,
                    isExpanded = selectedYear == yearData.year,
                    onToggleExpanded = { 
                        selectedYear = if (selectedYear == yearData.year) null else yearData.year 
                    }
                )
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryCard(sipResult: SIPResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Investment Summary",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryItem(
                        title = "Monthly SIP",
                        amount = sipResult.monthlyInvestment,
                        modifier = Modifier.weight(1f)
                    )
                    
                    SummaryItem(
                        title = "Duration",
                        value = "${sipResult.durationInYears} years",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryItem(
                        title = "Expected Return",
                        value = formatPercentage(sipResult.annualReturnRate),
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (sipResult.stepUpPercentage > 0) {
                        SummaryItem(
                            title = "Step-up",
                            value = formatPercentage(sipResult.stepUpPercentage),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                androidx.compose.material3.Divider(color = Color.White.copy(alpha = 0.3f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryItem(
                        title = "Total Invested",
                        amount = sipResult.totalInvested,
                        modifier = Modifier.weight(1f)
                    )
                    
                    SummaryItem(
                        title = "Total Returns",
                        amount = sipResult.totalGains,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                SummaryItem(
                    title = "Maturity Amount",
                    amount = sipResult.maturityAmount,
                    isHighlighted = true,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun SummaryItem(
    title: String,
    amount: Double? = null,
    value: String? = null,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (isHighlighted) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = title,
            style = if (isHighlighted) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f)
        )
        
        Text(
            text = amount?.let { formatCurrency(it) } ?: value ?: "",
            style = if (isHighlighted) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearCard(
    yearData: YearWiseData,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onToggleExpanded
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
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Year Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                YearSummaryItem("Opening", yearData.openingBalance)
                YearSummaryItem("Invested", yearData.totalInvestedThisYear)
                YearSummaryItem("Interest", yearData.interestEarnedThisYear)
                YearSummaryItem("Closing", yearData.closingBalance)
            }
            
            // Monthly breakdown (shown when expanded)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Divider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Monthly Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                yearData.monthlyData.forEach { monthlyData ->
                    MonthlyBreakdownRow(monthlyData)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun YearSummaryItem(label: String, amount: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MonthlyBreakdownRow(monthlyData: MonthlyData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
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
            text = formatCurrency(monthlyData.sipAmount),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = formatCurrency(monthlyData.interestEarned),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = formatCurrency(monthlyData.closingBalance),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}