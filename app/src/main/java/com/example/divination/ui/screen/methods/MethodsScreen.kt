package com.example.divination.ui.screen.methods

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.divination.model.DivinationMethod
import com.example.divination.ui.component.*
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography

/**
 * 算命方式页面
 * 
 * 显示所有可用的算命方法，支持分类筛选
 * 
 * 特性：
 * - iOS 风格 Large Title 导航栏
 * - 分段控制器（分类选择）
 * - 使用 IOSCard 显示每个算命方法
 * - 显示方法图标、名称、描述
 * - 点击卡片高亮效果和导航
 * 
 * **Validates: Requirements 19.1, 19.2, 19.3, 19.4, 19.5**
 */
@Composable
fun MethodsScreen(
    viewModel: MethodsViewModel = viewModel(),
    onMethodClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // iOS 风格导航栏
        IOSNavigationBar(
            title = "算命方式",
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
            // 分类选择器
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = IOSSpacing.PageHorizontal)
                ) {
                    IOSSegmentedControl(
                        items = listOf(
                            MethodCategory.ALL.displayName,
                            MethodCategory.CHINESE.displayName,
                            MethodCategory.WESTERN.displayName,
                            MethodCategory.PSYCHOLOGICAL.displayName
                        ),
                        selectedIndex = when (uiState.currentCategory) {
                            MethodCategory.ALL -> 0
                            MethodCategory.CHINESE -> 1
                            MethodCategory.WESTERN -> 2
                            MethodCategory.PSYCHOLOGICAL -> 3
                        },
                        onItemSelected = { index ->
                            val category = when (index) {
                                0 -> MethodCategory.ALL
                                1 -> MethodCategory.CHINESE
                                2 -> MethodCategory.WESTERN
                                3 -> MethodCategory.PSYCHOLOGICAL
                                else -> MethodCategory.ALL
                            }
                            viewModel.selectCategory(category)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(IOSSpacing.Medium))
                }
            }
            
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
            
            // 空状态
            if (!uiState.isLoading && uiState.filteredMethods.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(IOSSpacing.XXLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        IOSEmptyState(
                            title = "暂无${uiState.currentCategory.displayName}算命方法",
                            icon = Icons.Outlined.Star
                        )
                    }
                }
            }
            
            // 算命方法列表
            if (!uiState.isLoading && uiState.filteredMethods.isNotEmpty()) {
                item {
                    IOSSection(title = uiState.currentCategory.displayName) {
                        // 使用 Column 而不是直接在 items 中，以便在 IOSSection 内部
                    }
                }
                
                items(
                    items = uiState.filteredMethods,
                    key = { it.id }
                ) { method ->
                    MethodCard(
                        method = method,
                        onClick = { onMethodClick(method.id) },
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
 * 算命方法卡片
 */
@Composable
private fun MethodCard(
    method: DivinationMethod,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IOSCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(IOSSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标（使用占位符）
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = method.name,
                    tint = IOSColor.SystemBlue,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // 方法信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 方法名称
                Text(
                    text = method.name,
                    style = IOSTypography.Headline,
                    color = IOSColor.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                
                // 方法描述
                Text(
                    text = method.description,
                    style = IOSTypography.Footnote,
                    color = IOSColor.TextSecondary
                )
            }
            
            // 右箭头指示器
            Icon(
                imageVector = Icons.Outlined.Star, // 使用占位符，实际应该是右箭头
                contentDescription = "查看详情",
                tint = IOSColor.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
