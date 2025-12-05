package com.example.divination.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.divination.model.DivinationResult
import com.example.divination.model.MBTIResult
import com.example.divination.ui.component.*
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography
import java.text.SimpleDateFormat
import java.util.*

/**
 * 个人页面
 * 
 * 显示历史记录和 MBTI 信息
 */
@Composable
fun ProfileScreen(
    onNavigateToResult: (String) -> Unit = {},
    onNavigateToMBTITest: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                // 加载状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    IOSLoadingIndicator()
                }
            }
            
            is ProfileUiState.Success -> {
                // 成功状态
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // 导航栏
                    item {
                        IOSNavigationBar(
                            title = "个人",
                            scrollState = scrollState
                        )
                    }
                    
                    // MBTI 信息卡片
                    if (state.mbtiResult != null) {
                        item {
                            MBTIInfoCard(
                                mbtiResult = state.mbtiResult,
                                testCount = state.mbtiTestCount,
                                onNavigateToTest = onNavigateToMBTITest
                            )
                        }
                    } else {
                        item {
                            MBTIEmptyCard(onNavigateToTest = onNavigateToMBTITest)
                        }
                    }
                    
                    // 历史记录分区
                    item {
                        IOSSection(title = "历史记录") {
                            // 空内容，实际内容在下面的 items 中
                        }
                    }
                    
                    // 历史记录列表
                    if (state.historyRecords.isEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(IOSSpacing.Medium))
                            IOSEmptyState(
                                title = "暂无历史记录",
                                description = "开始你的第一次算命吧",
                                icon = Icons.Outlined.History
                            )
                        }
                    } else {
                        items(
                            items = state.historyRecords,
                            key = { it.id }
                        ) { record ->
                            HistoryRecordItem(
                                record = record,
                                onItemClick = { onNavigateToResult(record.id) },
                                onDeleteClick = { viewModel.deleteHistoryRecord(record.id) }
                            )
                        }
                        
                        // 清空按钮
                        item {
                            Spacer(modifier = Modifier.height(IOSSpacing.Medium))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = IOSSpacing.PageHorizontal)
                            ) {
                                IOSButton(
                                    text = "清空历史记录",
                                    onClick = { viewModel.clearAllHistory() },
                                    style = IOSButtonStyle.Destructive,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            
            is ProfileUiState.Error -> {
                // 错误状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(IOSSpacing.Medium)
                    ) {
                        Text(
                            text = state.message,
                            style = IOSTypography.Body,
                            color = IOSColor.TextSecondary
                        )
                        IOSButton(
                            text = "重试",
                            onClick = { viewModel.loadData() }
                        )
                    }
                }
            }
        }
    }
}

/**
 * MBTI 信息卡片
 */
@Composable
private fun MBTIInfoCard(
    mbtiResult: MBTIResult,
    testCount: Int,
    onNavigateToTest: () -> Unit
) {
    IOSSection(title = "MBTI 性格测试") {
        IOSCard(onClick = onNavigateToTest) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(IOSSpacing.Medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Psychology,
                        contentDescription = "MBTI",
                        tint = IOSColor.SystemBlue,
                        modifier = Modifier.size(40.dp)
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = mbtiResult.personalityType,
                            style = IOSTypography.Title2,
                            color = IOSColor.TextPrimary
                        )
                        Text(
                            text = "已测试 $testCount 次",
                            style = IOSTypography.Footnote,
                            color = IOSColor.TextSecondary
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "查看详情",
                    tint = IOSColor.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * MBTI 空状态卡片
 */
@Composable
private fun MBTIEmptyCard(onNavigateToTest: () -> Unit) {
    IOSSection(title = "MBTI 性格测试") {
        IOSCard(onClick = onNavigateToTest) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(IOSSpacing.Medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Psychology,
                        contentDescription = "MBTI",
                        tint = IOSColor.SystemBlue,
                        modifier = Modifier.size(40.dp)
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "开始测试",
                            style = IOSTypography.Headline,
                            color = IOSColor.TextPrimary
                        )
                        Text(
                            text = "了解你的性格类型",
                            style = IOSTypography.Footnote,
                            color = IOSColor.TextSecondary
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "开始测试",
                    tint = IOSColor.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 历史记录项
 */
@Composable
private fun HistoryRecordItem(
    record: DivinationResult,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = IOSSpacing.PageHorizontal)
            .padding(bottom = IOSSpacing.Small)
    ) {
        IOSCard(onClick = onItemClick) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = getMethodName(record.methodId),
                        style = IOSTypography.Body,
                        color = IOSColor.TextPrimary
                    )
                    Text(
                        text = formatDate(record.createTime),
                        style = IOSTypography.Footnote,
                        color = IOSColor.TextSecondary
                    )
                }
                
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "查看详情",
                    tint = IOSColor.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 格式化日期
 */
private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)
    return sdf.format(date)
}

/**
 * 获取算命方法名称
 */
private fun getMethodName(methodId: String): String {
    return when (methodId) {
        "zhouyi" -> "周易占卜"
        "bazi" -> "八字命理"
        "tarot" -> "塔罗牌"
        "astrology" -> "占星学"
        "dream" -> "解梦"
        "palmistry" -> "手相"
        "physiognomy" -> "面相"
        "mbti" -> "MBTI 测试"
        else -> "算命"
    }
}
