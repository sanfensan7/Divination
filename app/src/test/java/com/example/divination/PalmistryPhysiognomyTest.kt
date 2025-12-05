package com.example.divination

import com.example.divination.utils.DivinationMethodProvider
import org.junit.Assert.*
import org.junit.Test

/**
 * 手相面相功能测试
 */
class PalmistryPhysiognomyTest {

    /**
     * 测试手相功能是否正确注册
     */
    @Test
    fun testPalmistryMethodRegistered() {
        val palmistry = DivinationMethodProvider.getMethodById("palmistry")
        
        assertNotNull("手相功能应该被注册", palmistry)
        assertEquals("手相", palmistry?.name)
        assertEquals("通过手掌照片解读性格与命运", palmistry?.description)
        assertEquals(1, palmistry?.type) // 中国传统算命
        assertEquals(3, palmistry?.inputFields?.size) // 应该有3个输入字段
    }

    /**
     * 测试面相功能是否正确注册
     */
    @Test
    fun testFaceMethodRegistered() {
        val face = DivinationMethodProvider.getMethodById("face")
        
        assertNotNull("面相功能应该被注册", face)
        assertEquals("面相", face?.name)
        assertEquals("通过面部照片解读性格与运势", face?.description)
        assertEquals(1, face?.type) // 中国传统算命
        assertEquals(3, face?.inputFields?.size) // 应该有3个输入字段
    }

    /**
     * 测试手相输入字段配置
     */
    @Test
    fun testPalmistryInputFields() {
        val palmistry = DivinationMethodProvider.getMethodById("palmistry")
        assertNotNull(palmistry)
        
        val fields = palmistry!!.inputFields
        assertEquals(3, fields.size)
        
        // 检查手相照片字段
        val imageField = fields.find { it.id == "palmImage" }
        assertNotNull("应该有手相照片字段", imageField)
        assertEquals("手相照片", imageField?.name)
        assertEquals(5, imageField?.type) // 图片类型
        assertTrue("手相照片字段应该有提示文本", imageField?.hint?.isNotEmpty() == true)
        
        // 检查性别字段
        val genderField = fields.find { it.id == "gender" }
        assertNotNull("应该有性别字段", genderField)
        assertEquals("性别", genderField?.name)
        assertEquals(4, genderField?.type) // 选择类型
        assertEquals(2, genderField?.options?.size)
        assertTrue(genderField?.options?.contains("男") == true)
        assertTrue(genderField?.options?.contains("女") == true)
        
        // 检查出生日期字段
        val birthDateField = fields.find { it.id == "birthDate" }
        assertNotNull("应该有出生日期字段", birthDateField)
        assertEquals("出生日期", birthDateField?.name)
        assertEquals(2, birthDateField?.type) // 日期类型
    }

    /**
     * 测试面相输入字段配置
     */
    @Test
    fun testFaceInputFields() {
        val face = DivinationMethodProvider.getMethodById("face")
        assertNotNull(face)
        
        val fields = face!!.inputFields
        assertEquals(3, fields.size)
        
        // 检查面相照片字段
        val imageField = fields.find { it.id == "faceImage" }
        assertNotNull("应该有面相照片字段", imageField)
        assertEquals("面相照片", imageField?.name)
        assertEquals(5, imageField?.type) // 图片类型
        assertTrue("面相照片字段应该有提示文本", imageField?.hint?.isNotEmpty() == true)
        
        // 检查性别字段
        val genderField = fields.find { it.id == "gender" }
        assertNotNull("应该有性别字段", genderField)
        assertEquals("性别", genderField?.name)
        assertEquals(4, genderField?.type) // 选择类型
        
        // 检查出生日期字段
        val birthDateField = fields.find { it.id == "birthDate" }
        assertNotNull("应该有出生日期字段", birthDateField)
        assertEquals("出生日期", birthDateField?.name)
        assertEquals(2, birthDateField?.type) // 日期类型
    }

    /**
     * 测试手相面相在中国传统算命分类中
     */
    @Test
    fun testPalmistryAndFaceInChineseMethods() {
        val chineseMethods = DivinationMethodProvider.getChineseMethods()
        
        val palmistry = chineseMethods.find { it.id == "palmistry" }
        assertNotNull("手相应该在中国传统算命分类中", palmistry)
        
        val face = chineseMethods.find { it.id == "face" }
        assertNotNull("面相应该在中国传统算命分类中", face)
    }

    /**
     * 测试所有算命方法数量
     */
    @Test
    fun testTotalMethodsCount() {
        val allMethods = DivinationMethodProvider.getAllMethods()
        
        // 应该包含：八字、老黄历、紫微、周易、解梦、手相、面相（7个中国传统）
        // + 塔罗、占星、数字命理（3个西方传统）
        // + MBTI（1个心理测试）
        // 总共11个
        assertTrue("应该至少有11个算命方法", allMethods.size >= 11)
        
        val chineseMethods = DivinationMethodProvider.getChineseMethods()
        assertTrue("中国传统算命方法应该至少有7个", chineseMethods.size >= 7)
    }
}
