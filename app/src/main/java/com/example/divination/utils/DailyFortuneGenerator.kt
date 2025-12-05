package com.example.divination.utils

import com.example.divination.model.DailyFortune
import com.example.divination.model.UserProfile
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

/**
 * 每日运势生成器
 */
object DailyFortuneGenerator {
    
    /**
     * 生成今日运势
     */
    fun generateDailyFortune(userProfile: UserProfile?): DailyFortune {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        // 使用日期作为随机种子，确保同一天生成相同的运势
        val seed = today.hashCode().toLong()
        val random = Random(seed + (userProfile?.name?.hashCode() ?: 0))
        
        // 生成各项运势（1-5星）
        val overallLuck = random.nextInt(1, 6)
        val loveLuck = random.nextInt(1, 6)
        val careerLuck = random.nextInt(1, 6)
        val wealthLuck = random.nextInt(1, 6)
        val healthLuck = random.nextInt(1, 6)
        
        // 生成幸运元素
        val luckyColors = listOf("红色", "橙色", "黄色", "绿色", "蓝色", "紫色", "粉色", "白色", "金色", "银色")
        val luckyColor = luckyColors[random.nextInt(luckyColors.size)]
        
        val luckyNumber = random.nextInt(1, 100)
        
        val directions = listOf("东方", "南方", "西方", "北方", "东南", "西南", "东北", "西北")
        val luckyDirection = directions[random.nextInt(directions.size)]
        
        // 生成吉时和凶时
        val hours = (0..23).toList()
        val luckyHour = hours[random.nextInt(hours.size)]
        val avoidHour = hours.filter { it != luckyHour }[random.nextInt(hours.size - 1)]
        
        val luckyTime = "${luckyHour}:00-${(luckyHour + 1) % 24}:00"
        val avoidTime = "${avoidHour}:00-${(avoidHour + 1) % 24}:00"
        
        // 生成警告和建议
        val warnings = generateWarnings(overallLuck, random)
        val suggestions = generateSuggestions(overallLuck, userProfile, random)
        
        // 生成运势总结
        val summary = generateSummary(overallLuck, userProfile)
        
        return DailyFortune(
            date = today,
            overallLuck = overallLuck,
            loveLuck = loveLuck,
            careerLuck = careerLuck,
            wealthLuck = wealthLuck,
            healthLuck = healthLuck,
            luckyColor = luckyColor,
            luckyNumber = luckyNumber,
            luckyDirection = luckyDirection,
            warnings = warnings,
            suggestions = suggestions,
            luckyTime = luckyTime,
            avoidTime = avoidTime,
            summary = summary
        )
    }
    
    /**
     * 生成警告信息
     */
    private fun generateWarnings(overallLuck: Int, random: Random): List<String> {
        val allWarnings = listOf(
            "⚠️ 今日易与人发生口角，注意控制情绪",
            "⚠️ 出行需谨慎，注意交通安全",
            "⚠️ 财务方面需谨慎，避免冲动消费",
            "⚠️ 健康方面需注意，避免过度劳累",
            "⚠️ 工作中可能遇到小人，需提高警惕",
            "⚠️ 感情方面易产生误会，需加强沟通",
            "⚠️ 今日不宜做重大决策，宜观望",
            "⚠️ 注意饮食卫生，避免肠胃不适",
            "⚠️ 贵重物品需妥善保管，防止遗失",
            "⚠️ 避免与他人发生经济纠纷",
            "⚠️ 工作中容易出错，需仔细检查",
            "⚠️ 今日不宜签署重要合同"
        )
        
        val warningCount = when (overallLuck) {
            1 -> 3  // 凶：3个警告
            2 -> 2  // 小凶：2个警告
            3 -> 1  // 平：1个警告
            else -> 0  // 吉/大吉：无警告
        }
        
        if (warningCount == 0) {
            return listOf("✨ 今日运势良好，无需特别注意")
        }
        
        return allWarnings.shuffled(random).take(warningCount)
    }
    
    /**
     * 生成建议信息
     */
    private fun generateSuggestions(overallLuck: Int, userProfile: UserProfile?, random: Random): List<String> {
        val allSuggestions = listOf(
            "💡 保持积极乐观的心态，好运自然来",
            "💡 多与贵人交流，会有意外收获",
            "💡 适当运动，保持身心健康",
            "💡 学习新知识，提升自我能力",
            "💡 关心家人朋友，增进感情",
            "💡 整理工作计划，提高效率",
            "💡 适当放松休息，劳逸结合",
            "💡 主动出击，把握机会",
            "💡 保持低调谦虚，避免锋芒毕露",
            "💡 多做善事，积累福报",
            "💡 注意细节，成功在于细微之处",
            "💡 保持耐心，好事多磨",
            "💡 勇于尝试，突破舒适区",
            "💡 倾听他人意见，集思广益"
        )
        
        val suggestionCount = when (overallLuck) {
            5 -> 4  // 大吉：4个建议
            4 -> 3  // 吉：3个建议
            else -> 2  // 其他：2个建议
        }
        
        val suggestions = allSuggestions.shuffled(random).take(suggestionCount).toMutableList()
        
        // 根据用户星座添加特定建议
        if (userProfile?.zodiacSign?.isNotEmpty() == true) {
            suggestions.add("💡 ${userProfile.zodiacSign}今日宜多与水相关的活动")
        }
        
        return suggestions
    }
    
    /**
     * 生成运势总结
     */
    private fun generateSummary(overallLuck: Int, userProfile: UserProfile?): String {
        val name = userProfile?.getDisplayName() ?: "您"
        
        return when (overallLuck) {
            5 -> "${name}，今日运势极佳！诸事顺利，把握机会，大胆行动，必有收获。贵人相助，事半功倍。"
            4 -> "${name}，今日运势良好。保持积极心态，稳步前进，会有不错的进展。适合处理重要事务。"
            3 -> "${name}，今日运势平稳。按部就班即可，不宜冒进。保持平常心，顺其自然为上策。"
            2 -> "${name}，今日运势欠佳。需谨慎行事，避免冲动决策。多听取他人意见，低调为宜。"
            1 -> "${name}，今日运势不利。宜静不宜动，避免重大决策和行动。保持耐心，等待时机。"
            else -> "${name}，今日运势未知。"
        }
    }
}
