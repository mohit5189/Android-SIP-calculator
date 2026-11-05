package com.appshub.sipcalculator_financeplanner.utils

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.appshub.sipcalculator_financeplanner.ui.theme.GradientEnd
import com.appshub.sipcalculator_financeplanner.ui.theme.GradientStart

// Embedded hosts can provide one shared back control; standalone controls remain visible.
val LocalHostOwnsNavigation = staticCompositionLocalOf { false }

// Embedded hosts can restyle result highlights to match their own theme; standalone keeps the original gradient.
val LocalHostResultGradient = staticCompositionLocalOf<List<Color>> { listOf(GradientStart, GradientEnd) }
