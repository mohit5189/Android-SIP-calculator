package com.appshub.sipcalculator_financeplanner.presentation.ui.goal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.window.Dialog
import com.appshub.sipcalculator_financeplanner.data.entity.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingCard(
    saving: Saving,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Icon
            Text(
                text = saving.category.getIcon(),
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (saving.customCategoryName.isNotEmpty()) {
                        saving.customCategoryName
                    } else {
                        saving.category.getDisplayName()
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "₹${String.format("%.0f", saving.amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (saving.notes.isNotEmpty()) {
                    Text(
                        text = saving.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "Updated: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(saving.lastUpdated)}",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingDialog(
    onDismiss: () -> Unit,
    onSave: (SavingCategory, Double, String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(SavingCategory.BANK_SAVINGS) }
    var amount by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
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
                    text = "Add Investment/Saving",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = !showCategoryDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.getDisplayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Investment Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        leadingIcon = {
                            Text(
                                text = selectedCategory.getIcon(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        SavingCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = category.getIcon(),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(text = category.getDisplayName())
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Custom Name (if custom category)
                if (selectedCategory == SavingCategory.CUSTOM) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Custom Name") },
                        placeholder = { Text("e.g., NSC, Post Office") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Current Amount") },
                    placeholder = { Text("0") },
                    prefix = { Text("₹") },
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
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
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
                            if (amountValue > 0) {
                                onSave(selectedCategory, amountValue, customName, notes)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = amount.toDoubleOrNull()?.let { it > 0 } == true &&
                                 (selectedCategory != SavingCategory.CUSTOM || customName.isNotEmpty())
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSavingDialog(
    saving: Saving,
    onDismiss: () -> Unit,
    onSave: (Saving) -> Unit
) {
    var amount by remember { mutableStateOf(saving.amount.toString()) }
    var customName by remember { mutableStateOf(saving.customCategoryName) }
    var notes by remember { mutableStateOf(saving.notes) }
    
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
                    text = "Edit ${saving.category.getDisplayName()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Category Display (non-editable)
                OutlinedTextField(
                    value = saving.category.getDisplayName(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Investment Type") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Text(
                            text = saving.category.getIcon(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
                
                // Custom Name (if custom category)
                if (saving.category == SavingCategory.CUSTOM) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Custom Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Current Amount") },
                    prefix = { Text("₹") },
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
                            if (amountValue > 0) {
                                onSave(
                                    saving.copy(
                                        amount = amountValue,
                                        customCategoryName = customName,
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