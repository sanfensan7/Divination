package com.example.divination.utils

import com.example.divination.R
import com.example.divination.model.DivinationMethod
import com.example.divination.model.InputField

/**
 * 算命方法提供者工具类
 */
object DivinationMethodProvider {
    
    // 算命方法列表缓存
    private val methods = mutableListOf<DivinationMethod>()
    
    /**
     * 获取所有算命方法
     */
    fun getAllMethods(): List<DivinationMethod> {
        if (methods.isEmpty()) {
            initMethods()
        }
        return methods
    }
    
    /**
     * 获取中国传统算命方法
     */
    fun getChineseMethods(): List<DivinationMethod> {
        return getAllMethods().filter { it.type == 1 }
    }
    
    /**
     * 获取西方传统算命方法
     */
    fun getWesternMethods(): List<DivinationMethod> {
        return getAllMethods().filter { it.type == 2 }
    }
    
    /**
     * 根据ID获取算命方法
     */
    fun getMethodById(id: String): DivinationMethod? {
        return getAllMethods().find { it.id == id }
    }
    
    /**
     * 初始化算命方法列表
     */
    private fun initMethods() {
        // 清空列表
        methods.clear()
        
        // 中国传统算命方法
        methods.add(
            DivinationMethod(
                id = "bazi",
                name = "八字命理",
                description = "根据出生年月日时推算命运",
                iconResId = R.drawable.ic_bazi,
                type = 1,
                inputFields = listOf(
                    InputField(
                        id = "birthDate",
                        name = "出生日期",
                        type = 2
                    ),
                    InputField(
                        id = "birthTime",
                        name = "出生时间",
                        type = 3
                    ),
                    InputField(
                        id = "gender",
                        name = "性别",
                        type = 4,
                        options = listOf("男", "女")
                    )
                )
            )
        )
        
        // 添加老黄历功能
        methods.add(
            DivinationMethod(
                id = "almanac",
                name = "老黄历",
                description = "查询每日宜忌、吉凶方位和时辰信息",
                iconResId = R.drawable.ic_huang_li,
                type = 1,
                inputFields = listOf(
                    InputField(
                        id = "date",
                        name = "查询日期",
                        type = 2
                    )
                )
            )
        )
        
        methods.add(
            DivinationMethod(
                id = "ziwei",
                name = "紫微斗数",
                description = "通过星盘分析人生运势",
                iconResId = R.drawable.ic_ziwei,
                type = 1,
                inputFields = listOf(
                    InputField(
                        id = "birthDate",
                        name = "出生日期(阳历)",
                        type = 2
                    ),
                    InputField(
                        id = "birthTime",
                        name = "出生时辰",
                        type = 4,
                        options = listOf("子时(23:00-01:00)", "丑时(01:00-03:00)", 
                                        "寅时(03:00-05:00)", "卯时(05:00-07:00)",
                                        "辰时(07:00-09:00)", "巳时(09:00-11:00)",
                                        "午时(11:00-13:00)", "未时(13:00-15:00)",
                                        "申时(15:00-17:00)", "酉时(17:00-19:00)",
                                        "戌时(19:00-21:00)", "亥时(21:00-23:00)")
                    ),
                    InputField(
                        id = "gender",
                        name = "性别",
                        type = 4,
                        options = listOf("男", "女")
                    )
                )
            )
        )
        
        methods.add(
            DivinationMethod(
                id = "zhouyi",
                name = "周易卦象",
                description = "易经六十四卦预测",
                iconResId = R.drawable.ic_zhouyi,
                type = 1,
                inputFields = listOf(
                    InputField(
                        id = "question",
                        name = "预测问题",
                        type = 1
                    )
                )
            )
        )
        
        // 添加周公解梦功能
        methods.add(
            DivinationMethod(
                id = "dream",
                name = "周公解梦",
                description = "解析梦境寓意与预示",
                iconResId = R.drawable.ic_dream,
                type = 1,
                inputFields = listOf(
                    InputField(
                        id = "dreamContent",
                        name = "梦境内容",
                        type = 1
                    )
                )
            )
        )
        
        // 西方传统算命方法
        methods.add(
            DivinationMethod(
                id = "tarot",
                name = "塔罗牌",
                description = "通过塔罗牌阵解读命运",
                iconResId = R.drawable.ic_tarot,
                type = 2,
                inputFields = listOf(
                    InputField(
                        id = "question",
                        name = "咨询问题",
                        type = 1
                    )
                )
            )
        )
        
        methods.add(
            DivinationMethod(
                id = "astrology",
                name = "占星学",
                description = "星盘解读与行星运势",
                iconResId = R.drawable.ic_astrology,
                type = 2,
                inputFields = listOf(
                    InputField(
                        id = "birthDate",
                        name = "出生日期",
                        type = 2
                    ),
                    InputField(
                        id = "birthTime",
                        name = "出生时间",
                        type = 3
                    ),
                    InputField(
                        id = "birthPlace",
                        name = "出生地点",
                        type = 1
                    )
                )
            )
        )
        
        methods.add(
            DivinationMethod(
                id = "numerology",
                name = "数字命理学",
                description = "通过数字揭示命运密码",
                iconResId = R.drawable.ic_numerology,
                type = 2,
                inputFields = listOf(
                    InputField(
                        id = "fullName",
                        name = "全名",
                        type = 1
                    ),
                    InputField(
                        id = "birthDate",
                        name = "出生日期",
                        type = 2
                    )
                )
            )
        )
        
        // MBTI人格测试
        methods.add(
            DivinationMethod(
                id = "mbti",
                name = "MBTI人格测试",
                description = "通过60道专业问题分析你的16型人格",
                iconResId = R.drawable.ic_mbti,
                type = 3, // 3代表心理测试分类
                inputFields = emptyList() // MBTI不需要输入字段，直接进入测试
            )
        )
    }
} 