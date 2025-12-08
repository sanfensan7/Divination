package com.example.divination.logic

import com.example.divination.logic.engine.BaziEngine
import com.example.divination.logic.engine.TrueSolarTimeCalculator
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime

/**
 * 八字排盘引擎端到端测试
 * 
 * 验证：
 * 1. 真太阳时校正准确性
 * 2. 八字排盘数据完整性
 * 3. 五行统计正确性
 * 4. 十神判断逻辑
 */
class BaziEngineTest {
    
    @Test
    fun testTrueSolarTimeCorrection() {
        // 测试北京时间（无需校正）
        val beijingTime = LocalDateTime.of(1990, 5, 15, 14, 30)
        val correctedBeijing = TrueSolarTimeCalculator.calculateTrueSolarTime(
            beijingTime, 116.4, 39.9
        )
        
        // 北京经度接近120，校正应该很小（约-14分钟）
        val diffMinutes = java.time.Duration.between(beijingTime, correctedBeijing).toMinutes()
        assertTrue("北京时间校正应小于30分钟", Math.abs(diffMinutes) < 30)
        
        println("✓ 北京时间：$beijingTime → $correctedBeijing (${diffMinutes}分钟)")
    }
    
    @Test
    fun testTrueSolarTimeShanghaiCorrection() {
        // 测试上海时间（需要校正约+6分钟）
        val shanghaiTime = LocalDateTime.of(1990, 5, 15, 14, 30)
        val correctedShanghai = TrueSolarTimeCalculator.calculateTrueSolarTime(
            shanghaiTime, 121.5, 31.2  // 上海经纬度
        )
        
        val diffMinutes = java.time.Duration.between(shanghaiTime, correctedShanghai).toMinutes()
        assertTrue("上海时间校正应为正值", diffMinutes > 0)
        assertTrue("上海时间校正应小于15分钟", diffMinutes < 15)
        
        println("✓ 上海时间：$shanghaiTime → $correctedShanghai (${diffMinutes}分钟)")
    }
    
    @Test
    fun testBaziCalculation_Male() {
        // 测试男性八字排盘
        val birthTime = LocalDateTime.of(1990, 5, 15, 14, 30)
        val profile = BaziEngine.calculate(
            inputTime = birthTime,
            gender = "男",
            longitude = 116.4,
            latitude = 39.9
        )
        
        // 验证基本信息
        assertEquals("性别应为男", "男", profile.gender)
        assertNotNull("年柱不应为空", profile.yearPillar)
        assertNotNull("月柱不应为空", profile.monthPillar)
        assertNotNull("日柱不应为空", profile.dayPillar)
        assertNotNull("时柱不应为空", profile.hourPillar)
        
        // 验证四柱结构
        assertTrue("年柱干支应为2字", profile.yearPillar.ganzhi.length == 2)
        assertTrue("日主应为1字", profile.dayMaster.length == 1)
        
        // 验证五行分析
        assertNotNull("五行统计不应为空", profile.fiveElements)
        assertTrue("总五行数量应>0", profile.fiveElements.totalCount.values.sum() > 0)
        
        // 验证十神
        assertEquals("十神应有7个", 7, profile.tenGods.size)
        assertTrue("年干十神不应为空", profile.tenGods["年干"]?.isNotBlank() == true)
        
        println("✓ 男性八字排盘测试通过")
        println("  日主：${profile.dayMaster}（${profile.dayMasterElement}）")
        println("  格局：${profile.pattern}")
        println("  身强身弱：${profile.strength}")
        println("  用神：${profile.usefulGod}")
        println("  五行：${profile.fiveElements.totalSummary}")
    }
    
    @Test
    fun testBaziCalculation_Female() {
        // 测试女性八字排盘
        val birthTime = LocalDateTime.of(1985, 8, 20, 10, 15)
        val profile = BaziEngine.calculate(
            inputTime = birthTime,
            gender = "女",
            longitude = 121.5,  // 上海
            latitude = 31.2
        )
        
        assertEquals("性别应为女", "女", profile.gender)
        assertNotNull("当前大运不应为空", profile.currentDayun)
        
        println("✓ 女性八字排盘测试通过")
        println("  日主：${profile.dayMaster}")
        println("  当前大运：${profile.currentDayun?.ganzhi}")
    }
    
