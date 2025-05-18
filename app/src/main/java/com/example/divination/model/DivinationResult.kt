package com.example.divination.model

import java.util.Date

/**
 * 算命结果数据模型
 *
 * @property id 唯一标识符
 * @property methodId 算命方法ID
 * @property createTime 创建时间
 * @property inputData 输入数据
 * @property resultSections 结果分段列表
 */
data class DivinationResult(
    val id: String,
    val methodId: String,
    val createTime: Date,
    val inputData: Map<String, String>,
    val resultSections: List<ResultSection>
)

/**
 * 结果分段数据模型
 *
 * @property title 标题
 * @property content 内容
 * @property score 评分（0-100）
 */
data class ResultSection(
    val title: String,
    val content: String,
    val score: Int = -1 // -1表示无评分
) 