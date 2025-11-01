package com.appshub.sipcalculator_financeplanner.presentation.ui.goal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appshub.sipcalculator_financeplanner.data.entity.*
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.GoalHistoryViewModel
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.GoalHistoryUiState
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalHistoryScreen(
    goalId: String,
    goalName: String,
    onBackClick: () -> Unit,
    historyViewModel: GoalHistoryViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val historyState by historyViewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(goalId) {
        historyViewModel.loadHistoryForGoal(goalId)
    }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
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
                    text = "Progress History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        
        // Goal Name
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "📊 $goalName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Progress Chart
        if (historyState.historyList.isNotEmpty()) {
            item {
                ProgressChartCard(historyState.historyList)
            }
        }
        
        // History Timeline
        if (historyState.historyList.isNotEmpty()) {
            item {
                Text(
                    text = "📈 Monthly Progress Timeline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(historyState.historyList.reversed()) { history ->
                HistoryTimelineItem(history)
            }
        } else if (!historyState.isLoading) {
            item {
                EmptyHistoryState()
            }
        }
        
        // Loading State
        if (historyState.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading history...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // Error State
        historyState.error?.let { error ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Error: $error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressChartCard(historyList: List<GoalHistory>) {
    val recentHistory = historyList.takeLast(12) // Last 12 months
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📊 Progress Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (recentHistory.size >= 2) {
                ProgressChart(
                    history = recentHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Text(
                    text = "Need at least 2 months of data to show trend",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }
}

@Composable
fun ProgressChart(
    history: List<GoalHistory>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()
        
        val chartWidth = width - (padding * 2)
        val chartHeight = height - (padding * 2)
        
        if (history.isEmpty()) return@Canvas
        
        // Find min and max values for scaling
        val maxNetWorth = history.maxOfOrNull { it.netWorth } ?: 0.0
        val minNetWorth = history.minOfOrNull { it.netWorth } ?: 0.0
        val range = maxNetWorth - minNetWorth
        
        // Draw axes
        drawLine(
            color = surfaceVariant,
            start = Offset(padding, padding),
            end = Offset(padding, height - padding),
            strokeWidth = 2.dp.toPx()
        )
        
        drawLine(
            color = surfaceVariant,
            start = Offset(padding, height - padding),
            end = Offset(width - padding, height - padding),
            strokeWidth = 2.dp.toPx()
        )
        
        // Draw progress line
        if (history.size > 1) {
            val path = Path()
            val stepX = chartWidth / (history.size - 1)
            
            history.forEachIndexed { index, historyItem ->
                val x = padding + (index * stepX)
                val normalizedValue = if (range > 0) {
                    ((historyItem.netWorth - minNetWorth) / range).toFloat()
                } else 0.5f
                val y = height - padding - (normalizedValue * chartHeight)
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                
                // Draw data points
                drawCircle(
                    color = primaryColor,
                    radius = 6.dp.toPx(),
                    center = Offset(x, y)
                )
            }
            
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@Composable
fun HistoryTimelineItem(history: GoalHistory) {
    val isPositiveGrowth = history.netWorth >= 0
    val progressIcon = when {
        history.progressPercentage >= 100 -> "🎉"
        history.progressPercentage >= 75 -> "🔥"
        history.progressPercentage >= 50 -> "⭐"
        history.progressPercentage >= 25 -> "🌱"
        else -> "🚀"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPositiveGrowth) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month indicator
            Box(
                modifier = Modifier
                    .size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = progressIcon,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            
            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = history.monthYear,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${String.format("%.1f", history.progressPercentage)}% Progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Assets: ${formatCurrency(history.totalSavings)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (history.totalDebt > 0) {
                        Text(
                            text = "Debts: ${formatCurrency(history.totalDebt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Net Worth
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Net Worth",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = formatCurrency(history.netWorth),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositiveGrowth) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyHistoryState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timeline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "No Progress History Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Start recording your monthly progress to see your financial journey unfold here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(
                onClick = { /* Navigate back to dashboard */ }
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record First Progress")
            }
        }
    }
}