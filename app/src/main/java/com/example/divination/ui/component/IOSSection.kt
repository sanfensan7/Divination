package com.example.divination.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography

/**
 * iOS 风格分区布局组件
 * 
 * 特性：
 * - 分区标题（左对齐、大写、Footnote 样式）
 * - 分区内容容器（圆角卡片）
 * - 充足的留白间距
 * - 支持可选的标题参数
 * 
 * @param title 分区标题（可选）
 * @param modifier 修饰符
 * @param content 分区内容
 */
@Composable
fun IOSSection(
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = IOSSpacing.Small)
    ) {
        // 分区标题
        if (title != null) {
            Text(
                text = title.uppercase(),
                style = IOSTypography.Footnote,
                color = IOSColor.TextSecondary,
                modifier = Modifier.padding(
                    horizontal = IOSSpacing.PageHorizontal,
                    vertical = IOSSpacing.XSmall
                )
            )
        }
        
        // 分区内容
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = IOSSpacing.PageHorizontal),
            verticalArrangement = Arrangement.spacedBy(IOSSpacing.Small),
            content = content
        )
    }
}
