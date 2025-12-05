package com.example.divination

import com.example.divination.ui.theme.IOSShape
import org.junit.Test
import org.junit.Assert.*
import androidx.compose.ui.unit.dp

/**
 * 设计令牌单元测试 - 圆角一致性
 * 
 * **Feature: ios-style-ui-redesign, Property 3: 圆角一致性**
 * 
 * 验证卡片和按钮组件使用正确的圆角半径。
 * 
 * **Validates: Requirements 3.1, 3.2**
 */
class IOSShapeTest {
    
    /**
     * 测试卡片圆角半径是否为 24dp
     */
    @Test
    fun `CardCornerRadius should be 24dp`() {
        assertEquals(24.dp, IOSShape.CardCornerRadius)
    }
    
    /**
     * 测试按钮圆角半径是否为 14dp（胶囊形）
     */
    @Test
    fun `ButtonCornerRadius should be 14dp for capsule shape`() {
        assertEquals(14.dp, IOSShape.ButtonCornerRadius)
    }
    
    /**
     * 测试搜索栏圆角半径是否为 10dp
     */
    @Test
    fun `SearchBarCornerRadius should be 10dp`() {
        assertEquals(10.dp, IOSShape.SearchBarCornerRadius)
    }
    
    /**
     * 测试小圆角半径是否为 8dp
     */
    @Test
    fun `CornerRadiusSmall should be 8dp`() {
        assertEquals(8.dp, IOSShape.CornerRadiusSmall)
    }
    
    /**
     * 测试中等圆角半径是否为 12dp
     */
    @Test
    fun `CornerRadiusMedium should be 12dp`() {
        assertEquals(12.dp, IOSShape.CornerRadiusMedium)
    }
    
    /**
     * 测试大圆角半径是否为 16dp
     */
    @Test
    fun `CornerRadiusLarge should be 16dp`() {
        assertEquals(16.dp, IOSShape.CornerRadiusLarge)
    }
    
    /**
     * 测试特大圆角半径是否为 20dp
     */
    @Test
    fun `CornerRadiusXLarge should be 20dp`() {
        assertEquals(20.dp, IOSShape.CornerRadiusXLarge)
    }
    
    /**
     * 测试超大圆角半径是否为 24dp
     */
    @Test
    fun `CornerRadiusXXLarge should be 24dp`() {
        assertEquals(24.dp, IOSShape.CornerRadiusXXLarge)
    }
}
