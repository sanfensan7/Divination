package com.example.divination.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

/**
 * iOS 风格页面转场动画
 * 
 * 提供符合 iOS 导航模式的页面转场效果：
 * - 向前导航：新页面从右侧滑入，旧页面轻微左移并淡出
 * - 向后导航：当前页面向右滑出，前一页面从左侧滑入
 */
object PageTransition {
    /**
     * 页面转场动画持续时间（毫秒）
     */
    private const val TRANSITION_DURATION = 350
    
    /**
     * 向前导航的进入转场
     * 新页面从右侧滑入
     */
    fun enterTransition(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = TRANSITION_DURATION)
        ) + fadeIn(
            animationSpec = tween(durationMillis = TRANSITION_DURATION)
        )
    }
    
    /**
     * 向前导航的退出转场
     * 旧页面轻微左移并淡出
     */
    fun exitTransition(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth / 3 },
            animationSpec = tween(durationMillis = TRANSITION_DURATION)
        ) + fadeOut(
            animationSpec = tween(durationMillis = TRANSITION_DURATION)
        )
    }
    
    /**
     * 向后导航的进入转场
     * 前一页面从左侧滑入
     */
    fun popEnterTransition(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth / 3 },
            animationSpec = tween(durationMillis = TRANSITION_DURATION)
        ) + fadeIn(
            animationSpec = tween(durationMillis = TRANSITION_DURATION)
        )
    }
    
    /**
     * 向后导航的退出转场
     * 当前页面向右滑出
     */
    fun popExitTransition(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = TRANSITION_DURATION)
        ) + fadeOut(
            animationSpec = tween(durationMillis = TRANSITION_DURATION)
        )
    }
    
    /**
     * 模态框从底部滑入的转场
     */
    fun modalEnterTransition(): EnterTransition {
        return slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = TRANSITION_DURATION)
        ) + fadeIn(
            animationSpec = tween(durationMillis = TRANSITION_DURATION)
        )
    }
    
    /**
     * 模态框向底部滑出的转场
     */
    fun modalExitTransition(): ExitTransition {
        return slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = TRANSITION_DURATION)
        ) + fadeOut(
            animationSpec = tween(durationMillis = TRANSITION_DURATION)
        )
    }
}
