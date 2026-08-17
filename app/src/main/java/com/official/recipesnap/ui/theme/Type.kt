package com.official.recipesnap.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.official.recipesnap.R

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
val JosefinSans = FontFamily(
    Font(
        R.font.josefin_sans,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.josefin_sans,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        R.font.josefin_sans,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        R.font.josefin_sans,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(FontVariation.weight(300))
    )
)

private val defaultTypography = Typography()

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = JosefinSans),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = JosefinSans),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = JosefinSans),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = JosefinSans),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = JosefinSans),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = JosefinSans),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = JosefinSans),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = JosefinSans),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = JosefinSans),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = JosefinSans),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = JosefinSans),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = JosefinSans),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = JosefinSans),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = JosefinSans),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = JosefinSans)
)