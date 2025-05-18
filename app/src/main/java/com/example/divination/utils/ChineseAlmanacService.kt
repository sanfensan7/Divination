package com.example.divination.utils

import com.example.divination.model.ChineseAlmanac
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 老黄历服务类
 * 用于计算和获取黄历信息
 */
object ChineseAlmanacService {

    // 天干
    private val TIAN_GAN = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    
    // 地支
    private val DI_ZHI = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
    
    // 生肖
    private val CHINESE_ZODIAC = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
    
    // 农历月份
    private val LUNAR_MONTHS = arrayOf("正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月")
    
    // 农历日期
    private val LUNAR_DAYS = arrayOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )
    
    // 宜做的事情
    private val GOOD_ACTIVITIES = arrayOf(
        "祭祀", "祈福", "求嗣", "开光", "嫁娶", "会亲友", "开市", "交易", "入学", "习艺",
        "纳财", "纳畜", "牧养", "安床", "动土", "上梁", "修造", "起基", "竖柱", "安门",
        "栽种", "纳采", "订盟", "冠笄", "裁衣", "合帐", "经络", "安葬", "修坟", "破土"
    )
    
    // 忌做的事情
    private val BAD_ACTIVITIES = arrayOf(
        "诉讼", "安葬", "修坟", "开市", "动土", "祭祀", "出行", "赴任", "嫁娶", "开张",
        "搬迁", "入宅", "安床", "交易", "栽种", "开仓", "纳财", "修造", "动工", "竖柱"
    )
    
    // 获取某天的老黄历信息
    fun getAlmanacForDate(date: Date): ChineseAlmanac {
        // 创建日历对象
        val calendar = Calendar.getInstance()
        calendar.time = date
        
        // 获取农历日期
        val lunarDate = calculateLunarDate(calendar)
        
        // 获取当天的宜忌事项，这里只是模拟数据
        val goodActivities = getRandomItems(GOOD_ACTIVITIES, 5)
        val badActivities = getRandomItems(BAD_ACTIVITIES, 5)
        
        // 计算方位吉凶
        val directions = mapOf(
            "东" to if (calendar.get(Calendar.DAY_OF_MONTH) % 2 == 0) "吉" else "凶",
            "南" to if (calendar.get(Calendar.DAY_OF_MONTH) % 3 == 0) "吉" else "凶",
            "西" to if (calendar.get(Calendar.DAY_OF_MONTH) % 4 == 0) "吉" else "凶",
            "北" to if (calendar.get(Calendar.DAY_OF_MONTH) % 5 == 0) "吉" else "凶",
            "东北" to if (calendar.get(Calendar.DAY_OF_MONTH) % 6 == 0) "吉" else "凶",
            "东南" to if (calendar.get(Calendar.DAY_OF_MONTH) % 7 == 0) "吉" else "凶",
            "西南" to if (calendar.get(Calendar.DAY_OF_MONTH) % 8 == 0) "吉" else "凶",
            "西北" to if (calendar.get(Calendar.DAY_OF_MONTH) % 9 == 0) "吉" else "凶"
        )
        
        // 计算时辰吉凶
        val hourLucks = mutableMapOf<String, Boolean>()
        for (i in 0 until 12) {
            val diZhi = DI_ZHI[i]
            hourLucks[diZhi] = (calendar.get(Calendar.DAY_OF_YEAR) + i) % 3 != 0
        }
        
        // 计算胎神方位
        val godOfBaby = calculateGodOfBaby(calendar)
        
        // 计算冲煞
        val chongSha = calculateChongSha(calendar)
        
        // 获取当天的值星和五行
        val yearStar = calculateYearStar(calendar)
        val dayStar = calculateDayStar(calendar)
        val fiveElements = calculateFiveElements(calendar)
        
        // 获取生肖
        val zodiac = CHINESE_ZODIAC[(calendar.get(Calendar.YEAR) - 4) % 12]
        
        return ChineseAlmanac(
            date = date,
            lunarDate = lunarDate,
            chineseZodiac = zodiac,
            goodActivities = goodActivities,
            badActivities = badActivities,
            direction = directions,
            godOfBaby = godOfBaby,
            chongSha = chongSha,
            yearStar = yearStar,
            dayStar = dayStar,
            fiveElements = fiveElements,
            hourLucks = hourLucks
        )
    }
    
    // 计算农历日期（简化版）
    private fun calculateLunarDate(calendar: Calendar): String {
        // 这里是简化实现，实际上需要根据天文历法进行精确计算
        // 在实际应用中，应该使用专业的农历计算库
        
        // 简化：使用阳历月份和日期来模拟
        val month = (calendar.get(Calendar.MONTH) + 10) % 12
        val day = (calendar.get(Calendar.DAY_OF_MONTH) - 1) % 30
        
        return "${LUNAR_MONTHS[month]}${LUNAR_DAYS[day]}"
    }
    
    // 计算胎神所在方位
    private fun calculateGodOfBaby(calendar: Calendar): String {
        val positions = arrayOf("床头", "床尾", "灶前", "厨房", "仓库", "房门", "东南", "西北")
        val index = (calendar.get(Calendar.DAY_OF_MONTH) - 1) % positions.size
        return positions[index]
    }
    
    // 计算冲煞
    private fun calculateChongSha(calendar: Calendar): String {
        val animals = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
        val index = (calendar.get(Calendar.DAY_OF_MONTH) - 1) % animals.size
        return "冲${animals[index]}煞"
    }
    
    // 计算值年星宿
    private fun calculateYearStar(calendar: Calendar): String {
        val stars = arrayOf("贪狼", "巨门", "禄存", "文曲", "廉贞", "武曲", "破军", "左辅", "右弼")
        val year = calendar.get(Calendar.YEAR)
        return stars[year % stars.size]
    }
    
    // 计算值日星宿
    private fun calculateDayStar(calendar: Calendar): String {
        val stars = arrayOf(
            "角木蛟", "亢金龙", "氐土貉", "房日兔", "心月狐", "尾火虎", "箕水豹",
            "斗木獬", "牛金牛", "女土蝠", "虚日鼠", "危月燕", "室火猪", "壁水貐",
            "奎木狼", "娄金狗", "胃土雉", "昴日鸡", "毕月乌", "觜火猴", "参水猿",
            "井木犴", "鬼金羊", "柳土獐", "星日马", "张月鹿", "翼火蛇", "轸水蚓"
        )
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        return stars[dayOfYear % stars.size]
    }
    
    // 计算五行
    private fun calculateFiveElements(calendar: Calendar): String {
        val elements = arrayOf("金", "木", "水", "火", "土")
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        return elements[dayOfYear % elements.size]
    }
    
    // 从数组中随机获取指定数量的元素
    private fun getRandomItems(items: Array<String>, count: Int): List<String> {
        val result = mutableListOf<String>()
        val indices = (items.indices).shuffled().take(count)
        for (index in indices) {
            result.add(items[index])
        }
        return result
    }
    
    // 将公历日期格式化为字符串
    fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
        return sdf.format(date)
    }
} 