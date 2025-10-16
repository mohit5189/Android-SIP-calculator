package com.appshub.sipcalculator_financeplanner.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = Secondary,
    tertiary = SecondaryVariant,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = Error,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    onError = androidx.compose.ui.graphics.Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = SecondaryVariant,
    background = Background,
    surface = Surface,
    error = Error,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = OnSurface,
    onSurface = OnSurface,
    onError = androidx.compose.ui.graphics.Color.White
)

@Composable
fun SIPCalculatorFInancePlannerTheme(
    darkTheme: Boolean = false, // Always use light theme
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic colors to ensure consistent light theme
    content: @Composable () -> Unit
) {
    // Always use light color scheme
    val colorScheme = LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Remove deprecated statusBarColor setting for Android 15 compatibility
            // EdgeToEdge handles status bar appearance automatically
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}