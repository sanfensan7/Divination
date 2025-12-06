package com.example.divination.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.AlertDialog
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.divination.R
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
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }
    
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
                                uriHandler.openUri(PRIVACY_POLICY_URL)
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
                                showAboutDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(text = "关于我们") },
            text = {
                Text(text = context.getString(R.string.about_content, uiState.appVersion))
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(text = "确定")
                }
            }
        )
    }
}

private const val PRIVACY_POLICY_URL = "https://example.com/privacy"
