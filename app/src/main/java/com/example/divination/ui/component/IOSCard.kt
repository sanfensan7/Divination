package com.example.divination.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.divination.ui.animation.SpringAnimation
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSShape
import com.example.divination.ui.theme.IOSSpacing

/**
 * iOS 风格卡片组件
 * 
 * 特性：
 * - 24dp 圆角
 * - 微妙阴影和浮起效果
 * - 点击时缩放到 0.97
 * - 点击时亮度降低 15%
 * - 支持可选的 onClick 回调
 * 
 * @param modifier 修饰符
 * @param onClick 点击回调（可选）
 * @param content 卡片内容
 */
@Composable
fun IOSCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 按下时的缩放动画 - 缩放到 0.97
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = SpringAnimation.standard(),
        label = "cardScale"
    )
    
    // 按下时的亮度变化 - 降低 15%
    val brightness by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 100),
        label = "cardBrightness"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .graphicsLayer {
                alpha = brightness
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = IOSShape.CardShape,
        backgroundColor = IOSColor.CardBackground,
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier.padding(IOSSpacing.CardPadding),
            content = content
        )
    }
}
