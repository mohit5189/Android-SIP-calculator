package com.appshub.sipcalculator_financeplanner.presentation.ui.goal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appshub.sipcalculator_financeplanner.data.entity.*
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.InputCard
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.SavingViewModel
import com.appshub.sipcalculator_financeplanner.presentation.viewmodel.DebtViewModel
import com.appshub.sipcalculator_financeplanner.utils.CurrencyProvider
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialSetupScreen(
    goalId: String,
    onBackClick: () -> Unit,
    onSetupComplete: () -> Unit,
    savingViewModel: SavingViewModel = viewModel(),
    debtViewModel: DebtViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val savingState by savingViewModel.uiState.collectAsStateWithLifecycle()
    val debtState by debtViewModel.uiState.collectAsStateWithLifecycle()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showAddSavingDialog by remember { mutableStateOf(false) }
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var savingToDelete by remember { mutableStateOf<Saving?>(null) }
    var debtToDelete by remember { mutableStateOf<Debt?>(null) }
    
    LaunchedEffect(goalId) {
        savingViewModel.loadSavingsForGoal(goalId)
        debtViewModel.loadDebtsForGoal(goalId)
    }
    
    val scrollState = rememberScrollState()
    
    CurrencyProvider { currencyCode ->
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp)
            .verticalScroll(scrollState),
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
                text = "Add Your Finances",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // Progress Indicator
        LinearProgressIndicator(
            progress = 0.5f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Step 2 of 2: Add your current savings and debts",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("💰 Savings")
                        if (savingState.savings.isNotEmpty()) {
                            Badge {
                                Text("${savingState.savings.size}")
                            }
                        }
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("💸 Debts")
                        if (debtState.debts.isNotEmpty()) {
                            Badge {
                                Text("${debtState.debts.size}")
                            }
                        }
                    }
                }
            )
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> SavingsTab(
                savingState = savingState,
                onAddSaving = { showAddSavingDialog = true },
                onEditSaving = { savingViewModel.startEditingSaving(it) },
                onDeleteSaving = { savingToDelete = it },
                modifier = Modifier.weight(1f)
            )
            1 -> DebtsTab(
                debtState = debtState,
                onAddDebt = { showAddDebtDialog = true },
                onEditDebt = { debtViewModel.startEditingDebt(it) },
                onDeleteDebt = { debtToDelete = it },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Summary Card
        FinancialSummaryCard(
            totalSavings = savingState.totalSavings,
            totalDebts = debtState.totalDebts,
            currencyCode = currencyCode
        )
        
        // Complete Setup Button
        Button(
            onClick = onSetupComplete,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (savingState.savings.isNotEmpty() || debtState.debts.isNotEmpty()) {
                    "Complete Setup & View Goal"
                } else {
                    "Skip & View Goal"
                },
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
    
    // Add Saving Dialog
    if (showAddSavingDialog) {
        AddSavingDialog(
            onDismiss = { showAddSavingDialog = false },
            onSave = { category, amount, customName, notes ->
                savingViewModel.addSaving(goalId, category, amount, customName, notes)
                showAddSavingDialog = false
            }
        )
    }
    
    // Add Debt Dialog
    if (showAddDebtDialog) {
        AddDebtDialog(
            onDismiss = { showAddDebtDialog = false },
            onSave = { debtType, amount, emiAmount, interestRate, customName, notes ->
                debtViewModel.addDebt(goalId, debtType, amount, emiAmount, interestRate, customName, notes)
                showAddDebtDialog = false
            }
        )
    }
    
    // Edit Saving Dialog
    savingState.editingSaving?.let { saving ->
        EditSavingDialog(
            saving = saving,
            onDismiss = { savingViewModel.cancelEditing() },
            onSave = { updatedSaving ->
                savingViewModel.updateSaving(updatedSaving)
            }
        )
    }
    
    // Edit Debt Dialog
    debtState.editingDebt?.let { debt ->
        EditDebtDialog(
            debt = debt,
            onDismiss = { debtViewModel.cancelEditing() },
            onSave = { updatedDebt ->
                debtViewModel.updateDebt(updatedDebt)
            }
        )
    }
    
    // Delete Saving Confirmation Dialog
    savingToDelete?.let { saving ->
        AlertDialog(
            onDismissRequest = { savingToDelete = null },
            title = { Text("Delete Saving") },
            text = {
                Text("Are you sure you want to delete this ${saving.category.getDisplayName().lowercase()} investment of ${formatCurrency(saving.amount, currencyCode)}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        savingViewModel.deleteSaving(saving)
                        savingToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { savingToDelete = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete Debt Confirmation Dialog
    debtToDelete?.let { debt ->
        AlertDialog(
            onDismissRequest = { debtToDelete = null },
            title = { Text("Delete Debt") },
            text = {
                Text("Are you sure you want to delete this ${debt.debtType.getDisplayName().lowercase()} debt of ${formatCurrency(debt.amount, currencyCode)}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        debtViewModel.deleteDebt(debt)
                        debtToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { debtToDelete = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    }
}

@Composable
fun SavingsTab(
    savingState: com.appshub.sipcalculator_financeplanner.presentation.viewmodel.SavingUiState,
    onAddSaving: () -> Unit,
    onEditSaving: (Saving) -> Unit,
    onDeleteSaving: (Saving) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add Saving Button
        OutlinedButton(
            onClick = onAddSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Investment/Saving")
        }
        
        // Savings List
        if (savingState.savings.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No savings added yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Add your current investments and savings to track your progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            savingState.savings.forEach { saving ->
                SavingCard(
                    saving = saving,
                    onEdit = { onEditSaving(saving) },
                    onDelete = { onDeleteSaving(saving) }
                )
            }
        }
    }
}

@Composable
fun DebtsTab(
    debtState: com.appshub.sipcalculator_financeplanner.presentation.viewmodel.DebtUiState,
    onAddDebt: () -> Unit,
    onEditDebt: (Debt) -> Unit,
    onDeleteDebt: (Debt) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add Debt Button
        OutlinedButton(
            onClick = onAddDebt,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Debt/Loan")
        }
        
        // Debts List
        if (debtState.debts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No debts added",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Add your loans and debts to get a complete financial picture",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            debtState.debts.forEach { debt ->
                DebtCard(
                    debt = debt,
                    onEdit = { onEditDebt(debt) },
                    onDelete = { onDeleteDebt(debt) }
                )
            }
        }
    }
}

@Composable
fun FinancialSummaryCard(
    totalSavings: Double,
    totalDebts: Double,
    currencyCode: String
) {
    val netWorth = totalSavings - totalDebts
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (netWorth >= 0) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "💰 Financial Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (netWorth >= 0) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Assets",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (netWorth >= 0) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Text(
                        text = formatCurrency(totalSavings, currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (netWorth >= 0) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
                
                Column {
                    Text(
                        text = "Total Debts",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (netWorth >= 0) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Text(
                        text = formatCurrency(totalDebts, currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (netWorth >= 0) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }
            
            Divider(
                color = if (netWorth >= 0) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.3f)
                }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Net Worth",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (netWorth >= 0) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                
                Text(
                    text = formatCurrency(netWorth, currencyCode),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (netWorth >= 0) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
        }
    }
}