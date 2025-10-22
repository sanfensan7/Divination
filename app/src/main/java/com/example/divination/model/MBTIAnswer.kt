package com.example.divination.model

import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

/**
 * MBTI答题记录
 * 
 * @property questionId 题目ID
 * @property selectedOption 选择的选项 (范围: -3 到 +3)
 */
data class MBTIAnswer(
    val questionId: Int,
    val selectedOption: Int  // -3: 强烈不同意, -2: 不同意, -1: 略不同意, 0: 中立, 1: 略同意, 2: 同意, 3: 强烈同意
): Serializable {
    init {
        require(selectedOption in -3..3) { 
            "Selected option must be in range -3 to 3, got: $selectedOption" 
        }
    }
    
    /**
     * 答案选项枚举
     */
    enum class Option(val value: Int, val displayName: String) {
        STRONGLY_DISAGREE(-3, "强烈不同意"),
        DISAGREE(-2, "不同意"),
        SLIGHTLY_DISAGREE(-1, "略不同意"),
        NEUTRAL(0, "中立"),
        SLIGHTLY_AGREE(1, "略同意"),
        AGREE(2, "同意"),
        STRONGLY_AGREE(3, "强烈同意");
        
        companion object {
            fun fromValue(value: Int): Option {
                return values().find { it.value == value } 
                    ?: throw IllegalArgumentException("Unknown option value: $value")
            }
        }
    }
    
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("questionId", questionId)
            put("selectedOption", selectedOption)
        }
    }
    
    companion object {
        fun fromJson(json: JSONObject): MBTIAnswer {
            return MBTIAnswer(
                questionId = json.getInt("questionId"),
                selectedOption = json.getInt("selectedOption")
            )
        }
        
        /**
         * 将答案列表转换为JSON字符串
         */
        fun toJsonString(answers: List<MBTIAnswer>): String {
            val jsonArray = JSONArray()
            answers.forEach { jsonArray.put(it.toJson()) }
            return jsonArray.toString()
        }
        
        /**
         * 从JSON字符串解析答案列表
         */
        fun fromJsonString(jsonString: String): List<MBTIAnswer> {
            val jsonArray = JSONArray(jsonString)
            val answers = mutableListOf<MBTIAnswer>()
            for (i in 0 until jsonArray.length()) {
                answers.add(fromJson(jsonArray.getJSONObject(i)))
            }
            return answers
        }
    }
}


