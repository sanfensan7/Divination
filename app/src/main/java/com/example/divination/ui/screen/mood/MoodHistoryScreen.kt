package com.example.divination.ui.screen.mood

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.divination.model.DivinationResult
import com.example.divination.ui.component.*
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography
import com.example.divination.utils.LocalStorageService
import java.text.SimpleDateFormat
import java.util.*

data class MoodRecord(
    val id: String,
    val mood: String,
    val note: String,
    val date: Date,
    val icon: ImageVector,
    val score: Int? = null
)

/**
 * 心情历史页面
 * 
 * 显示用户的心情记录历史
 * 
 * 特性：
 * - iOS 风格导航栏
 * - 使用 IOSCard 显示心情记录
 * - 使用 IOSEmptyState 显示空状态
 * 
 * **Validates: Requirements 21.1, 21.2**
 */
@Composable
fun MoodHistoryScreen() {
    val scrollState = rememberLazyListState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // 从本地存储读取所有算命结果
    val allResults by remember {
        mutableStateOf(LocalStorageService.getAllResults(context))
    }

    // 按日期分组（仅按天，不含时分秒）
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val resultsByDay: Map<String, List<DivinationResult>> = allResults.groupBy { result ->
        dateFormat.format(result.createTime)
    }

    // 生成每天的运势分析记录（带日评分）
    val dailyRecords: List<MoodRecord> = resultsByDay.entries.map { (dateKey, results) ->
        val anyResult = results.first()
        val dayDate = anyResult.createTime

        val allSections = results.flatMap { it.resultSections }
        val mergedText = allSections.joinToString("\n") { it.content }

        val scoredSections = allSections.mapNotNull { section ->
            section.score.takeIf { it >= 0 }
        }

        // 1) 优先使用分段 score 计算日评分
        val dayScoreFromSections: Int? = if (scoredSections.isNotEmpty()) {
            scoredSections.average().toInt().coerceIn(0, 100)
        } else {
            null
        }

        // 2) 如果没有 score，使用关键词启发式算一个粗略分数
        val goodKeywords = listOf("吉", "顺", "机会", "机遇", "良好", "提升", "上升")
        val badKeywords = listOf("凶", "阻", "压力", "挑战", "波动", "风险", "不利")

        val goodHits = goodKeywords.count { kw -> mergedText.contains(kw) }
        val badHits = badKeywords.count { kw -> mergedText.contains(kw) }

        val heuristicScore: Int? = if (goodHits == 0 && badHits == 0) {
            null
        } else {
            // 以 60 为基准，每个好词 +5，每个坏词 -5
            (60 + (goodHits - badHits) * 5).coerceIn(20, 95)
        }

        val dayScore: Int? = dayScoreFromSections ?: heuristicScore

        val fortuneTitle = when {
            dayScore == null -> "运势平稳，可按原计划推进"
            dayScore >= 75 -> "整体运势偏顺利"
            dayScore <= 45 -> "运势起伏较大，宜稳中求进"
            else -> "运势平稳，可按原计划推进"
        }

        val hasNobleHint = mergedText.contains("贵人") || mergedText.contains("相助") || mergedText.contains("帮助")
        val nobleSummary = if (hasNobleHint) {
            "今日有贵人运，适合多与人交流、主动寻求合作与支持。"
        } else {
            "今日贵人运平稳，更适合靠自身节奏推进，少做情绪化决定。"
        }

        val countText = "今日共进行了${results.size}次占卜。"

        val summary = buildString {
            append(countText)
            if (dayScore != null) {
                append("\n今日综合运势评分约为 ${dayScore} 分（0-100）。")
            }
            append("\n")
            append(nobleSummary)
        }

        val icon = when {
            (dayScore ?: 60) >= 75 -> Icons.Outlined.WbSunny
            (dayScore ?: 60) <= 45 -> Icons.Outlined.Cloud
            else -> Icons.Outlined.BrightnessMedium
        }

        MoodRecord(
            id = dateKey,
            mood = fortuneTitle,
            note = summary,
            date = dayDate,
            icon = icon,
            score = dayScore
        )
    }.sortedByDescending { it.date }

    val totalDays = dailyRecords.size
    val totalCount = allResults.size

    val personaTitle = if (totalDays == 0) {
        "暂时没有历史运势记录"
    } else {
        "历史运势总览"
    }

    // 最近 7 天运势趋势分析
    val recentRecords = dailyRecords.take(7).filter { it.score != null }
    val trendText: String? = if (recentRecords.size >= 3) {
        val scores = recentRecords.mapNotNull { it.score }
        if (scores.size < 3) {
            null
        } else {
            val mid = scores.size / 2
            val earlier = scores.take(mid)
            val later = scores.takeLast(scores.size - mid)

            val earlierAvg = earlier.average()
            val laterAvg = later.average()
            val diff = laterAvg - earlierAvg

            when {
                diff >= 5 -> "最近 7 天整体运势呈上升趋势，适合逐步推进重要计划。"
                diff <= -5 -> "最近 7 天整体运势略有回落，更适合稳住阵脚、避免激进行动。"
                else -> "最近 7 天整体运势基本持平，可以按原有节奏稳步前进。"
            }
        }
    } else {
        null
    }

    val personaDescription = when {
        totalDays == 0 -> "完成几次占卜后，这里会根据你的历史结果，帮你看出一段时间内的整体运势趋势。"
        totalCount >= 15 -> "你已经累计做了较多次占卜，可以从历史结果中看出比较清晰的节奏变化。建议把重要决策与这些高低起伏结合参考，但最终仍以现实情况为准。"
        totalCount in 5..14 -> "你已经有一定数量的历史占卜记录，可以大致看出最近一段时间的运势走向。后续多做一些在关键节点的占卜，会让趋势判断更准确。"
        else -> "当前历史记录还不算多，可以作为参考，后续随着记录增多，整体趋势分析会更稳定。"
    }.let { base ->
        if (trendText != null) {
            base + "\n\n" + trendText
        } else {
            base
        }
    }

    // 今日运势（如果今天有记录）
    val todayKey = dateFormat.format(Date())
    val todayRecord = dailyRecords.find { dateFormat.format(it.date) == todayKey }

    val warningTitle: String
    val warningDescription: String

    if (todayRecord != null) {
        warningTitle = "今日运势：${todayRecord.mood}"
        warningDescription = todayRecord.note
    } else {
        warningTitle = "今日尚无占卜记录"
        warningDescription = "今天还没有新的占卜结果。如果你正面临重要选择或感觉状态有波动，可以做一次占卜，稍后这里会给出基于今天结果的运势提示。"
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // iOS 风格导航栏
        IOSNavigationBar(
            title = "历史算命分析",
            scrollState = scrollState
        )
        
        // 内容区域
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(vertical = IOSSpacing.Medium)
        ) {
            if (dailyRecords.isEmpty()) {
                // 空状态
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(IOSSpacing.XXLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        IOSEmptyState(
                            title = "还没有历史运势记录",
                            description = "完成几次占卜后，这里会根据你的历史结果，帮你看出一段时间内的整体运势趋势。",
                            icon = Icons.Outlined.AutoAwesome
                        )
                    }
                }
            } else {
                // 用户画像
                item {
                    IOSSection(title = "运势画像") {
                        IOSCard {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(IOSSpacing.Small)
                            ) {
                                Text(
                                    text = personaTitle,
                                    style = IOSTypography.Title3,
                                    color = IOSColor.TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = personaDescription,
                                    style = IOSTypography.Body,
                                    color = IOSColor.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(IOSSpacing.Small))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatItem(label = "有记录的天数", value = "$totalDays")
                                    StatItem(label = "累计占卜次数", value = "$totalCount")
                                }
                            }
                        }
                    }
                }

                // 今日运势 / 风险提醒
                item {
                    IOSSection(title = "今日运势") {
                        IOSCard {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(IOSSpacing.Small)
                            ) {
                                Text(
                                    text = warningTitle,
                                    style = IOSTypography.Title3,
                                    color = if (todayRecord == null) IOSColor.TextSecondary else IOSColor.SystemBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = warningDescription,
                                    style = IOSTypography.Body,
                                    color = IOSColor.TextSecondary
                                )
                            }
                        }
                    }
                }
                
                // 心情记录列表
                item {
                    IOSSection(title = "历史每日分析") {
                        // 空白，标题已显示
                    }
                }
                
                items(dailyRecords) { record ->
                    MoodRecordCard(
                        record = record,
                        modifier = Modifier.padding(
                            horizontal = IOSSpacing.PageHorizontal,
                            vertical = IOSSpacing.XSmall
                        )
                    )
                }
            }
        }
    }
}

