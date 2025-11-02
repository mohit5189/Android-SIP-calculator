package com.appshub.sipcalculator_financeplanner.presentation.ui.calculator

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.*
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.GoalCalculatorViewModel
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.GoalCalculatorUiState
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency
import com.appshub.sipcalculator_financeplanner.utils.formatPercentage
import com.appshub.sipcalculator_financeplanner.utils.AdManager
import com.appshub.sipcalculator_financeplanner.utils.CurrencyProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCalculatorScreen(
    analyticsManager: com.appshub.sipcalculator_financeplanner.utils.FirebaseAnalyticsManager? = null,
    onSetAsGoal: ((com.appshub.sipcalculator_financeplanner.data.models.GoalResult) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: GoalCalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    if (uiState.showDetailedBreakdown && uiState.result != null) {
        CurrencyProvider { currencyCode ->
            GoalDetailedBreakdownScreen(
                goalResult = uiState.result!!,
                onBackClick = { viewModel.hideDetailedBreakdown() },
                onSetAsGoal = onSetAsGoal,
                currencyCode = currencyCode
            )
        }
    } else {
        CurrencyProvider { currencyCode ->
            GoalCalculatorMainScreen(
                uiState = uiState,
                viewModel = viewModel,
                analyticsManager = analyticsManager,
                currencyCode = currencyCode,
                modifier = modifier
            )
        }
    }
}

@Composable
fun GoalCalculatorMainScreen(
    uiState: GoalCalculatorUiState,
    viewModel: GoalCalculatorViewModel,
    analyticsManager: com.appshub.sipcalculator_financeplanner.utils.FirebaseAnalyticsManager? = null,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // Input Card
        InputCard(title = "Goal Planning Details") {
            // Target Goal Amount
            SuggestiveNumberInputField(
                value = uiState.targetAmount,
                onValueChange = viewModel::updateTargetAmount,
                label = "Target Goal Amount",
                prefix = com.appshub.sipcalculator_financeplanner.data.preferences.CurrencyInfo.getCurrencyByCode(currencyCode)?.symbol ?: "₹",
                suggestions = SuggestionData.goalAmounts(com.appshub.sipcalculator_financeplanner.data.preferences.CurrencyInfo.getCurrencyByCode(currencyCode)?.symbol ?: "₹"),
                helperText = "How much money do you need?",
                isError = uiState.error != null && uiState.targetAmount.isEmpty()
            )
            
            // Time Horizon
            SuggestiveNumberInputField(
                value = uiState.timeHorizon,
                onValueChange = viewModel::updateTimeHorizon,
                label = "Time to Achieve Goal",
                suffix = "years",
                suggestions = SuggestionData.durations,
                helperText = "When do you need this money?",
                isError = uiState.error != null && uiState.timeHorizon.isEmpty()
            )
            
            // Initial Amount
            SuggestiveNumberInputField(
                value = uiState.initialAmount,
                onValueChange = viewModel::updateInitialAmount,
                label = "Initial Lump Sum (Optional)",
                prefix = com.appshub.sipcalculator_financeplanner.data.preferences.CurrencyInfo.getCurrencyByCode(currencyCode)?.symbol ?: "₹",
                suggestions = SuggestionData.initialCorpus(com.appshub.sipcalculator_financeplanner.data.preferences.CurrencyInfo.getCurrencyByCode(currencyCode)?.symbol ?: "₹"),
                helperText = "Any amount you already have or can invest now"
            )
            
            // Expected Return Rate
            SuggestiveNumberInputField(
                value = uiState.expectedReturn,
                onValueChange = viewModel::updateExpectedReturn,
                label = "Expected Annual Return",
                suffix = "%",
                suggestions = SuggestionData.returnRates,
                helperText = "Expected yearly return from your investment",
                isError = uiState.error != null && uiState.expectedReturn.isEmpty()
            )
            
            // Step-up SIP Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Step-up SIP",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Increase SIP amount annually to beat inflation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = uiState.isStepUpEnabled,
                    onCheckedChange = { viewModel.toggleStepUp() }
                )
            }
            
            // Step-up percentage (shown only when enabled)
            if (uiState.isStepUpEnabled) {
                SuggestiveNumberInputField(
                    value = uiState.stepUpPercentage,
                    onValueChange = viewModel::updateStepUpPercentage,
                    label = "Annual Step-up",
                    suffix = "%",
                    suggestions = SuggestionData.stepUpPercentages,
                    helperText = "Increase SIP amount by this % every year"
                )
            }
        }
        
        val context = LocalContext.current
        
        // Calculate Button
        Button(
            onClick = { 
                viewModel.calculateGoal() 
                
                // Trigger ad logic after calculation
                if (context is android.app.Activity) {
                    AdManager.getInstance().onCalculationPerformed(context, AdManager.Companion.CalculationType.GOAL)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = uiState.targetAmount.isNotEmpty() && 
                     uiState.timeHorizon.isNotEmpty() && 
                     uiState.expectedReturn.isNotEmpty()
        ) {
            if (uiState.isCalculating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (uiState.isCalculating) "Calculating..." else "Calculate Goal Plan",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        // Error message
        uiState.error?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailedBreakdownScreen(
    goalResult: com.appshub.sipcalculator_financeplanner.data.models.GoalResult,
    onBackClick: () -> Unit,
    onSetAsGoal: ((com.appshub.sipcalculator_financeplanner.data.models.GoalResult) -> Unit)? = null,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.lazy.LazyColumn(
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
                GoalSummaryCard(goalResult, currencyCode)
            }
            
            // Set as Goal Button
            onSetAsGoal?.let { callback ->
                item {
                    // Animation states
                    var animationStarted by remember { mutableStateOf(false) }
                    
                    val buttonScale by animateFloatAsState(
                        targetValue = if (animationStarted) 1f else 0.8f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "buttonScale"
                    )
                    
                    val buttonAlpha by animateFloatAsState(
                        targetValue = if (animationStarted) 1f else 0f,
                        animationSpec = tween(durationMillis = 800),
                        label = "buttonAlpha"
                    )
                    
                    // Pulsing animation for attention
                    val pulseAnimation = rememberInfiniteTransition(label = "pulse")
                    val buttonPulse by pulseAnimation.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "buttonPulse"
                    )
                    
                    // Shimmer effect for the icon
                    val shimmerAnimation = rememberInfiniteTransition(label = "shimmer")
                    val iconShimmer by shimmerAnimation.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "iconShimmer"
                    )
                    
                    LaunchedEffect(Unit) {
                        delay(500) // Delay to make it appear after other content
                        animationStarted = true
                    }
                    
                    Button(
                        onClick = { callback(goalResult) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .graphicsLayer(
                                scaleX = buttonScale * buttonPulse,
                                scaleY = buttonScale * buttonPulse,
                                alpha = buttonAlpha,
                                shadowElevation = 12f
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 16.dp,
                            focusedElevation = 12.dp,
                            hoveredElevation = 12.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer(alpha = iconShimmer)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Set as My Goal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Required SIP Card
            item {
                RequiredSIPCard(goalResult)
            }
            
            // Goal Achievement Probability
            item {
                GoalProbabilityCard(goalResult, currencyCode)
            }
            
            // Milestones
            item {
                Text(
                    text = "Goal Milestones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(goalResult.milestones) { milestone ->
                MilestoneCard(milestone, currencyCode)
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSummaryCard(goalResult: com.appshub.sipcalculator_financeplanner.data.models.GoalResult, currencyCode: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            com.appshub.sipcalculator_financeplanner.ui.theme.GradientStart, 
                            com.appshub.sipcalculator_financeplanner.ui.theme.GradientEnd
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🎯 Goal Summary",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GoalSummaryItem(
                        title = "Target Amount",
                        amount = goalResult.targetAmount,
                        currencyCode = currencyCode,
                        modifier = Modifier.weight(1f)
                    )
                    
                    GoalSummaryItem(
                        title = "Time Horizon",
                        value = "${goalResult.timeHorizon} years",
                        currencyCode = currencyCode,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (goalResult.initialAmount > 0) {
                    GoalSummaryItem(
                        title = "Initial Investment",
                        amount = goalResult.initialAmount,
                        currencyCode = currencyCode,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequiredSIPCard(goalResult: com.appshub.sipcalculator_financeplanner.data.models.GoalResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Required Monthly SIP",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = formatCurrency(goalResult.requiredMonthlySIP),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold
            )
            
            if (goalResult.stepUpPercentage > 0) {
                Text(
                    text = "With ${formatPercentage(goalResult.stepUpPercentage)} annual step-up",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalProbabilityCard(goalResult: com.appshub.sipcalculator_financeplanner.data.models.GoalResult, currencyCode: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (goalResult.goalAchievementProbability >= 0.7) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (goalResult.goalAchievementProbability >= 0.7) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.Warning
                },
                contentDescription = null,
                tint = if (goalResult.goalAchievementProbability >= 0.7) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Goal Achievement Probability",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (goalResult.goalAchievementProbability >= 0.7) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                
                Text(
                    text = "${(goalResult.goalAchievementProbability * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (goalResult.goalAchievementProbability >= 0.7) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                
                Text(
                    text = when {
                        goalResult.goalAchievementProbability >= 0.8 -> "Excellent chance of achieving your goal!"
                        goalResult.goalAchievementProbability >= 0.7 -> "Good chance of achieving your goal"
                        goalResult.goalAchievementProbability >= 0.6 -> "Moderate chance - consider increasing SIP"
                        else -> "Low probability - consider longer duration or higher SIP"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (goalResult.goalAchievementProbability >= 0.7) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestoneCard(milestone: com.appshub.sipcalculator_financeplanner.data.models.Milestone, currencyCode: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Progress circle
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = milestone.percentage / 100f,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "${milestone.percentage}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${milestone.percentage}% of Goal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = formatCurrency(milestone.targetAmount, currencyCode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Expected in ${milestone.timeToReach} years",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = if (milestone.isAchieved) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.Schedule
                },
                contentDescription = null,
                tint = if (milestone.isAchieved) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
fun GoalSummaryItem(
    title: String,
    amount: Double? = null,
    value: String? = null,
    currencyCode: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f)
        )
        
        Text(
            text = amount?.let { formatCurrency(it, currencyCode ?: "INR") } ?: value ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}