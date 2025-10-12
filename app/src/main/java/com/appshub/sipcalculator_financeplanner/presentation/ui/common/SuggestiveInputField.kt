package com.appshub.sipcalculator_financeplanner.presentation.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestiveNumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    helperText: String? = null,
    isError: Boolean = false,
    errorText: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                // Only allow numbers and decimal point
                val filteredValue = newValue.filter { it.isDigit() || it == '.' }
                // Prevent multiple decimal points
                val finalValue = if (filteredValue.count { it == '.' } <= 1) filteredValue else value
                onValueChange(finalValue)
            },
            label = { Text(label) },
            prefix = if (prefix.isNotEmpty()) {
                { Text(prefix, style = MaterialTheme.typography.bodyMedium) }
            } else null,
            suffix = if (suffix.isNotEmpty()) {
                { Text(suffix, style = MaterialTheme.typography.bodyMedium) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            singleLine = true
        )
        
        // Show suggestion chips if available
        if (suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            SuggestionChips(
                suggestions = suggestions,
                onSuggestionClick = { suggestion ->
                    val cleanValue = suggestion.replace(Regex("[^\\d.]"), "")
                    onValueChange(cleanValue)
                }
            )
        }
        
        if (isError && errorText != null) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        } else if (helperText != null) {
            Text(
                text = helperText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

object SuggestionData {
    val monthlyAmounts = listOf(
        "₹1,000", "₹2,500", "₹5,000", "₹10,000", "₹15,000", "₹25,000", "₹50,000"
    )
    
    val durations = listOf(
        "1 year", "3 years", "5 years", "10 years", "15 years", "20 years", "25 years"
    )
    
    val returnRates = listOf(
        "8%", "10%", "12%", "14%", "15%", "18%"
    )
    
    val stepUpPercentages = listOf(
        "5%", "10%", "15%", "20%"
    )
    
    val withdrawalAmounts = listOf(
        "₹5,000", "₹10,000", "₹25,000", "₹50,000", "₹75,000", "₹1,00,000"
    )
    
    val initialCorpus = listOf(
        "₹5,00,000", "₹10,00,000", "₹25,00,000", "₹50,00,000", "₹1,00,00,000"
    )
    
    val goalAmounts = listOf(
        "₹10,00,000", "₹25,00,000", "₹50,00,000", "₹1,00,00,000", "₹2,00,00,000", "₹5,00,00,000"
    )
}

@Composable
fun SuggestionChips(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(suggestions) { suggestion ->
            SuggestionChip(
                onClick = { onSuggestionClick(suggestion) },
                label = { Text(suggestion) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}