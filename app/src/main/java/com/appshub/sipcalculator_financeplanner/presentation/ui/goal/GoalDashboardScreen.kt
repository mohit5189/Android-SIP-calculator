package com.appshub.sipcalculator_financeplanner.presentation.ui.goal

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appshub.sipcalculator_financeplanner.data.entity.*
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.*
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency
import com.appshub.sipcalculator_financeplanner.utils.CurrencyProvider
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDashboardScreen(
    goalId: String,
    onBackClick: () -> Unit,
    onEditGoal: () -> Unit,
    onManageFinances: () -> Unit,
    goalViewModel: GoalViewModel = viewModel(),
    savingViewModel: SavingViewModel = viewModel(),
    debtViewModel: DebtViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val goalState by goalViewModel.uiState.collectAsStateWithLifecycle()
    val savingState by savingViewModel.uiState.collectAsStateWithLifecycle()
    val debtState by debtViewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(goalId) {
        goalViewModel.selectGoal(goalId)
        savingViewModel.loadSavingsForGoal(goalId)
        debtViewModel.loadDebtsForGoal(goalId)
    }
    
    val goal = goalState.selectedGoal
    val progress = goalState.goalProgress
    
    if (goal == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        return
    }
    
    CurrencyProvider { currencyCode ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Header
        item {
            GoalDashboardHeader(
                goal = goal,
                progress = progress,
                onBackClick = onBackClick,
                onEditGoal = onEditGoal,
                onRecordProgress = { goalViewModel.recordMonthlyProgress(goalId) }
            )
        }
        
        // Progress Visualization
        item {
            GoalProgressCard(
                goal = goal,
                progress = progress,
                currencyCode = currencyCode
            )
        }
        
        // Financial Summary
        item {
            FinancialOverviewCard(
                totalSavings = savingState.totalSavings,
                totalDebts = debtState.totalDebts,
                targetAmount = goal.targetAmount,
                onManageFinances = onManageFinances,
                currencyCode = currencyCode
            )
        }
        
        // Savings Breakdown
        if (savingState.savings.isNotEmpty()) {
            item {
                SavingsBreakdownCard(
                    savings = savingState.savings,
                    savingsSummary = savingState.savingsSummary,
                    currencyCode = currencyCode
                )
            }
        }
        
        // Debts Breakdown
        if (debtState.debts.isNotEmpty()) {
            item {
                DebtsBreakdownCard(
                    debts = debtState.debts,
                    debtsSummary = debtState.debtsSummary,
                    currencyCode = currencyCode
                )
            }
        }
        
        // Motivational Insights
        item {
            progress?.let { 
                MotivationalInsightsCard(
                    goal = goal,
                    progress = it,
                    currencyCode = currencyCode
                )
            }
        }
        
        // Monthly Update Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📊 Monthly Update",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Record your progress and get updated insights for this month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Button(
                        onClick = onManageFinances,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record This Month's Progress")
                    }
                }
            }
        }
    }
    }
}

@Composable
fun GoalDashboardHeader(
    goal: Goal,
    progress: GoalProgress?,
    onBackClick: () -> Unit,
    onEditGoal: () -> Unit,
    onRecordProgress: () -> Unit
) {
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
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${goal.goalType.getIcon()} ${goal.goalName}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(goal.targetDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(onClick = onEditGoal) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Goal"
            )
        }
    }
}

