package com.example.divination.ui.screen.mbti

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.divination.model.MBTIAnswer
import com.example.divination.model.MBTIQuestion
import com.example.divination.ui.component.*
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography
import com.example.divination.utils.MBTICalculator
import com.example.divination.utils.MBTIQuestionProvider
import com.example.divination.utils.MBTIStorageService

/**
 * MBTI 测试页面
 * 
 * 提供 MBTI 人格测试
 * 
 * 特性：
 * - iOS 风格导航栏
 * - 进度条显示测试进度
 * - 使用 IOSCard 显示题目
 * - 使用 IOSButton 选择答案
 * 
 * **Validates: Requirements 21.1, 21.2**
 */
@Composable
fun MBTITestScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToResult: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val questionProvider = remember { MBTIQuestionProvider.getInstance(context) }
    val calculator = remember { MBTICalculator.getInstance() }
    val storageService = remember { MBTIStorageService.getInstance(context) }

    val scrollState = rememberLazyListState()
    val questions = remember { questionProvider.getAllQuestions() }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var answers by remember { mutableStateOf<Map<Int, MBTIAnswer>>(emptyMap()) }

    val totalQuestions = questions.size.coerceAtLeast(1)
    val progress = (currentQuestionIndex + 1).toFloat() / totalQuestions
    
    Column(modifier = Modifier.fillMaxSize()) {
        // iOS 风格导航栏
        IOSNavigationBar(
            title = "MBTI 人格测试",
            scrollState = scrollState
        )
        
        // 进度条
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = IOSSpacing.PageHorizontal)
                .padding(top = IOSSpacing.Medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "第 ${currentQuestionIndex + 1} / ${questions.size} 题",
                    style = IOSTypography.Footnote,
                    color = IOSColor.TextSecondary
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = IOSTypography.Footnote,
                    color = IOSColor.SystemBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(IOSSpacing.Small))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = IOSColor.SystemBlue,
                backgroundColor = IOSColor.Separator
            )
        }
        
        // 内容区域
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(vertical = IOSSpacing.Large)
        ) {
            if (questions.isEmpty()) {
                item {
                    IOSCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = IOSSpacing.PageHorizontal)
                    ) {
                        Text(
                            text = "题库加载失败，请稍后重试。",
                            style = IOSTypography.Body,
                            color = IOSColor.TextPrimary
                        )
                    }
                }
            } else if (currentQuestionIndex < questions.size) {
                val question = questions[currentQuestionIndex]
                
                // 题目
                item {
                    IOSCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = IOSSpacing.PageHorizontal)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(IOSSpacing.Large)
                        ) {
                            Text(
                                text = question.text,
                                style = IOSTypography.Title3,
                                color = IOSColor.TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            // 七级量表说明 + 选项
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(IOSSpacing.Small)
                            ) {
                                Text(
                                    text = "请根据第一直觉，在下方 7 个选项中选择一个：",
                                    style = IOSTypography.Footnote,
                                    color = IOSColor.TextSecondary
                                )

                                val options = MBTIAnswer.Option.values().toList()
                                val selectedValue = answers[question.id]?.selectedOption

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "强烈不同意",
                                        style = IOSTypography.Footnote,
                                        color = IOSColor.TextSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "强烈同意",
                                        style = IOSTypography.Footnote,
                                        color = IOSColor.TextSecondary,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = IOSSpacing.Small),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    options.forEach { option ->
                                        val isSelected = selectedValue == option.value
                                        val bgColor = if (isSelected) IOSColor.SystemBlue else Color.Transparent
                                        val textColor = if (isSelected) IOSColor.BackgroundPrimary else IOSColor.TextPrimary

                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) IOSColor.SystemBlue else IOSColor.Separator,
                                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                                                )
                                                .background(
                                                    color = bgColor,
                                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                                                )
                                                .clickable {
                                                    val newAnswer = MBTIAnswer(
                                                        questionId = question.id,
                                                        selectedOption = option.value
                                                    )
                                                    answers = answers.toMutableMap().apply {
                                                        put(question.id, newAnswer)
                                                    }

                                                    if (currentQuestionIndex < questions.size - 1) {
                                                        currentQuestionIndex++
                                                    } else {
                                                        // 所有题目完成，计算结果并导航
                                                        val answerList = answers.values.toList()
                                                        val result = calculator.calculateResult(
                                                            answers = answerList,
                                                            questions = questions,
                                                            version = questionProvider.getVersion()
                                                        )
                                                        storageService.saveResult(result)
                                                        onNavigateToResult(result.personalityType)
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = option.value.toString(),
                                                style = IOSTypography.Footnote.copy(fontSize = 12.sp),
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 上一题按钮
                if (currentQuestionIndex > 0) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = IOSSpacing.PageHorizontal)
                                .padding(top = IOSSpacing.Large)
                        ) {
                            IOSButton(
                                text = "上一题",
                                onClick = { currentQuestionIndex-- },
                                style = IOSButtonStyle.Secondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            
            // 提示信息
            item {
                IOSCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = IOSSpacing.PageHorizontal)
                        .padding(top = IOSSpacing.Large)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(IOSSpacing.XSmall)
                    ) {
                        Text(
                            text = "测试说明",
                            style = IOSTypography.Footnote,
                            color = IOSColor.TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• 请根据第一直觉选择答案\n• 没有对错之分，选择最符合你的选项\n• 完成所有题目后将获得详细的人格分析",
                            style = IOSTypography.Footnote,
                            color = IOSColor.TextSecondary,
                            lineHeight = IOSTypography.Footnote.lineHeight
                        )
                    }
                }
            }
        }
    }
}
