package com.appshub.sipcalculator_financeplanner.presentation.ui.goal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.appshub.sipcalculator_financeplanner.data.entity.*
import com.appshub.sipcalculator_financeplanner.utils.CurrencyProvider
import com.appshub.sipcalculator_financeplanner.utils.formatCurrency
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtCard(
    debt: Debt,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    CurrencyProvider { currencyCode ->
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Debt Type Icon
            Text(
                text = debt.debtType.getIcon(),
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (debt.customDebtName.isNotEmpty()) {
                        debt.customDebtName
                    } else {
                        debt.debtType.getDisplayName()
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = formatCurrency(debt.amount, currencyCode),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                
                if (debt.emiAmount > 0) {
                    Text(
                        text = "EMI: ${formatCurrency(debt.emiAmount, currencyCode)}/month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (debt.interestRate > 0) {
                    Text(
                        text = "Interest: ${String.format("%.1f", debt.interestRate)}% p.a.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (debt.notes.isNotEmpty()) {
                    Text(
                        text = debt.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "Updated: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(debt.lastUpdated)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Actions
            Column {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtDialog(
    onDismiss: () -> Unit,
    onSave: (DebtType, Double, Double, Double, String, String) -> Unit
) {
    CurrencyProvider { currencyCode ->
        val currencySymbol = com.appshub.sipcalculator_financeplanner.data.preferences.CurrencyInfo.getCurrencyByCode(currencyCode)?.symbol ?: "₹"
    var selectedDebtType by remember { mutableStateOf(DebtType.PERSONAL_LOAN) }
    var amount by remember { mutableStateOf("") }
    var emiAmount by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showDebtTypeDropdown by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Debt/Loan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Debt Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = showDebtTypeDropdown,
                    onExpandedChange = { showDebtTypeDropdown = !showDebtTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedDebtType.getDisplayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Debt Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDebtTypeDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = {
                            Text(
                                text = selectedDebtType.getIcon(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showDebtTypeDropdown,
                        onDismissRequest = { showDebtTypeDropdown = false }
                    ) {
                        DebtType.values().forEach { debtType ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = debtType.getIcon(),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(text = debtType.getDisplayName())
                                    }
                                },
                                onClick = {
                                    selectedDebtType = debtType
                                    showDebtTypeDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Custom Name (if custom debt type)
                if (selectedDebtType == DebtType.CUSTOM) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Custom Debt Name") },
                        placeholder = { Text("e.g., Gold Loan, Friend Loan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Outstanding Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Outstanding Amount") },
                    placeholder = { Text("0") },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // EMI Amount (Optional)
                OutlinedTextField(
                    value = emiAmount,
                    onValueChange = { emiAmount = it },
                    label = { Text("Monthly EMI (Optional)") },
                    placeholder = { Text("0") },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Interest Rate (Optional)
                OutlinedTextField(
                    value = interestRate,
                    onValueChange = { interestRate = it },
                    label = { Text("Interest Rate (Optional)") },
                    placeholder = { Text("0") },
                    suffix = { Text("% p.a.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Additional details") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull() ?: 0.0
                            val emiValue = emiAmount.toDoubleOrNull() ?: 0.0
                            val interestValue = interestRate.toDoubleOrNull() ?: 0.0
                            
                            if (amountValue > 0) {
                                onSave(selectedDebtType, amountValue, emiValue, interestValue, customName, notes)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = amount.toDoubleOrNull()?.let { it > 0 } == true &&
                                 (selectedDebtType != DebtType.CUSTOM || customName.isNotEmpty())
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDebtDialog(
    debt: Debt,
    onDismiss: () -> Unit,
    onSave: (Debt) -> Unit
) {
    CurrencyProvider { currencyCode ->
        val currencySymbol = com.appshub.sipcalculator_financeplanner.data.preferences.CurrencyInfo.getCurrencyByCode(currencyCode)?.symbol ?: "₹"
    var amount by remember { mutableStateOf(debt.amount.toString()) }
    var emiAmount by remember { mutableStateOf(debt.emiAmount.toString()) }
    var interestRate by remember { mutableStateOf(debt.interestRate.toString()) }
    var customName by remember { mutableStateOf(debt.customDebtName) }
    var notes by remember { mutableStateOf(debt.notes) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit ${debt.debtType.getDisplayName()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Debt Type Display (non-editable)
                OutlinedTextField(
                    value = debt.debtType.getDisplayName(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Debt Type") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Text(
                            text = debt.debtType.getIcon(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
                
                // Custom Name (if custom debt type)
                if (debt.debtType == DebtType.CUSTOM) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Custom Debt Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Outstanding Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Outstanding Amount") },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // EMI Amount
                OutlinedTextField(
                    value = emiAmount,
                    onValueChange = { emiAmount = it },
                    label = { Text("Monthly EMI (Optional)") },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Interest Rate
                OutlinedTextField(
                    value = interestRate,
                    onValueChange = { interestRate = it },
                    label = { Text("Interest Rate (Optional)") },
                    suffix = { Text("% p.a.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull() ?: 0.0
                            val emiValue = emiAmount.toDoubleOrNull() ?: 0.0
                            val interestValue = interestRate.toDoubleOrNull() ?: 0.0
                            
                            if (amountValue > 0) {
                                onSave(
                                    debt.copy(
                                        amount = amountValue,
                                        emiAmount = emiValue,
                                        interestRate = interestValue,
                                        customDebtName = customName,
                                        notes = notes
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = amount.toDoubleOrNull()?.let { it > 0 } == true
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
    }
}