package com.example.divination.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * iOS 风格加载状态动画
 * 
 * 提供加载状态切换时的平滑过渡效果。
 */
object LoadingAnimation {
    /**
     * 加载状态切换动画持续时间（毫秒）
     */
    const val LOADING_TRANSITION_DURATION = 250
    
    /**
     * 内容淡入淡出动画持续时间（毫秒）
     */
    const val CONTENT_FADE_DURATION = 200
    
    /**
     * 加载指示器旋转动画持续时间（毫秒）
     */
    const val SPINNER_ROTATION_DURATION = 1000
    
    /**
     * 创建加载状态的内容切换动画
     * 
     * @return 内容转换规范
     */
    fun contentTransition(): ContentTransform {
        return fadeIn(
            animationSpec = tween(
                durationMillis = CONTENT_FADE_DURATION,
                easing = LinearOutSlowInEasing
            )
        ) togetherWith fadeOut(
            animationSpec = tween(
                durationMillis = CONTENT_FADE_DURATION,
                easing = FastOutLinearInEasing
            )
        )
    }
    
    /**
     * 创建加载状态的滑动切换动画
     * 
     * @return 内容转换规范
     */
    fun slideTransition(): ContentTransform {
        return (slideInVertically(
            animationSpec = tween(
                durationMillis = LOADING_TRANSITION_DURATION,
                easing = FastOutSlowInEasing
            ),
            initialOffsetY = { it / 4 }
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = LOADING_TRANSITION_DURATION,
                easing = LinearOutSlowInEasing
            )
        )) togetherWith (slideOutVertically(
            animationSpec = tween(
                durationMillis = LOADING_TRANSITION_DURATION,
                easing = FastOutSlowInEasing
            ),
            targetOffsetY = { -it / 4 }
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = LOADING_TRANSITION_DURATION,
                easing = FastOutLinearInEasing
            )
        ))
    }
    
    /**
     * 创建旋转动画（用于加载指示器）
     * 
     * @return 无限旋转动画规范
     */
    fun spinnerRotation(): InfiniteRepeatableSpec<Float> {
        return infiniteRepeatable(
            animation = tween(
                durationMillis = SPINNER_ROTATION_DURATION,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    }
    
    /**
     * 创建脉冲动画（用于骨架屏）
     * 
     * @return 无限脉冲动画规范
     */
    fun skeletonPulse(): InfiniteRepeatableSpec<Float> {
        return infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    }
    
    /**
     * 为内容添加加载状态动画修饰符
     * 
     * @param isLoading 是否正在加载
     * @return 修饰符
     */
    @Composable
    fun Modifier.loadingState(
        @Suppress("UNUSED_PARAMETER") isLoading: Boolean
    ): Modifier {
        return this.animateContentSize(
            animationSpec = SpringAnimation.smooth()
        )
    }
}
