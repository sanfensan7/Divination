package com.example.divination.ui.screen.result

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.divination.model.ResultSection
import com.example.divination.ui.component.*
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * 算命结果页面
 * 
 * 显示算命结果的详细内容
 * 
 * 特性：
 * - iOS 风格导航栏显示 "算命结果"
 * - 使用 IOSCard 显示结果内容
 * - 使用 IOSSection 组织不同部分的结果
 * - 实现分享和保存按钮
 * - 实现结果动画展示效果
 * 
 * **Validates: Requirements 21.1, 21.2**
 */
@Composable
fun DivinationResultScreen(
    resultId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: DivinationResultViewModel = viewModel(
        factory = DivinationResultViewModelFactory(
            context = LocalContext.current,
            resultId = resultId
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    
    // 动画状态
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.result) {
        if (uiState.result != null) {
            delay(100)
            showContent = true
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // iOS 风格导航栏
        IOSNavigationBar(
            title = "算命结果",
            scrollState = scrollState,
            actions = {
                // 分享按钮
                androidx.compose.material.IconButton(
                    onClick = {
                        // TODO: 实现分享功能
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "分享",
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
            
            // 结果内容
            uiState.result?.let { result ->
                // 基本信息
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        IOSSection(title = "基本信息") {
                            IOSCard {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(IOSSpacing.Small)
                                ) {
                                    InfoRow(
                                        label = "算命时间",
                                        value = formatDate(result.createTime)
                                    )
                                    
                                    if (result.inputData.isNotEmpty()) {
                                        Divider(
                                            color = IOSColor.Separator,
                                            modifier = Modifier.padding(vertical = IOSSpacing.XSmall)
                                        )
                                        
                                        result.inputData.forEach { (key, value) ->
                                            val displayValue = if ((key.contains("Image") || key.contains("image")) && value.length > 100) {
                                                "已上传照片（长度 ${value.length} 字符）"
                                            } else {
                                                value
                                            }
                                            InfoRow(label = key, value = displayValue)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 结果分段
                itemsIndexed(result.resultSections) { index, section ->
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn() + slideInVertically(
                            initialOffsetY = { it / 2 }
                        )
                    ) {
                        LaunchedEffect(Unit) {
                            delay(index * 100L)
                        }
                        
                        ResultSectionCard(
                            section = section,
                            modifier = Modifier.padding(
                                horizontal = IOSSpacing.PageHorizontal,
                                vertical = IOSSpacing.XSmall
                            )
                        )
                    }
                }
                
                // 操作按钮
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = IOSSpacing.PageHorizontal)
                                .padding(top = IOSSpacing.Large, bottom = IOSSpacing.Medium),
                            verticalArrangement = Arrangement.spacedBy(IOSSpacing.Medium)
                        ) {
                            // 保存按钮
                            IOSButton(
                                text = if (uiState.isSaving) "保存中..." else "保存到历史记录",
                                onClick = { viewModel.saveResult() },
                                enabled = !uiState.isSaving,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            // 保存成功提示
                            if (uiState.saveSuccess) {
                                Text(
                                    text = "✓ 已保存到历史记录",
                                    style = IOSTypography.Footnote,
                                    color = IOSColor.SystemGreen,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                            
                            // 返回按钮
                            IOSButton(
                                text = "返回",
                                onClick = onNavigateBack,
                                style = IOSButtonStyle.Secondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 结果分段卡片
 */
@Composable
private fun ResultSectionCard(
    section: ResultSection,
    modifier: Modifier = Modifier
) {
    IOSCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(IOSSpacing.Medium)
        ) {
            // 标题和评分
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title,
                    style = IOSTypography.Title3,
                    color = IOSColor.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                // 评分（如果有）
                if (section.score >= 0) {
                    ScoreBadge(score = section.score)
                }
            }
            
            // 内容
            Text(
                text = section.content,
                style = IOSTypography.Body,
                color = IOSColor.TextPrimary,
                lineHeight = IOSTypography.Body.lineHeight
            )
        }
    }
}

/**
 * 评分徽章
 */
@Composable
private fun ScoreBadge(score: Int) {
    val color = when {
        score >= 80 -> IOSColor.SystemGreen
        score >= 60 -> IOSColor.SystemOrange
        else -> IOSColor.SystemRed
    }
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = IOSSpacing.Small, vertical = IOSSpacing.XSmall)
    ) {
        Text(
            text = "$score",
            style = IOSTypography.Title3,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "分",
            style = IOSTypography.Footnote,
            color = IOSColor.TextSecondary
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
 * 格式化日期
 */
private fun formatDate(date: Date): String {
    val format = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA)
    return format.format(date)
}
