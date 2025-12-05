package com.example.divination.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.divination.ui.theme.IOSColor

/**
 * iOS 风格开关组件
 * 
 * 特性：
 * - 圆角矩形轨道
 * - 圆形滑块
 * - 开启时显示 #007AFF 蓝色
 * - 关闭时显示灰色
 * - 平滑的滑动动画
 * 
 * @param checked 是否选中
 * @param onCheckedChange 状态变化回调
 * @param modifier 修饰符
 * @param enabled 是否启用
 */
@Composable
fun IOSSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // 背景颜色动画
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) IOSColor.SystemBlue else Color(0xFFE5E5EA),
        animationSpec = tween(durationMillis = 200),
        label = "switchBackgroundColor"
    )
    
    // 滑块位置动画
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        animationSpec = tween(durationMillis = 200),
        label = "switchThumbOffset"
    )
    
    Box(
        modifier = modifier
            .width(51.dp)
            .height(31.dp)
            .clip(RoundedCornerShape(15.5.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // 滑块
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(27.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
