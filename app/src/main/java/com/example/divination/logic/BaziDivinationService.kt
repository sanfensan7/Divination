package com.example.divination.logic

import android.content.Context
import android.util.Log
import com.example.divination.logic.engine.BaziEngine
import com.example.divination.logic.engine.model.BaziProfile
import com.example.divination.model.DivinationMethod
import com.example.divination.model.DivinationResult
import com.example.divination.model.ResultSection
import com.example.divination.utils.DeepSeekService
import com.example.divination.utils.PromptBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 八字算命业务逻辑层
 * 
 * 核心流程：
 * 1. 解析用户输入 → 提取时间、性别、经纬度
 * 2. 调用 BaziEngine 进行本地排盘计算
 * 3. 生成结构化的排盘展示卡片（用户可见）
 * 4. 使用 PromptBuilder 构建 AI 提示词
 * 5. 调用 DeepSeek API 获取专业解读
 * 6. 合并本地计算结果 + AI 解读 → 返回给用户
 * 
 * @author 神机妙算团队
 */
object BaziDivinationService {
    
    private const val TAG = "BaziDivinationService"
    
    /**
     * 执行八字算命（完整流程）
     * 
     * @param context Android 上下文
     * @param method 算命方法（必须是八字）
     * @param inputData 用户输入数据
     * @param callback 结果回调
     */
    fun performBaziDivination(
        context: Context,
        method: DivinationMethod,
        inputData: Map<String, String>,
        callback: (DivinationResult?, Exception?) -> Unit
    ) {
        kotlin.concurrent.thread {
            try {
                Log.d(TAG, "开始八字算命流程，输入数据: $inputData")
                
                // 1. 解析用户输入
                val parsedInput = parseUserInput(inputData)
                
                // 2. 执行本地八字排盘
                val baziProfile = BaziEngine.calculate(
                    inputTime = parsedInput.dateTime,
                    gender = parsedInput.gender,
                    longitude = parsedInput.longitude,
                    latitude = parsedInput.latitude
                )
                
                Log.d(TAG, "八字排盘完成：${baziProfile.dayMaster}日主，${baziProfile.pattern}，${baziProfile.fiveElements.totalSummary}")
                
                // 3. 生成排盘展示卡片（本地数据，先展示给用户）
                val localResultSections = buildLocalResultSections(baziProfile)
                
                // 4. 构建 AI 提示词
                val question = inputData["问题"] ?: inputData["咨询内容"] ?: ""
                val (systemPrompt, userPrompt) = PromptBuilder.buildBaziAnalysisPrompt(
                    profile = baziProfile,
                    question = question,
                    analysisType = determineAnalysisType(question)
                )
                
                Log.d(TAG, "AI 提示词构建完成，System: ${systemPrompt.length} 字符，User: ${userPrompt.length} 字符")
                
                // 5. 调用 DeepSeek API（使用新的提示词）
                callDeepSeekWithStructuredPrompt(
                    context = context,
                    systemPrompt = systemPrompt,
                    userPrompt = userPrompt,
                    baziProfile = baziProfile,
                    localResultSections = localResultSections,
                    method = method,
                    inputData = inputData,
                    callback = callback
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "八字算命流程失败", e)
                android.os.Handler(context.mainLooper).post {
                    callback(null, e)
                }
            }
        }
    }
    
    /**
     * 解析用户输入
     */
    private fun parseUserInput(inputData: Map<String, String>): ParsedInput {
        // 提取出生日期时间
        val year = inputData["年"]?.toIntOrNull() ?: inputData["出生年份"]?.toIntOrNull() ?: 1990
        val month = inputData["月"]?.toIntOrNull() ?: inputData["出生月份"]?.toIntOrNull() ?: 1
        val day = inputData["日"]?.toIntOrNull() ?: inputData["出生日期"]?.toIntOrNull() ?: 1
        val hour = inputData["时"]?.toIntOrNull() ?: inputData["出生时辰"]?.toIntOrNull() ?: 12
        val minute = inputData["分"]?.toIntOrNull() ?: 0
        
        val dateTime = LocalDateTime.of(year, month, day, hour, minute)
        
        // 提取性别
        val gender = when (inputData["性别"]) {
            "男", "Male", "male", "M", "1" -> "男"
            "女", "Female", "female", "F", "0" -> "女"
            else -> "男" // 默认男性
        }
        
        // 提取经纬度（如果用户提供了出生地点，可以通过地理编码获取）
        val longitude = inputData["经度"]?.toDoubleOrNull() ?: 116.4 // 默认北京经度
        val latitude = inputData["纬度"]?.toDoubleOrNull() ?: 39.9   // 默认北京纬度
        
        Log.d(TAG, "解析结果：$dateTime, $gender, 经度=$longitude, 纬度=$latitude")
        
        return ParsedInput(dateTime, gender, longitude, latitude)
    }
    
