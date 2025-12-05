package com.example.divination.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.divination.ui.animation.SpringAnimation
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing

/**
 * iOS 风格导航栏组件
 * 
 * 特性：
 * - Large Title（34sp）显示
 * - 滚动时标题收缩动画（34sp → 17sp）
 * - 半透明毛玻璃背景效果
 * - 滚动到顶部时标题展开
 * - 支持右侧操作按钮
 * 
 * @param title 导航栏标题
 * @param scrollState 滚动状态，用于控制收缩动画
 * @param modifier 修饰符
 * @param actions 右侧操作按钮
 */
@Composable
fun IOSNavigationBar(
    title: String,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    // 计算标题收缩进度（0.0 = 完全展开，1.0 = 完全收缩）
    val collapseProgress = remember {
        derivedStateOf {
            val offset = scrollState.firstVisibleItemScrollOffset.toFloat()
            val threshold = 100f
            (offset / threshold).coerceIn(0f, 1f)
        }
    }
    
    // 标题大小插值（34sp → 17sp）
    val titleSize by animateFloatAsState(
        targetValue = lerp(34f, 17f, collapseProgress.value),
        animationSpec = SpringAnimation.smooth(),
        label = "titleSize"
    )
    
    // 背景透明度（0.0 → 0.9）
    val backgroundAlpha by animateFloatAsState(
        targetValue = lerp(0f, 0.9f, collapseProgress.value),
        animationSpec = SpringAnimation.smooth(),
        label = "backgroundAlpha"
    )
    
    // 背景模糊效果
    val blurRadius by animateDpAsState(
        targetValue = if (collapseProgress.value > 0.5f) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "blurRadius"
    )
    
    // 是否显示阴影
    val elevation = if (collapseProgress.value > 0.5f) 4.dp else 0.dp
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .blur(blurRadius)
            .background(
                IOSColor.BackgroundPrimary.copy(alpha = backgroundAlpha)
            ),
        elevation = elevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(IOSColor.BackgroundPrimary.copy(alpha = backgroundAlpha))
                .padding(
                    horizontal = IOSSpacing.PageHorizontal,
                    vertical = IOSSpacing.Medium
                )
        ) {
            // 标题
            Text(
                text = title,
                fontSize = titleSize.sp,
                fontWeight = FontWeight.Bold,
                color = IOSColor.TextPrimary,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            
            // 右侧操作按钮
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(IOSSpacing.Small)
            ) {
                actions()
            }
        }
    }
}
