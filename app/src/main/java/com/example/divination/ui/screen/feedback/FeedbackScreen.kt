package com.example.divination.ui.screen.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.divination.ui.component.*
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 反馈页面
 * 
 * 允许用户提交反馈和建议
 * 
 * 特性：
 * - iOS 风格导航栏
 * - 使用 IOSCard 和 IOSSection 组织内容
 * - 使用 IOSButton 提交反馈
 * 
 * **Validates: Requirements 21.1, 21.2**
 */
@Composable
fun FeedbackScreen(
    onNavigateBack: () -> Unit = {}
) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var feedbackType by remember { mutableStateOf("功能建议") }
    var feedbackContent by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitSuccess by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // iOS 风格导航栏
        IOSNavigationBar(
            title = "意见反馈",
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
            // 反馈类型选择
            item {
                IOSSection(title = "反馈类型") {
                    IOSCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(IOSSpacing.Small)
                        ) {
                            listOf("功能建议", "问题反馈", "使用咨询", "其他").forEach { type ->
                                IOSButton(
                                    text = type,
                                    onClick = { feedbackType = type },
                                    style = if (feedbackType == type) IOSButtonStyle.Primary else IOSButtonStyle.Secondary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            
            // 反馈内容
            item {
                IOSSection(title = "反馈内容") {
                    IOSCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(IOSSpacing.Small)
                        ) {
                            Text(
                                text = "请详细描述您的反馈",
                                style = IOSTypography.Footnote,
                                color = IOSColor.TextSecondary
                            )
                            
                            OutlinedTextField(
                                value = feedbackContent,
                                onValueChange = { feedbackContent = it },
                                placeholder = {
                                    Text(
                                        text = "请输入您的反馈内容...",
                                        style = IOSTypography.Body,
                                        color = IOSColor.TextTertiary
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                textStyle = IOSTypography.Body.copy(color = IOSColor.TextPrimary),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = IOSColor.SystemBlue,
                                    unfocusedBorderColor = IOSColor.Separator,
                                    cursorColor = IOSColor.SystemBlue
                                ),
                                maxLines = 8
                            )
                        }
                    }
                }
            }
            
            // 联系方式（可选）
            item {
                IOSSection(title = "联系方式（可选）") {
                    IOSCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(IOSSpacing.Small)
                        ) {
                            Text(
                                text = "如需回复，请留下您的联系方式",
                                style = IOSTypography.Footnote,
                                color = IOSColor.TextSecondary
                            )
                            
                            OutlinedTextField(
                                value = contactInfo,
                                onValueChange = { contactInfo = it },
                                placeholder = {
                                    Text(
                                        text = "邮箱或手机号",
                                        style = IOSTypography.Body,
                                        color = IOSColor.TextTertiary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = IOSTypography.Body.copy(color = IOSColor.TextPrimary),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = IOSColor.SystemBlue,
                                    unfocusedBorderColor = IOSColor.Separator,
                                    cursorColor = IOSColor.SystemBlue
                                ),
                                singleLine = true
                            )
                        }
                    }
                }
            }
            
            // 提交按钮
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = IOSSpacing.PageHorizontal)
                        .padding(top = IOSSpacing.Large),
                    verticalArrangement = Arrangement.spacedBy(IOSSpacing.Medium)
                ) {
                    IOSButton(
                        text = if (isSubmitting) "提交中..." else "提交反馈",
                        onClick = {
                            if (feedbackContent.isNotBlank()) {
                                isSubmitting = true
                                // 模拟提交
                                coroutineScope.launch {
                                    delay(1000)
                                    isSubmitting = false
                                    submitSuccess = true
                                    delay(2000)
                                    submitSuccess = false
                                    feedbackContent = ""
                                    contactInfo = ""
                                }
                            }
                        },
                        enabled = !isSubmitting && feedbackContent.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (submitSuccess) {
                        Text(
                            text = "✓ 感谢您的反馈！",
                            style = IOSTypography.Body,
                            color = IOSColor.SystemGreen,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
            
            // 提示信息
            item {
                IOSCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = IOSSpacing.PageHorizontal)
                        .padding(top = IOSSpacing.Medium)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(IOSSpacing.XSmall)
                    ) {
                        Text(
                            text = "温馨提示",
                            style = IOSTypography.Footnote,
                            color = IOSColor.TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• 我们会认真阅读每一条反馈\n• 优质反馈将获得特别奖励\n• 通常会在3个工作日内回复",
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
