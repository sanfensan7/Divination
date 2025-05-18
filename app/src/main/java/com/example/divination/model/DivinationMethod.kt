package com.example.divination.model

/**
 * 算命方法数据模型
 *
 * @property id 唯一标识符
 * @property name 算命方法名称
 * @property description 算命方法描述
 * @property iconResId 图标资源ID
 * @property type 类型：1-中国传统，2-西方传统
 * @property inputFields 需要输入的字段列表
 */
data class DivinationMethod(
    val id: String,
    val name: String,
    val description: String,
    val iconResId: Int,
    val type: Int, // 1: 中国传统, 2: 西方传统
    val inputFields: List<InputField> = listOf()
)

/**
 * 输入字段数据模型
 *
 * @property id 字段ID
 * @property name 字段名称
 * @property type 字段类型（1-文本，2-日期，3-时间，4-选择）
 * @property required 是否必填
 * @property options 选择类型的选项
 */
data class InputField(
    val id: String,
    val name: String,
    val type: Int, // 1: 文本, 2: 日期, 3: 时间, 4: 选择
    val required: Boolean = true,
    val options: List<String> = listOf()
) 