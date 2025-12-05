package com.example.divination.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography

/**
 * iOS 风格空状态视图组件
 * 
 * 特性：
 * - 居中显示
 * - 图标 + 标题 + 描述
 * - 可选操作按钮
 * - 符合 iOS 空状态设计
 * 
 * @param title 标题
 * @param description 描述文本（可选）
 * @param icon 图标
 * @param modifier 修饰符
 * @param action 操作按钮（可选）
 */
@Composable
fun IOSEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector = Icons.Outlined.Inbox,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(IOSSpacing.XXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(IOSSpacing.Medium)
    ) {
        // 图标
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = IOSColor.TextSecondary,
            modifier = Modifier.size(64.dp)
        )
        
        // 标题
        Text(
            text = title,
            style = IOSTypography.Title3,
            color = IOSColor.TextPrimary,
            textAlign = TextAlign.Center
        )
        
        // 描述
        if (description != null) {
            Text(
                text = description,
                style = IOSTypography.Body,
                color = IOSColor.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
        
        // 操作按钮
        if (action != null) {
            Spacer(modifier = Modifier.height(IOSSpacing.Small))
            action()
        }
    }
}
