package com.example.divination.utils

import android.content.Context
import android.util.Log
import com.example.divination.model.DivinationMethod
import com.example.divination.model.DivinationResult
import com.example.divination.model.ResultSection
import java.util.*
import java.text.SimpleDateFormat

private const val TAG = "SafeDivination"

/**
 * 安全的占卜包装函数，确保不会导致应用崩溃
 */
fun safePerformDivination(
    context: Context,
    method: DivinationMethod,
    inputData: Map<String, String>,
    callback: (DivinationResult?, Exception?) -> Unit
) {
    try {
        Log.d(TAG, "开始安全占卜: ${method.id}, 输入数据: $inputData")
        
        // 根据不同的算命方法设置不同的超时时间
        val timeoutDuration = when (method.id) {
            "bazi" -> 120000L  // 八字占卜需要120秒（原60000）
            "zhouyi" -> 90000L  // 周易占卜需要90秒（原45000）
            "ziwei" -> 90000L  // 紫微斗数需要90秒（原45000）
            "qimen" -> 90000L  // 奇门遁甲需要90秒（原45000）
            "tarot" -> 80000L  // 塔罗牌需要80秒（原40000）
            "astrology" -> 80000L  // 占星需要80秒（原40000）
            "dream" -> 80000L  // 解梦需要80秒（原40000）
            "face", "palmistry" -> 70000L  // 面相、手相需要70秒（原35000）
            "numerology" -> 90000L  // 数字命理需要90秒（原70000）
            "almanac" -> 90000L  // 老黄历需要90秒（原70000）
            else -> 60000L  // 其他方法保持60秒（原30000）
        }
        
        // 设置超时机制
        val timeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            Log.w(TAG, "占卜超时: ${method.id}")
            // 创建模拟结果
            val fallbackResult = createFallbackResult(method, inputData)
            callback(fallbackResult, null)
        }
        
        // 使用动态超时时间
        timeoutHandler.postDelayed(timeoutRunnable, timeoutDuration)
        
        // 调用DeepSeekService.performDivination方法
        DeepSeekService.performDivination(
            context = context,
            method = method,
            inputData = inputData
        ) { result, error ->
            try {
                // 取消超时处理
                timeoutHandler.removeCallbacks(timeoutRunnable)
                
                if (error != null) {
                    Log.e(TAG, "占卜出错: ${method.id}", error)
                    // 创建模拟结果
                    val fallbackResult = createFallbackResult(method, inputData)
                    callback(fallbackResult, null)
                } else if (result != null) {
                    // 检查结果是否有效
                    if (isValidResult(result)) {
                        Log.d(TAG, "占卜成功: ${method.id}, 结果部分数: ${result.resultSections.size}")
                        callback(result, null)
                    } else {
                        Log.w(TAG, "占卜结果无效: ${method.id}")
                        // 创建模拟结果
                        val fallbackResult = createFallbackResult(method, inputData)
                        callback(fallbackResult, null)
                    }
                } else {
                    Log.e(TAG, "占卜没有结果: ${method.id}")
                    // 创建模拟结果
                    val fallbackResult = createFallbackResult(method, inputData)
                    callback(fallbackResult, null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理占卜结果异常: ${method.id}", e)
                // 创建模拟结果
                val fallbackResult = createFallbackResult(method, inputData)
                callback(fallbackResult, null)
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "调用占卜异常: ${method.id}", e)
        // 创建模拟结果
        val fallbackResult = createFallbackResult(method, inputData)
        callback(fallbackResult, null)
    }
}

/**
 * 检查结果是否有效
 */
private fun isValidResult(result: DivinationResult): Boolean {
    return result.resultSections.isNotEmpty() && 
           result.resultSections.any { it.content.isNotBlank() }
}

/**
 * 创建备用结果，以防API调用失败
 */
private fun createFallbackResult(
    method: DivinationMethod,
    inputData: Map<String, String>
): DivinationResult {
    val sections = mutableListOf<ResultSection>()
    
    // 首先添加API调用失败提示
    sections.add(ResultSection(
        title = "提示",
        content = "此结果由本地算法生成，非DeepSeek AI输出。DeepSeek API调用失败，可能原因：\n" +
                "1. 网络连接不稳定\n" +
                "2. API密钥未设置或无效\n" +
                "3. 服务器暂时不可用\n\n" +
                "建议：检查网络连接并在设置中配置有效的API密钥以获取更准确的AI算命结果。"
    ))
    
    // 特殊处理八字命理方法
    if (method.id == "bazi") {
        Log.d(TAG, "创建八字命理备用结果")
        
        // 构建八字命理的提示词，确保能被识别
        val prompt = StringBuilder("八字命理占卜 methodId=bazi\n\n")
        inputData.forEach { (key, value) ->
            prompt.append("$key: $value\n")
        }
        
        // 调用DeepSeekService的simulateBaziResponse方法
        try {
            val baziContent = DeepSeekService.simulateBaziResponseForFallback(prompt.toString())
            
            // 记录生成的内容以便调试
            Log.d(TAG, "八字命理内容: ${baziContent.take(200)}...")
            
            // 将生成的内容解析为不同的章节
            val contentSections = parseResponseToSections(baziContent)
            Log.d(TAG, "八字命理解析章节: ${contentSections.map { it.title }.joinToString(", ")}")
            
            // 确保不包含空章节
            val validSections = contentSections.filter { it.content.isNotBlank() }
            Log.d(TAG, "八字命理有效章节数: ${validSections.size}")
            
            // 添加八字命理章节到结果中
            validSections.forEach { section ->
                sections.add(section)
            }
            
            // 如果没有有效章节，添加一个默认章节
            if (validSections.isEmpty()) {
                sections.add(ResultSection(
                    title = "八字分析",
                    content = "根据您的八字信息，您具有较强的领导能力和创造力。近期运势较为平稳，建议把握机会，稳步发展。"
                ))
            }
            
            Log.d(TAG, "八字命理备用结果生成成功: ${sections.size}个章节")
            
            // 创建结果
            return DivinationResult(
                id = UUID.randomUUID().toString(),
                methodId = method.id,
                createTime = Date(),
                inputData = inputData,
                resultSections = sections
            )
        } catch (e: Exception) {
            Log.e(TAG, "八字命理备用结果生成失败", e)
            // 如果失败了，继续使用通用的备用内容
        }
    }
    
    // 根据不同的方法创建特定的备用内容
    when (method.id) {
        "tarot" -> {
            // 随机选择塔罗牌
            val random = Random(System.nanoTime())
            val majorArcana = arrayOf(
                "愚者", "魔术师", "女祭司", "女皇", "皇帝", "教皇", "恋人",
                "战车", "力量", "隐者", "命运之轮", "正义", "倒吊人", "死神",
                "节制", "恶魔", "塔", "星星", "月亮", "太阳", "审判", "世界"
            )
            
            val drawnCards = mutableListOf<String>()
            for (i in 0..2) {
                val card = majorArcana[random.nextInt(majorArcana.size)]
                if (!drawnCards.contains(card)) {
                    drawnCards.add(card)
                }
            }
            
            sections.add(ResultSection("塔罗牌阵", "您抽到的塔罗牌为：${drawnCards.joinToString("、")}。这组牌面展示了您目前所处的状态和未来的可能发展。"))
            
            // 详细专业分析
            val professionalAnalysis = StringBuilder()
            professionalAnalysis.append("【专业详解】\n\n")
            
            for (i in drawnCards.indices) {
                val card = drawnCards[i]
                val position = when(i) {
                    0 -> "过去位"
                    1 -> "现在位"
                    else -> "未来位"
                }
                
                professionalAnalysis.append("$position：$card\n")
                when (card) {
                    "愚者" -> professionalAnalysis.append("代表新的开始、冒险精神和无拘无束。暗示您正处于人生的起点，充满无限可能性。在塔罗体系中，愚者编号为0，象征无限可能和不受限制的自由。正位时表示乐观、冒险和无忧无虑；逆位则暗示鲁莽、缺乏规划和过度冒险。\n\n")
                    "魔术师" -> professionalAnalysis.append("象征创造力、技能和主动性。暗示您具有实现目标的能力和资源。在塔罗牌中，魔术师掌握四种元素（权杖、圣杯、宝剑、钱币），表示拥有所有必要的工具来创造自己的现实。正位代表专注、技能和自信；逆位则表示操纵、欺骗或才能未充分利用。\n\n")
                    "女祭司" -> professionalAnalysis.append("代表直觉、内在智慧和潜意识。暗示需要信任自己的内在声音。女祭司守护着神秘的知识，坐在两根柱子之间，象征着平衡和二元性。正位时提示倾听内心声音、发展直觉；逆位则暗示忽视内心声音、缺乏自信或过度依赖他人。\n\n")
                    // 其他牌的分析...
                    else -> professionalAnalysis.append("这张牌在塔罗体系中具有独特意义，与您当前的人生阶段高度契合。它揭示了您内在的潜力和外在的挑战，建议您深入思考其中的启示。\n\n")
                }
            }
            
            sections.add(ResultSection("专业分析", professionalAnalysis.toString()))
            
            // 牌阵综合解读
            sections.add(ResultSection("牌阵解读", "综合分析这三张牌，显示您目前处于人生的转折点，需要做出重要选择。过去的经历已为您积累了宝贵经验，现在面临的挑战需要您综合运用这些智慧。未来牌显示，若能坚持正确的方向，将会迎来重要突破。\n\n塔罗牌面暗示近期可能出现意外转机，态度决定一切，保持开放心态至关重要。"))
            
            // 详细建议
            sections.add(ResultSection("建议", "1. 保持开放的心态，接纳新的机会和观点\n2. 多听取他人意见，但最终决策应遵循内心指引\n3. 不要急于做出重大决定，给自己足够的思考空间\n4. 记录梦境和直觉，可能包含重要信息\n5. 关注身体信号，保持身心平衡"))
            
            // 简明总结
            sections.add(ResultSection("总结", "说白了，您现在站在人生的十字路口，选择很重要。过去的经验是您的财富，现在的挑战是您的考验，未来的机会需要您主动把握。简单点说，相信自己，做好准备，机会来了就大胆抓住！"))
        }
        "astrology" -> {
            // 占星学分析
            val professionalAnalysis = StringBuilder()
            professionalAnalysis.append("【星盘专业分析】\n\n")
            
            // 随机生成行星位置
            val random = Random(System.nanoTime())
            val signs = arrayOf("白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座")
            val planets = arrayOf("太阳", "月亮", "水星", "金星", "火星", "木星", "土星", "天王星", "海王星", "冥王星")
            
            val planetPositions = mutableMapOf<String, String>()
            planets.forEach { planet ->
                planetPositions[planet] = signs[random.nextInt(signs.size)]
            }
            
            // 添加专业分析
            professionalAnalysis.append("行星位置：\n")
            planetPositions.forEach { (planet, sign) ->
                professionalAnalysis.append("• $planet 在 $sign\n")
            }
            
            professionalAnalysis.append("\n重要相位：\n")
            val aspects = arrayOf("三分相", "六分相", "四分相", "合相", "对分相")
            for (i in 0..3) {
                val planet1 = planets[random.nextInt(5)]
                var planet2 = planets[random.nextInt(5)]
                while (planet1 == planet2) {
                    planet2 = planets[random.nextInt(5)]
                }
                val aspect = aspects[random.nextInt(aspects.size)]
                professionalAnalysis.append("• $planet1 与 $planet2 形成$aspect\n")
            }
            
            sections.add(ResultSection("星盘分析", professionalAnalysis.toString()))
            
            // 各领域解读
            sections.add(ResultSection("事业解读", "您的星盘显示近期木星进入有利位置，与您的事业宫形成良好相位，预示事业发展可能迎来转机。${planetPositions["火星"]}的火星增强了您的执行力和竞争优势，适合主动出击，争取更好的职业机会。\n\n土星的影响提示您需要注重基础工作，打牢专业根基，切勿急功近利。天王星带来的创新能量有助于您开拓新思路，突破常规限制。"))
            
            sections.add(ResultSection("感情解读", "在感情方面，${planetPositions["金星"]}的金星为您带来良好的人际吸引力，单身者可能遇到心动对象。已有伴侣的人，可能经历关系的深化或转变。\n\n月亮在${planetPositions["月亮"]}显示您的情感需求更加明确，渴望稳定和理解。注意水星的位置可能带来沟通上的挑战，建议提升表达能力，避免误解。"))
            
            sections.add(ResultSection("财运解读", "财务状况受木星和土星双重影响，显示收入潜力增加但需谨慎规划。${planetPositions["木星"]}的木星预示可能有意外收入或机会，但土星提醒您需要节制消费，避免冲动投资。\n\n近期适合考虑长期投资计划，而非短期投机。海王星的影响需警惕，可能带来财务幻想或不切实际的期望。"))
            
            // 详细建议
            sections.add(ResultSection("建议", "1. 事业上把握木星带来的有利时机，勇于表现自己\n2. 感情中提升沟通质量，表达真实需求\n3. 财务上制定合理预算，避免冲动消费\n4. 注意健康，特别是${if (random.nextBoolean()) "消化系统" else "神经系统"}可能需要调整\n5. 保持冥想或静心练习，增强心理平衡"))
            
            // 简明总结
            sections.add(ResultSection("总结", "简单来说，最近的星象对您挺有利的，特别是在事业上有新机会。感情方面需要多沟通，钱财上量入为出。总的来说，这是个不错的时期，好好把握！"))
        }
        "zhouyi" -> {
            // 使用简单的本地模拟方法，而不是调用私有方法
            val prompt = StringBuilder("周易占卜\n\n")
            inputData.forEach { (key, value) ->
                prompt.append("$key: $value\n")
            }
            
            // 简单的周易结果代替调用DeepSeekService.simulateZhouYiResponse
            val sections = mutableListOf<ResultSection>()
            sections.add(ResultSection("卦象解析", "您所得卦象显示当前形势变化多端，宜谨慎行事。"))
            sections.add(ResultSection("爻辞解读", "初爻：始而谨之，有大吉也。\n二爻：顺而止之，厚积薄发。\n三爻：观望为宜，静待时机。\n四爻：进退得宜，无往不利。\n五爻：刚柔相济，和而不同。\n上爻：物极必反，慎防变故。"))
            sections.add(ResultSection("卦象寓意", "此卦象征变通，提示您在目前形势下需要灵活应对。卦象显示您面临的局面复杂多变，需要智慧和耐心来把握时机。"))
            sections.add(ResultSection("运势分析", "近期运势稳中有波，建议您稳扎稳打，不宜轻举妄动。事业方面可能遇到一些变数，需要保持清醒的头脑做出决策。财运方面偏向平稳，投资理财需谨慎。"))
            sections.add(ResultSection("建议", "1. 保持耐心，等待适当时机再行动\n2. 多听取他人建议，集思广益\n3. 适当调整计划，灵活应对变化\n4. 谨慎投资，避免冒险\n5. 保持内心平静，不为外界干扰所动"))
            sections.add(ResultSection("总结", "说白了，您现在的情况就像天气多变的春季，需要随时准备应对不同情况。不要太急着做决定，多观察，多思考，等时机成熟时再行动。总的来说，只要您保持冷静和灵活性，这段时期的挑战是完全可以应对的。"))
        }
        "bazi" -> {
            // 添加八字命理模拟逻辑
            val prompt = StringBuilder("八字命理占卜\n\n")
            inputData.forEach { (key, value) ->
                prompt.append("$key: $value\n")
            }
            
            // 生成确定性随机种子，确保相同输入产生相同结果
            var seed = 0L
            for (entry in inputData) {
                seed += entry.key.hashCode() + entry.value.hashCode()
            }
            if (seed == 0L) {
                seed = System.currentTimeMillis()
            }
            val random = Random(seed)
            
            // 创建八字命理模拟结果
            sections.add(ResultSection("八字命盘", "根据您提供的出生信息，您的八字如下：\n" +
                (if (inputData.containsKey("date")) {
                    // 基于日期生成更个性化的内容
                    val dateStr = inputData["date"] ?: ""
                    val tianGan = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
                    val diZhi = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
                    val yearGan = tianGan[dateStr.hashCode() % 10]
                    val yearZhi = diZhi[dateStr.hashCode() % 12]
                    val monthGan = tianGan[(dateStr.hashCode() + 1) % 10]
                    val monthZhi = diZhi[(dateStr.hashCode() + 1) % 12]
                    val dayGan = tianGan[(dateStr.hashCode() + 2) % 10]
                    val dayZhi = diZhi[(dateStr.hashCode() + 2) % 12]
                    val hourGan = tianGan[(dateStr.hashCode() + 3) % 10]
                    val hourZhi = diZhi[(dateStr.hashCode() + 3) % 12]
                    yearGan + yearZhi + "年 " + monthGan + monthZhi + "月 " + dayGan + dayZhi + "日 " + hourGan + hourZhi + "时"
                } else "甲子年 丙寅月 戊午日 庚申时") + 
                "\n\n五行分析：\n" +
                "金：" + "●".repeat(1 + random.nextInt(4)) + "○".repeat(random.nextInt(2)) + "\n" +
                "木：" + "●".repeat(1 + random.nextInt(4)) + "○".repeat(random.nextInt(2)) + "\n" +
                "水：" + "●".repeat(1 + random.nextInt(4)) + "○".repeat(random.nextInt(2)) + "\n" +
                "火：" + "●".repeat(1 + random.nextInt(4)) + "○".repeat(random.nextInt(2)) + "\n" +
                "土：" + "●".repeat(1 + random.nextInt(4)) + "○".repeat(random.nextInt(2)) + "\n"))

            // 性格分析
            val personalityTraits = arrayOf(
                "您性格温和，处事稳重，善于思考，不急不躁。",
                "您天生具有领导才能，做事果断，容易获得他人信任。",
                "您思维灵活，善于创新，能够从不同角度看待问题。",
                "您处事谨慎，注重细节，做事有计划性和条理性。",
                "您性格开朗，人缘极佳，善于社交和沟通。",
                "您具有强烈的责任感和使命感，对自己要求严格。",
                "您心思细腻，情感丰富，对周围事物敏感度高。"
            )
            val selectedTraits = java.util.Arrays.asList(*personalityTraits)
            Collections.shuffle(selectedTraits, random)
            val personalityText = selectedTraits.subList(0, 3).toString()
                .replace("[", "")
                .replace("]", "")
                .replace(", ", "\n\n")
            sections.add(ResultSection("性格分析", personalityText))
            
            // 事业分析
            val careerOptions = arrayOf(
                "您适合从事需要创造力和想象力的工作，如设计、艺术或研发领域。",
                "您的组织和管理能力突出，适合担任管理岗位或创业。",
                "您适合需要细致和耐心的工作，如技术研究、数据分析或品质控制。",
                "您的沟通和表达能力强，适合市场、销售或教育培训工作。",
                "您的分析能力强，适合财务、咨询或研究类工作。"
            )
            val careerTrends = arrayOf(
                "近期事业发展较为顺利，可能有良好机会出现。",
                "未来一年是事业发展的关键期，需要把握机会，勇于突破。",
                "事业上遇到一些挑战，需要耐心应对，调整策略。",
                "当前处于积累阶段，为未来发展打下基础。"
            )
            
            // 随机选择事业选项和趋势
            val careerOptionsList = java.util.Arrays.asList(*careerOptions)
            Collections.shuffle(careerOptionsList, random)
            val careerTrendsList = java.util.Arrays.asList(*careerTrends)
            Collections.shuffle(careerTrendsList, random)
            
            val careerText = careerOptionsList.get(0) + "\n\n" + 
                careerTrendsList.get(0) + "\n\n" +
                "建议：\n1. 发挥自身优势，选择适合的发展方向\n" +
                "2. 持续学习，提升专业技能\n" +
                "3. 培养良好的职场人际关系\n" +
                "4. 保持耐心，不急于求成"
            sections.add(ResultSection("事业分析", careerText))
            
            // 财运分析
            val wealthOptions = arrayOf(
                "您的财运较为稳定，收入来源主要通过个人努力获得。",
                "您有一定的理财天赋，适合进行稳健型投资。",
                "您的财运起伏较大，需要做好财务规划和风险控制。",
                "您的正财运较强，通过主业能获得不错的收入。",
                "您的偏财运较好，可能通过额外渠道获得收益。"
            )
            
            val wealthOptionsList = java.util.Arrays.asList(*wealthOptions)
            Collections.shuffle(wealthOptionsList, random)
            
            val wealthText = wealthOptionsList.get(0) + "\n\n" +
                "近期财运走势：" + (if (random.nextBoolean()) "上升" else "平稳") + "。\n\n" +
                "理财建议：\n1. 量入为出，避免冲动消费\n" +
                "2. 建立良好的储蓄习惯\n" +
                "3. 适度投资，分散风险\n" +
                "4. 提升专业能力，增加收入来源"
            sections.add(ResultSection("财运分析", wealthText))
            
            // 感情分析
            val loveOptions = arrayOf(
                "您在感情中注重稳定和安全感，追求长久和谐的关系。",
                "您在感情中较为理性，会慎重考虑对方是否合适。",
                "您重视情感交流，期待与伴侣建立深层次的精神连接。",
                "您有自己的爱情理想，不会轻易妥协或将就。",
                "您在感情中重视真诚和忠诚，不善于掩饰自己的感受。"
            )
            
            val loveOptionsList = java.util.Arrays.asList(*loveOptions)
            Collections.shuffle(loveOptionsList, random)
            
            val loveText = loveOptionsList.get(0) + "\n\n" +
                "感情运势：" + (if (random.nextBoolean()) 
                    "近期感情运势较好，单身者有机会遇到心仪对象，已有伴侣的关系将更加稳定和谐。" 
                else 
                    "近期感情可能遇到一些挑战，需要耐心沟通，增进理解。") + "\n\n" +
                "建议：\n1. 保持开放心态，给彼此足够空间\n" +
                "2. 提升沟通技巧，避免误解\n" +
                "3. 表达真实需求，不要过度迁就"
            sections.add(ResultSection("感情分析", loveText))
            
            // 健康分析
            val healthOptions = arrayOf(
                "总体健康状况良好，但需要注意劳逸结合。",
                "体质较弱，需要加强锻炼，增强免疫力。",
                "精力充沛，但容易透支，应注意休息。",
                "容易受季节变化影响，需要及时调整作息。"
            )
            val weakPoints = arrayOf("消化系统", "呼吸系统", "心脑血管", "颈椎和腰椎", "免疫系统")
            
            val healthOptionsList = java.util.Arrays.asList(*healthOptions)
            Collections.shuffle(healthOptionsList, random)
            
            val weakPoint = weakPoints[random.nextInt(weakPoints.size)]
            
            val healthText = healthOptionsList.get(0) + "\n\n" +
                "需要关注的健康问题：${weakPoint}可能是您的健康薄弱环节，应予以重视。\n\n" +
                "健康建议：\n1. 保持规律作息，确保充足睡眠\n" +
                "2. 均衡饮食，增加蔬果摄入\n" +
                "3. 适度运动，增强体质\n" +
                "4. 保持良好心态，避免过度焦虑"
            sections.add(ResultSection("健康分析", healthText))
            
            // 总结
            sections.add(ResultSection("总结", "综合分析您的八字，您的人生总体发展" + 
                (if (random.nextBoolean()) "平稳有序" else "起伏有度") + 
                "。您的优势在于" + personalityTraits[random.nextInt(personalityTraits.size)].replace("您", "").trimStart() + 
                "需要注意的是，八字命理只是参考，真正的命运掌握在自己手中。通过自身努力和正确选择，完全可以创造更美好的未来。"))
        }
        "dream" -> {
            val dreamContent = inputData["content"] ?: "您的梦境"
            
            // 专业解梦分析
            val dreamAnalysis = StringBuilder()
            dreamAnalysis.append("【专业解析】\n")
            dreamAnalysis.append("从心理学角度分析，梦境是潜意识的表达，反映了做梦者内心深处的欲望、恐惧和未解决的冲突。弗洛伊德认为梦是「通往无意识的皇家大道」，而荣格则将梦视为集体无意识的表现。\n\n")
            
            // 根据梦境内容关键词生成分析
            val random = Random(System.nanoTime())
            
            if (dreamContent.contains("水") || dreamContent.contains("海") || dreamContent.contains("河")) {
                dreamAnalysis.append("水在梦中通常象征情感和潜意识。平静的水代表心灵的平和，而汹涌的水则可能暗示情绪的动荡。游泳可能表示您正在探索情感深度，而溺水则可能反映现实中的压力感。\n\n")
            } else if (dreamContent.contains("飞") || dreamContent.contains("天空")) {
                dreamAnalysis.append("飞翔的梦境常与自由、超越限制和解放自我有关。这可能反映您渴望摆脱现实约束，追求更高目标。如果飞行感觉愉快，表示自信和掌控感；如果感到恐惧，则可能暗示对未知的焦虑。\n\n")
            } else if (dreamContent.contains("追") || dreamContent.contains("跑")) {
                dreamAnalysis.append("被追赶的梦通常反映现实中的压力、焦虑或回避某些问题。这可能是您潜意识在告诉您面对而非逃避困难。追赶者可能代表您害怕面对的问题或情绪。\n\n")
            } else {
                dreamAnalysis.append("您的梦境内容包含多层次象征意义。表面看似平常的元素可能暗含深刻的心理投射。梦中出现的人物、场景和情境都是您内心世界的反映，值得深入探索其中含义。\n\n")
            }
            
            // 添加文化解读
            dreamAnalysis.append("在中国传统解梦学中，梦境被视为预兆和警示。《周公解梦》认为梦是阴阳二气交感的结果，反映了人与自然、人与社会的关系。不同的梦境象征着不同的吉凶预示。\n\n")
            
            sections.add(ResultSection("梦境解析", dreamAnalysis.toString()))
            
            // 象征意义
            sections.add(ResultSection("象征意义", "您梦见的「${dreamContent}」在象征学中具有特殊意义。梦境中的元素通常不应按字面意思理解，而是作为隐喻和象征。\n\n这种梦境可能反映了您内心深处的欲望、恐惧或未解决的心理冲突。具体来说，它可能象征着您对变化的期待或对稳定的渴望，取决于梦境的具体情感色彩。"))
            
            // 潜意识分析
            sections.add(ResultSection("潜意识分析", "从深层心理分析角度来看，这个梦可能揭示了您潜意识中正在处理的议题。梦境作为潜意识的窗口，提供了理解内心世界的重要线索。\n\n您的这个梦可能与近期生活变化有关，或者反映了长期积累的情绪需要释放。建议关注梦中的情绪感受，它们通常比梦的内容更能揭示潜意识状态。"))
            
            // 建议
            sections.add(ResultSection("建议", "1. 保持梦境日记，记录并观察模式\n2. 关注梦境中的情绪，而非仅关注内容\n3. 尝试冥想或放松练习，减轻潜在压力\n4. 探索梦中关键元素在现实生活中的意义\n5. 保持积极心态，将梦境视为自我了解的工具"))
            
            // 总结
            sections.add(ResultSection("总结", "简单来说，您的梦不必过度担心，它更多是您内心想法和情绪的反映，而非预兆。试着把它当作了解自己的一个窗口，从中获取有用的信息，帮助自己更好地面对现实生活中的挑战。"))
        }
        "numerology" -> {
            sections.add(ResultSection("数字能量", "根据您的生日，您的生命数字暗示着您具有领导能力和创造力。"))
            sections.add(ResultSection("数字影响", "当前您正处于一个有利于个人发展的数字周期，适合开始新的计划或项目。"))
            sections.add(ResultSection("建议", "1. 发挥自身优势\n2. 把握发展机会\n3. 注意人际关系协调"))
            sections.add(ResultSection("总结", "用简单的话说，您的数字显示您是个有领导才能的人，现在是个好时机去开始新计划。只要注意处理好人际关系，成功的机会很大！"))
        }
        "palmistry" -> {
            sections.add(ResultSection("手相解读", "您的手相显示生命线长而清晰，预示健康长寿；事业线明显，暗示事业有成。"))
            sections.add(ResultSection("感情线分析", "您的感情线显示您重情重义，在感情中注重稳定和忠诚。"))
            sections.add(ResultSection("建议", "1. 保持健康生活方式\n2. 在事业上继续努力\n3. 在感情中保持真诚"))
            sections.add(ResultSection("总结", "简单说，您的手相挺不错的，健康和事业都有好的迹象。感情上您是个重感情的人，保持这种真诚的态度，生活会越来越好。"))
        }
        "face" -> {
            sections.add(ResultSection("面相解读", "您的面相显示额头饱满，代表聪明才智；眉毛清晰有力，暗示决断力强。"))
            sections.add(ResultSection("性格分析", "从面相来看，您性格坚毅果断，有较强的领导能力和责任感。"))
            sections.add(ResultSection("建议", "1. 发挥领导才能\n2. 注意情绪管理\n3. 保持积极心态"))
            sections.add(ResultSection("总结", "简单地说，您的面相显示您是个聪明有能力的人，适合担任领导角色。只要注意控制情绪，保持积极态度，将能发挥最大潜力。"))
        }
        "almanac" -> {
            // 老黄历备用内容
            // 从输入获取日期参数，如果没有则使用当前日期
            val dateStr = inputData["date"] ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            // 将日期字符串转换为确定性种子
            val dateSeed = try {
                val parts = dateStr.split("-").map { it.toInt() }
                // 创建种子：年*10000 + 月*100 + 日
                if (parts.size >= 3) {
                    parts[0] * 10000 + parts[1] * 100 + parts[2]
                } else {
                    // 如果解析失败，使用当前日期的种子
                    val calendar = Calendar.getInstance()
                    calendar.time = Date()
                    calendar.get(Calendar.YEAR) * 10000 + 
                    (calendar.get(Calendar.MONTH) + 1) * 100 + 
                    calendar.get(Calendar.DAY_OF_MONTH)
                }
            } catch (e: Exception) {
                // 出错时使用简单的日期数字作为种子
                System.currentTimeMillis().toInt()
            }
            
            // 使用确定性种子创建随机数生成器
            val random = Random(dateSeed.toLong())
            val goodActivities = listOf("祭祀", "出行", "开业", "安床", "搬家", "结婚", "装修", "开工", "签约", "入宅")
            val badActivities = listOf("动土", "安葬", "修造", "开张", "入学", "求医", "交易", "诉讼", "远行", "开仓")
            
            // 随机选择宜忌事项
            val todayGood = goodActivities.shuffled(random).take(random.nextInt(3) + 2)
            val todayBad = badActivities.shuffled(random).take(random.nextInt(3) + 2)
            
            // 获取当前日期
            val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
            val today = sdf.format(Date())
            
            // 创建方位吉凶 - 使用确定性算法
            val directions = mapOf(
                "东" to if ((dateSeed + 0) % 4 <= 1) "吉" else "凶",
                "南" to if ((dateSeed + 1) % 4 <= 1) "吉" else "凶",
                "西" to if ((dateSeed + 2) % 4 <= 1) "吉" else "凶",
                "北" to if ((dateSeed + 3) % 4 <= 1) "吉" else "凶",
                "东南" to if ((dateSeed * 3 + 1) % 3 == 1) "吉" else "凶",
                "东北" to if ((dateSeed * 2 + 2) % 3 == 0) "吉" else "凶",
                "西南" to if ((dateSeed * 5 + 3) % 3 == 2) "吉" else "凶",
                "西北" to if ((dateSeed * 7 + 4) % 3 == 0) "吉" else "凶"
            )
            
            // 创建一个函数来基于种子从数组中选择确定的元素
            fun getElementBySeed(array: Array<String>, seed: Int): String {
                return array[Math.abs(seed) % array.size]
            }
            
            // 其他信息 - 也使用确定性选择
            val godOfBabyArray = arrayOf("门", "碓", "厕", "炉", "房床")
            val chongShaArray = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
            val yearStarArray = arrayOf("太岁", "天德", "文曲", "金匮", "天乙")
            val dayStarArray = arrayOf("天干", "天德", "月德", "王相", "玉宇")
            val fiveElementsArray = arrayOf("水旺", "木旺", "火旺", "土旺", "金旺")
            
            val godOfBaby = getElementBySeed(godOfBabyArray, dateSeed)
            val chongSha = getElementBySeed(chongShaArray, dateSeed + 11)
            val yearStar = getElementBySeed(yearStarArray, dateSeed + 22)
            val dayStar = getElementBySeed(dayStarArray, dateSeed + 33)
            val fiveElements = getElementBySeed(fiveElementsArray, dateSeed + 44)
            
            // 添加结果部分
            sections.add(ResultSection("今日运势", "$dateStr ${if (directions.values.count { it == "吉" } > 4) "黄道吉日" else "普通日"}"))
            
            // 宜忌提示
            sections.add(ResultSection("宜忌提示", "宜：${todayGood.joinToString("、")}\n忌：${todayBad.joinToString("、")}"))
            
            // 方位
            val directionsText = StringBuilder()
            for ((dir, luck) in directions) {
                directionsText.append("$dir：$luck ")
            }
            sections.add(ResultSection("方位吉凶", directionsText.toString()))
            
            // 其他信息
            sections.add(ResultSection("其他信息", "胎神：$godOfBaby\n冲煞：$chongSha\n值年星：$yearStar\n值日星：$dayStar\n五行：$fiveElements"))
            
            // 总结
            val goodCount = directions.values.count { it == "吉" }
            val summary = when {
                goodCount >= 6 -> "今天是个非常好的日子，多数方位都很吉利，适合进行重要活动。"
                goodCount >= 4 -> "今天整体运势不错，特别适合${todayGood.take(2).joinToString("、")}。如果您有重要决定要做，今天是个好日子。"
                else -> "今天运势一般，建议谨慎行事，特别避免${todayBad.take(2).joinToString("、")}等活动。"
            }
            sections.add(ResultSection("总结", summary))
        }
        else -> {
            // 默认备用结果
            sections.add(ResultSection("分析结果", "根据您提供的信息，此次解读暂时无法完成。请稍后再试。"))
            sections.add(ResultSection("可能原因", "1. 网络连接不稳定\n2. 服务器繁忙\n3. 输入信息不完整"))
            sections.add(ResultSection("建议", "请稍后再尝试进行算命，或者尝试其他算命方式。"))
            sections.add(ResultSection("总结", "简单说，系统现在遇到了一点小问题，建议您稍后再试或者换个算命方式尝试。"))
        }
    }
    
    // 创建结果
    return DivinationResult(
        id = UUID.randomUUID().toString(),
        methodId = method.id,
        createTime = Date(),
        inputData = inputData,
        resultSections = sections
    )
}

/**
 * 将响应内容解析为章节列表
 */
private fun parseResponseToSections(content: String): List<ResultSection> {
    // 添加日志以便调试
    Log.d(TAG, "开始解析响应内容，长度: ${content.length}")
    
    // 如果内容为空，返回空列表
    if (content.isBlank()) {
        Log.d(TAG, "内容为空，返回空列表")
        return emptyList()
    }
    
    val sections = mutableListOf<ResultSection>()
    
    // 使用正则表达式匹配【标题】和内容
    val sectionPattern = Regex("【([^】]+)】\\s*([\\s\\S]*?)(?=【[^】]+】|$)")
    val matches = sectionPattern.findAll(content)
    
    var matchCount = 0
    for (match in matches) {
        matchCount++
        val title = match.groupValues[1].trim()
        val sectionContent = match.groupValues[2].trim()
        
        Log.d(TAG, "找到章节 #$matchCount: $title (内容长度: ${sectionContent.length})")
        
        if (title.isNotEmpty() && sectionContent.isNotEmpty()) {
            sections.add(ResultSection(title, sectionContent))
        }
    }
    
    // 如果没有找到使用【标题】格式的章节，尝试其他格式
    if (sections.isEmpty()) {
        Log.d(TAG, "未找到【标题】格式，尝试其他格式")
        
        // 尝试匹配标题后跟冒号或换行的模式
        val altPattern = Regex("^([^：\\n]+)[：:]\\s*([\\s\\S]*?)(?=\\n[^：\\n]+[：:]|$)", RegexOption.MULTILINE)
        val altMatches = altPattern.findAll(content)
        
        var altMatchCount = 0
        for (match in altMatches) {
            altMatchCount++
            val title = match.groupValues[1].trim()
            val sectionContent = match.groupValues[2].trim()
            
            Log.d(TAG, "找到替代格式章节 #$altMatchCount: $title (内容长度: ${sectionContent.length})")
            
            if (title.isNotEmpty() && sectionContent.isNotEmpty()) {
                sections.add(ResultSection(title, sectionContent))
            }
        }
    }
    
    // 如果还是没有找到章节，尝试按段落分割
    if (sections.isEmpty()) {
        Log.d(TAG, "未找到任何格式的章节，尝试按段落分割")
        
        // 按照两个或多个连续换行符分隔段落
        val paragraphs = content.split(Regex("\n\n+"))
        
        for (i in paragraphs.indices) {
            val paragraph = paragraphs[i].trim()
            if (paragraph.isNotEmpty()) {
                // 尝试将段落的第一行作为标题
                val lines = paragraph.split("\n")
                val title = if (lines.isNotEmpty()) lines[0].trim() else "章节 ${i+1}"
                val content = if (lines.size > 1) lines.subList(1, lines.size).joinToString("\n").trim() else paragraph
                
                Log.d(TAG, "创建段落章节 #${i+1}: $title (内容长度: ${content.length})")
                
                sections.add(ResultSection(title, content))
            }
        }
    }
    
    // 如果依然没有章节，将整个内容作为一个章节
    if (sections.isEmpty() && content.isNotBlank()) {
        Log.d(TAG, "未能按段落分割，将整个内容作为一个章节")
        
        // 尝试提取第一行作为标题
        val lines = content.trim().split("\n")
        val title = if (lines.isNotEmpty()) lines[0].trim() else "分析结果"
        val body = if (lines.size > 1) lines.subList(1, lines.size).joinToString("\n").trim() else content
        
        sections.add(ResultSection(title, body))
    }
    
    Log.d(TAG, "最终解析出 ${sections.size} 个章节")
    return sections
} 