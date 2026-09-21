package com.soultware.scrollbill.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF1769F5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4EDFF),
    onPrimaryContainer = Color(0xFF092B78),
    secondary = Color(0xFF00A98F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7F7F1),
    onSecondaryContainer = Color(0xFF00382F),
    tertiary = Color(0xFF7555E9),
    background = Color(0xFFF4F7FF),
    surface = Color.White,
    surfaceVariant = Color(0xFFEAF0FC),
    onSurface = Color(0xFF10204D),
    onSurfaceVariant = Color(0xFF5B6A92),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DBBFF),
    onPrimary = Color(0xFF002B76),
    primaryContainer = Color(0xFF08429E),
    onPrimaryContainer = Color(0xFFDCE6FF),
    secondary = Color(0xFF5DE0C9),
    onSecondary = Color(0xFF00382F),
    secondaryContainer = Color(0xFF075448),
    onSecondaryContainer = Color(0xFF8FF5DF),
    tertiary = Color(0xFFC6B9FF),
    background = Color(0xFF0A1536),
    surface = Color(0xFF101F48),
    surfaceVariant = Color(0xFF243765),
    onSurface = Color(0xFFEAF0FF),
    onSurfaceVariant = Color(0xFFB7C5E7),
)

private val ScrollBillTypography = androidx.compose.material3.Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.6).sp,
        ),
        headlineMedium = headlineMedium.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.35).sp,
        ),
        headlineSmall = headlineSmall.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
        ),
        titleLarge = titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(fontFamily = FontFamily.SansSerif, lineHeight = 25.sp),
        labelLarge = labelLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, letterSpacing = 0.35.sp),
    )
}

@Composable
fun ScrollBillTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        shapes = androidx.compose.material3.Shapes(
            small = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(30.dp),
        ),
        typography = ScrollBillTypography,
        content = content,
    )
}
