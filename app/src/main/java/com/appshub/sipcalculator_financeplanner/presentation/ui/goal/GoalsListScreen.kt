package com.appshub.sipcalculator_financeplanner.presentation.ui.goal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appshub.sipcalculator_financeplanner.data.entity.*
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.GoalViewModel
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.PinViewModel
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.PinUiState
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency
import com.appshub.sipcalculator_financeplanner.utils.CurrencyProvider
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsListScreen(
    onGoalClick: (String) -> Unit,
    onCreateGoal: () -> Unit,
    goalViewModel: GoalViewModel = viewModel(),
    pinViewModel: PinViewModel? = null,
    modifier: Modifier = Modifier
) {
    val goalState by goalViewModel.uiState.collectAsStateWithLifecycle()
    val pinState by pinViewModel?.uiState?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(PinUiState()) }
    var showPinOptions by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Goals are automatically loaded in ViewModel init
        // PIN settings are automatically loaded in PinViewModel init
    }
    
    CurrencyProvider { currencyCode ->
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯 My Goals",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                FloatingActionButton(
                    onClick = onCreateGoal,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Goal"
                    )
                }
            }
        }
        
        // Goals Summary
        if (goalState.goals.isNotEmpty()) {
            item {
                GoalsSummaryCard(goalState.goals, currencyCode)
            }
        }
        
        // PIN Settings Card
        item {
            PinSettingsCard(
                isPinEnabled = pinState.isPinEnabled,
                onTogglePin = { enabled ->
                    if (enabled) {
                        showPinOptions = true
                    } else {
                        pinViewModel?.disablePin()
                    }
                }
            )
        }
        
        // Goals List
        if (goalState.goals.isEmpty() && !goalState.isLoading) {
            item {
                EmptyGoalsState(onCreateGoal)
            }
        } else {
            items(goalState.goals) { goal ->
                val progress = goalState.goalsProgress[goal.goalId]
                GoalCard(
                    goal = goal,
                    progress = progress,
                    onClick = { onGoalClick(goal.goalId) },
                    onStatusChange = { status ->
                        goalViewModel.updateGoalStatus(goal.goalId, status)
                    },
                    currencyCode = currencyCode
                )
            }
        }
        
        // Loading State
        if (goalState.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading goals...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // Error State
        goalState.error?.let { error ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Error loading goals",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
    }
    
    // PIN Setup Dialog
    if (showPinOptions) {
        AlertDialog(
            onDismissRequest = { showPinOptions = false },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Enable PIN Protection")
                }
            },
            text = {
                Text("Secure your financial data with a 4-digit PIN. You'll need to enter this PIN each time you open the app.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPinOptions = false
                        // Navigate to PIN setup screen would be implemented here
                        // For now, we'll just show a placeholder
                    }
                ) {
                    Text("Set PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinOptions = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun GoalsSummaryCard(goals: List<Goal>, currencyCode: String) {
    val activeGoals = goals.filter { it.status == GoalStatus.ACTIVE }
    val completedGoals = goals.filter { it.status == GoalStatus.COMPLETED }
    val totalTargetAmount = activeGoals.sumOf { it.targetAmount }
    val totalCurrentAmount = activeGoals.sumOf { it.currentAmount }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 Goals Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GoalSummaryMetric(
                    title = "Active",
                    value = activeGoals.size.toString(),
                    icon = "🎯",
                    modifier = Modifier.weight(1f)
                )
                
                GoalSummaryMetric(
                    title = "Completed",
                    value = completedGoals.size.toString(),
                    icon = "✅",
                    modifier = Modifier.weight(1f)
                )
                
                GoalSummaryMetric(
                    title = "Total Target",
                    value = formatCurrency(totalTargetAmount, currencyCode),
                    icon = "💰",
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (totalTargetAmount > 0) {
                val overallProgress = (totalCurrentAmount / totalTargetAmount) * 100
                
                Text(
                    text = "Overall Progress: ${String.format("%.1f", overallProgress)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                LinearProgressIndicator(
                    progress = (overallProgress / 100.0).coerceIn(0.0, 1.0).toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun GoalSummaryMetric(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineSmall
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCard(
    goal: Goal,
    progress: GoalProgress?,
    onClick: () -> Unit,
    onStatusChange: (GoalStatus) -> Unit,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    val progressPercentage = progress?.progressPercentage ?: 0.0
    val currentAmount = progress?.netWorth ?: 0.0
    val remainingAmount = progress?.remainingAmount ?: goal.targetAmount
    val today = Date()
    val daysRemaining = TimeUnit.MILLISECONDS.toDays(goal.targetDate.time - today.time)
    val isOverdue = daysRemaining < 0
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (goal.status) {
                GoalStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                GoalStatus.PAUSED -> MaterialTheme.colorScheme.surfaceVariant
                GoalStatus.ACTIVE -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = when (goal.status) {
                GoalStatus.COMPLETED -> 8.dp
                GoalStatus.ACTIVE -> 6.dp
                GoalStatus.PAUSED -> 3.dp
            }
        ),
        border = when (goal.status) {
            GoalStatus.COMPLETED -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            else -> null
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
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
                        text = goal.goalType.getIcon(),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Column {
                        Text(
                            text = if (goal.status == GoalStatus.COMPLETED) "✓ ${goal.goalName}" else goal.goalName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (goal.status == GoalStatus.COMPLETED) 
                                MaterialTheme.colorScheme.primary else 
                                MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = goal.goalType.getDisplayName(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Status Badge
                GoalStatusBadge(
                    status = goal.status,
                    onStatusChange = onStatusChange
                )
            }
            
            // Progress Section
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${String.format("%.1f", progressPercentage)}% Complete",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = formatCurrency(goal.targetAmount, currencyCode),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                LinearProgressIndicator(
                    progress = (progressPercentage / 100.0).coerceIn(0.0, 1.0).toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        goal.status == GoalStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                        progressPercentage >= 75 -> Color(0xFF4CAF50)
                        progressPercentage >= 50 -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatCurrency(currentAmount, currencyCode),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "${formatCurrency(remainingAmount, currencyCode)} remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Timeline Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Target Date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(goal.targetDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isOverdue && goal.status == GoalStatus.ACTIVE) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                
                if (goal.status == GoalStatus.ACTIVE) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isOverdue) "Overdue" else "Time Left",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = if (isOverdue) {
                                "${Math.abs(daysRemaining)} days ago"
                            } else {
                                when {
                                    daysRemaining < 30 -> "$daysRemaining days"
                                    daysRemaining < 365 -> "${daysRemaining / 30} months"
                                    else -> "${daysRemaining / 365} years"
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (isOverdue) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
            
            // Monthly SIP Info
            if (goal.monthlySipNeeded > 0) {
                Divider()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 Suggested Monthly SIP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = formatCurrency(goal.monthlySipNeeded, currencyCode),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalStatusBadge(
    status: GoalStatus,
    onStatusChange: (GoalStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        FilterChip(
            onClick = { expanded = true },
            label = {
                Text(
                    text = when (status) {
                        GoalStatus.ACTIVE -> "Active"
                        GoalStatus.COMPLETED -> "Completed"
                        GoalStatus.PAUSED -> "Paused"
                    }
                )
            },
            selected = true,
            leadingIcon = {
                Icon(
                    imageVector = when (status) {
                        GoalStatus.ACTIVE -> Icons.Default.PlayArrow
                        GoalStatus.COMPLETED -> Icons.Default.CheckCircle
                        GoalStatus.PAUSED -> Icons.Default.Pause
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            GoalStatus.values().forEach { goalStatus ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when (goalStatus) {
                                    GoalStatus.ACTIVE -> Icons.Default.PlayArrow
                                    GoalStatus.COMPLETED -> Icons.Default.CheckCircle
                                    GoalStatus.PAUSED -> Icons.Default.Pause
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = when (goalStatus) {
                                    GoalStatus.ACTIVE -> "Active"
                                    GoalStatus.COMPLETED -> "Completed"
                                    GoalStatus.PAUSED -> "Paused"
                                }
                            )
                        }
                    },
                    onClick = {
                        onStatusChange(goalStatus)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyGoalsState(onCreateGoal: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "No Goals Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Create your first financial goal and start tracking your progress towards achieving it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(
                onClick = onCreateGoal
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Your First Goal")
            }
        }
    }
}

@Composable
fun PinSettingsCard(
    isPinEnabled: Boolean,
    onTogglePin: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        text = "PIN Protection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Text(
                        text = if (isPinEnabled) "Your goals are protected" else "Secure your financial data",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            Switch(
                checked = isPinEnabled,
                onCheckedChange = onTogglePin,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}