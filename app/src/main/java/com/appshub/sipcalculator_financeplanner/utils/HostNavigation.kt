package com.appshub.sipcalculator_financeplanner.utils

import androidx.compose.runtime.staticCompositionLocalOf

// Embedded hosts can provide one shared back control; standalone controls remain visible.
val LocalHostOwnsNavigation = staticCompositionLocalOf { false }
