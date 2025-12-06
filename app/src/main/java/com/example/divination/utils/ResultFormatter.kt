package com.example.divination.utils

import com.example.divination.model.ResultSection

/**
 * 将AI原始解析内容转化为统一友好的展示结构
 */
object ResultFormatter {

    private val questionKeys = listOf(
        "question", "dreamContent", "content", "topic",
        "goal", "target", "愿望", "咨询问题"
    )

    fun format(
        methodId: String,
        inputData: Map<String, String>,
        rawSections: List<ResultSection>,
        poemSection: ResultSection? = null
    ): List<ResultSection> {
        val friendlySections = mutableListOf<ResultSection>()
        poemSection?.let { friendlySections.add(it) }

        val sanitized = sanitizeSections(rawSections)
        val question = extractQuestion(inputData)

        friendlySections.add(buildQuestionResponse(question, sanitized))
        friendlySections.add(buildCoreInsights(sanitized))
        friendlySections.add(buildOpportunityAndRisk(sanitized))
        friendlySections.add(buildActionAdvice(question))
        friendlySections.add(buildWarmSummary(question, methodId))

        return friendlySections
    }

    private fun sanitizeSections(raw: List<ResultSection>): List<ResultSection> {
        if (raw.isEmpty()) {
            return listOf(ResultSection("分析", "暂无详细内容，但整体趋势平稳，可放心调整节奏。"))
        }
        return raw.map {
            it.copy(content = condenseText(it.content))
        }
    }

    private fun extractQuestion(inputData: Map<String, String>): String? {
        questionKeys.forEach { key ->
            val value = inputData[key]
            if (!value.isNullOrBlank()) return value.trim()
        }
        return null
    }

    private fun buildQuestionResponse(question: String?, sections: List<ResultSection>): ResultSection {
        val focus = sections.firstOrNull()
        val highlight = focus?.let { summarize(it.content, 160) } ?: "目前的节奏需要先稳再进。"
        val detail = sections.getOrNull(1)?.let { summarize(it.content, 100) }

        val message = if (question != null) {
            val extra = detail?.let { "另一个需要留意的点是：$it" } ?: ""
            "你提到「$question」，整体解读提醒：$highlight。$extra 简单说，先抓住最关键的一点，再慢慢展开就好。"
        } else {
            "综合来看，当前局势的关键词是：$highlight。保持清晰的优先级，事情会逐渐回到可控节奏。"
        }

        return ResultSection("问题回应", message.trim())
    }

    private fun buildCoreInsights(sections: List<ResultSection>): ResultSection {
        val bullets = sections.take(3).map {
            val title = it.title.ifBlank { "重点" }
            "• $title：${summarize(it.content, 90)}"
        }

        val body = if (bullets.isNotEmpty()) {
            bullets.joinToString("\n")
        } else {
            "• 暂无更多洞察，保持观望并记录新的线索。"
        }

        return ResultSection("核心洞察", body)
    }

    private fun buildOpportunityAndRisk(sections: List<ResultSection>): ResultSection {
        val opportunitySource = sections.getOrNull(1) ?: sections.first()
        val riskSource = sections.getOrNull(2) ?: sections.last()

        val text = buildString {
            append("机会：${summarize(opportunitySource.content, 120)}\n")
            append("风险：${summarize(riskSource.content, 120)}")
        }

        return ResultSection("机会与风险", text)
    }

    private fun buildActionAdvice(question: String?): ResultSection {
        val suggestions = mutableListOf<String>()

        if (!question.isNullOrBlank()) {
            suggestions.add("围绕「$question」写下你最想看到的理想结果，并倒推出今天可以完成的一小步。")
        }

        suggestions.add("设定一个本周必须完成的「10分钟小任务」，完成后立即复盘感受与收获。")
        suggestions.add("保持基础自律：规律睡眠、少量运动、定时补水，状态稳定才能做出好判断。")
        suggestions.add("每晚花3分钟记录当天最开心与最担心的各一件事，帮助大脑放下杂念。")

        val content = suggestions.take(3)
            .mapIndexed { index, item -> "${index + 1}. $item" }
            .joinToString("\n")

        return ResultSection("行动建议", content)
    }

    private fun buildWarmSummary(question: String?, methodId: String): ResultSection {
        val methodNote = when (methodId) {
            "zhouyi" -> "顺势而为、以柔克刚是这次卦象给你的提示。"
            "bazi" -> "命盘里的力量会随着心态转移，别让焦虑掌控你。"
            "tarot" -> "牌面只是镜子，真正的选择权始终在你手里。"
            else -> "命理是参考，真正改变生活的还是你的每一次决定。"
        }

        val text = if (question != null) {
            "关于「$question」，请记得：$methodNote 放慢一点没关系，只要方向对，就已经在前进。"
        } else {
            "$methodNote 慢慢来比较快，给自己一些弹性空间，你已经做得很好。"
        }

        return ResultSection("暖心总结", text)
    }

    private fun condenseText(text: String): String {
        return text
            .replace(Regex("【.*?】"), "")
            .replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun summarize(text: String, limit: Int): String {
        if (text.isBlank()) return "暂无更多信息。"
        val cleaned = text.replace("\n", " ").replace(Regex("\\s+"), " ").trim()
        return if (cleaned.length <= limit) cleaned else cleaned.substring(0, limit).trimEnd() + "..."
    }
}
