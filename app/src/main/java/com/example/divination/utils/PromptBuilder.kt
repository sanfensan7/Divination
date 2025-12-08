package com.example.divination.utils

import com.example.divination.logic.engine.model.BaziProfile

/**
 * AI 提示词构建器
 * 
 * 核心理念：
 * - 不再让 AI 计算八字，而是把本地计算好的硬数据投喂给 AI
 * - AI 的职责仅限于：基于准确数据做专业解读、生成润色文本
 * - System Prompt 包含完整的排盘数据，User Prompt 包含用户问题
 * 
 * @author 神机妙算团队
 */
object PromptBuilder {
    
    /**
     * 构建八字分析的完整提示词
     * 
     * @param profile 本地算法计算出的八字数据
     * @param question 用户的具体问题（如"今年事业运如何？"）
     * @param analysisType 分析类型："综合" / "事业" / "财运" / "感情" / "健康" / "流年"
     * @return 格式化的提示词（适合发送给 DeepSeek）
     */
    fun buildBaziAnalysisPrompt(
        profile: BaziProfile,
        question: String,
        analysisType: String = "综合"
    ): Pair<String, String> {
        val systemPrompt = buildBaziSystemPrompt(profile, analysisType)
        val userPrompt = buildBaziUserPrompt(question, analysisType)
        
        return Pair(systemPrompt, userPrompt)
    }
    
    /**
     * 构建 System Prompt（定义 AI 角色 + 投喂硬数据）
     */
    private fun buildBaziSystemPrompt(profile: BaziProfile, analysisType: String): String {
        return """
你是一位拥有30年实战经验的八字命理大师，精通子平真诠、滴天髓、穷通宝鉴等经典命理著作。

【重要约束】
1. 以下排盘数据由专业算法精确计算而成，请直接引用，**不要自己重新排盘或推算干支**
2. 真太阳时已根据经纬度校正，确保时辰准确无误
3. 五行统计、十神配置、格局判断均已完成，请基于这些数据进行深度解读
4. 分析要专业、客观，避免模糊笼统的套话，多用命理术语和具体论断

${profile.toPromptString()}

【分析重点】
根据用户提问，聚焦于"$analysisType"方面的解读。结合四柱配置、大运流年、用神喜忌，给出专业且有深度的分析。

【输出风格】
- 语言专业但不晦涩，适当解释命理术语
- 结论明确，避免"可能""也许"等模糊表述
- 提供具体的趋势判断和实用建议
- 保持东方玄学的神秘感和权威性
        """.trimIndent()
    }
    
    /**
     * 构建 User Prompt（用户问题）
     */
    private fun buildBaziUserPrompt(question: String, analysisType: String): String {
        return if (question.isNotBlank()) {
            question
        } else {
            val currentYear = java.time.LocalDateTime.now().year
            when (analysisType) {
                "事业" -> "请基于我的八字，分析我的事业运势、适合的职业方向、发展建议。"
                "财运" -> "请分析我的财运状况，包括财富累积能力、投资理财建议、财运高峰期。"
                "感情" -> "请分析我的感情婚姻运势，包括桃花运、配偶特征、婚姻稳定性。"
                "健康" -> "请基于八字分析我的健康状况，容易出现的疾病隐患，以及养生建议。"
                "流年" -> "请分析我今年（${currentYear}年）的整体运势，包括事业、财运、感情、健康等方面。"
                else -> "请对我的八字进行全面分析，包括性格特点、命运走势、用神喜忌、人生建议等。"
            }
        }
    }
    
    /**
     * 构建紫微斗数的提示词（占位，待实现）
     */
    fun buildZiweiAnalysisPrompt(
        // TODO: 紫微斗数数据结构
        question: String
    ): Pair<String, String> {
        val systemPrompt = """
你是一位精通紫微斗数的命理专家。
[紫微排盘数据将在此处插入]

请基于以上排盘数据进行专业解读，不要自己重新排盘。
        """.trimIndent()
        
        return Pair(systemPrompt, question)
    }
    
    /**
     * 构建周易六爻的提示词（保留原有逻辑）
     */
    fun buildZhouyiPrompt(
        question: String,
        hexagramData: Map<String, String>
    ): Pair<String, String> {
        val systemPrompt = """
你是一位精通周易六爻的卦象大师。

【卦象数据】
主卦：${hexagramData["mainHexagram"]}
变卦：${hexagramData["changedHexagram"]}
动爻：${hexagramData["changingLines"]}

请基于卦象进行解读，结合问题给出专业的分析。
        """.trimIndent()
        
        return Pair(systemPrompt, question)
    }
    
    /**
     * 构建通用算命提示词（用于其他算命方式）
     */
    fun buildGeneralPrompt(
        methodName: String,
        inputData: Map<String, String>,
        question: String
    ): Pair<String, String> {
        val systemPrompt = """
你是一位精通${methodName}的专业大师。

【用户信息】
${inputData.entries.joinToString("\n") { "${it.key}：${it.value}" }}

请基于以上信息进行专业解读。
        """.trimIndent()
        
        return Pair(systemPrompt, question)
    }
    
    /**
     * 格式化最终发送给 API 的完整提示词
     * 
     * @param systemPrompt System 角色提示词
     * @param userPrompt User 角色提示词
     * @return 完整的提示词文本（用于调试或日志）
     */
    fun formatFullPrompt(systemPrompt: String, userPrompt: String): String {
        return """
=== SYSTEM ===
$systemPrompt

=== USER ===
$userPrompt
        """.trimIndent()
    }
    
    /**
     * 构建 JSON 格式的 messages 数组（用于 DeepSeek API）
     */
    fun buildMessagesJson(systemPrompt: String, userPrompt: String): String {
        return """
[
  {
    "role": "system",
    "content": ${escapeJson(systemPrompt)}
  },
  {
    "role": "user",
    "content": ${escapeJson(userPrompt)}
  }
]
        """.trimIndent()
    }
    
    /**
     * JSON 转义工具
     */
    private fun escapeJson(text: String): String {
        return com.google.gson.Gson().toJson(text)
    }
}
