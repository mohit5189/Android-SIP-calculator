package com.appshub.sipcalculator_financeplanner.presentation.ui.goal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.appshub.sipcalculator_financeplanner.data.entity.*
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.InputCard
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.GoalViewModel
import com.appshub.sipcalculator_financeplanner.utils.CurrencyProvider
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSetupScreen(
    goalResult: com.appshub.sipcalculator_financeplanner.data.models.GoalResult,
    onBackClick: () -> Unit,
    onGoalCreated: (String) -> Unit,
    goalViewModel: GoalViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var goalName by remember { mutableStateOf("") }
    var goalType by remember { mutableStateOf(GoalType.CUSTOM) }
    var description by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    
    CurrencyProvider { currencyCode ->
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
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
                text = "Set Up Your Goal",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // Goal Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎯 Your Goal Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Target Amount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = formatCurrency(goalResult.targetAmount, currencyCode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Monthly SIP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = formatCurrency(goalResult.requiredMonthlySIP, currencyCode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Text(
                    text = "Time Horizon: ${goalResult.timeHorizon} years",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Goal Details Input
        InputCard(title = "Goal Information") {
            // Goal Name
            OutlinedTextField(
                value = goalName,
                onValueChange = { goalName = it },
                label = { Text("Goal Name") },
                placeholder = { Text("e.g., Dream House, Car, Retirement") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.clearFocus() }
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null
                    )
                }
            )
            
            // Goal Type Dropdown
            ExposedDropdownMenuBox(
                expanded = showDropdown,
                onExpandedChange = { showDropdown = !showDropdown }
            ) {
                OutlinedTextField(
                    value = goalType.getDisplayName(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Goal Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    leadingIcon = {
                        Text(
                            text = goalType.getIcon(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
                
                ExposedDropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false }
                ) {
                    GoalType.values().forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = type.getIcon(),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(text = type.getDisplayName())
                                }
                            },
                            onClick = {
                                goalType = type
                                showDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("Add notes about your goal") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null
                    )
                }
            )
        }
        
        // Information Card
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "What happens next?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Text(
                    text = "• Add your current savings and investments\n" +
                          "• Track your progress monthly\n" +
                          "• Get insights on your financial journey\n" +
                          "• Update amounts as you invest more",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        
        // Create Goal Button
        val coroutineScope = rememberCoroutineScope()
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        val targetDate = Calendar.getInstance().apply {
                            add(Calendar.YEAR, goalResult.timeHorizon)
                        }.time
                        
                        val goalId = goalViewModel.createGoal(
                            goalName = goalName.ifEmpty { "${goalType.getDisplayName()} Goal" },
                            targetAmount = goalResult.targetAmount,
                            targetDate = targetDate,
                            monthlySipNeeded = goalResult.requiredMonthlySIP,
                            goalType = goalType,
                            description = description
                        )
                        
                        println("GoalSetupScreen: Goal created with ID: '$goalId'")
                        onGoalCreated(goalId)
                    } catch (e: Exception) {
                        println("GoalSetupScreen: Error creating goal: ${e.message}")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = goalName.isNotEmpty() || goalType != GoalType.CUSTOM
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Create Goal & Add Finances",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
    }
}

fun GoalType.getDisplayName(): String {
    return when (this) {
        GoalType.HOUSE -> "Dream House"
        GoalType.CAR -> "Car Purchase"
        GoalType.EDUCATION -> "Education"
        GoalType.RETIREMENT -> "Retirement"
        GoalType.MARRIAGE -> "Marriage"
        GoalType.VACATION -> "Vacation"
        GoalType.EMERGENCY_FUND -> "Emergency Fund"
        GoalType.CUSTOM -> "Custom Goal"
    }
}

fun GoalType.getIcon(): String {
    return when (this) {
        GoalType.HOUSE -> "🏠"
        GoalType.CAR -> "🚗"
        GoalType.EDUCATION -> "🎓"
        GoalType.RETIREMENT -> "👴"
        GoalType.MARRIAGE -> "💒"
        GoalType.VACATION -> "🏖️"
        GoalType.EMERGENCY_FUND -> "🆘"
        GoalType.CUSTOM -> "🎯"
    }
}