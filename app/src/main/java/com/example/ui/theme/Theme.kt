package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = XboxNeon,
    onPrimary = Color.Black,
    secondary = XboxGreen,
    onSecondary = Color.White,
    tertiary = L4DBloodRed,
    background = Color(0xFF0F0907),
    surface = Color(0xFF1B1411),
    onBackground = Color(0xFFF3E2D2),
    onSurface = Color(0xFFF3E2D2)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = XboxGreen,
    secondary = XboxNeon,
    tertiary = L4DBloodRed,
    background = Color(0xFF0F0907),
    surface = Color(0xFF1B1411),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFF3E2D2),
    onSurface = Color(0xFFF3E2D2)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