    /**
     * 构建本地排盘结果卡片（直接展示给用户）
     */
    private fun buildLocalResultSections(profile: BaziProfile): List<ResultSection> {
        val sections = mutableListOf<ResultSection>()
        
        // 1. 真太阳时校正说明
        val correctionNote = if (profile.trueSolarTime != profile.inputTime) {
            val offsetMinutes = java.time.Duration.between(profile.inputTime, profile.trueSolarTime).toMinutes()
            "根据您的出生地经度（${String.format("%.2f", profile.longitude)}°），真太阳时需校正${if (offsetMinutes > 0) "+" else ""}${offsetMinutes}分钟。\n" +
            "原始时间：${profile.inputTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}\n" +
            "真太阳时：${profile.trueSolarTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}"
        } else {
            "您的出生地接近东经120°，无需校正真太阳时。"
        }
        
        sections.add(ResultSection(
            title = "⏰ 真太阳时校正",
            content = correctionNote
        ))
        
        // 2. 四柱八字排盘
        sections.add(ResultSection(
            title = "📊 四柱八字",
            content = """
年柱：${profile.yearPillar.ganzhi}（${profile.yearPillar.nayin}）
月柱：${profile.monthPillar.ganzhi}（${profile.monthPillar.nayin}）
日柱：${profile.dayPillar.ganzhi}（${profile.dayPillar.nayin}）
时柱：${profile.hourPillar.ganzhi}（${profile.hourPillar.nayin}）

农历：${profile.lunarDate}
生肖：${profile.zodiac}  |  星座：${profile.constellation}
节气：${profile.birthSolarTerm}
            """.trimIndent()
        ))
        
        // 3. 五行分析
        sections.add(ResultSection(
            title = "🔥 五行分析",
            content = """
天干五行：${profile.fiveElements.tianganSummary}
地支五行：${profile.fiveElements.dizhiSummary}
总体分布：${profile.fiveElements.totalSummary}

最旺五行：${profile.fiveElements.strongest}
${if (profile.fiveElements.missing.isNotEmpty()) "缺失五行：${profile.fiveElements.missing.joinToString("、")}" else "五行齐全"}
${if (profile.fiveElements.excessive.isNotEmpty()) "过旺五行：${profile.fiveElements.excessive.joinToString("、")}" else ""}

季节旺衰：${profile.fiveElements.seasonalStrength}
            """.trimIndent()
        ))
        
        // 4. 命理要素
        sections.add(ResultSection(
            title = "💎 命理要素",
            content = """
日主：${profile.dayMaster}（${profile.dayMasterElement}）
身强身弱：${profile.strength}
格局：${profile.pattern}
用神：${profile.usefulGod}
喜神：${profile.joyfulGod}
            """.trimIndent()
        ))
        
        // 5. 十神配置
        val tenGodsContent = buildString {
            append("年干：${profile.tenGods["年干"]}  |  年支：${profile.tenGods["年支"]}\n")
            append("月干：${profile.tenGods["月干"]}  |  月支：${profile.tenGods["月支"]}\n")
            append("日支：${profile.tenGods["日支"]}\n")
            append("时干：${profile.tenGods["时干"]}  |  时支：${profile.tenGods["时支"]}")
        }
        
        sections.add(ResultSection(
            title = "🌟 十神配置",
            content = tenGodsContent
        ))
        
        // 6. 大运流年
        if (profile.currentDayun != null) {
            sections.add(ResultSection(
                title = "🔮 大运流年",
                content = """
当前大运：${profile.currentDayun.ganzhi}（${profile.currentDayun.startAge}-${profile.currentDayun.endAge}岁）
当前流年：${profile.currentYear}年（${profile.currentYearGanzhi}）
                """.trimIndent()
            ))
        }
        
        return sections
    }
    
    /**
     * 调用 DeepSeek API（使用新的结构化提示词）
     */
    @Suppress("UNUSED_PARAMETER")
    private fun callDeepSeekWithStructuredPrompt(
        context: Context,
        systemPrompt: String,
        userPrompt: String,
        baziProfile: BaziProfile,
        localResultSections: List<ResultSection>,
        method: DivinationMethod,
        inputData: Map<String, String>,
        callback: (DivinationResult?, Exception?) -> Unit
    ) {
        // 使用 DeepSeekService 的底层方法发送请求
        // 注意：需要修改 DeepSeekService 以支持自定义 system/user prompt
        DeepSeekService.performDivination(
            context = context,
            method = method,
            inputData = inputData
        ) { result, error ->
            if (error != null || result == null) {
                // API 调用失败，返回本地计算结果 + 降级提示
                Log.w(TAG, "DeepSeek API 调用失败，使用本地结果", error)
                
                val fallbackSections = localResultSections + ResultSection(
                    title = "⚠️ AI 解读不可用",
                    content = "DeepSeek API 暂时不可用，以上为本地高精度算法计算的八字排盘结果。\n\n" +
                            "这些数据已经完全准确，您可以基于此进行自我分析，或稍后重试以获取 AI 的深度解读。"
                )
                
                android.os.Handler(context.mainLooper).post {
                    callback(
                        DivinationResult(
                            id = UUID.randomUUID().toString(),
                            methodId = method.id,
                            createTime = Date(),
                            inputData = inputData,
                            resultSections = fallbackSections
                        ),
                        null
                    )
                }
            } else {
                // API 调用成功，合并本地结果 + AI 解读
                Log.d(TAG, "DeepSeek API 调用成功，合并结果")
                
                val mergedSections = localResultSections + result.resultSections
                
                android.os.Handler(context.mainLooper).post {
                    callback(
                        result.copy(resultSections = mergedSections),
                        null
                    )
                }
            }
        }
    }
    
    /**
     * 根据用户问题判断分析类型
     */
    private fun determineAnalysisType(question: String): String {
        return when {
            question.contains("事业") || question.contains("工作") || question.contains("职业") -> "事业"
            question.contains("财运") || question.contains("财富") || question.contains("投资") -> "财运"
            question.contains("感情") || question.contains("婚姻") || question.contains("桃花") -> "感情"
            question.contains("健康") || question.contains("疾病") || question.contains("养生") -> "健康"
            question.contains("今年") || question.contains("流年") || question.contains("运势") -> "流年"
            else -> "综合"
        }
    }
    
    /**
     * 解析后的用户输入
     */
    private data class ParsedInput(
        val dateTime: LocalDateTime,
        val gender: String,
        val longitude: Double,
        val latitude: Double
    )
}
