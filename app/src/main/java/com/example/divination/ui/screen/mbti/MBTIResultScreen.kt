package com.example.divination.ui.screen.mbti

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.divination.model.MBTIResult
import com.example.divination.ui.component.*
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography
import com.example.divination.utils.MBTIStorageService
import com.example.divination.utils.ShareUtils
import kotlinx.coroutines.launch

/**
 * MBTI 结果页面
 * 
 * 显示 MBTI 测试结果和详细分析
 * 
 * 特性：
 * - iOS 风格导航栏
 * - 使用 IOSCard 显示结果内容
 * - 使用 IOSSection 组织不同部分
 * - 支持分享和保存功能
 * 
 * **Validates: Requirements 21.1, 21.2**
 */
@Composable
fun MBTIResultScreen(
    resultId: String = "",
    onNavigateBack: () -> Unit = {}
) {
    val scrollState = rememberLazyListState()
    val context = LocalContext.current
    val storageService = remember { MBTIStorageService.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    var isSharing by remember { mutableStateOf(false) }

    val result: MBTIResult? = remember {
        if (resultId.isNotEmpty()) {
            // 当前存储服务只支持按索引/最新结果，这里简单使用最新结果
            storageService.getLatestResult()
        } else {
            storageService.getLatestResult()
        }
    }

    val profile = result?.personalityType?.let { type ->
        MBTIProfileTexts.getProfile(type)
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // iOS 风格导航栏
        IOSNavigationBar(
            title = "测试结果",
            scrollState = scrollState,
            actions = {
                // 分享按钮
                androidx.compose.material.IconButton(
                    enabled = result != null && !isSharing,
                    onClick = {
                        val data = result
                        if (data != null) {
                            coroutineScope.launch {
                                try {
                                    isSharing = true
                                    ShareUtils.shareMBTIResult(context, data)
                                } catch (e: Exception) {
                                    // TODO: Snackbar/Toast 提示
                                } finally {
                                    isSharing = false
                                }
                            }
                        }
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
            if (result == null || profile == null) {
                item {
                    IOSCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = IOSSpacing.PageHorizontal)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(IOSSpacing.Medium)
                        ) {
                            Text(
                                text = "暂未找到测试结果",
                                style = IOSTypography.Title3,
                                color = IOSColor.TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "请先完成一次 MBTI 测试。",
                                style = IOSTypography.Body,
                                color = IOSColor.TextSecondary
                            )
                        }
                    }
                }
                return@LazyColumn
            }

            val mbtiType = result.personalityType
            val (mbtiName, description, traits, careerText, growthSuggestions) = profile

            // 人格类型
            item {
                IOSCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = IOSSpacing.PageHorizontal)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(IOSSpacing.Medium)
                    ) {
                        Text(
                            text = "你的人格类型是",
                            style = IOSTypography.Body,
                            color = IOSColor.TextSecondary
                        )
                        
                        Text(
                            text = mbtiType,
                            style = IOSTypography.LargeTitle,
                            color = IOSColor.SystemBlue,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = mbtiName,
                            style = IOSTypography.Title2,
                            color = IOSColor.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Divider(
                            color = IOSColor.Separator,
                            modifier = Modifier.padding(vertical = IOSSpacing.Small)
                        )
                        
                        Text(
                            text = description,
                            style = IOSTypography.Body,
                            color = IOSColor.TextPrimary,
                            lineHeight = IOSTypography.Body.lineHeight
                        )
                    }
                }
            }
            
            // 维度分析
            item {
                IOSSection(title = "维度分析") {
                    IOSCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(IOSSpacing.Medium)
                        ) {
                            DimensionItem(
                                dimension = "外向 (E) vs 内向 (I)",
                                result = result.getEILabel(),
                                percentage = result.getEIPercentage()
                            )
                            Divider(color = IOSColor.Separator)
                            DimensionItem(
                                dimension = "实感 (S) vs 直觉 (N)",
                                result = result.getSNLabel(),
                                percentage = result.getSNPercentage()
                            )
                            Divider(color = IOSColor.Separator)
                            DimensionItem(
                                dimension = "思考 (T) vs 情感 (F)",
                                result = result.getTFLabel(),
                                percentage = result.getTFPercentage()
                            )
                            Divider(color = IOSColor.Separator)
                            DimensionItem(
                                dimension = "判断 (J) vs 知觉 (P)",
                                result = result.getJPLabel(),
                                percentage = result.getJPPercentage()
                            )
                        }
                    }
                }
            }
            
            // 性格特点
            item {
                IOSSection(title = "性格特点") {
                    IOSCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(IOSSpacing.Small)
                        ) {
                            traits.forEachIndexed { index, (title, desc) ->
                                TraitItem(title, desc)
                                if (index != traits.lastIndex) {
                                    Divider(color = IOSColor.Separator)
                                }
                            }
                        }
                    }
                }
            }
            
            // 适合的职业
            item {
                IOSSection(title = "适合的职业") {
                    IOSCard {
                        Text(
                            text = careerText,
                            style = IOSTypography.Body,
                            color = IOSColor.TextPrimary,
                            lineHeight = IOSTypography.Body.lineHeight
                        )
                    }
                }
            }
            
            // 发展建议
            item {
                IOSSection(title = "发展建议") {
                    IOSCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(IOSSpacing.Small)
                        ) {
                            growthSuggestions.forEach { line ->
                                Text(
                                    text = "• $line",
                                    style = IOSTypography.Body,
                                    color = IOSColor.TextPrimary
                                )
                            }
                        }
                    }
                }
            }
            
            // 操作按钮
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = IOSSpacing.PageHorizontal)
                        .padding(top = IOSSpacing.Large),
                    verticalArrangement = Arrangement.spacedBy(IOSSpacing.Medium)
                ) {
                    IOSButton(
                        text = "保存结果",
                        onClick = {
                            // TODO: 保存结果
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    IOSButton(
                        text = "重新测试",
                        onClick = onNavigateBack,
                        style = IOSButtonStyle.Secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * 维度项组件
 */
@Composable
private fun DimensionItem(
    dimension: String,
    result: String,
    percentage: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(IOSSpacing.XSmall)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dimension,
                style = IOSTypography.Body,
                color = IOSColor.TextSecondary
            )
            Text(
                text = result,
                style = IOSTypography.Body,
                color = IOSColor.SystemBlue,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(IOSSpacing.XSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material.LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp),
                color = IOSColor.SystemBlue,
                backgroundColor = IOSColor.Separator
            )
            Text(
                text = "$percentage%",
                style = IOSTypography.Footnote,
                color = IOSColor.TextSecondary
            )
        }
    }
}

/**
 * 特质项组件
 */
@Composable
private fun TraitItem(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = IOSTypography.Body,
            color = IOSColor.TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            style = IOSTypography.Footnote,
            color = IOSColor.TextSecondary
        )
    }
}
