package com.tonihacks.qalam.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tonihacks.qalam.R

val HankenGrotesk = FontFamily(
    Font(R.font.hanken_grotesk_black, FontWeight.Black),
    Font(R.font.hanken_grotesk_blackitalic, FontWeight.Black, FontStyle.Italic),
    Font(R.font.hanken_grotesk_bold, FontWeight.Bold),
    Font(R.font.hanken_grotesk_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.hanken_grotesk_extrabold, FontWeight.ExtraBold),
    Font(R.font.hanken_grotesk_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.hanken_grotesk_light, FontWeight.Light),
    Font(R.font.hanken_grotesk_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.hanken_grotesk_medium, FontWeight.Medium),
    Font(R.font.hanken_grotesk_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.hanken_grotesk_regular, FontWeight.Normal),
    Font(R.font.hanken_grotesk_semibold, FontWeight.SemiBold),
    Font(R.font.hanken_grotesk_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.hanken_grotesk_thin, FontWeight.Thin),
)

val Amiri = FontFamily(
    Font(R.font.amiri_bold, FontWeight.Bold),
    Font(R.font.amiri_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.amiri_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.amiri_regular, FontWeight.Normal),
)

val NewsReader = FontFamily(
    Font(R.font.newsreader_bold, FontWeight.Bold),
    Font(R.font.newsreader_extrabold, FontWeight.ExtraBold),
    Font(R.font.newsreader_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.newsreader_medium, FontWeight.Medium),
    Font(R.font.newsreader_regular, FontWeight.Normal),
    Font(R.font.newsreader_semibold, FontWeight.SemiBold),
)

val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = NewsReader,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    // Section headers within screens
    headlineMedium = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    // Card titles, bar titles, word translation headline
    titleLarge = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    // Standard body copy
    bodyLarge = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    // Prose / transliterations (Newsreader italic used via fontStyle override at call site)
    bodyMedium = TextStyle(
        fontFamily = NewsReader,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    // Navigation labels, chip labels, button text
    labelLarge = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
)