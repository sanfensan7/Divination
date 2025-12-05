package com.example.divination.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.divination.ui.component.*
import com.example.divination.ui.navigation.Routes
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography

/**
 * 设置页面
 * 
 * 使用 iOS 风格组件显示应用设置和信息
 */
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    
    Scaffold(
        topBar = {
            IOSNavigationBar(
                title = "设置",
                scrollState = scrollState
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = IOSSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(IOSSpacing.ListSectionSpacing)
        ) {
            // 使用统计
            item {
                IOSSection(title = "使用统计") {
                    IOSCard {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(IOSSpacing.Medium)
                        ) {
                            // 今日使用
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "今日使用",
                                    style = IOSTypography.Body,
                                    color = IOSColor.TextPrimary
                                )
                                Text(
                                    text = "${uiState.todayUsageCount} 次",
                                    style = IOSTypography.Body,
                                    color = IOSColor.TextSecondary
                                )
                            }
                            
                            // 总使用次数
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "总使用次数",
                                    style = IOSTypography.Body,
                                    color = IOSColor.TextPrimary
                                )
                                Text(
                                    text = "${uiState.totalUsageCount} 次",
                                    style = IOSTypography.Body,
                                    color = IOSColor.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
            
            // 反馈与支持
            item {
                IOSSection(title = "反馈与支持") {
                    IOSList {
                        IOSListItem(
                            title = "意见反馈",
                            onClick = {
                                navController.navigate(Routes.FEEDBACK)
                            }
                        )
                        
                        IOSListItem(
                            title = "隐私政策",
                            onClick = {
                                // TODO: 打开隐私政策页面
                            }
                        )
                    }
                }
            }
            
            // 关于
            item {
                IOSSection(title = "关于") {
                    IOSList {
                        IOSListItem(
                            title = "版本信息",
                            showChevron = false,
                            trailingContent = {
                                Text(
                                    text = uiState.appVersion,
                                    style = IOSTypography.Body,
                                    color = IOSColor.TextSecondary
                                )
                            }
                        )
                        
                        IOSListItem(
                            title = "关于我们",
                            onClick = {
                                // TODO: 打开关于页面
                            }
                        )
                    }
                }
            }
        }
    }
}
