package com.example.divination.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.divination.ui.component.*
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography
import com.example.divination.utils.ChineseAlmanacService
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 首页屏幕
 * 
 * 显示老黄历信息，包括：
 * - 日期、农历、生肖、五行
 * - 宜忌事项
 * - 方位吉凶
 * - 其他黄历信息
 * 
 * 特性：
 * - iOS 风格 Large Title 导航栏
 * - 滚动时标题收缩效果
 * - 使用 IOSCard 显示老黄历信息
 * - 使用 IOSSection 组织内容
 * - 支持日期选择
 * 
 * **Validates: Requirements 16.1, 16.2, 16.3, 16.4, 16.5**
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 主页内容
        Column(modifier = Modifier.fillMaxSize()) {
            // iOS 风格导航栏
            IOSNavigationBar(
                title = "首页",
                scrollState = scrollState,
                actions = {
                    // 日期选择按钮
                    IconButton(onClick = { viewModel.showDatePicker() }) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = "选择日期",
                            tint = IOSColor.SystemBlue
                        )
                    }
                }
            )
            
            // 内容区域
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = IOSSpacing.Medium)
            ) {
                // 加载状态
                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(IOSSpacing.XXLarge),
                            contentAlignment = Alignment.Center
                        ) {
                            IOSLoadingIndicator()
                        }
                    }
                }
                
                // 错误状态
                if (uiState.error != null) {
                    item {
                        IOSSection(title = "错误") {
                            IOSCard {
                                Text(
                                    text = uiState.error ?: "未知错误",
                                    style = IOSTypography.Body,
                                    color = IOSColor.SystemRed
                                )
                            }
                        }
                    }
                }
                
                // 老黄历内容
                uiState.almanac?.let { almanac ->
                    // 日期信息卡片
                    item {
                        IOSSection(title = "今日黄历") {
                            IOSCard {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(IOSSpacing.Small)
                                ) {
                                    // 公历日期
                                    Text(
                                        text = ChineseAlmanacService.formatDate(almanac.date),
                                        style = IOSTypography.Title2,
                                        color = IOSColor.TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    // 农历日期
                                    Text(
                                        text = "农历 ${almanac.lunarDate}",
                                        style = IOSTypography.Body,
                                        color = IOSColor.TextSecondary
                                    )
                                    
                                    Divider(
                                        color = IOSColor.Separator,
                                        modifier = Modifier.padding(vertical = IOSSpacing.XSmall)
                                    )
                                    
                                    // 生肖和五行
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        InfoItem(label = "生肖", value = almanac.chineseZodiac)
                                        InfoItem(label = "五行", value = almanac.fiveElements)
                                    }
                                    
                                    // 星宿信息
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        InfoItem(label = "值年星", value = almanac.yearStar)
                                        InfoItem(label = "值日星", value = almanac.dayStar)
                                    }
                                }
                            }
                        }
                    }
                    
                    // 宜忌事项
                    item {
                        IOSSection(title = "宜忌") {
                            IOSCard {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(IOSSpacing.Medium)
                                ) {
                                    // 宜
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(IOSSpacing.XSmall)
                                    ) {
                                        Text(
                                            text = "宜",
                                            style = IOSTypography.Headline,
                                            color = IOSColor.SystemGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = almanac.goodActivities.joinToString("  "),
                                            style = IOSTypography.Body,
                                            color = IOSColor.TextPrimary
                                        )
                                    }
                                    
                                    Divider(color = IOSColor.Separator)
                                    
                                    // 忌
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(IOSSpacing.XSmall)
                                    ) {
                                        Text(
                                            text = "忌",
                                            style = IOSTypography.Headline,
                                            color = IOSColor.SystemRed,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = almanac.badActivities.joinToString("  "),
                                            style = IOSTypography.Body,
                                            color = IOSColor.TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // 方位吉凶
                    item {
                        IOSSection(title = "方位") {
                            IOSCard {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(IOSSpacing.Small)
                                ) {
                                    almanac.direction.entries.chunked(2).forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            rowItems.forEach { (direction, luck) ->
                                                DirectionItem(
                                                    direction = direction,
                                                    luck = luck,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            // 如果只有一个元素，添加空白占位
                                            if (rowItems.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // 其他信息
                    item {
                        IOSSection(title = "其他") {
                            IOSCard {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(IOSSpacing.Small)
                                ) {
                                    InfoRow(label = "胎神", value = almanac.godOfBaby)
                                    Divider(color = IOSColor.Separator)
                                    InfoRow(label = "冲煞", value = almanac.chongSha)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 日期选择器覆盖在内容之上
        IOSDatePicker(
            visible = uiState.showDatePicker,
            selectedDate = uiState.selectedDate,
            onDateSelected = { date ->
                viewModel.selectDate(date)
            },
            onDismiss = {
                viewModel.hideDatePicker()
            }
        )
    }
}

/**
 * 信息项组件
 */
@Composable
private fun InfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = IOSTypography.Footnote,
            color = IOSColor.TextSecondary
        )
        Text(
            text = value,
            style = IOSTypography.Body,
            color = IOSColor.TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 信息行组件
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = IOSTypography.Body,
            color = IOSColor.TextSecondary
        )
        Text(
            text = value,
            style = IOSTypography.Body,
            color = IOSColor.TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 方位项组件
 */
@Composable
private fun DirectionItem(
    direction: String,
    luck: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(IOSSpacing.XSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = direction,
            style = IOSTypography.Body,
            color = IOSColor.TextSecondary
        )
        Text(
            text = luck,
            style = IOSTypography.Body,
            color = if (luck == "吉") IOSColor.SystemGreen else IOSColor.SystemRed,
            fontWeight = FontWeight.SemiBold
        )
    }
}
