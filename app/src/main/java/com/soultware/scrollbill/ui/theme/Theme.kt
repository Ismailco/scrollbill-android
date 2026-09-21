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
    primary = Color(0xFF3159C8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE5FF),
    onPrimaryContainer = Color(0xFF00164E),
    secondary = Color(0xFF0C7B73),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4F3EC),
    onSecondaryContainer = Color(0xFF00201D),
    tertiary = Color(0xFFC66B35),
    background = Color(0xFFF7F8FC),
    surface = Color(0xFFF7F8FC),
    surfaceVariant = Color(0xFFE9ECF4),
    onSurface = Color(0xFF151A27),
    onSurfaceVariant = Color(0xFF5C6475),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB6C4FF),
    onPrimary = Color(0xFF112A72),
    primaryContainer = Color(0xFF2B438F),
    onPrimaryContainer = Color(0xFFDCE2FF),
    secondary = Color(0xFF73D5C7),
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF075048),
    onSecondaryContainer = Color(0xFF92F2E3),
    tertiary = Color(0xFFFFB789),
    background = Color(0xFF0D111B),
    surface = Color(0xFF0D111B),
    surfaceVariant = Color(0xFF202635),
    onSurface = Color(0xFFE9ECF5),
    onSurfaceVariant = Color(0xFFBEC5D5),
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
