package com.upb.petcare.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Paleta de marca: verde azulado (salud) + naranja cálido (mascotas)
private val Teal = Color(0xFF0F766E)
private val TealLight = Color(0xFF5EEAD4)
private val Orange = Color(0xFFEA580C)
private val OrangeLight = Color(0xFFFDBA74)

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF042F2E),
    secondary = Orange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = Color(0xFF431407),
    tertiary = Color(0xFF6D28D9),
    tertiaryContainer = Color(0xFFEDE9FE),
    onTertiaryContainer = Color(0xFF2E1065),
    background = Color(0xFFF4FBF9),
    surface = Color(0xFFF4FBF9),
    surfaceContainer = Color(0xFFE8F3F0),
    surfaceContainerLow = Color(0xFFEFF7F5),
    surfaceContainerHigh = Color(0xFFE2EDEA),
    error = Color(0xFFB91C1C),
)

private val DarkColors = darkColorScheme(
    primary = TealLight,
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF115E59),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = OrangeLight,
    onSecondary = Color(0xFF431407),
    secondaryContainer = Color(0xFF7C2D12),
    onSecondaryContainer = Color(0xFFFFEDD5),
    tertiary = Color(0xFFC4B5FD),
    tertiaryContainer = Color(0xFF4C1D95),
    onTertiaryContainer = Color(0xFFEDE9FE),
    background = Color(0xFF0E1514),
    surface = Color(0xFF0E1514),
    surfaceContainer = Color(0xFF1A2221),
    surfaceContainerLow = Color(0xFF161E1D),
    surfaceContainerHigh = Color(0xFF242D2B),
)

/** Colores de estado de vacunas (se ajustan a modo claro/oscuro). */
data class StatusColors(val container: Color, val content: Color)

@Composable
fun statusColors(kind: String): StatusColors {
    val dark = MaterialTheme.colorScheme.background == DarkColors.background
    return when (kind) {
        "overdue" -> if (dark) StatusColors(Color(0xFF7F1D1D), Color(0xFFFECACA)) else StatusColors(Color(0xFFFEE2E2), Color(0xFF991B1B))
        "soon" -> if (dark) StatusColors(Color(0xFF78350F), Color(0xFFFDE68A)) else StatusColors(Color(0xFFFEF3C7), Color(0xFF92400E))
        "ok" -> if (dark) StatusColors(Color(0xFF14532D), Color(0xFFBBF7D0)) else StatusColors(Color(0xFFDCFCE7), Color(0xFF166534))
        else -> if (dark) StatusColors(Color(0xFF334155), Color(0xFFE2E8F0)) else StatusColors(Color(0xFFE2E8F0), Color(0xFF334155))
    }
}

private val AppTypography = Typography().run {
    copy(
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    )
}

@Composable
fun PetCareTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
