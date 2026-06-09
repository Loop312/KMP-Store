package io.github.kmpstore.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Core Brand Colors
private val PrimaryColor = Color(0xFF6200EE)
private val SecondaryColor = Color(0xFF03DAC6)

// Light Mode Palette
internal val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF5F5F5),
    onPrimary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1D1B20),
    onSurface = Color(0xFF1D1B20)
)

// Dark Mode Palette
internal val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    background = Color(0xFF000000),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color(0xFF000000),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5)
)

var isDarkTheme by mutableStateOf(false)