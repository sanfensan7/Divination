package com.example.divination.ui.animation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp

/**
 * iOS 风格滚动行为
 * 
 * 提供 iOS 风格的滚动相关动画效果，如导航栏收缩、标题大小变化等。
 */
object ScrollBehavior {
    /**
     * 导航栏收缩阈值（像素）
     * 滚动超过此值时，导航栏开始收缩
     */
    private const val COLLAPSE_THRESHOLD = 100f
    
    /**
     * 计算导航栏收缩进度
     * 
     * @param scrollState 滚动状态
     * @return 收缩进度（0.0 = 完全展开，1.0 = 完全收缩）
     */
    @Composable
    fun calculateCollapseProgress(scrollState: LazyListState): Float {
        val collapseProgress = remember {
            derivedStateOf {
                val offset = scrollState.firstVisibleItemScrollOffset.toFloat()
                (offset / COLLAPSE_THRESHOLD).coerceIn(0f, 1f)
            }
        }
        
        return collapseProgress.value
    }
    
    /**
     * 计算导航栏标题大小
     * 
     * @param scrollState 滚动状态
     * @param largeTitleSize Large Title 大小（默认 34sp）
     * @param smallTitleSize 小标题大小（默认 17sp）
     * @return 动画标题大小
     */
    @Composable
    fun animateTitleSize(
        scrollState: LazyListState,
        largeTitleSize: Float = 34f,
        smallTitleSize: Float = 17f
    ): Float {
        val collapseProgress = calculateCollapseProgress(scrollState)
        
        val titleSize by animateFloatAsState(
            targetValue = androidx.compose.ui.util.lerp(
                largeTitleSize,
                smallTitleSize,
                collapseProgress
            ),
            animationSpec = SpringAnimation.smooth(),
            label = "titleSize"
        )
        
        return titleSize
    }
    
    /**
     * 计算导航栏背景透明度
     * 
     * @param scrollState 滚动状态
     * @return 背景透明度（0.0 = 完全透明，1.0 = 完全不透明）
     */
    @Composable
    fun calculateBackgroundAlpha(scrollState: LazyListState): Float {
        val collapseProgress = calculateCollapseProgress(scrollState)
        
        val alpha by animateFloatAsState(
            targetValue = androidx.compose.ui.util.lerp(0f, 0.9f, collapseProgress),
            animationSpec = SpringAnimation.smooth(),
            label = "backgroundAlpha"
        )
        
        return alpha
    }
    
    /**
     * 计算导航栏模糊半径
     * 
     * @param scrollState 滚动状态
     * @return 模糊半径
     */
    @Composable
    fun calculateBlurRadius(scrollState: LazyListState): Dp {
        val collapseProgress = calculateCollapseProgress(scrollState)
        
        val blurRadius by animateFloatAsState(
            targetValue = if (collapseProgress > 0.5f) 20f else 0f,
            animationSpec = SpringAnimation.smooth(),
            label = "blurRadius"
        )
        
        return blurRadius.dp
    }
    
    /**
     * 计算导航栏高度
     * 
     * @param scrollState 滚动状态
     * @return 是否显示阴影
     */
    @Composable
    fun shouldShowElevation(scrollState: LazyListState): Boolean {
        val collapseProgress = calculateCollapseProgress(scrollState)
        return collapseProgress > 0.5f
    }
}
