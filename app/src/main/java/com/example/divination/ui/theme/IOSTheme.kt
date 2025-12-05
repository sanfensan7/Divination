package com.example.divination.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * iOS 主题
 * 
 * 组合所有设计令牌（颜色、字体、形状、间距）创建统一的 iOS 风格主题。
 * 这个主题应该包裹整个应用的 Composable 内容。
 */

// 浅色主题颜色方案
private val LightColorPalette = lightColors(
    primary = IOSColor.SystemBlue,
    primaryVariant = IOSColor.SystemBlue,
    secondary = IOSColor.SystemGray,
    secondaryVariant = IOSColor.SystemGray,
    background = IOSColor.BackgroundPrimary,
    surface = IOSColor.CardBackground,
    error = IOSColor.SystemRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = IOSColor.TextPrimary,
    onSurface = IOSColor.TextPrimary,
    onError = Color.White
)

// 深色主题颜色方案（未来扩展）
private val DarkColorPalette = darkColors(
    primary = IOSColor.SystemBlue,
    primaryVariant = IOSColor.SystemBlue,
    secondary = IOSColor.SystemGray,
    secondaryVariant = IOSColor.SystemGray,
    background = IOSColor.DarkBackgroundPrimary,
    surface = IOSColor.DarkBackgroundSecondary,
    error = IOSColor.SystemRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

/**
 * iOS 主题 Composable
 * 
 * 应用 iOS 风格的设计令牌到整个应用。
 * 
 * @param darkTheme 是否使用深色主题（默认跟随系统设置）
 * @param content 应用的内容
 */
@Composable
fun IOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = androidx.compose.material.Typography(
            h1 = IOSTypography.LargeTitle,
            h2 = IOSTypography.Title1,
            h3 = IOSTypography.Title2,
            h4 = IOSTypography.Title3,
            h5 = IOSTypography.Headline,
            h6 = IOSTypography.Headline,
            subtitle1 = IOSTypography.Callout,
            subtitle2 = IOSTypography.Subheadline,
            body1 = IOSTypography.Body,
            body2 = IOSTypography.Body,
            button = IOSTypography.Body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
            caption = IOSTypography.Caption1,
            overline = IOSTypography.Caption2
        ),
        shapes = androidx.compose.material.Shapes(
            small = IOSShape.SmallShape,
            medium = IOSShape.MediumShape,
            large = IOSShape.LargeShape
        ),
        content = content
    )
}
