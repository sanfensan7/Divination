package com.example.divination.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.divination.ui.animation.LoadingAnimation
import com.example.divination.ui.theme.IOSColor

/**
 * iOS 风格加载指示器组件
 * 
 * 特性：
 * - 细线圆形旋转器
 * - 平滑旋转动画
 * - 符合 iOS 视觉风格
 * 
 * @param modifier 修饰符
 * @param size 指示器大小
 * @param strokeWidth 线条宽度
 */
@Composable
fun IOSLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    strokeWidth: Dp = 3.dp
) {
    // 无限旋转动画 - 使用统一的加载动画配置
    val infiniteTransition = rememberInfiniteTransition(label = "loadingRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = LoadingAnimation.spinnerRotation(),
        label = "rotation"
    )
    
    Canvas(
        modifier = modifier.size(size)
    ) {
        // 绘制圆弧
        drawArc(
            color = IOSColor.SystemBlue,
            startAngle = rotation,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}
