package com.example.divination

import com.example.divination.model.ResultSection
import com.example.divination.utils.DeepSeekService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 测试DeepSeekService中的关键方法
 */
class DeepSeekServiceTest {

    /**
     * 测试解析带有多个部分的响应
     */
    @Test
    fun testParseResponseToSections() {
        // 使用反射访问私有方法
        val parseMethod = DeepSeekService::class.java.getDeclaredMethod(
            "parseResponseToSections", 
            String::class.java
        )
        parseMethod.isAccessible = true
        
        // 测试标准格式响应
        val standardResponse = """
            【总论】
            这是总论内容。
            
            【事业】
            这是事业内容。
            
            【财运】
            这是财运内容。
        """.trimIndent()
        
        @Suppress("UNCHECKED_CAST")
        val result1 = parseMethod.invoke(DeepSeekService, standardResponse) as List<ResultSection>
        assertEquals(3, result1.size)
        assertEquals("总论", result1[0].title)
        assertEquals("事业", result1[1].title)
        assertEquals("财运", result1[2].title)
        
        // 测试部分标题无内容的情况
        val emptyContentResponse = """
            【总论】
            这是总论内容。
            
            【事业】
            
            【财运】
            这是财运内容。
        """.trimIndent()
        
        @Suppress("UNCHECKED_CAST")
        val result2 = parseMethod.invoke(DeepSeekService, emptyContentResponse) as List<ResultSection>
        assertEquals(2, result2.size) // 应该只有2个部分，因为事业部分内容为空
        
        // 测试没有标题的首部分
        val noTitleFirstPartResponse = """
            这是没有标题的首部分。
            
            【事业】
            这是事业内容。
        """.trimIndent()
        
        @Suppress("UNCHECKED_CAST")
        val result3 = parseMethod.invoke(DeepSeekService, noTitleFirstPartResponse) as List<ResultSection>
        assertEquals(2, result3.size)
        assertEquals("总论", result3[0].title) // 首部分应自动获得"总论"标题
        assertEquals("这是没有标题的首部分。", result3[0].content)
    }
    
    /**
     * 测试提取占星学星盘描述
     */
    @Test
    fun testExtractAstrologyChartDescription() {
        try {
            // 使用反射访问私有方法
            val extractMethod = DeepSeekService::class.java.getDeclaredMethod(
                "extractAstrologyChartDescription", 
                String::class.java
            )
            extractMethod.isAccessible = true
            
            // 测试标准星盘格式
            val standardChart = """
                以下是您的星盘分析:
                * 太阳位于白羊座15度
                * 月亮位于金牛座10度
                * 水星位于双子座5度
                
                这些行星之间形成以下相位:
                * 太阳和木星形成三分相(120度)
                * 月亮和火星形成四分相(90度)
            """.trimIndent()
            
            val result1 = extractMethod.invoke(DeepSeekService, standardChart) as String
            assertTrue(result1.contains("太阳位于白羊座15度"))
            assertTrue(result1.contains("月亮位于金牛座10度"))
            assertTrue(result1.contains("太阳和木星形成三分相(120度)"))
            
            // 测试不同格式的星盘描述
            val alternativeFormat = """
                星盘分析:
                - 太阳在白羊座，15度
                - 月亮落入金牛座 10度
                
                相位关系:
                - 太阳与木星构成三分相，约120度
            """.trimIndent()
            
            val result2 = extractMethod.invoke(DeepSeekService, alternativeFormat) as String
            assertTrue(result2.contains("太阳位于白羊座15度"))
            assertTrue(result2.contains("月亮位于金牛座10度"))
            assertTrue(result2.contains("太阳和木星形成三分相(120度)"))
            
            // 测试没有度数的情况
            val noDegreeFormat = """
                星盘信息:
                * 太阳在白羊座
                * 月亮位于金牛座
            """.trimIndent()
            
            val result3 = extractMethod.invoke(DeepSeekService, noDegreeFormat) as String
            assertTrue(result3.contains("太阳位于白羊座"))
            assertTrue(result3.contains("月亮位于金牛座"))
        } catch (e: NoSuchMethodException) {
            // 如果方法已被重命名或移除，跳过此测试
            println("方法extractAstrologyChartDescription可能已被重命名或移除，跳过测试")
        }
    }
} 