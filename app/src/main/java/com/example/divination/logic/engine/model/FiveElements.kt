package com.example.divination.logic.engine.model

/**
 * 五行分析结果
 * 
 * 统计八字中的五行分布，判断旺衰、缺失等
 */
data class FiveElements(
    // === 天干五行统计 ===
    val tianganCount: Map<String, Int>,    // {"金":1, "木":0, "水":2, "火":0, "土":1}
    
    // === 地支五行统计（本气） ===
    val dizhiCount: Map<String, Int>,      // 地支本气统计
    
    // === 地支藏干统计（考虑余气、中气） ===
    val zhiHiddenCount: Map<String, Int>,  // 藏干综合统计
    
    // === 总体统计 ===
    val totalCount: Map<String, Int>,      // 综合统计（天干 + 地支本气）
    
    // === 分析结果 ===
    val strongest: String,                 // 最旺的五行
    val weakest: String,                   // 最弱的五行
    val missing: List<String>,             // 缺失的五行
    val excessive: List<String>,           // 过旺的五行（数量>=3）
    
    // === 季节旺衰（根据月令） ===
    val seasonalStrength: String,          // "金旺木相水休火囚土死"（根据月令推导）
    
    // === 格式化摘要 ===
    val tianganSummary: String,            // "金1 水2 土1"
    val dizhiSummary: String,              // "木1 火2 土1"
    val totalSummary: String               // "金2 木1 水3 火2 土2（水旺，缺木）"
) {
    companion object {
        private val ELEMENTS = listOf("金", "木", "水", "火", "土")
        
        /**
         * 根据统计结果创建五行分析
         */
        fun from(
            tianganCount: Map<String, Int>,
            dizhiCount: Map<String, Int>,
            zhiHiddenCount: Map<String, Int>,
            monthElement: String
        ): FiveElements {
            // 计算总体统计（天干权重1.0 + 地支本气权重1.0）
            val totalCount = ELEMENTS.associateWith { element ->
                (tianganCount[element] ?: 0) + (dizhiCount[element] ?: 0)
            }
            
            // 找出最强和最弱
            val maxCount = totalCount.values.maxOrNull() ?: 0
            val minCount = totalCount.values.filter { it > 0 }.minOrNull() ?: 0
            
            val strongest = totalCount.filter { it.value == maxCount }.keys.firstOrNull() ?: "无"
            val weakest = totalCount.filter { it.value == minCount && it.value > 0 }.keys.firstOrNull() ?: "无"
            
            // 找出缺失和过旺
            val missing = ELEMENTS.filter { (totalCount[it] ?: 0) == 0 }
            val excessive = totalCount.filter { it.value >= 3 }.keys.toList()
            
            // 生成摘要
            val tianganSummary = ELEMENTS
                .filter { (tianganCount[it] ?: 0) > 0 }
                .joinToString(" ") { "$it${tianganCount[it]}" }
                .ifEmpty { "无" }
            
            val dizhiSummary = ELEMENTS
                .filter { (dizhiCount[it] ?: 0) > 0 }
                .joinToString(" ") { "$it${dizhiCount[it]}" }
                .ifEmpty { "无" }
            
            val totalSummary = buildString {
                ELEMENTS.forEach { element ->
                    val count = totalCount[element] ?: 0
                    if (count > 0) {
                        if (isNotEmpty()) append(" ")
                        append("$element$count")
                    }
                }
                if (missing.isNotEmpty()) {
                    append("（缺${missing.joinToString("、")}）")
                }
                if (excessive.isNotEmpty()) {
                    append("（${excessive.joinToString("、")}旺）")
                }
            }
            
            // 根据月令五行推导旺相休囚死
            val seasonalStrength = getSeasonalStrength(monthElement)
            
            return FiveElements(
                tianganCount = tianganCount,
                dizhiCount = dizhiCount,
                zhiHiddenCount = zhiHiddenCount,
                totalCount = totalCount,
                strongest = strongest,
                weakest = weakest,
                missing = missing,
                excessive = excessive,
                seasonalStrength = seasonalStrength,
                tianganSummary = tianganSummary,
                dizhiSummary = dizhiSummary,
                totalSummary = totalSummary
            )
        }
        
        /**
         * 根据月令五行推导旺相休囚死
         * 旺：当令者
         * 相：令生者
         * 休：生令者
         * 囚：令克者
         * 死：克令者
         */
        private fun getSeasonalStrength(monthElement: String): String {
            return when (monthElement) {
                "木" -> "木旺 火相 水休 金囚 土死"
                "火" -> "火旺 土相 木休 水囚 金死"
                "土" -> "土旺 金相 火休 木囚 水死"
                "金" -> "金旺 水相 土休 火囚 木死"
                "水" -> "水旺 木相 金休 土囚 火死"
                else -> "未知"
            }
        }
    }
}
