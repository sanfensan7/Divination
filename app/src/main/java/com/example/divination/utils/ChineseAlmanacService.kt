package com.example.divination.utils

import com.example.divination.model.ChineseAlmanac
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

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
    
    /**
     * 1900-2099 年农历数据
     * 每个条目包含闰月信息以及每个月是大月（30天）还是小月（29天）
     */
    private val LUNAR_INFO = longArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x05ac0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
        0x14b63
    )
    
    private val BASE_CALENDAR = Calendar.getInstance().apply {
        set(1900, Calendar.JANUARY, 31, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    
    // 扩展的宜忌活动列表
    private val GOOD_ACTIVITIES = arrayOf(
        "祭祀", "祈福", "求嗣", "开光", "嫁娶", "会亲友", "开市", "交易", "入学", "习艺",
        "纳财", "纳畜", "牧养", "安床", "动土", "上梁", "修造", "起基", "竖柱", "安门",
        "栽种", "纳采", "订盟", "冠笄", "裁衣", "合帐", "经络", "安葬", "修坟", "破土",
        "结网", "畋猎", "余事勿取", "馀事勿取", "捕捉", "畋猎", "取鱼", "乘船", "穿井", "扫舍",
        "结婚", "开业", "开张", "出行", "旅游", "搬家", "装修", "入宅", "移徙", "盖屋",
        "谢土", "启钻", "破土", "安葬", "立碑", "成服", "除服", "开仓", "纳畜", "牧养"
    )
    
    private val BAD_ACTIVITIES = arrayOf(
        "诉讼", "安葬", "修坟", "开市", "动土", "祭祀", "出行", "赴任", "嫁娶", "开张",
        "搬迁", "入宅", "安床", "交易", "栽种", "开仓", "纳财", "修造", "动工", "竖柱",
        "破土", "伐木", "捕捉", "畋猎", "取鱼", "乘船", "穿井", "掘井", "开池", "扫舍",
        "造船", "作灶", "掘井", "乘船", "行船", "针灸", "出师", "上官", "栽种", "求医",
        "治病", "词讼", "起基", "动土", "破土", "安门", "开仓", "开渠", "筑堤", "放水",
        "穿井", "造桥", "造命", "修造", "竖柱", "盖屋", "移柩", "合寿木", "入木", "启攒"
    )
    
    // 每日吉凶指数集合
    private val LUCK_LEVEL = arrayOf("大吉", "吉", "中吉", "小吉", "平", "小凶", "凶", "大凶")
    
    // 星宿集合
    private val STARS = arrayOf(
        "角木蛟", "亢金龙", "氐土貉", "房日兔", "心月狐", "尾火虎", "箕水豹",
        "斗木獬", "牛金牛", "女土蝠", "虚日鼠", "危月燕", "室火猪", "壁水貐",
        "奎木狼", "娄金狗", "胃土雉", "昴日鸡", "毕月乌", "觜火猴", "参水猿",
        "井木犴", "鬼金羊", "柳土獐", "星日马", "张月鹿", "翼火蛇", "轸水蚓"
    )
    
    // 胎神方位
    private val THAI_POSITIONS = arrayOf(
        "占门碓外东北", "占门床外正东", "占炉灶外正南", "占厨灶外西南",
        "占仓库外正西", "占厕外西北", "占房内北", "占灶外房内南",
        "占门鱼池外东南", "占碓磨外正南", "占厕灶外正北", "占北床外西北"
    )
    
    /**
     * 全新实现：获取某天的老黄历信息
     */
    fun getAlmanacForDate(date: Date): ChineseAlmanac {
        // 创建日历对象
        val calendar = Calendar.getInstance()
        calendar.time = date
        
        // 获取基本日期信息
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1  // 月份从0开始，需要+1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        // 创建日期哈希值 - 每天都不同
        val dateHash = generateDateHash(year, month, day, dayOfYear)
        
        // 计算农历日期
        val lunarDate = calculateLunarDateNew(calendar)
        
        // 获取生肖
        val zodiacIndex = (year - 4) % 12
        val zodiac = CHINESE_ZODIAC[zodiacIndex]
        
        // 获取干支日
        val tianganIndex = (dayOfYear + year) % 10
        val dizhiIndex = (dayOfYear + year) % 12
        val dayGanZhi = TIAN_GAN[tianganIndex] + DI_ZHI[dizhiIndex]
        
        // 获取宜忌事项 - 每天不同，但同一天相同
        val goodCount = 3 + (dateHash % 3) // 3-5个宜项
        val badCount = 3 + ((dateHash / 10) % 3) // 3-5个忌项
        val goodActivities = getFixedRandomItems(GOOD_ACTIVITIES, goodCount, dateHash)
        val badActivities = getFixedRandomItems(BAD_ACTIVITIES, badCount, dateHash + 123456)
        
        // 确保宜忌不重复
        val badFiltered = badActivities.filter { it !in goodActivities }
        
        // 计算八方吉凶
        val directions = calculateDirections(dateHash)
        
        // 计算时辰吉凶
        val hourLucks = calculateHourLucks(dateHash, dayGanZhi)
        
        // 计算胎神方位
        val godOfBaby = THAI_POSITIONS[(dateHash % THAI_POSITIONS.size)]
        
        // 计算冲煞
        val chongSha = calculateChongSha(dateHash, zodiacIndex)
        
        // 计算值年星宿
        val yearStar = calculateYearStar(year, month)
        val dayStar = STARS[(dateHash % STARS.size)]
        
        // 计算五行
        val fiveElements = calculateFiveElements(tianganIndex, dizhiIndex)
        
        // 返回今日黄历
        return ChineseAlmanac(
            date = date,
            lunarDate = lunarDate,
            chineseZodiac = zodiac,
            goodActivities = goodActivities,
            badActivities = badFiltered,
            direction = directions,
            godOfBaby = godOfBaby,
            chongSha = chongSha,
            yearStar = yearStar,
            dayStar = dayStar,
            fiveElements = fiveElements,
            hourLucks = hourLucks
        )
    }
    
    /**
     * 生成日期哈希值 - 确保每天都有唯一值且分布均匀
     */
    private fun generateDateHash(year: Int, month: Int, day: Int, dayOfYear: Int): Int {
        // 使用多个质数和不同位运算确保分布
        val baseHash = ((year * 10007) + (month * 1009) + (day * 101) + dayOfYear)
        val rotated = ((baseHash shl 5) or (baseHash ushr 27)) and 0x7FFFFFFF
        return abs(rotated)
    }
    
    /**
     * 全新实现：计算农历日期
     */
    private fun calculateLunarDateNew(calendar: Calendar): String {
        val normalizedCalendar = calendar.clone() as Calendar
        normalizedCalendar.set(Calendar.HOUR_OF_DAY, 0)
        normalizedCalendar.set(Calendar.MINUTE, 0)
        normalizedCalendar.set(Calendar.SECOND, 0)
        normalizedCalendar.set(Calendar.MILLISECOND, 0)
        
        var offset = ((normalizedCalendar.timeInMillis - BASE_CALENDAR.timeInMillis) / 86400000L).toInt()
        if (offset < 0) offset = 0
        
        var lunarYear = 1900
        var daysInYear = yearDays(lunarYear)
        while (offset >= daysInYear && lunarYear < 2100) {
            offset -= daysInYear
            lunarYear++
            daysInYear = yearDays(lunarYear)
        }
        
        val leapMonth = leapMonth(lunarYear)
        var isLeapMonth = false
        var lunarMonth = 1
        var daysInMonth: Int
        
        while (true) {
            daysInMonth = if (isLeapMonth) {
                leapDays(lunarYear)
            } else {
                monthDays(lunarYear, lunarMonth)
            }
            
            if (offset < daysInMonth) {
                break
            }
            
            offset -= daysInMonth
            
            if (leapMonth > 0 && lunarMonth == leapMonth && !isLeapMonth) {
                isLeapMonth = true
            } else {
                if (isLeapMonth && lunarMonth == leapMonth) {
                    isLeapMonth = false
                }
                lunarMonth++
            }
        }
        
        val lunarDay = offset + 1
        val monthName = buildString {
            if (isLeapMonth && leapMonth != 0) {
                append("闰")
            }
            append(LUNAR_MONTHS[(lunarMonth - 1).coerceIn(LUNAR_MONTHS.indices)])
        }
        val dayName = LUNAR_DAYS[(lunarDay - 1).coerceIn(LUNAR_DAYS.indices)]
        
        return "$monthName$dayName"
    }
    
    private fun yearDays(year: Int): Int {
        var sum = 348
        var info = LUNAR_INFO.getOrNull(year - 1900)?.toInt() ?: return 0
        var mask = 0x8000
        while (mask > 0x8) {
            sum += if ((info and mask) != 0) 1 else 0
            mask = mask shr 1
        }
        return sum + leapDays(year)
    }
    
    private fun monthDays(year: Int, month: Int): Int {
        val info = LUNAR_INFO.getOrNull(year - 1900) ?: return 29
        val mask = 0x10000 shr month
        return if ((info and mask.toLong()) == 0L) 29 else 30
    }
    
    private fun leapMonth(year: Int): Int {
        val info = LUNAR_INFO.getOrNull(year - 1900) ?: return 0
        return (info and 0xF).toInt()
    }
    
    private fun leapDays(year: Int): Int {
        val leapMonth = leapMonth(year)
        if (leapMonth == 0) return 0
        val info = LUNAR_INFO[year - 1900]
        return if ((info and 0x10000) != 0L) 30 else 29
    }
    
    /**
     * 固定随机：基于种子确定性地获取数组的随机元素
     */
    private fun getFixedRandomItems(items: Array<String>, count: Int, seed: Int): List<String> {
        // 使用线性同余法生成确定性伪随机序列
        val result = mutableListOf<String>()
        val used = mutableSetOf<Int>()
        var currentSeed = seed
        
        while (result.size < count && result.size < items.size) {
            // 基于线性同余法生成下一个随机数
            currentSeed = (currentSeed * 48271 + 12345) % Int.MAX_VALUE
            val index = abs(currentSeed) % items.size
            
            // 确保不重复
            if (index !in used) {
                used.add(index)
                result.add(items[index])
            }
        }
        
        return result
    }
    
    /**
     * 计算八方吉凶
     */
    private fun calculateDirections(seed: Int): Map<String, String> {
        val directions = arrayOf("东", "南", "西", "北", "东北", "东南", "西南", "西北")
        val result = mutableMapOf<String, String>()
        
        // 为每个方位使用不同的种子变种
        directions.forEachIndexed { index, direction ->
            // 使用不同的算法确定吉凶
            val dirSeed = seed + (index * 103) // 使用质数偏移
            val luckValue = when {
                (dirSeed % 9 == 0) -> "大吉"
                (dirSeed % 7 == 0) -> "吉"
                (dirSeed % 5 == 0) -> "中吉"
                (dirSeed % 3 == 0) -> "小吉"
                (dirSeed % 11 == 0) -> "大凶"
                (dirSeed % 13 == 0) -> "凶"
                (dirSeed % 17 == 0) -> "小凶"
                else -> "平"
            }
            
            // 简化为吉/凶显示
            result[direction] = if (luckValue.contains("吉")) "吉" else "凶"
        }
        
        return result
    }
    
    /**
     * 计算时辰吉凶
     */
    private fun calculateHourLucks(seed: Int, dayGanZhi: String): Map<String, Boolean> {
        val result = mutableMapOf<String, Boolean>()
        
        // 用天干地支的索引生成基础数字
        val baseValue = dayGanZhi.hashCode()
        
        // 为每个时辰生成吉凶
        for (i in 0 until 12) {
            val hourSeed = seed + baseValue + (i * 157) // 使用质数偏移
            
            // 复杂的判断规则，确保分布更随机
            val isGood = when {
                (hourSeed % 19 == 1) -> false
                (hourSeed % 13 == 2) -> false
                (hourSeed % 11 == 3) -> false
                (hourSeed % 7 == 4) -> false
                (hourSeed % 5 == 0) -> false
                else -> true
            }
            
            result[DI_ZHI[i]] = isGood
        }
        
        return result
    }
    
    /**
     * 计算冲煞
     */
    private fun calculateChongSha(seed: Int, zodiacIndex: Int): String {
        // 以本命生肖为基础，计算所冲生肖
        // 地支六冲：子午相冲、丑未相冲、寅申相冲、辰戌相冲、卯酉相冲、巳亥相冲
        val chongIndex = (zodiacIndex + 6) % 12
        
        // 添加不同的煞类型，基于种子
        val shaTypes = arrayOf("岁煞", "月煞", "日煞", "时煞", "天煞", "地煞", "年煞")
        val shaTypeIndex = seed % shaTypes.size
        
        return "冲${CHINESE_ZODIAC[chongIndex]}${shaTypes[shaTypeIndex]}"
    }
    
    /**
     * 计算值年星宿
     */
    private fun calculateYearStar(year: Int, month: Int): String {
        val yearStars = arrayOf("贪狼", "巨门", "禄存", "文曲", "廉贞", "武曲", "破军", "左辅", "右弼")
        // 基于年月计算星宿
        val index = ((year % 9) * 12 + month) % yearStars.size
        return yearStars[index]
    }
    
    /**
     * 基于天干地支计算五行
     */
    private fun calculateFiveElements(tianganIndex: Int, dizhiIndex: Int): String {
        // 天干五行：甲乙木、丙丁火、戊己土、庚辛金、壬癸水
        // 地支五行：寅卯木、巳午火、辰戌丑未土、申酉金、亥子水
        val tianganWuxing = arrayOf("木", "木", "火", "火", "土", "土", "金", "金", "水", "水")
        val dizhiWuxing = arrayOf("水", "土", "木", "木", "土", "火", "火", "土", "金", "金", "土", "水")
        
        // 综合天干地支的五行
        val tianganElement = tianganWuxing[tianganIndex]
        val dizhiElement = dizhiWuxing[dizhiIndex]
        
        // 如果天干地支五行相同，则强化；否则取天干五行
        return if (tianganElement == dizhiElement) {
            "$tianganElement"
        } else {
            tianganElement
        }
    }
    
    // 将公历日期格式化为字符串
    fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
        return sdf.format(date)
    }
} 