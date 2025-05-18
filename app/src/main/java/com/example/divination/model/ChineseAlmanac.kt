package com.example.divination.model

import java.util.Date

/**
 * 老黄历数据模型
 *
 * @property date 日期
 * @property lunarDate 农历日期
 * @property chineseZodiac 生肖
 * @property goodActivities 宜做的事情
 * @property badActivities 忌做的事情
 * @property direction 吉凶方位
 * @property godOfBaby 胎神所在
 * @property chongSha 冲煞
 * @property yearStar 值年星宿
 * @property dayStar 值日星宿
 * @property fiveElements 五行
 * @property hourLucks 时辰吉凶
 */
data class ChineseAlmanac(
    val date: Date,
    val lunarDate: String,
    val chineseZodiac: String,
    val goodActivities: List<String>,
    val badActivities: List<String>,
    val direction: Map<String, String>,
    val godOfBaby: String,
    val chongSha: String,
    val yearStar: String,
    val dayStar: String,
    val fiveElements: String,
    val hourLucks: Map<String, Boolean>
)

/**
 * 黄历宜忌项目
 */
data class AlmanacActivity(
    val name: String,
    val description: String
) 