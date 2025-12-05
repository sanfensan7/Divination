package com.example.divination.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSShape
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography

/**
 * iOS 风格搜索栏组件
 * 
 * 特性：
 * - 胶囊形状（10dp 圆角）
 * - 浅灰色背景
 * - 搜索图标
 * - 占位符文本
 * 
 * @param value 搜索文本
 * @param onValueChange 文本变化回调
 * @param modifier 修饰符
 * @param placeholder 占位符文本
 */
@Composable
fun IOSSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索"
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(
                color = IOSColor.BackgroundSecondary,
                shape = IOSShape.SearchBarShape
            )
            .padding(horizontal = IOSSpacing.Small),
        textStyle = IOSTypography.Body.copy(color = IOSColor.TextPrimary),
        singleLine = true,
        cursorBrush = SolidColor(IOSColor.SystemBlue),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(IOSSpacing.XSmall)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "搜索",
                    tint = IOSColor.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = IOSTypography.Body,
                            color = IOSColor.TextSecondary
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}
