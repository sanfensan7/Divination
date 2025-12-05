package com.example.divination.ui.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.SpringSpec

/**
 * iOS 风格弹簧动画工具
 * 
 * 提供符合 iOS 动画特性的弹簧动画配置。
 * iOS 动画通常使用基于物理的弹簧动画，具有自然的阻尼和回弹效果。
 */
object SpringAnimation {
    /**
     * 标准弹簧动画
     * 用于大多数 UI 交互，如按钮按下、卡片点击等
     * 
     * 阻尼比：0.8（中等回弹）
     * 刚度：Spring.StiffnessMedium
     */
    fun <T> standard(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    /**
     * 快速弹簧动画
     * 用于需要快速响应的交互，如开关切换
     * 
     * 阻尼比：0.7（较强回弹）
     * 刚度：Spring.StiffnessHigh
     */
    fun <T> fast(): SpringSpec<T> = spring(
        dampingRatio = 0.7f,
        stiffness = Spring.StiffnessHigh
    )
    
    /**
     * 柔和弹簧动画
     * 用于大型元素的移动，如页面转场、模态框出现
     * 
     * 阻尼比：0.8（中等回弹）
     * 刚度：Spring.StiffnessLow
     */
    fun <T> gentle(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    /**
     * 平滑弹簧动画
     * 用于滚动相关的动画，如导航栏收缩
     * 
     * 阻尼比：1.0（无回弹）
     * 刚度：Spring.StiffnessLow
     */
    fun <T> smooth(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    /**
     * 有弹性的弹簧动画
     * 用于需要明显回弹效果的交互
     * 
     * 阻尼比：0.5（强回弹）
     * 刚度：Spring.StiffnessMedium
     */
    fun <T> bouncy(): SpringSpec<T> = spring(
        dampingRatio = 0.5f,
        stiffness = Spring.StiffnessMedium
    )
    
    /**
     * 列表项出现动画
     * 用于列表项的渐入效果
     * 
     * 阻尼比：0.75（轻微回弹）
     * 刚度：Spring.StiffnessMediumLow
     */
    fun <T> listItem(): SpringSpec<T> = spring(
        dampingRatio = 0.75f,
        stiffness = Spring.StiffnessMediumLow
    )
}
