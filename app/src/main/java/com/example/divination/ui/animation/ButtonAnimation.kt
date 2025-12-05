package com.example.divination.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

/**
 * iOS 风格按钮交互动画
 * 
 * 提供按钮按下和释放时的缩放动画效果。
 * iOS 按钮通常在按下时缩小到原始大小的 94-97%，释放时回弹。
 */
object ButtonAnimation {
    /**
     * 按钮按下时的缩放比例
     */
    const val BUTTON_PRESS_SCALE = 0.94f
    
    /**
     * 卡片按下时的缩放比例
     */
    const val CARD_PRESS_SCALE = 0.97f
    
    /**
     * 按下时的亮度降低比例
     */
    const val PRESS_BRIGHTNESS = 0.85f
    
    /**
     * 动画持续时间（毫秒）
     */
    const val ANIMATION_DURATION = 200
    
    /**
     * 获取按钮按下时的缩放动画值
     * 
     * @param interactionSource 交互源，用于检测按下状态
     * @param pressScale 按下时的缩放比例，默认为 0.94
     * @return 动画缩放值
     */
    @Composable
    fun animateButtonScale(
        interactionSource: InteractionSource,
        pressScale: Float = BUTTON_PRESS_SCALE
    ): Float {
        val isPressed by interactionSource.collectIsPressedAsState()
        
        val scale by animateFloatAsState(
            targetValue = if (isPressed) pressScale else 1f,
            animationSpec = SpringAnimation.standard(),
            label = "buttonScale"
        )
        
        return scale
    }
    
    /**
     * 获取卡片按下时的缩放动画值
     * 
     * @param interactionSource 交互源，用于检测按下状态
     * @return 动画缩放值
     */
    @Composable
    fun animateCardScale(
        interactionSource: InteractionSource
    ): Float {
        return animateButtonScale(interactionSource, CARD_PRESS_SCALE)
    }
    
    /**
     * 获取按下时的亮度动画值
     * 
     * @param interactionSource 交互源，用于检测按下状态
     * @return 动画亮度值（0.85 = 降低 15%）
     */
    @Composable
    fun animateBrightness(
        interactionSource: InteractionSource
    ): Float {
        val isPressed by interactionSource.collectIsPressedAsState()
        
        val brightness by animateFloatAsState(
            targetValue = if (isPressed) PRESS_BRIGHTNESS else 1f,
            animationSpec = tween(durationMillis = 100),
            label = "brightness"
        )
        
        return brightness
    }
}
