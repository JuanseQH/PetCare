package com.upb.petcare.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upb.petcare.R

// ------------------------------------------------------------------ Tipografía
// Fredoka (títulos, redondeada y amigable) + Nunito (texto). Ambas son fuentes variables.

@OptIn(ExperimentalTextApi::class)
private fun variable(res: Int, weight: Int) =
    Font(res, FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight)))

val Fredoka = FontFamily(
    variable(R.font.fredoka, 400), variable(R.font.fredoka, 500),
    variable(R.font.fredoka, 600), variable(R.font.fredoka, 700),
)
val Nunito = FontFamily(
    variable(R.font.nunito, 400), variable(R.font.nunito, 600),
    variable(R.font.nunito, 700), variable(R.font.nunito, 800),
)

private val AppTypography = Typography(
    displaySmall = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 42.sp),
    headlineLarge = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 14.sp),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

// ------------------------------------------------------------------ Colores
// Jade (salud) + coral (cariño) + girasol (alegría) sobre un fondo cálido.

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E6B5C), onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEBE2), onPrimaryContainer = Color(0xFF08332B),
    secondary = Color(0xFFE8673F), onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE1D6), onSecondaryContainer = Color(0xFF5A1E0A),
    tertiary = Color(0xFFB7800F), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEFC6), onTertiaryContainer = Color(0xFF4A3300),
    background = Color(0xFFFAF6F1), onBackground = Color(0xFF1E2522),
    surface = Color(0xFFFAF6F1), onSurface = Color(0xFF1E2522),
    surfaceVariant = Color(0xFFEDE6DD), onSurfaceVariant = Color(0xFF5E6763),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF5EFE7),
    surfaceContainer = Color(0xFFF0E9E0), surfaceContainerHigh = Color(0xFFEAE2D8),
    surfaceContainerHighest = Color(0xFFE3DBD0),
    outline = Color(0xFFBDB4A8), outlineVariant = Color(0xFFE3DCD2),
    error = Color(0xFFC0362C), errorContainer = Color(0xFFFFDAD5), onErrorContainer = Color(0xFF5C0F09),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FD3BE), onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF1C5246), onPrimaryContainer = Color(0xFFCDEBE2),
    secondary = Color(0xFFFFB49B), onSecondary = Color(0xFF5A1E0A),
    secondaryContainer = Color(0xFF7A3320), onSecondaryContainer = Color(0xFFFFE1D6),
    tertiary = Color(0xFFF5C95E), onTertiary = Color(0xFF3F2C00),
    tertiaryContainer = Color(0xFF5C4300), onTertiaryContainer = Color(0xFFFFEFC6),
    background = Color(0xFF0F1513), onBackground = Color(0xFFE3EAE6),
    surface = Color(0xFF0F1513), onSurface = Color(0xFFE3EAE6),
    surfaceVariant = Color(0xFF2A3330), onSurfaceVariant = Color(0xFFA9B4AF),
    surfaceContainerLowest = Color(0xFF0A0F0D), surfaceContainerLow = Color(0xFF161D1A),
    surfaceContainer = Color(0xFF1B2320), surfaceContainerHigh = Color(0xFF232C29),
    surfaceContainerHighest = Color(0xFF2C3632),
    outline = Color(0xFF5B6662), outlineVariant = Color(0xFF333D39),
    error = Color(0xFFFFB4AB), errorContainer = Color(0xFF7D1E16), onErrorContainer = Color(0xFFFFDAD5),
)

/** Colores que identifican a cada mascota (elegidos en el formulario). */
val PetPalette = listOf(
    Color(0xFFF2754E), // coral
    Color(0xFF2E9C83), // jade
    Color(0xFFF5B83D), // girasol
    Color(0xFF8E7CF0), // lavanda
    Color(0xFF4FA8DE), // cielo
    Color(0xFFE9709F), // rosa
    Color(0xFF7FAE6E), // salvia
    Color(0xFFC9925E), // arena
)

fun petColor(index: Int): Color = PetPalette[index.mod(PetPalette.size)]

// ------------------------------------------------------------------ Colores de estado

@Immutable
data class StatusColors(val container: Color, val content: Color, val dot: Color)

@Immutable
data class AppExtras(
    val dark: Boolean,
    val overdue: StatusColors,
    val soon: StatusColors,
    val ok: StatusColors,
    val done: StatusColors,
)

private val LightExtras = AppExtras(
    dark = false,
    overdue = StatusColors(Color(0xFFFFE0DB), Color(0xFFA42A1F), Color(0xFFE5483A)),
    soon = StatusColors(Color(0xFFFFF0C9), Color(0xFF8A5A00), Color(0xFFF2A922)),
    ok = StatusColors(Color(0xFFD7F2E6), Color(0xFF17624C), Color(0xFF2EA77D)),
    done = StatusColors(Color(0xFFE9E4DD), Color(0xFF575049), Color(0xFF9C948B)),
)
private val DarkExtras = AppExtras(
    dark = true,
    overdue = StatusColors(Color(0xFF5E1B14), Color(0xFFFFC9C1), Color(0xFFFF7A6B)),
    soon = StatusColors(Color(0xFF4F3A05), Color(0xFFFFE39E), Color(0xFFF5C24E)),
    ok = StatusColors(Color(0xFF113F31), Color(0xFFB4EBD4), Color(0xFF5ED1A6)),
    done = StatusColors(Color(0xFF2E3532), Color(0xFFD5DCD8), Color(0xFF8C9692)),
)

val LocalAppExtras = staticCompositionLocalOf { LightExtras }

@Composable
fun PetCareTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppExtras provides if (darkTheme) DarkExtras else LightExtras) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
