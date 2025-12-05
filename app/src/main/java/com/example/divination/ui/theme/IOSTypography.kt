package com.example.divination.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * iOS 字体系统
 * 
 * 定义了符合 Apple 人机界面指南（HIG）的标准字体层次。
 * 这些文本样式用于创建清晰的视觉层次和可读性。
 */
object IOSTypography {
    /**
     * Large Title - 34sp
     * 用于页面主标题，通常在导航栏中显示
     */
    val LargeTitle = TextStyle(
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 41.sp,
        letterSpacing = 0.37.sp
    )
    
    /**
     * Title 1 - 28sp
     * 用于重要的标题
     */
    val Title1 = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 34.sp,
        letterSpacing = 0.36.sp
    )
    
    /**
     * Title 2 - 22sp
     * 用于次要标题
     */
    val Title2 = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp,
        letterSpacing = 0.35.sp
    )
    
    /**
     * Title 3 - 20sp
     * 用于小标题
     */
    val Title3 = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 25.sp,
        letterSpacing = 0.38.sp
    )
    
    /**
     * Headline - 17sp
     * 用于分区标题和强调文本
     */
    val Headline = TextStyle(
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp,
        letterSpacing = (-0.41).sp
    )
    
    /**
     * Body - 15sp
     * 用于正文文本
     */
    val Body = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        letterSpacing = (-0.24).sp
    )
    
    /**
     * Callout - 16sp
     * 用于标注文本
     */
    val Callout = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 21.sp,
        letterSpacing = (-0.32).sp
    )
    
    /**
     * Subheadline - 15sp
     * 用于副标题
     */
    val Subheadline = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        letterSpacing = (-0.24).sp
    )
    
    /**
     * Footnote - 13sp
     * 用于脚注和说明文字
     */
    val Footnote = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp,
        letterSpacing = (-0.08).sp
    )
    
    /**
     * Caption 1 - 12sp
     * 用于图片说明和辅助信息
     */
    val Caption1 = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
    
    /**
     * Caption 2 - 11sp
     * 用于最小的文本，如标签文字
     */
    val Caption2 = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 13.sp,
        letterSpacing = 0.07.sp
    )
}
