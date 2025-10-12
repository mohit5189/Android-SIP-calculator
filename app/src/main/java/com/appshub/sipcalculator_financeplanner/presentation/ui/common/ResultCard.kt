package com.appshub.sipcalculator_financeplanner.presentation.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appshub.sipcalculator_financeplanner.ui.theme.GradientEnd
import com.appshub.sipcalculator_financeplanner.ui.theme.GradientStart
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultCard(
    title: String,
    totalInvested: Double,
    maturityAmount: Double,
    totalGains: Double,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    additionalInfo: @Composable ColumnScope.() -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(20.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ResultItem(
                            title = "Invested",
                            amount = totalInvested,
                            modifier = Modifier.weight(1f)
                        )
                        
                        ResultItem(
                            title = "Returns",
                            amount = totalGains,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    androidx.compose.material3.Divider(color = Color.White.copy(alpha = 0.3f))
                    
                    ResultItem(
                        title = "Maturity Amount",
                        amount = maturityAmount,
                        isHighlighted = true
                    )
                    
                    additionalInfo()
                }
            }
        }
    }
}

@Composable
fun ResultItem(
    title: String,
    amount: Double,
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
        
        AnimatedNumber(
            targetValue = amount,
            style = if (isHighlighted) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AnimatedNumber(
    targetValue: Double,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    fontWeight: FontWeight
) {
    var displayValue by remember { mutableStateOf(0.0) }
    
    LaunchedEffect(targetValue) {
        val animationDuration = 500L
        val steps = 50
        val increment = targetValue / steps
        
        for (i in 1..steps) {
            displayValue = increment * i
            kotlinx.coroutines.delay(animationDuration / steps)
        }
        displayValue = targetValue
    }
    
    Text(
        text = formatCurrency(displayValue),
        style = style,
        color = color,
        fontWeight = fontWeight
    )
}