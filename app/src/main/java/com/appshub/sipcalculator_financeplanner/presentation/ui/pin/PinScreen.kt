package com.appshub.sipcalculator_financeplanner.presentation.ui.pin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinScreen(
    onPinEntered: (String) -> Unit,
    onForgotPin: () -> Unit,
    isSetupMode: Boolean = false,
    actualPin: String? = null, // For development 5-tap feature
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirmMode by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var tapCount by remember { mutableStateOf(0) }
    var showToast by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // Secret tap feature for forgot PIN
    LaunchedEffect(tapCount) {
        if (tapCount >= 5) {
            showToast = true
            kotlinx.coroutines.delay(3000) // Show for longer
            showToast = false
            tapCount = 0
        }
    }
    
    // Error handling
    LaunchedEffect(showError) {
        if (showError && isSetupMode && isConfirmMode && confirmPin.isEmpty()) {
            kotlinx.coroutines.delay(2000)
            showError = false
            isConfirmMode = false
            pin = ""
        }
    }
    
    // Toast for secret tap
    if (showToast) {
        LaunchedEffect(Unit) {
            if (actualPin != null && !isSetupMode) {
                // Show actual PIN for development/recovery
                android.widget.Toast.makeText(
                    context, 
                    "🔐 Your PIN: $actualPin", 
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } else {
                android.widget.Toast.makeText(
                    context, 
                    "🔐 PIN Recovery: Email utilityappsmaker5189@gmail.com for assistance", 
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(32.dp)
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) {
                if (!isSetupMode) {
                    tapCount++
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = when {
                isSetupMode && !isConfirmMode -> "Set Your PIN"
                isSetupMode && isConfirmMode -> "Confirm Your PIN"
                else -> "Enter Your PIN"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle
        Text(
            text = when {
                isSetupMode && !isConfirmMode -> "Create a 4-digit PIN to protect your financial data"
                isSetupMode && isConfirmMode -> "Enter the same PIN again to confirm"
                else -> "Enter your 4-digit PIN to access your goals"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // PIN Input Display
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < (if (isConfirmMode) confirmPin.length else pin.length)) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < (if (isConfirmMode) confirmPin.length else pin.length)) {
                        Text(
                            text = "●",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Error Message
        if (showError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // PIN Keypad
        PinKeypad(
            onNumberClick = { number ->
                val currentPin = if (isConfirmMode) confirmPin else pin
                if (currentPin.length < 4) {
                    val newPin = currentPin + number
                    if (isConfirmMode) {
                        confirmPin = newPin
                        if (newPin.length == 4) {
                            // Check if confirmation matches
                            if (newPin == pin) {
                                onPinEntered(newPin)
                            } else {
                                showError = true
                                errorMessage = "PINs don't match. Try again."
                                confirmPin = ""
                            }
                        }
                    } else {
                        pin = newPin
                        if (newPin.length == 4) {
                            if (isSetupMode) {
                                isConfirmMode = true
                            } else {
                                onPinEntered(newPin)
                            }
                        }
                    }
                }
            },
            onBackspaceClick = {
                if (isConfirmMode) {
                    if (confirmPin.isNotEmpty()) {
                        confirmPin = confirmPin.dropLast(1)
                    }
                } else {
                    if (pin.isNotEmpty()) {
                        pin = pin.dropLast(1)
                    }
                }
                showError = false
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Forgot PIN (only in auth mode)
        if (!isSetupMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Forgot PIN? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Contact Support",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        // Show toast with email instructions
                        android.widget.Toast.makeText(
                            context,
                            "📧 Send email to: utilityappsmaker5189@gmail.com",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
        
        // Setup mode cancel
        if (isSetupMode) {
            TextButton(
                onClick = {
                    if (isConfirmMode) {
                        isConfirmMode = false
                        confirmPin = ""
                        pin = ""
                    } else {
                        onPinEntered("") // Cancel setup
                    }
                }
            ) {
                Text(
                    text = if (isConfirmMode) "Back" else "Skip",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PinKeypad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Row 1: 1, 2, 3
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            repeat(3) { index ->
                PinKeypadButton(
                    text = (index + 1).toString(),
                    onClick = { onNumberClick((index + 1).toString()) }
                )
            }
        }
        
        // Row 2: 4, 5, 6
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            repeat(3) { index ->
                PinKeypadButton(
                    text = (index + 4).toString(),
                    onClick = { onNumberClick((index + 4).toString()) }
                )
            }
        }
        
        // Row 3: 7, 8, 9
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            repeat(3) { index ->
                PinKeypadButton(
                    text = (index + 7).toString(),
                    onClick = { onNumberClick((index + 7).toString()) }
                )
            }
        }
        
        // Row 4: empty, 0, backspace
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Empty space
            Spacer(modifier = Modifier.size(72.dp))
            
            // 0 button
            PinKeypadButton(
                text = "0",
                onClick = { onNumberClick("0") }
            )
            
            // Backspace button
            PinKeypadButton(
                icon = Icons.Default.Backspace,
                onClick = onBackspaceClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinKeypadButton(
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.size(72.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Backspace",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ForgotPinDialog(
    onDismiss: () -> Unit,
    onSendEmail: () -> Unit
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Forgot PIN?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("To recover your PIN, please send an email to:")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "utilityappsmaker5189@gmail.com",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "💡 Pro tip: Tap anywhere on the PIN screen 5 times quickly to see a hint!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:utilityappsmaker5189@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "PIN Recovery Request - SIP Calculator")
                        putExtra(Intent.EXTRA_TEXT, "Hello,\n\nI need help recovering my PIN for the SIP Calculator app.\n\nThank you!")
                    }
                    context.startActivity(intent)
                    onDismiss()
                }
            ) {
                Text("Send Email")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}