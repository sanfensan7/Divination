package com.example.divination.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * iOS 形状系统
 * 
 * 定义了符合 Apple 人机界面指南（HIG）的标准圆角半径和形状。
 * 这些形状用于创建柔和、易于接近的 UI 元素。
 */
object IOSShape {
    // 圆角半径
    /** 小圆角 - 8dp */
    val CornerRadiusSmall = 8.dp
    
    /** 中等圆角 - 12dp */
    val CornerRadiusMedium = 12.dp
    
    /** 大圆角 - 16dp */
    val CornerRadiusLarge = 16.dp
    
    /** 特大圆角 - 20dp */
    val CornerRadiusXLarge = 20.dp
    
    /** 超大圆角 - 24dp */
    val CornerRadiusXXLarge = 24.dp
    
    /** 按钮圆角（胶囊形）- 14dp */
    val ButtonCornerRadius = 14.dp
    
    /** 卡片圆角 - 24dp */
    val CardCornerRadius = 24.dp
    
    /** 搜索栏圆角 - 10dp */
    val SearchBarCornerRadius = 10.dp
    
    // 形状定义
    /** 小形状 - 8dp 圆角 */
    val SmallShape = RoundedCornerShape(CornerRadiusSmall)
    
    /** 中等形状 - 12dp 圆角 */
    val MediumShape = RoundedCornerShape(CornerRadiusMedium)
    
    /** 大形状 - 16dp 圆角 */
    val LargeShape = RoundedCornerShape(CornerRadiusLarge)
    
    /** 卡片形状 - 24dp 圆角 */
    val CardShape = RoundedCornerShape(CardCornerRadius)
    
    /** 按钮形状 - 14dp 圆角（胶囊形） */
    val ButtonShape = RoundedCornerShape(ButtonCornerRadius)
    
    /** 搜索栏形状 - 10dp 圆角 */
    val SearchBarShape = RoundedCornerShape(SearchBarCornerRadius)
}