@Composable
fun GoalProgressCard(
    goal: Goal,
    progress: GoalProgress?,
    currencyCode: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🎯 Goal Progress",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = formatCurrency(goal.targetAmount, currencyCode),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                // Circular Progress
                progress?.let {
                    AnimatedCircularProgress(
                        progress = (it.progressPercentage / 100.0).toFloat(),
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
            
            progress?.let { progressData ->
                // Progress Bar
                LinearProgressIndicator(
                    progress = (progressData.progressPercentage / 100.0).coerceIn(0.0, 1.0).toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${String.format("%.1f", progressData.progressPercentage)}% Complete",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = "${formatCurrency(progressData.remainingAmount, currencyCode)} to go",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                // Status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (progressData.isOnTrack) {
                            Icons.Default.TrendingUp
                        } else {
                            Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = if (progressData.isOnTrack) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFFF9800)
                        }
                    )
                    
                    Text(
                        text = if (progressData.isOnTrack) {
                            "🎉 On track! You're doing great!"
                        } else {
                            "⚠️ Need ${formatCurrency(progressData.monthlyProgressNeeded, currencyCode)}/month to catch up"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 1000
        )
    )
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)
            
            // Background circle
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            
            // Progress arc
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }
        
        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun FinancialOverviewCard(
    totalSavings: Double,
    totalDebts: Double,
    targetAmount: Double,
    onManageFinances: () -> Unit,
    currencyCode: String
) {
    val netWorth = totalSavings - totalDebts
    
    // Animation states
    val animationState = remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (animationState.value) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )
    
    val cardAlpha by animateFloatAsState(
        targetValue = if (animationState.value) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "cardAlpha"
    )
    
    LaunchedEffect(Unit) {
        delay(100) // Small delay for better effect
        animationState.value = true
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(
                scaleX = cardScale,
                scaleY = cardScale,
                alpha = cardAlpha
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp,
            pressedElevation = 16.dp,
            focusedElevation = 14.dp,
            hoveredElevation = 14.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Animated title with shimmer effect
            val shimmerAnimation = rememberInfiniteTransition(label = "shimmer")
            val shimmerAlpha by shimmerAnimation.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "shimmerAlpha"
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💰",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.graphicsLayer(alpha = shimmerAlpha)
                    )
                    Text(
                        text = "Financial Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                TextButton(onClick = onManageFinances) {
                    Text("Manage")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FinancialMetricCard(
                    title = "Assets",
                    amount = totalSavings,
                    icon = "💎",
                    color = MaterialTheme.colorScheme.primary,
                    currencyCode = currencyCode,
                    modifier = Modifier.weight(1f)
                )
                
                FinancialMetricCard(
                    title = "Debts",
                    amount = totalDebts,
                    icon = "💸",
                    color = MaterialTheme.colorScheme.error,
                    currencyCode = currencyCode,
                    modifier = Modifier.weight(1f)
                )
                
                FinancialMetricCard(
                    title = "Net Worth",
                    amount = netWorth,
                    icon = if (netWorth >= 0) "📈" else "📉",
                    color = if (netWorth >= 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    currencyCode = currencyCode,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun FinancialMetricCard(
    title: String,
    amount: Double,
    icon: String,
    color: Color,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    // Individual animations for each metric card
    val animationState = remember { mutableStateOf(false) }
    val slideOffset by animateIntAsState(
        targetValue = if (animationState.value) 0 else 100,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "slideOffset"
    )
    
    val iconScale by animateFloatAsState(
        targetValue = if (animationState.value) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )
    
    // Pulsing animation for the icon
    val pulseAnimation = rememberInfiniteTransition(label = "pulse")
    val iconPulse by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconPulse"
    )
    
    LaunchedEffect(Unit) {
        delay(200) // Staggered animation
        animationState.value = true
    }
    
    Card(
        modifier = modifier
            .padding(4.dp)
            .offset(y = slideOffset.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp,
            focusedElevation = 10.dp,
            hoveredElevation = 10.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.graphicsLayer(
                    scaleX = iconScale * iconPulse,
                    scaleY = iconScale * iconPulse,
                    shadowElevation = 4f,
                    clip = false
                )
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = formatCurrency(amount, currencyCode),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun SavingsBreakdownCard(
    savings: List<Saving>,
    savingsSummary: List<com.appshub.sipcalculator_financeplanner.data.dao.SavingsSummary>,
    currencyCode: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 Savings Portfolio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            savingsSummary.forEach { summary ->
                val percentage = if (savings.sumOf { it.amount } > 0) {
                    (summary.total / savings.sumOf { it.amount }) * 100
                } else 0.0
                
                SavingBreakdownItem(
                    category = summary.category,
                    amount = summary.total,
                    percentage = percentage,
                    currencyCode = currencyCode
                )
            }
        }
    }
}

@Composable
fun SavingBreakdownItem(
    category: SavingCategory,
    amount: Double,
    percentage: Double,
    currencyCode: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = category.getIcon(),
            style = MaterialTheme.typography.titleMedium
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.getDisplayName(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            LinearProgressIndicator(
                progress = (percentage / 100.0).coerceIn(0.0, 1.0).toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatCurrency(amount, currencyCode),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${String.format("%.1f", percentage)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DebtsBreakdownCard(
    debts: List<Debt>,
    debtsSummary: List<com.appshub.sipcalculator_financeplanner.data.dao.DebtsSummary>,
    currencyCode: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "💳 Debt Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            
            debtsSummary.forEach { summary ->
                val percentage = if (debts.sumOf { it.amount } > 0) {
                    (summary.total / debts.sumOf { it.amount }) * 100
                } else 0.0
                
                DebtBreakdownItem(
                    debtType = summary.debtType,
                    amount = summary.total,
                    percentage = percentage,
                    currencyCode = currencyCode
                )
            }
        }
    }
}

@Composable
fun DebtBreakdownItem(
    debtType: DebtType,
    amount: Double,
    percentage: Double,
    currencyCode: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = debtType.getIcon(),
            style = MaterialTheme.typography.titleMedium
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = debtType.getDisplayName(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            LinearProgressIndicator(
                progress = (percentage / 100.0).coerceIn(0.0, 1.0).toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.error
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatCurrency(amount, currencyCode),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            
            Text(
                text = "${String.format("%.1f", percentage)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MotivationalInsightsCard(
    goal: Goal,
    progress: GoalProgress,
    currencyCode: String
) {
    val insights = generateInsights(goal, progress, currencyCode)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "💡 Smart Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            insights.forEach { insight ->
                InsightItem(insight)
            }
        }
    }
}

@Composable
fun InsightItem(insight: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = insight,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

fun generateInsights(goal: Goal, progress: GoalProgress, currencyCode: String): List<String> {
    val insights = mutableListOf<String>()
    
    when {
        progress.progressPercentage >= 100 -> {
            insights.add("🎉 Congratulations! You've achieved your goal!")
        }
        progress.progressPercentage >= 75 -> {
            insights.add("🔥 You're in the final stretch! Just ${formatCurrency(progress.remainingAmount, currencyCode)} to go!")
            insights.add("💪 Keep up the momentum - you're almost there!")
        }
        progress.progressPercentage >= 50 -> {
            insights.add("⭐ Great progress! You've crossed the halfway mark!")
            insights.add("📈 You're building wealth steadily. Stay consistent!")
        }
        progress.progressPercentage >= 25 -> {
            insights.add("🌱 Your wealth is growing! You're ${String.format("%.1f", progress.progressPercentage)}% there!")
            insights.add("🎯 Focus on consistent investments to reach your goal faster.")
        }
        else -> {
            insights.add("🚀 Every journey begins with a single step. You've started!")
            insights.add("💡 Consider increasing your monthly investment for faster progress.")
        }
    }
    
    if (!progress.isOnTrack && progress.monthsToGo > 0) {
        insights.add("⚡ To get back on track, try investing ${formatCurrency(progress.monthlyProgressNeeded, currencyCode)} per month.")
    }
    
    if (progress.monthsToGo <= 12) {
        insights.add("📅 Less than a year to go! Your goal is within reach!")
    }
    
    return insights
}
