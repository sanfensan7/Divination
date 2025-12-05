package com.example.divination.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.divination.ui.animation.SpringAnimation
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSShape
import com.example.divination.ui.theme.IOSTypography

/**
 * iOS 按钮样式枚举
 */
enum class IOSButtonStyle {
    /** 主要按钮 - 蓝色背景，白色文字 */
    Primary,
    
    /** 次要按钮 - 浅灰色背景，蓝色文字 */
    Secondary,
    
    /** 破坏性按钮 - 红色背景，白色文字 */
    Destructive
}

/**
 * iOS 风格按钮组件
 * 
 * 特性：
 * - 14dp 圆角胶囊形状
 * - 三种样式（Primary、Secondary、Destructive）
 * - 按下时缩放到 0.94
 * - 释放时回弹效果
 * - 支持 enabled/disabled 状态
 * 
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param enabled 是否启用
 * @param style 按钮样式
 */
@Composable
fun IOSButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: IOSButtonStyle = IOSButtonStyle.Primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 按下时的缩放动画 - 缩放到 0.94
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = SpringAnimation.standard(),
        label = "buttonScale"
    )
    
    // 根据样式确定颜色
    val backgroundColor = when (style) {
        IOSButtonStyle.Primary -> IOSColor.SystemBlue
        IOSButtonStyle.Secondary -> IOSColor.BackgroundSecondary
        IOSButtonStyle.Destructive -> IOSColor.SystemRed
    }
    
    val textColor = when (style) {
        IOSButtonStyle.Primary -> Color.White
        IOSButtonStyle.Secondary -> IOSColor.SystemBlue
        IOSButtonStyle.Destructive -> Color.White
    }
    
    Button(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .height(50.dp),
        enabled = enabled,
        shape = IOSShape.ButtonShape,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            contentColor = textColor,
            disabledBackgroundColor = backgroundColor.copy(alpha = 0.5f),
            disabledContentColor = textColor.copy(alpha = 0.5f)
        ),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            style = IOSTypography.Body.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}
