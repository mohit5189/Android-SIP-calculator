package com.appshub.sipcalculator_financeplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.appshub.sipcalculator_financeplanner.presentation.ui.calculator.SIPCalculatorScreen
import com.appshub.sipcalculator_financeplanner.presentation.ui.calculator.SWPCalculatorScreen
import com.appshub.sipcalculator_financeplanner.presentation.ui.calculator.GoalCalculatorScreen
import com.appshub.sipcalculator_financeplanner.ui.theme.SIPCalculatorFInancePlannerTheme
import com.appshub.sipcalculator_financeplanner.utils.FirebaseAnalyticsManager
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SIPCalculatorFInancePlannerTheme {
                SIPCalculatorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SIPCalculatorApp() {
    val context = LocalContext.current
    val analyticsManager = FirebaseAnalyticsManager.getInstance(context)
    var selectedCalculator by remember { mutableStateOf(0) }
    
    val calculatorTitles = listOf(
        "SIP Calculator",
        "SWP Calculator", 
        "Goal Planning"
    )
    
    val screenNames = listOf(
        "sip_calculator",
        "swp_calculator",
        "goal_calculator"
    )
    
    // Track initial screen view
    LaunchedEffect(Unit) {
        analyticsManager.logScreenView(screenNames[0], "SIPCalculatorScreen")
    }
    
    // Track screen changes
    LaunchedEffect(selectedCalculator) {
        analyticsManager.logScreenView(screenNames[selectedCalculator], calculatorTitles[selectedCalculator])
        if (selectedCalculator > 0) {
            analyticsManager.logNavigation(screenNames[0], screenNames[selectedCalculator])
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(calculatorTitles[selectedCalculator]) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                    label = { Text("SIP") },
                    selected = selectedCalculator == 0,
                    onClick = { 
                        analyticsManager.logButtonClick("navigation_sip", screenNames[selectedCalculator])
                        selectedCalculator = 0 
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.TrendingDown, contentDescription = null) },
                    label = { Text("SWP") },
                    selected = selectedCalculator == 1,
                    onClick = { 
                        analyticsManager.logButtonClick("navigation_swp", screenNames[selectedCalculator])
                        selectedCalculator = 1 
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Flag, contentDescription = null) },
                    label = { Text("Goal") },
                    selected = selectedCalculator == 2,
                    onClick = { 
                        analyticsManager.logButtonClick("navigation_goal", screenNames[selectedCalculator])
                        selectedCalculator = 2 
                    }
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedCalculator) {
                0 -> SIPCalculatorScreen(analyticsManager)
                1 -> SWPCalculatorScreen(analyticsManager)
                2 -> GoalCalculatorScreen(analyticsManager)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SIPCalculatorAppPreview() {
    SIPCalculatorFInancePlannerTheme {
        SIPCalculatorApp()
    }
}