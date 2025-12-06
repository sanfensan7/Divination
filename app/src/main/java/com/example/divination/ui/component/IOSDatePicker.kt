package com.example.divination.ui.component

import android.widget.NumberPicker
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography
import java.text.SimpleDateFormat
import java.util.*

/**
 * iOS 风格日期选择器组件
 * 
 * 特性：
 * - 从底部滑入的模态框动画
 * - iOS 风格的日期选择 UI
 * - 背景遮罩淡入效果（alpha 0.3）
 * - 确认和取消按钮
 * - 使用 ease-out 时间函数
 * 
 * @param visible 是否显示日期选择器
 * @param selectedDate 当前选中的日期
 * @param onDateSelected 日期选择回调
 * @param onDismiss 关闭回调
 * 
 * **Validates: Requirements 10.1, 10.2, 10.3, 10.4, 16.5**
 */
@Composable
fun IOSDatePicker(
    visible: Boolean,
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    // 内部状态：临时选中的日期
    var tempDate by remember(selectedDate) { mutableStateOf(selectedDate) }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 200)),
        exit = fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
        // 背景遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            // 日期选择器卡片
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { /* 阻止点击穿透 */ }
                        ),
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    ),
                    backgroundColor = IOSColor.BackgroundPrimary,
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(IOSSpacing.Medium)
                    ) {
                        // 标题栏
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = IOSSpacing.Medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 取消按钮
                            TextButton(onClick = onDismiss) {
                                Text(
                                    text = "取消",
                                    style = IOSTypography.Body,
                                    color = IOSColor.SystemBlue
                                )
                            }
                            
                            // 标题
                            Text(
                                text = "选择日期",
                                style = IOSTypography.Headline,
                                color = IOSColor.TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            // 确认按钮
                            TextButton(
                                onClick = {
                                    onDateSelected(tempDate)
                                    onDismiss()
                                }
                            ) {
                                Text(
                                    text = "确定",
                                    style = IOSTypography.Body,
                                    color = IOSColor.SystemBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        Divider(color = IOSColor.Separator)
                        
                        Spacer(modifier = Modifier.height(IOSSpacing.Medium))
                        
                        // 日期选择器内容
                        DatePickerContent(
                            selectedDate = tempDate,
                            onDateChanged = { tempDate = it }
                        )
                        
                        Spacer(modifier = Modifier.height(IOSSpacing.Medium))
                    }
                }
            }
        }
    }
}

@Composable
private fun YearWheelPicker(
    year: Int,
    onYearChanged: (Int) -> Unit,
    range: IntRange = 1900..2100
) {
    val clampedYear = year.coerceIn(range.first, range.last)
    val height = 140.dp
    val overlayHeight = with(LocalDensity.current) { (height / 4).toPx() }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = IOSSpacing.Large)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = IOSColor.BackgroundSecondary,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp),
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = range.first
                    maxValue = range.last
                    wrapSelectorWheel = false
                    value = clampedYear
                    descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                    textSize = 48f
                    setOnValueChangedListener { _, _, newVal ->
                        onYearChanged(newVal)
                    }
                }
            },
            update = { picker ->
                if (picker.value != clampedYear) {
                    picker.value = clampedYear
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Divider(color = IOSColor.SystemBlue.copy(alpha = 0.3f), thickness = 1.dp)
            Spacer(modifier = Modifier.weight(1f))
            Divider(color = IOSColor.SystemBlue.copy(alpha = 0.3f), thickness = 1.dp)
        }
    }
}

/**
 * 日期选择器内容
 */
@Composable
private fun DatePickerContent(
    selectedDate: Date,
    onDateChanged: (Date) -> Unit
) {
    val calendar = remember(selectedDate) {
        Calendar.getInstance().apply { time = selectedDate }
    }
    
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(IOSSpacing.Medium)
    ) {
        // 显示当前选中的日期
        Text(
            text = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(selectedDate),
            style = IOSTypography.Title2,
            color = IOSColor.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(IOSSpacing.Small))
        
        YearWheelPicker(
            year = year,
            onYearChanged = { newYear ->
                val newCalendar = Calendar.getInstance().apply {
                    time = selectedDate
                    set(Calendar.YEAR, newYear)
                }
                onDateChanged(newCalendar.time)
            }
        )
        
        // 月份选择
        DatePickerRow(
            label = "月",
            value = month + 1, // Calendar.MONTH 从 0 开始
            range = 1..12,
            onValueChanged = { newMonth ->
                val newCalendar = Calendar.getInstance().apply {
                    time = selectedDate
                    set(Calendar.MONTH, newMonth - 1)
                }
                onDateChanged(newCalendar.time)
            }
        )
        
        // 日期选择
        DatePickerRow(
            label = "日",
            value = day,
            range = 1..calendar.getActualMaximum(Calendar.DAY_OF_MONTH),
            onValueChanged = { newDay ->
                val newCalendar = Calendar.getInstance().apply {
                    time = selectedDate
                    set(Calendar.DAY_OF_MONTH, newDay)
                }
                onDateChanged(newCalendar.time)
            }
        )
    }
}

/**
 * 日期选择器行
 */
@Composable
private fun DatePickerRow(
    label: String,
    value: Int,
    range: IntRange,
    onValueChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = IOSSpacing.Large),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 标签
        Text(
            text = label,
            style = IOSTypography.Body,
            color = IOSColor.TextSecondary
        )
        
        // 值选择器
        Row(
            horizontalArrangement = Arrangement.spacedBy(IOSSpacing.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 减少按钮
            IOSButton(
                text = "-",
                onClick = {
                    if (value > range.first) {
                        onValueChanged(value - 1)
                    }
                },
                modifier = Modifier.width(50.dp),
                enabled = value > range.first,
                style = IOSButtonStyle.Secondary
            )
            
            // 当前值
            Text(
                text = value.toString(),
                style = IOSTypography.Title3,
                color = IOSColor.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(60.dp)
            )
            
            // 增加按钮
            IOSButton(
                text = "+",
                onClick = {
                    if (value < range.last) {
                        onValueChanged(value + 1)
                    }
                },
                modifier = Modifier.width(50.dp),
                enabled = value < range.last,
                style = IOSButtonStyle.Secondary
            )
        }
    }
}