    @Test
    fun testFiveElementsAnalysis() {
        val birthTime = LocalDateTime.of(1990, 5, 15, 14, 30)
        val profile = BaziEngine.calculate(birthTime, "男", 116.4, 39.9)
        
        val fiveElements = profile.fiveElements
        
        // 验证五行统计
        assertTrue("天干统计不应为空", fiveElements.tianganCount.isNotEmpty())
        assertTrue("地支统计不应为空", fiveElements.dizhiCount.isNotEmpty())
        
        // 验证最旺五行
        assertNotNull("最旺五行应有值", fiveElements.strongest)
        assertTrue("最旺五行应为：金/木/水/火/土", 
            listOf("金", "木", "水", "火", "土").contains(fiveElements.strongest))
        
        println("✓ 五行分析测试通过")
        println("  天干：${fiveElements.tianganSummary}")
        println("  地支：${fiveElements.dizhiSummary}")
        println("  总计：${fiveElements.totalSummary}")
        println("  最旺：${fiveElements.strongest}")
        println("  季节旺衰：${fiveElements.seasonalStrength}")
    }
    
    @Test
    fun testPromptGeneration() {
        val birthTime = LocalDateTime.of(1990, 5, 15, 14, 30)
        val profile = BaziEngine.calculate(birthTime, "男", 116.4, 39.9)
        
        // 测试提示词生成
        val promptString = profile.toPromptString()
        
        assertTrue("提示词应包含日主", promptString.contains("日主："))
        assertTrue("提示词应包含格局", promptString.contains("格局："))
        assertTrue("提示词应包含用神", promptString.contains("用神："))
        assertTrue("提示词应包含五行分析", promptString.contains("五行分析"))
        assertTrue("提示词应包含四柱八字", promptString.contains("四柱八字"))
        
        println("✓ 提示词生成测试通过")
        println("提示词长度：${promptString.length} 字符")
        println("提示词预览：")
        println(promptString.substring(0, Math.min(500, promptString.length)))
    }
    
    @Test
    fun testDifferentLocations() {
        val birthTime = LocalDateTime.of(1990, 5, 15, 14, 30)
        
        // 测试不同地点的时间校正
        val locations = listOf(
            Triple("北京", 116.4, 39.9),
            Triple("上海", 121.5, 31.2),
            Triple("广州", 113.3, 23.1),
            Triple("乌鲁木齐", 87.6, 43.8),
            Triple("拉萨", 91.1, 29.6)
        )
        
        println("✓ 不同地点时间校正测试：")
        for ((city, lon, lat) in locations) {
            val corrected = TrueSolarTimeCalculator.calculateTrueSolarTime(
                birthTime, lon, lat
            )
            val diff = java.time.Duration.between(birthTime, corrected).toMinutes()
            println("  $city（${lon}°）：${diff}分钟校正")
            
            // 验证校正合理性（东经相差1度约4分钟）
            val expectedDiff = ((lon - 120.0) * 4).toInt()
            assertTrue("${city}校正应接近预期值", Math.abs(diff - expectedDiff) < 20)
        }
    }
    
    @Test
    fun testEdgeCases() {
        // 测试边界情况
        
        // 1. 子时（23:00-01:00）
        val ziTime = LocalDateTime.of(1990, 1, 1, 0, 30)
        val ziProfile = BaziEngine.calculate(ziTime, "男", 116.4, 39.9)
        assertNotNull("子时排盘应成功", ziProfile)
        
        // 2. 午时（11:00-13:00）
        val wuTime = LocalDateTime.of(1990, 6, 15, 12, 0)
        val wuProfile = BaziEngine.calculate(wuTime, "男", 116.4, 39.9)
        assertNotNull("午时排盘应成功", wuProfile)
        
        // 3. 极端经度（新疆）
        val xinjiangTime = LocalDateTime.of(1990, 5, 15, 14, 30)
        val xinjiangProfile = BaziEngine.calculate(xinjiangTime, "男", 87.6, 43.8)
        assertNotNull("新疆时区排盘应成功", xinjiangProfile)
        val correction = java.time.Duration.between(
            xinjiangTime, xinjiangProfile.trueSolarTime
        ).toMinutes()
        assertTrue("新疆时间校正应为负值", correction < 0)
        
        println("✓ 边界情况测试通过")
        println("  子时排盘：${ziProfile.hourPillar.ganzhi}")
        println("  午时排盘：${wuProfile.hourPillar.ganzhi}")
        println("  新疆校正：${correction}分钟")
    }
}
