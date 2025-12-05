package com.example.divination.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/**
 * iOS 风格列表项动画
 * 
 * 提供列表项出现时的渐入和滑入动画效果。
 */
object ListItemAnimation {
    /**
     * 列表项出现动画持续时间（毫秒）
     */
    const val ITEM_APPEAR_DURATION = 300
    
    /**
     * 列表项之间的延迟（毫秒）
     */
    const val ITEM_STAGGER_DELAY = 50L
    
    /**
     * 列表项初始偏移量（dp）
     */
    const val ITEM_INITIAL_OFFSET = 20f
    
    /**
     * 为列表项添加出现动画
     * 
     * @param index 列表项索引，用于计算延迟
     * @param visible 是否可见
     * @return 修饰符
     */
    @Composable
    fun Modifier.animateListItem(
        index: Int,
        visible: Boolean = true
    ): Modifier {
        var animationPlayed by remember { mutableStateOf(false) }
        
        val alpha by animateFloatAsState(
            targetValue = if (visible && animationPlayed) 1f else 0f,
            animationSpec = tween(
                durationMillis = ITEM_APPEAR_DURATION,
                easing = FastOutSlowInEasing
            ),
            label = "listItemAlpha"
        )
        
        val translationY by animateFloatAsState(
            targetValue = if (visible && animationPlayed) 0f else ITEM_INITIAL_OFFSET,
            animationSpec = SpringAnimation.listItem(),
            label = "listItemTranslationY"
        )
        
        LaunchedEffect(visible) {
            if (visible) {
                delay(index * ITEM_STAGGER_DELAY)
                animationPlayed = true
            }
        }
        
        return this.graphicsLayer {
            this.alpha = alpha
            this.translationY = translationY
        }
    }
    
    /**
     * 为列表项添加悬停动画
     * 
     * @param hovered 是否悬停
     * @return 修饰符
     */
    @Composable
    fun Modifier.animateListItemHover(
        hovered: Boolean
    ): Modifier {
        val scale by animateFloatAsState(
            targetValue = if (hovered) 1.02f else 1f,
            animationSpec = SpringAnimation.fast(),
            label = "listItemHoverScale"
        )
        
        return this.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    }
}
