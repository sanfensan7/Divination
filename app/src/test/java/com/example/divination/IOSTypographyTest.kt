package com.example.divination

import com.example.divination.ui.theme.IOSTypography
import org.junit.Test
import org.junit.Assert.*
import androidx.compose.ui.unit.sp

/**
 * 设计令牌单元测试 - 字体层次一致性
 * 
 * **Feature: ios-style-ui-redesign, Property 2: 字体层次一致性**
 * 
 * 验证所有文本元素使用 iOS 标准字体大小。
 * 
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5**
 */
class IOSTypographyTest {
    
    /**
     * 测试 Large Title 字体大小是否为 34sp
     */
    @Test
    fun `LargeTitle should have 34sp font size`() {
        assertEquals(34.sp, IOSTypography.LargeTitle.fontSize)
    }
    
    /**
     * 测试 Title1 字体大小是否为 28sp
     */
    @Test
    fun `Title1 should have 28sp font size`() {
        assertEquals(28.sp, IOSTypography.Title1.fontSize)
    }
    
    /**
     * 测试 Title2 字体大小是否为 22sp
     */
    @Test
    fun `Title2 should have 22sp font size`() {
        assertEquals(22.sp, IOSTypography.Title2.fontSize)
    }
    
    /**
     * 测试 Title3 字体大小是否为 20sp
     */
    @Test
    fun `Title3 should have 20sp font size`() {
        assertEquals(20.sp, IOSTypography.Title3.fontSize)
    }
    
    /**
     * 测试 Headline 字体大小是否为 17sp
     */
    @Test
    fun `Headline should have 17sp font size`() {
        assertEquals(17.sp, IOSTypography.Headline.fontSize)
    }
    
    /**
     * 测试 Body 字体大小是否为 15sp
     */
    @Test
    fun `Body should have 15sp font size`() {
        assertEquals(15.sp, IOSTypography.Body.fontSize)
    }
    
    /**
     * 测试 Callout 字体大小是否为 16sp
     */
    @Test
    fun `Callout should have 16sp font size`() {
        assertEquals(16.sp, IOSTypography.Callout.fontSize)
    }
    
    /**
     * 测试 Footnote 字体大小是否为 13sp
     */
    @Test
    fun `Footnote should have 13sp font size`() {
        assertEquals(13.sp, IOSTypography.Footnote.fontSize)
    }
    
    /**
     * 测试 Caption1 字体大小是否为 12sp
     */
    @Test
    fun `Caption1 should have 12sp font size`() {
        assertEquals(12.sp, IOSTypography.Caption1.fontSize)
    }
    
    /**
     * 测试 Caption2 字体大小是否为 11sp
     */
    @Test
    fun `Caption2 should have 11sp font size`() {
        assertEquals(11.sp, IOSTypography.Caption2.fontSize)
    }
    
    /**
     * 测试所有字体大小都是 iOS 标准大小之一
     */
    @Test
    fun `All typography sizes should be iOS standard sizes`() {
        val standardSizes = setOf(34.sp, 28.sp, 22.sp, 20.sp, 17.sp, 16.sp, 15.sp, 13.sp, 12.sp, 11.sp)
        
        val allSizes = listOf(
            IOSTypography.LargeTitle.fontSize,
            IOSTypography.Title1.fontSize,
            IOSTypography.Title2.fontSize,
            IOSTypography.Title3.fontSize,
            IOSTypography.Headline.fontSize,
            IOSTypography.Body.fontSize,
            IOSTypography.Callout.fontSize,
            IOSTypography.Subheadline.fontSize,
            IOSTypography.Footnote.fontSize,
            IOSTypography.Caption1.fontSize,
            IOSTypography.Caption2.fontSize
        )
        
        allSizes.forEach { size ->
            assertTrue("Font size $size is not a standard iOS size", size in standardSizes)
        }
    }
}
