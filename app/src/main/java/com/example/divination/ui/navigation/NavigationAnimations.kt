package com.example.divination.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry

/**
 * iOS 风格导航动画
 * 
 * 提供符合 iOS 导航模式的页面转场效果
 */
object NavigationAnimations {
    /**
     * 页面转场动画持续时间（毫秒）
     * iOS 标准转场时间为 350ms
     */
    private const val TRANSITION_DURATION = 350
    
    /**
     * 快速转场动画持续时间（毫秒）
     * 用于模态框等快速交互
     */
    private const val FAST_TRANSITION_DURATION = 250
    
    /**
     * 页面阴影偏移比例
     * 旧页面在新页面下方时的偏移量
     */
    private const val SHADOW_OFFSET_RATIO = 0.3f
    
    /**
     * 淡入淡出动画延迟（毫秒）
     * 使淡入淡出效果更自然
     */
    private const val FADE_DELAY = 50

    /**
     * 底部 Tab 路由集合
     * 用于区分 Tab 切换和普通 push/pop 导航
     */
    private val bottomNavRoutes = setOf(
        Routes.HOME,
        Routes.MOOD_HISTORY,
        Routes.METHODS,
        Routes.PROFILE,
        Routes.SETTINGS
    )

    /**
     * 是否为底部 Tab 之间的切换
     */
    private fun AnimatedContentTransitionScope<NavBackStackEntry>.isBottomTabSwitch(): Boolean {
        val from = initialState.destination.route
        val to = targetState.destination.route
        return from != null && to != null &&
                from in bottomNavRoutes && to in bottomNavRoutes
    }
    
    /**
     * 向前导航的进入转场
     * 新页面从右侧滑入，带有轻微的淡入效果
     */
    fun enterTransition(): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
        if (isBottomTabSwitch()) {
            // 底部 Tab 切换：新页面从右侧滑入 + 轻微缩放与淡入，旧页面瞬间移除，避免重叠
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(
                    durationMillis = 260,
                    easing = FastOutSlowInEasing
                )
            ) + scaleIn(
                animationSpec = tween(
                    durationMillis = 260,
                    easing = FastOutSlowInEasing
                ),
                initialScale = 0.92f
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 220,
                    easing = LinearOutSlowInEasing
                ),
                initialAlpha = 0.7f
            )
        } else {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION - FADE_DELAY,
                    delayMillis = FADE_DELAY,
                    easing = LinearOutSlowInEasing
                )
            )
        }
    }
    
    /**
     * 向前导航的退出转场
     * 旧页面轻微左移（1/3 宽度）并淡出，模拟被推到后面的效果
     */
    fun exitTransition(): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
        if (isBottomTabSwitch()) {
            // Tab 切换：旧页面在新页面动效开始前直接移除，避免重叠
            ExitTransition.None
        } else {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION,
                    easing = FastOutSlowInEasing
                ),
                targetOffset = { fullWidth -> -(fullWidth * SHADOW_OFFSET_RATIO).toInt() }
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION / 2,
                    easing = FastOutLinearInEasing
                ),
                targetAlpha = 0.7f
            )
        }
    }
    
    /**
     * 向后导航的进入转场
     * 前一页面从左侧滑入（从 -1/3 位置），带有淡入效果
     */
    fun popEnterTransition(): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
        if (isBottomTabSwitch()) {
            // Tab 之间理论上不会触发 pop*，但保持与 enterTransition 一致以防万一
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(
                    durationMillis = 260,
                    easing = FastOutSlowInEasing
                )
            ) + scaleIn(
                animationSpec = tween(
                    durationMillis = 260,
                    easing = FastOutSlowInEasing
                ),
                initialScale = 0.92f
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 220,
                    easing = LinearOutSlowInEasing
                ),
                initialAlpha = 0.7f
            )
        } else {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION,
                    easing = FastOutSlowInEasing
                ),
                initialOffset = { fullWidth -> -(fullWidth * SHADOW_OFFSET_RATIO).toInt() }
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION / 2,
                    delayMillis = TRANSITION_DURATION / 4,
                    easing = LinearOutSlowInEasing
                ),
                initialAlpha = 0.7f
            )
        }
    }
    
    /**
     * 向后导航的退出转场
     * 当前页面向右滑出，带有淡出效果
     */
    fun popExitTransition(): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
        if (isBottomTabSwitch()) {
            // Tab 切换：旧页面直接移除即可
            ExitTransition.None
        } else {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = TRANSITION_DURATION - FADE_DELAY,
                    easing = FastOutLinearInEasing
                )
            )
        }
    }
    
    /**
     * 模态框进入转场
     * 从底部滑入，带有淡入效果
     */
    fun modalEnterTransition(): EnterTransition {
        return slideInVertically(
            animationSpec = tween(
                durationMillis = FAST_TRANSITION_DURATION,
                easing = FastOutSlowInEasing
            ),
            initialOffsetY = { it }
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = FAST_TRANSITION_DURATION,
                easing = LinearOutSlowInEasing
            )
        )
    }
    
    /**
     * 模态框退出转场
     * 向底部滑出，带有淡出效果
     */
    fun modalExitTransition(): ExitTransition {
        return slideOutVertically(
            animationSpec = tween(
                durationMillis = FAST_TRANSITION_DURATION,
                easing = FastOutSlowInEasing
            ),
            targetOffsetY = { it }
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = FAST_TRANSITION_DURATION,
                easing = FastOutLinearInEasing
            )
        )
    }
}
