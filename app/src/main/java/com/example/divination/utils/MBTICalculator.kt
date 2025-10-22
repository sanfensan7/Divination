package com.example.divination.utils

import com.example.divination.model.MBTIAnswer
import com.example.divination.model.MBTIQuestion
import com.example.divination.model.MBTIResult

/**
 * MBTI计算服务
 * 负责根据答题结果计算人格类型
 * 
 * 算法说明：
 * - 确定性算法，相同答案总是得到相同结果
 * - 无随机因素
 * - 每个维度独立计算
 */
class MBTICalculator {
    
    /**
     * 计算MBTI测试结果
     * 
     * @param answers 用户答案列表
     * @param questions 题目列表
     * @param version 题库版本
     * @return MBTI测试结果
     */
    fun calculateResult(
        answers: List<MBTIAnswer>,
        questions: List<MBTIQuestion>,
        version: String = "1.0.0"
    ): MBTIResult {
        // 初始化四个维度的得分
        var eiScore = 0
        var snScore = 0
        var tfScore = 0
        var jpScore = 0
        
        // 遍历所有答案，累加得分
        answers.forEach { answer ->
            val question = questions.find { it.id == answer.questionId } ?: return@forEach
            
            // 计算实际得分（考虑题目方向）
            val score = answer.selectedOption * question.direction
            
            // 根据维度累加到对应的得分
            when (question.dimension) {
                MBTIQuestion.Dimension.EI -> eiScore += score
                MBTIQuestion.Dimension.SN -> snScore += score
                MBTIQuestion.Dimension.TF -> tfScore += score
                MBTIQuestion.Dimension.JP -> jpScore += score
            }
        }
        
        // 根据得分判定人格类型
        val personalityType = buildPersonalityType(eiScore, snScore, tfScore, jpScore)
        
        return MBTIResult(
            personalityType = personalityType,
            eiScore = eiScore,
            snScore = snScore,
            tfScore = tfScore,
            jpScore = jpScore,
            testDate = System.currentTimeMillis(),
            version = version,
            answers = answers
        )
    }
    
    /**
     * 根据四个维度的得分构建人格类型代码
     * 
     * 规则：
     * - EI: score > 0 则 E (外向), 否则 I (内向)
     * - SN: score > 0 则 N (直觉), 否则 S (感觉)
     * - TF: score > 0 则 F (情感), 否则 T (思考)
     * - JP: score > 0 则 P (感知), 否则 J (判断)
     * 
     * 注意：当分数为0时，默认选择后者（I, S, T, J）
     */
    private fun buildPersonalityType(
        eiScore: Int,
        snScore: Int,
        tfScore: Int,
        jpScore: Int
    ): String {
        val e_or_i = if (eiScore > 0) "E" else "I"
        val s_or_n = if (snScore > 0) "N" else "S"
        val t_or_f = if (tfScore > 0) "F" else "T"
        val j_or_p = if (jpScore > 0) "P" else "J"
        
        return "$e_or_i$s_or_n$t_or_f$j_or_p"
    }
    
    /**
     * 验证答案的完整性
     * 
     * @param answers 答案列表
     * @param expectedCount 期望的答案数量
     * @return 是否完整
     */
    fun validateAnswers(answers: List<MBTIAnswer>, expectedCount: Int): Boolean {
        if (answers.size != expectedCount) {
            return false
        }
        
        // 检查是否有重复答案
        val uniqueQuestionIds = answers.map { it.questionId }.toSet()
        if (uniqueQuestionIds.size != answers.size) {
            return false
        }
        
        // 检查答案选项是否在有效范围内
        return answers.all { it.selectedOption in -3..3 }
    }
    
    /**
     * 获取答题进度
     * 
     * @param answers 已完成的答案列表
     * @param totalQuestions 总题目数
     * @return 进度百分比 (0-100)
     */
    fun getProgress(answers: List<MBTIAnswer>, totalQuestions: Int): Int {
        if (totalQuestions == 0) return 0
        return (answers.size * 100) / totalQuestions
    }
    
    companion object {
        @Volatile
        private var instance: MBTICalculator? = null
        
        fun getInstance(): MBTICalculator {
            return instance ?: synchronized(this) {
                instance ?: MBTICalculator().also { instance = it }
            }
        }
    }
}

