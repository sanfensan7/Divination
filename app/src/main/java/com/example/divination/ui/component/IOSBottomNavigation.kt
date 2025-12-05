package com.example.divination.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.divination.ui.animation.SpringAnimation
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import com.example.divination.ui.theme.IOSTypography

/**
 * 底部导航项数据类
 */
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val index: Int
)

/**
 * iOS 风格底部导航组件
 * 
 * 特性：
 * - 5 个标签（首页、心情、算命、个人、设置）
 * - 使用线条风格图标（Material Icons Extended）
 * - 选中时显示 #007AFF 蓝色
 * - 未选中时显示 #8E8E93 灰色
 * - 点击时的微妙缩放动画
 * 
 * @param selectedTab 当前选中的标签索引
 * @param onTabSelected 标签选中回调
 * @param modifier 修饰符
 */
@Composable
fun IOSBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        BottomNavItem("首页", Icons.Outlined.Home, 0),
        BottomNavItem("心情", Icons.Outlined.FavoriteBorder, 1),
        BottomNavItem("算命", Icons.Outlined.Star, 2),
        BottomNavItem("个人", Icons.Outlined.Person, 3),
        BottomNavItem("设置", Icons.Outlined.Settings, 4)
    )
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = IOSColor.BackgroundPrimary,
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = IOSSpacing.XSmall),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                IOSBottomNavItem(
                    item = tab,
                    selected = selectedTab == tab.index,
                    onClick = { onTabSelected(tab.index) }
                )
            }
        }
    }
}

/**
 * 底部导航单个项
 */
@Composable
private fun IOSBottomNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 点击时的缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = SpringAnimation.standard(),
        label = "navItemScale"
    )
    
    // 颜色动画
    val color by animateColorAsState(
        targetValue = if (selected) IOSColor.SystemBlue else IOSColor.SystemGray,
        animationSpec = tween(durationMillis = 200),
        label = "navItemColor"
    )
    
    Column(
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(IOSSpacing.XSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = item.label,
            style = IOSTypography.Caption2,
            color = color
        )
    }
}
