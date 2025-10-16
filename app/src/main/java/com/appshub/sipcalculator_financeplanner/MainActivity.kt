package com.appshub.sipcalculator_financeplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
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
import com.appshub.sipcalculator_financeplanner.presentation.ui.calculator.MoreScreen
import com.appshub.sipcalculator_financeplanner.presentation.ui.calculator.MoreCalculatorType
import com.appshub.sipcalculator_financeplanner.presentation.ui.screens.MoreAppsScreen
import com.appshub.sipcalculator_financeplanner.presentation.ui.common.RatingDialog
import com.appshub.sipcalculator_financeplanner.ui.theme.SIPCalculatorFInancePlannerTheme
import com.appshub.sipcalculator_financeplanner.utils.FirebaseAnalyticsManager
import com.appshub.sipcalculator_financeplanner.utils.RatingManager
import com.appshub.sipcalculator_financeplanner.utils.AdManager
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize UI first to prevent splash screen hanging
        setContent {
            SIPCalculatorFInancePlannerTheme {
                SIPCalculatorApp()
            }
        }
        
        // Initialize managers asynchronously to prevent blocking main thread
        initializeManagersAsync()
    }
    
    private fun initializeManagersAsync() {
        // Use a separate thread for initialization to prevent main thread blocking
        Thread {
            try {
                // Initialize rating manager (lightweight)
                RatingManager.getInstance().initialize(this)
                
                // Initialize ads on main thread (required by AdMob)
                runOnUiThread {
                    try {
                        AdManager.getInstance().initializeAds(this)
                    } catch (e: Exception) {
                        // Graceful fallback if ads fail to initialize
                        android.util.Log.e("MainActivity", "Failed to initialize ads: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Failed to initialize managers: ${e.message}")
            }
        }.start()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SIPCalculatorApp() {
    val context = LocalContext.current
    val analyticsManager = FirebaseAnalyticsManager.getInstance(context)
    var selectedCalculator by remember { mutableStateOf(0) }
    var showMoreApps by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var isRatingEligible by remember { mutableStateOf(false) }
    var moreScreenState by remember { mutableStateOf<MoreCalculatorType?>(null) }
    
    // Dynamic title based on current screen
    val currentTitle = when (moreScreenState) {
        MoreCalculatorType.SIMPLE_INTEREST -> "Simple Interest"
        MoreCalculatorType.COMPOUND_INTEREST -> "Compound Interest"
        MoreCalculatorType.EMI -> "EMI Calculator"
        MoreCalculatorType.RD -> "RD Calculator"
        MoreCalculatorType.PPF -> "PPF Calculator"
        MoreCalculatorType.FD -> "FD Calculator"
        null -> listOf("SIP Calculator", "SWP Calculator", "Goal Planning", "More")[selectedCalculator]
    }
    
    val calculatorTitles = listOf(
        "SIP Calculator",
        "SWP Calculator", 
        "Goal Planning",
        "More"
    )
    
    val screenNames = listOf(
        "sip_calculator",
        "swp_calculator",
        "goal_calculator",
        "more_calculators"
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
    
    // Handle More Apps screen
    if (showMoreApps) {
        MoreAppsScreen(
            onBackClick = { showMoreApps = false },
            analyticsManager = analyticsManager
        )
        return
    }
    
    // Check rating eligibility with delay to ensure app is fully loaded
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000) // Wait 2 seconds after app starts
        try {
            isRatingEligible = RatingManager.getInstance().isEligibleToShowRating()
            if (isRatingEligible) {
                showRatingDialog = true
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error checking rating eligibility: ${e.message}")
            isRatingEligible = false
        }
    }
    
    if (showRatingDialog) {
        RatingDialog(
            onDismiss = { showRatingDialog = false },
            onRateUs = { showRatingDialog = false }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // Rate Us option (only show if eligible)
                            if (isRatingEligible) {
                                DropdownMenuItem(
                                    text = { Text("Rate Us ⭐") },
                                    onClick = {
                                        showMenu = false
                                        showRatingDialog = true
                                        analyticsManager.logButtonClick("rate_us_menu", "MainActivity")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                            
                            // More Apps option
                            DropdownMenuItem(
                                text = { Text("More Apps") },
                                onClick = {
                                    showMenu = false
                                    showMoreApps = true
                                    analyticsManager.logButtonClick("more_apps_menu", "MainActivity")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
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
                NavigationBarItem(
                    icon = { Icon(Icons.Default.MoreHoriz, contentDescription = null) },
                    label = { Text("More") },
                    selected = selectedCalculator == 3,
                    onClick = { 
                        analyticsManager.logButtonClick("navigation_more", screenNames[selectedCalculator])
                        selectedCalculator = 3 
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
                3 -> MoreScreen(
                    analyticsManager = analyticsManager,
                    currentCalculator = moreScreenState,
                    onCalculatorChanged = { type -> moreScreenState = type }
                )
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