/**
 * 统计项组件
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = IOSTypography.Title2,
            color = IOSColor.SystemBlue,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = IOSTypography.Footnote,
            color = IOSColor.TextSecondary
        )
    }
}

/**
 * 心情记录卡片
 */
@Composable
private fun MoodRecordCard(
    record: MoodRecord,
    modifier: Modifier = Modifier
) {
    IOSCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(IOSSpacing.Medium),
            verticalAlignment = Alignment.Top
        ) {
            // 心情图标
            Icon(
                imageVector = record.icon,
                contentDescription = record.mood,
                tint = IOSColor.SystemBlue,
                modifier = Modifier.size(32.dp)
            )
            
            // 心情信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(IOSSpacing.XSmall)
            ) {
                // 心情和日期
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.mood,
                        style = IOSTypography.Headline,
                        color = IOSColor.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatDate(record.date),
                        style = IOSTypography.Footnote,
                        color = IOSColor.TextSecondary
                    )
                }
                
                // 笔记
                if (record.note.isNotEmpty()) {
                    Text(
                        text = record.note,
                        style = IOSTypography.Body,
                        color = IOSColor.TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * 格式化日期
 */
private fun formatDate(date: Date): String {
    val now = Date()
    val diff = now.time - date.time
    val days = diff / (24 * 60 * 60 * 1000)
    
    return when {
        days == 0L -> "今天"
        days == 1L -> "昨天"
        days == 2L -> "前天"
        else -> {
            val format = SimpleDateFormat("MM月dd日", Locale.CHINA)
            format.format(date)
        }
    }
}
