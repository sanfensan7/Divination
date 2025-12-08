package com.example.divination.logic

import org.junit.Test
import org.junit.Assert.*

/**
 * 测试 lunar-java 库是否可用
 */
class LunarLibTest {
    
    @Test
    fun testLunarImport() {
        try {
            // 尝试使用 lunar-java 的类
            val solar = com.nlf.calendar.Solar.fromYmdHms(2024, 12, 8, 14, 30, 0)
            assertNotNull("Solar 对象应创建成功", solar)
            
            val lunar = solar.lunar
            assertNotNull("Lunar 对象应获取成功", lunar)
            
            val eightChar = lunar.eightChar
            assertNotNull("EightChar 对象应获取成功", eightChar)
            
            println("✓ lunar-java 库导入成功")
            println("  公历：${solar.year}年${solar.month}月${solar.day}日")
            println("  农历：${lunar.yearInChinese}年${lunar.monthInChinese}月${lunar.dayInChinese}")
            println("  八字：${eightChar.year} ${eightChar.month} ${eightChar.day} ${eightChar.time}")
            
        } catch (e: ClassNotFoundException) {
            fail("lunar-java 类未找到：${e.message}")
        } catch (e: NoClassDefFoundError) {
            fail("lunar-java 类定义错误：${e.message}")
        } catch (e: Exception) {
            fail("lunar-java 调用失败：${e.message}")
        }
    }
}
