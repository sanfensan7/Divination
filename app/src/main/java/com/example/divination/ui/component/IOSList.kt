package com.example.divination.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.divination.ui.animation.ListItemAnimation.animateListItem
import com.example.divination.ui.animation.SpringAnimation
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography

/**
 * iOS 风格列表项组件
 * 
 * 特性：
 * - 左侧标题和可选副标题
 * - 右侧可选内容或箭头
 * - 分隔线
 * - 点击交互
 * - 出现动画和按下动画
 * 
 * @param title 标题
 * @param subtitle 副标题（可选）
 * @param onClick 点击回调（可选）
 * @param showChevron 是否显示右侧箭头
 * @param trailingContent 右侧自定义内容
 * @param index 列表项索引，用于错开动画
 * @param animated 是否启用出现动画
 */
@Composable
fun IOSListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null,
    index: Int = 0,
    animated: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 按下时的背景高亮效果
    val backgroundColor by animateFloatAsState(
        targetValue = if (isPressed) 0.05f else 0f,
        animationSpec = SpringAnimation.fast(),
        label = "listItemBackground"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (animated) Modifier.animateListItem(index) else Modifier)
            .graphicsLayer {
                // 添加轻微的背景高亮
                alpha = 1f - backgroundColor
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = IOSSpacing.Medium,
                    vertical = IOSSpacing.Small
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧内容
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = IOSTypography.Body,
                    color = IOSColor.TextPrimary
                )
                
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = IOSTypography.Footnote,
                        color = IOSColor.TextSecondary
                    )
                }
            }
            
            // 右侧内容
            Row(
                horizontalArrangement = Arrangement.spacedBy(IOSSpacing.XSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (trailingContent != null) {
                    trailingContent()
                }
                
                if (showChevron && onClick != null) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "导航",
                        tint = IOSColor.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        // 分隔线
        Divider(
            color = IOSColor.Separator,
            thickness = 0.5.dp,
            modifier = Modifier.padding(start = IOSSpacing.Medium)
        )
    }
}

/**
 * iOS 风格列表容器
 * 
 * 将多个列表项组合在一起，带有圆角背景
 */
@Composable
fun IOSList(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    IOSCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}
