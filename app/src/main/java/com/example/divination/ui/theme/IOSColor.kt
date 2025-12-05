package com.example.divination.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * iOS 颜色系统
 * 
 * 定义了符合 Apple 人机界面指南（HIG）的标准颜色值。
 * 这些颜色用于创建与 iOS 原生应用一致的视觉体验。
 */
object IOSColor {
    // 主要颜色
    /** iOS 系统蓝色 - 用于主要交互元素 */
    val SystemBlue = Color(0xFF007AFF)
    
    /** iOS 系统灰色 - 用于次要文本和未选中状态 */
    val SystemGray = Color(0xFF8E8E93)
    
    // 背景颜色
    /** 主要背景色 - 纯白色 */
    val BackgroundPrimary = Color(0xFFFFFFFF)
    
    /** 次要背景色 - 浅灰色 */
    val BackgroundSecondary = Color(0xFFF7F7F7)
    
    /** 三级背景色 - 用于分区分隔 */
    val BackgroundTertiary = Color(0xFFEFEFF0)
    
    // 分隔线
    /** 分隔线颜色 - 微妙的灰色 */
    val Separator = Color(0xFFE5E5EA)
    
    // 文本颜色
    /** 主要文本颜色 - 黑色 */
    val TextPrimary = Color(0xFF000000)
    
    /** 次要文本颜色 - 灰色 */
    val TextSecondary = Color(0xFF8E8E93)
    
    /** 三级文本颜色 - 浅灰色 */
    val TextTertiary = Color(0xFFC7C7CC)
    
    // 卡片
    /** 卡片背景色 - 白色 */
    val CardBackground = Color(0xFFFFFFFF)
    
    /** 卡片阴影颜色 - 半透明黑色 */
    val CardShadow = Color(0x0A000000)
    
    // 深色模式（未来扩展）
    /** 深色模式主要背景 - 纯黑色 */
    val DarkBackgroundPrimary = Color(0xFF000000)
    
    /** 深色模式次要背景 - 深灰色 */
    val DarkBackgroundSecondary = Color(0xFF1C1C1E)
    
    /** 深色模式三级背景 - 中灰色 */
    val DarkBackgroundTertiary = Color(0xFF2C2C2E)
    
    // 其他系统颜色
    /** 破坏性操作颜色 - 红色 */
    val SystemRed = Color(0xFFFF3B30)
    
    /** 成功/确认颜色 - 绿色 */
    val SystemGreen = Color(0xFF34C759)
    
    /** 警告颜色 - 橙色 */
    val SystemOrange = Color(0xFFFF9500)
    
    /** 信息颜色 - 黄色 */
    val SystemYellow = Color(0xFFFFCC00)
}
