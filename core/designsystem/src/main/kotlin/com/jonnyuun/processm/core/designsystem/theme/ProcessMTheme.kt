package com.jonnyuun.processm.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.darkColorScheme

/** Acento por defecto. En la Fase 1 pasará a ser configurable desde DataStore. */
val DefaultAccent = Color(0xFF8B5CF6)

/**
 * Tema oscuro AMOLED sobre MIUIX. El acento se inyecta en el esquema para que
 * todos los componentes compartan el mismo color.
 */
@Composable
fun ProcessMTheme(content: @Composable () -> Unit) {
    val controller = remember {
        ThemeController(
            colorSchemeMode = ColorSchemeMode.Dark,
            darkColors = amoledDarkColors(DefaultAccent),
        )
    }
    MiuixTheme(controller = controller, content = content)
}

private fun amoledDarkColors(accent: Color): Colors = darkColorScheme(
    primary = accent,
    primaryVariant = Color(0xFF7C3AED),
    primaryContainer = Color(0xFF4C1D95),
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color(0xFF0E0E0E),
    surfaceContainer = Color(0xFF121212),
    surfaceContainerHigh = Color(0xFF181818),
    surfaceContainerHighest = Color(0xFF1E1E1E),
    outline = Color(0xFF2C2C2C),
)
