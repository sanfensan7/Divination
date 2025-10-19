package com.example.divination.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.divination.model.DivinationMethod
import com.example.divination.model.DivinationResult
import com.example.divination.model.ResultSection
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread
import kotlin.math.max
import java.util.regex.Pattern
import java.util.Calendar

/**
 * DeepSeek API服务类
 */
object DeepSeekService {
    
    private const val TAG = "DeepSeekService"
    private const val API_URL = "https://api.deepseek.com/v1/chat/completions"  // DeepSeek API地址
    
    // 公开的方法，供外部调用生成八字命理备用响应
    fun simulateBaziResponseForFallback(prompt: String): String {
        return simulateBaziResponse(prompt)
    }
    
    // 内置的 DeepSeek API 密钥
    private const val DEEPSEEK_API_KEY = "sk-89f7a9dbb66f4a8e8682116c1b116257"
    
    // 获取API密钥
    private fun getApiKey(context: Context): String {
        return DEEPSEEK_API_KEY
    }
    
    /**
     * 执行算命请求
     * 
     * @param context 上下文
     * @param method 算命方法
     * @param inputData 输入数据
     * @param callback 结果回调
     */
    fun performDivination(
        context: Context,
        method: DivinationMethod,
        inputData: Map<String, String>,
        callback: (DivinationResult?, Exception?) -> Unit
    ) {
        // 在后台线程执行网络请求
        thread {
            try {
                // 构建提示词
                val prompt = generatePrompt(method, inputData)
                
                // 检查API密钥
                val apiKey = getApiKey(context)
                if (apiKey.isEmpty()) {
                    // API密钥未设置，使用模拟响应并显示提示
                    showToast(context, "API密钥未设置，使用本地模拟模式")
                    Log.i(TAG, "API密钥未设置，使用本地模拟模式")
                    
                    // 创建模拟响应
                    val response = simulateResponse(prompt)
                    
                    // 添加模拟标记和解析响应
                    val simulatedSection = ResultSection(
                        title = "⚠️ 本地模拟提示",
                        content = "本次结果由本地算法模拟生成，非DeepSeek AI服务输出。为获取更专业、更个性化的AI算命结果，请在设置中配置有效的API密钥。\n\n高质量的AI算命结果将包含更丰富的专业分析和更个性化的解读。"
                    )
                    val sections = parseResponseToSections(response)
                    val result = DivinationResult(
                        id = UUID.randomUUID().toString(),
                        methodId = method.id,
                        createTime = Date(),
                        inputData = inputData,
                        resultSections = listOf(simulatedSection) + sections
                    )
                    
                    // 回调主线程
                    android.os.Handler(context.mainLooper).post {
                        callback(result, null)
                    }
                    return@thread
                }
                
                try {
                    // 尝试发送API请求
                    showToast(context, "正在连接AI服务...")
                    Log.d(TAG, "开始调用DeepSeek API，methodId: ${method.id}")
                    val response = sendRequest(context, apiKey, prompt, method.id)
                    Log.d(TAG, "API调用成功，返回数据长度: ${response.length}字符")
                    
                    // 解析响应
                    val result = parseResponse(method, inputData, response)
                    
                    // 回调主线程
                    android.os.Handler(context.mainLooper).post {
                        callback(result, null)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "API调用失败: ${e.javaClass.simpleName}: ${e.message}", e)
                    
                    // 使用模拟响应作为备选
                    showToast(context, "AI服务调用失败：${e.message?.take(50)}...，使用本地模拟模式")
                    
                    // 创建模拟响应
                    val response = simulateResponse(prompt)
                    
                    // 添加模拟标记和解析响应
                    val errorSection = ResultSection(
                        title = "⚠️ AI服务连接失败",
                        content = "DeepSeek AI服务调用失败：${e.message}\n\n系统已自动切换到本地算法模拟模式，但分析深度和个性化程度将受限。\n\n可能原因：\n1. 网络连接不稳定\n2. API密钥无效或已过期\n3. API服务暂时不可用\n\n建议：检查网络连接并在设置中更新API密钥。"
                    )
                    val sections = parseResponseToSections(response)
                    val result = DivinationResult(
                        id = UUID.randomUUID().toString(),
                        methodId = method.id,
                        createTime = Date(),
                        inputData = inputData,
                        resultSections = listOf(errorSection) + sections
                    )
                    
                    // 回调主线程
                    android.os.Handler(context.mainLooper).post {
                        callback(result, null)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "算命请求失败", e)
                
                // 回调主线程
                android.os.Handler(context.mainLooper).post {
                    callback(null, e)
                }
            }
        }
    }
    
    /**
     * 显示提示消息
     */
    private fun showToast(context: Context, message: String) {
        android.os.Handler(context.mainLooper).post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * 生成DeepSeek提示词
     */
    private fun generatePrompt(method: DivinationMethod, inputData: Map<String, String>): String {
        val basePrompt = """
        你是一位精通中国传统和西方占卜术的资深算命大师，拥有30年实践经验。
        请根据以下信息，生成一份详细、专业且符合文化背景的${method.name}分析结果：
        
        ## 分析背景
        - 分析方法：${method.name}（${if (method.type == 1) "中国传统" else "西方传统"}）
        - 分析目的：提供准确、有深度的命运解读，帮助用户理解自身状况和未来发展
        - 分析特点：结合传统理论与现代心理学，既尊重传统又符合现代人思维
        
        ## 用户输入信息
        ${inputData.entries.joinToString("\n") { "- ${it.key}: ${it.value}" }}
        
        ## 输出要求
        1. 内容分为至少4-5个独立章节，每章节有明确标题和详细解读
        2. 整体分析长度应在1200-2000字之间，内容丰富且专业
        3. 前面章节必须使用大量专业术语和深度分析，展示你的专业性和权威性
           - 要大量使用行业专业术语，尽可能展现深奥晦涩的分析风格
           - 引用经典著作和理论，使用专业的引用格式和术语解释
           - 分析必须详尽深入，使用完整的理论框架，展示分析的系统性和专业性
        4. 必须包含传统文化元素、术语和典故，配合专业理论分析
        5. 分析必须全面平衡，既指出积极优势，也要直接指出潜在问题、性格弱点和需要注意的风险
        6. 避免极端负面预测（如灾难、死亡等），但应指出若不改变会带来的实际挑战和后果
        7. 给予具体建议时，应包含规避风险、克服弱点的实际方法，至少3-5条
        8. 在适当位置引用传统经典语句增加可信度和深度
        9. 最后必须添加一个【总结】部分，与前面的专业分析形成鲜明对比：
           - 使用非常通俗易懂的语言，像与朋友对话一样，简明扼要地总结关键点
           - 完全避免任何专业术语，确保普通人也能轻松理解
           - 使用日常对话风格，就像朋友间聊天那样自然随意
           - 总结部分应当是独立的，即使不看前面的专业分析，也能明白主要结论
           - 总结应包含对用户情况的关键洞察和最重要的建议，但用最简单的语言表达
        
        ## 特殊要求
        ${getMethodSpecificPrompt(method.id)}
        
        请确保分析符合${method.name}的核心理论和方法论，不要混入其他占卜体系的元素。
        分析应具有个性化和针对性，避免空泛、模糊的表述。
        提供的建议应当实用、可操作，既有正面引导也有问题警示。
        前面的章节要展示你作为专业大师的深奥知识和洞察力，使用专业行话，而总结部分则要用外行人容易理解的白话，像老朋友一样直接聊天。
        """
        
        return basePrompt
    }
    
    /**
     * 根据不同算命方法获取特定提示词补充
     */
    private fun getMethodSpecificPrompt(methodId: String): String {
        return when (methodId) {
            "bazi" -> """
                - 必须按照天干地支理论分析八字四柱（年柱、月柱、日柱、时柱）
                - 详细解读用户五行强弱、日主特质、大运走向和流年影响
                - 分析命局中的喜用神与忌神，提出趋吉避凶的方法
                - 解释十神关系如正官、七杀、正印、偏印等在命局中的作用
                - 评估日主与各柱之间的关系，分析财、官、印、食等方面的表现
                - 包含传统五行相生相克理论对人生的影响
                - 指出命局中的冲克、刑害关系及其可能带来的挑战
                - 说明大运流年对命局的影响，包括顺逆运势的客观分析
                - 提出针对命局弱点的调整方法，如调整五行、选择适当的方位等
            """
            
            "ziwei" -> """
                - 必须基于紫微斗数十四主星和诸多辅星的组合与飞化
                - 详细分析十二宫位（命宫、财帛宫、兄弟宫、夫妻宫等）的主星组合
                - 解读四化星（化科、化禄、化权、化忌）对命盘的影响
                - 评估三方四正的吉凶格局
                - 分析大限、小限的流年运势变化
                - 重点解读命宫、身宫的星曜组合对性格的影响
                - 详细说明各宫位主管事项的发展情况
                - 直接指出各宫位中不利星曜的作用及其可能带来的挑战
                - 解释命盘中的煞星组合，如擎羊、陀罗、火星、铃星等的不利影响
                - 提供如何通过后天努力减轻不利星曜影响的建议
            """
            
            "qimen" -> """
                - 依据奇门遁甲九宫八门九星理论进行分析
                - 详细解读所在局的天盘、地盘、人盘三盘关系
                - 分析值符、值使所在宫位及其表现
                - 解释九星（天蓬、天任、天冲等）与八门（休、生、伤、杜等）的组合
                - 评估八神（值符、腾蛇、太阴等）在盘中的吉凶
                - 针对用户关注事项，分析适用宫位的吉凶与趋势
                - 说明三奇六仪太阳星的落宫对大局的影响
                - 明确指出盘中的凶门、煞神所在位置及其不利影响
                - 分析门户冲克关系带来的潜在问题
                - 建议如何趋吉避凶，规避盘中不利因素
            """
            
            "astrology" -> """
                - 必须首先提供星盘图的详细文字描述，以便我们能在APP中还原星盘图，请使用以下格式：
                  * 太阳位于白羊座15度
                  * 月亮位于金牛座10度
                  * 水星位于双子座5度
                  * 金星位于处女座20度
                  * 火星位于天秤座15度
                  * 木星位于天蝎座30度
                  * 土星位于金牛座0度
                  * 天王星位于双子座15度
                  * 海王星位于双鱼座0度
                  * 冥王星位于摩羯座0度
                  * 上升点位于白羊座0度
                  * 中天点位于摩羯座0度
                  (必须包含以上所有行星位置和度数)
                - 必须详细描述行星之间的相位关系，使用以下格式：
                  * 太阳和木星形成三分相(120度)
                  * 太阳和土星形成对分相(180度)
                  * 火星和冥王星形成四分相(90度)
                  * 金星和海王星形成六分相(60度)
                  (至少列出5个主要相位)
                - 详细分析太阳星座、月亮星座、上升星座三大主要星座特质
                - 解读十大行星在各宫位的影响和相位关系
                - 分析命盘中的相位组合（三分相、六分相、四分相、合相等）
                - 解释北交点、南交点对灵魂使命的启示
                - 评估命盘中的元素平衡（火、地、风、水）
                - 分析命盘中的主要格局和星盘形状（大三角、T十字等）
                - 结合用户当前的行运和进行相位分析
                - 详细解读不利相位（如四分相、对分相）的挑战和潜在问题
                - 分析受克制的行星及其可能导致的人生困境
                - 说明命盘中的缺失元素或弱势宫位带来的不平衡
                - 提供如何通过意识和行动弥补星盘缺陷的建议
            """
            
            "tarot" -> """
                - 详细解读每张塔罗牌的象征意义和牌面故事
                - 分析牌阵中的相互关系和整体故事线
                - 针对每个位置的牌义进行专业诠释（过去、现在、未来等）
                - 解释正位与逆位的不同含义
                - 分析大阿卡纳牌与小阿卡纳牌的不同侧重
                - 结合牌面元素（数字、符号、颜色）进行多层次解读
                - 对牌阵整体趋势给予清晰总结
                - 包含塔罗经典理论和心理学元素的专业解析
                - 直接解读牌阵中的负面牌（如死神、塔、恶魔等）的警示意义
                - 指出逆位牌所代表的具体挑战和阻碍
                - 分析牌阵中的不协调关系和潜在冲突
                - 在建议中包含如何应对负面牌预示的困境
            """
            
            "face" -> """
                - 基于传统面相学五官与命运关系的分析
                - 详细解读额头（天庭）、眉毛、眼睛、鼻子、嘴巴、下巴（地阁）等特征
                - 分析面部气色、痣点、纹理对运势的影响
                - 根据三停五岳理论评估面相整体平衡
                - 解释耳朵、眉毛等细节特征的命理含义
                - 分析面相中的财富线、婚姻线、事业线等特征
                - 结合传统相学与现代面部特征研究
                - 提供面相改善的建议（如提升气色、注意健康等）
                - 指出五官中的不协调特征及其反映的性格弱点
                - 解读面相中的"破绽"和"缺陷"对运势的影响
                - 分析面部特征中预示的健康隐患和注意事项
                - 提供如何通过表情、气色调整来改善面相能量的建议
            """
            
            "palm" -> """
                - 详细分析手掌的生命线、智慧线、感情线、命运线等主要线纹
                - 解读手型（水型、火型、土型、风型）对性格的影响
                - 评估指纹类型及指节特征
                - 分析大拇指、金星丘、月丘等丘陵的特点
                - 解释掌纹交叉、断裂、分叉等特殊纹路的含义
                - 结合两手掌纹的差异（先天与后天的对比）
                - 详细说明特殊标记（岛、星、格等）的命理意义
                - 提供如何强化有利掌纹能量的建议
                - 直接指出掌纹中的断裂、岛纹、交叉等不利特征的具体影响
                - 解读掌纹中预示的健康、情感或事业挑战
                - 分析手掌整体能量中的弱点和不平衡之处
                - 提供如何通过行为和习惯改变来弥补掌纹缺陷的方法
            """
            
            "fengshui" -> """
                - 基于传统风水学理论分析住宅或办公环境
                - 详细解读房屋坐向、门窗位置与五行关系
                - 分析住宅内部格局与八宅派、玄空派理论的契合度
                - 评估环境中的明堂、水口、煞气等因素
                - 解释室内装饰、颜色、材质对宅运的影响
                - 根据用户八字分析最适宜的风水布局
                - 提出具体可行的风水调整建议
                - 说明不同空间（卧室、客厅、厨房等）的风水要点
                - 指出住宅中存在的煞气、穿堂煞、冲煞等不利因素
                - 解析格局中的缺角、尖角对居住者的潜在影响
                - 评估住宅朝向与住户命理相冲的问题
                - 分析装修色彩、材质与主人五行不协调之处
                - 提供消除或化解不良风水的具体方法
            """
            
            "dream" -> """
                - 基于中国传统周公解梦和现代心理学理论
                - 详细解析梦境中的人物、事件、场景、情绪的象征意义
                - 分析梦境与做梦者现实生活的关联
                - 解读不同类型梦境（预知梦、重复梦、清醒梦等）的特殊含义
                - 结合用户生活背景与心理状态进行个性化解读
                - 从潜意识角度探讨梦境的深层次信息
                - 提供如何利用梦境指引改善现实生活的建议
                - 特别关注梦境中的关键符号和情感体验
                - 直接解读梦境中的负面元素（如死亡、坠落、追逐等）的警示意义
                - 分析梦中出现的焦虑、恐惧等情绪所反映的潜在问题
                - 指出梦境可能揭示的自我欺骗或盲点
                - 提供如何面对梦中揭示的内心冲突和不安的方法
            """
            
            "name" -> """
                - 基于传统姓名学五格剖象法进行详细分析
                - 解读天格、地格、人格、外格、总格的数理吉凶
                - 分析姓名的三才配置（天才、人才、地才）
                - 评估姓名中汉字的五行属性与用户八字的搭配
                - 解释姓名笔画数与数理能量的关系
                - 分析姓名的音律和声调对运势的影响
                - 提供姓名能量对事业、婚姻、健康等方面的影响分析
                - 如有必要，建议更为吉利的用字或改名方向
                - 直接指出姓名中的凶数、不良音律或失衡五行
                - 解析姓名与八字相冲或内部结构不协调的问题
                - 评估姓名可能对性格形成的负面影响
                - 提供根据具体情况如何选择更为平衡的用字方案
            """
            
            "zhouyi" -> """
                - 必须基于周易六十四卦和阴阳爻理论进行详细分析
                - 首先明确给出所得卦象，同时说明是上卦、下卦组合
                - 详细解读卦辞、彖辞、象辞的含义及其现代意义
                - 分析重点爻辞及其变化，解释爻位与人事的相应关系
                - 阐述卦象所反映的困境与机遇，以及适当的应对方式
                - 从五行属性和阴阳变化角度分析卦象内涵
                - 结合用户问题，针对性地解释卦象对当下形势的反映
                - 分析卦象中吉凶之象，提供趋吉避凶的实际建议
                - 解释卦象变化的趋势及其对未来发展的指引
                - 提供与卦象相关的传统智慧，引用《易经》原文加深解析
                - 直接指出卦象中的警示信息和需要注意的隐患
                - 给出符合卦象指引的行动建议和决策参考
            """
            
            else -> """
                - 提供全面、系统的分析，涵盖命理主要方面
                - 结合传统理论与现代解读，平衡古典智慧与实用性
                - 关注用户最关心的问题，提供针对性的建议
                - 包含近期运势变化与长期发展趋势的分析
                - 提供具体可行的改善建议和行动方向
                - 直接指出分析中发现的潜在风险和弱点
                - 客观评估用户面临的挑战和可能的阻碍
                - 避免过度乐观，保持客观平衡的分析态度
                - 提供实际的风险规避建议和应对策略
            """
        }
    }
    
    /**
     * 发送API请求
     */
    private fun sendRequest(context: Context, apiKey: String, prompt: String, methodId: String = ""): String {
        // 创建连接
        val url = URL(API_URL)
        val connection = url.openConnection() as HttpsURLConnection
        
        try {
            Log.d(TAG, "开始API请求，methodId: $methodId, URL: $API_URL")
            
            // 设置连接属性
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.doOutput = true
            
            // 根据不同的占卜方法设置不同的超时时间
            val connectTimeout = when (methodId) {
                "bazi" -> 60000 // 八字占卜连接超时60秒（原30000）
                "zhouyi" -> 40000 // 周易占卜连接超时40秒（原25000）
                "ziwei" -> 40000 // 紫微斗数连接超时40秒（原25000）
                "tarot" -> 35000 // 塔罗牌连接超时35秒（原20000）
                "astrology" -> 35000 // 占星连接超时35秒（原20000）
                "dream" -> 35000 // 解梦连接超时35秒（原20000）
                "face", "palmistry" -> 30000 // 面相、手相连接超时30秒（原15000）
                "numerology" -> 40000 // 数字命理连接超时40秒（原30000）
                else -> 30000 // 其他方法连接超时30秒（原15000）
            }
            
            val readTimeout = when (methodId) {
                "bazi" -> 120000 // 八字占卜读取超时120秒（原60000）
                "zhouyi" -> 80000 // 周易占卜读取超时80秒（原50000）
                "ziwei" -> 80000 // 紫微斗数读取超时80秒（原50000）
                "tarot" -> 75000 // 塔罗牌读取超时75秒（原45000）
                "astrology" -> 75000 // 占星读取超时75秒（原45000）
                "dream" -> 75000 // 解梦读取超时75秒（原45000）
                "face", "palmistry" -> 65000 // 面相、手相读取超时65秒（原40000）
                "numerology" -> 85000 // 数字命理读取超时85秒（原65000）
                else -> 60000 // 其他方法读取超时60秒（原35000）
            }
            
            Log.d(TAG, "设置API超时参数 - methodId: $methodId, 连接超时: ${connectTimeout}ms, 读取超时: ${readTimeout}ms")
            
            // 设置连接和读取超时
            connection.connectTimeout = connectTimeout
            connection.readTimeout = readTimeout
            
            // 创建请求体
            val requestBody = JSONObject().apply {
                put("model", "deepseek-chat")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                
                // 增强DeepSeek API参数，提高推理稳定性和速度
                put("max_tokens", 4000)
                put("temperature", 0.7)
                put("top_p", 0.85)
                put("frequency_penalty", 0.2)
                put("presence_penalty", 0.1)
                put("stream", false)
            }
            
            // 记录请求详情
            Log.d(TAG, "API请求参数: ${requestBody.toString().take(200)}...")
            
            if (methodId == "bazi") {
                Log.d(TAG, "八字命理API请求正在发送，完整提示词长度: ${prompt.length}字符")
                Log.d(TAG, "八字命理API提示词前300字符: ${prompt.take(300)}...")
            }
            
            // 写入请求体
            DataOutputStream(connection.outputStream).use { it.write(requestBody.toString().toByteArray()) }
            
            // 检查响应码
            val responseCode = connection.responseCode
            Log.d(TAG, "API响应码: $responseCode")
            
            if (methodId == "bazi") {
                Log.d(TAG, "八字命理API响应码: $responseCode")
            }
            
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorMessage = BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
                Log.e(TAG, "API错误: $responseCode, $errorMessage")
                
                if (methodId == "bazi") {
                    Log.e(TAG, "八字命理API调用失败: $responseCode, 错误详情: $errorMessage")
                }
                
                throw Exception("API调用失败 (HTTP $responseCode): ${getApiErrorMessage(errorMessage)}")
            }
            
            // 读取响应
            val response = BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8)).use { it.readText() }
            Log.d(TAG, "API响应内容前200字符: ${response.take(200)}...")
            
            if (methodId == "bazi") {
                Log.d(TAG, "八字命理API响应成功，响应长度: ${response.length}字符")
                Log.d(TAG, "八字命理API响应内容前300字符: ${response.take(300)}...")
            }
            
            return response
        } catch (e: Exception) {
            if (methodId == "bazi") {
                Log.e(TAG, "八字命理API请求发生异常: ${e.javaClass.simpleName}: ${e.message}", e)
            }
            throw e
        } finally {
            connection.disconnect()
        }
    }
    
    /**
     * 模拟API响应（仅在实际API调用失败时使用）
     */
    private fun simulateResponse(prompt: String): String {
        // 获取传入提示词中的methodId或method.name
        val methodId = when {
            prompt.contains("methodId=bazi") || prompt.contains("method.id=bazi") -> "bazi"
            prompt.contains("methodId=zhouyi") || prompt.contains("method.id=zhouyi") -> "zhouyi"
            // 其他方法...
            else -> null
        }
        
        // 优先判断methodId，确保八字命理一定会使用专门的实现
        return when {
            methodId == "bazi" || prompt.contains("八字命理") || prompt.contains("生辰八字") -> {
                Log.d("DeepSeekService", "正在使用八字命理备用响应")
                simulateBaziResponse(prompt)
            }
            methodId == "zhouyi" || prompt.contains("周易") -> simulateZhouYiResponse(prompt)
            prompt.contains("塔罗牌") -> simulateTarotResponse(prompt)
            prompt.contains("紫微斗数") -> simulateZiweiResponse(prompt)
            prompt.contains("星盘") || prompt.contains("占星") -> simulateAstrologyResponse(prompt) 
            prompt.contains("手相") -> simulatePalmistryResponse(prompt)
            prompt.contains("面相") -> simulateFaceReadingResponse(prompt)
            prompt.contains("解梦") -> simulateDreamInterpretationResponse(prompt)
            prompt.contains("黄历") || prompt.contains("择日") -> simulateAlmanacResponse(prompt)
            prompt.contains("数字命理") -> simulateNumerologyResponse(prompt)
            prompt.contains("奇门遁甲") -> simulateQimenResponse(prompt)
            else -> simulateGenericResponse(prompt)
        }
    }
    
    /**
     * 模拟占星学响应（专门为占星学方法设计）
     */
    private fun simulateAstrologyResponse(prompt: String): String {
        // 使用固定的种子使每次结果不同但可预测
        val currentTime = System.currentTimeMillis()
        val random = Random(currentTime)
        
        // 提取用户输入的关键信息
        val birthDateMatch = "出生日期：([^\\n]+)".toRegex().find(prompt)
        val birthTimeMatch = "出生时间：([^\\n]+)|出生时辰：([^\\n]+)".toRegex().find(prompt)
        val genderMatch = "性别：([^\\n]+)".toRegex().find(prompt)
        val questionMatch = "预测问题：([^\\n]+)|咨询问题：([^\\n]+)".toRegex().find(prompt)
        val fullNameMatch = "全名：([^\\n]+)".toRegex().find(prompt)
        
        val birthDate = birthDateMatch?.groupValues?.get(1)?.trim()
        val birthTime = birthTimeMatch?.groupValues?.get(1)?.takeIf { it.isNotEmpty() } 
            ?: birthTimeMatch?.groupValues?.get(2)?.trim()
        val gender = genderMatch?.groupValues?.get(1)?.trim()
        val question = questionMatch?.groupValues?.get(1)?.takeIf { it.isNotEmpty() } 
            ?: questionMatch?.groupValues?.get(2)?.trim()
        val fullName = fullNameMatch?.groupValues?.get(1)?.trim()
        
        val sb = StringBuilder()
        
        // 确保使用"星盘描述"作为标题，以便DivinationResultFragment识别
        sb.append("【星盘描述】\n")
        
        // 生成更明确、更标准的行星位置信息，确保格式统一，便于AstrologyChartView解析
        sb.append("以下是您的星盘基本信息：\n\n")
        
        // 添加出生信息以增加真实性
        sb.append("出生日期：${birthDate ?: "1990年1月1日"}\n")
        sb.append("出生时间：${birthTime ?: "12:00"}\n")
        if (gender != null) {
            sb.append("性别：$gender\n")
        }
        sb.append("\n")
        
        // 行星位置信息，确保格式非常清晰规范
        sb.append("行星位置：\n")
        
        val zodiacSigns = listOf("白羊", "金牛", "双子", "巨蟹", "狮子", "处女", "天秤", "天蝎", "射手", "摩羯", "水瓶", "双鱼")
        
        // 生成行星位置，每个行星使用一致的格式
        val planets = listOf("太阳", "月亮", "水星", "金星", "火星", "木星", "土星", "天王星", "海王星", "冥王星", "上升点", "中天点")
        val usedPositions = mutableSetOf<Int>() // 用于跟踪已使用的位置，避免行星全都在同一个位置
        
        for (planet in planets) {
            // 确保每个行星位置稍有不同
            var zodiacIndex: Int
            var degree: Int
            
            do {
                zodiacIndex = random.nextInt(12)
                degree = random.nextInt(30)
                // 创建唯一标识符确保位置不完全重复
                val positionKey = zodiacIndex * 100 + degree
                if (!usedPositions.contains(positionKey)) {
                    usedPositions.add(positionKey)
                    break
                }
            } while (usedPositions.size < 12 * 30) // 最多尝试一定次数，避免无限循环
            
            // 使用标准格式：* 行星位于星座度数
            sb.append("* ${planet}位于${zodiacSigns[zodiacIndex]}座${degree}度\n")
        }
        
        // 添加行星相位关系信息
        sb.append("\n行星相位关系：\n")
        
        // 定义可能的相位类型
        val aspectTypes = listOf("三分相", "六分相", "四分相", "对分相", "合相")
        val aspectDegrees = mapOf(
            "三分相" to 120,
            "六分相" to 60,
            "四分相" to 90, 
            "对分相" to 180,
            "合相" to 0
        )
        
        // 生成5-7个不重复的相位关系
        val aspectCount = 5 + random.nextInt(3)
        val usedPairs = mutableSetOf<Pair<String, String>>()
        
        for (i in 0 until aspectCount) {
            // 选择两个不同的行星
            var planet1: String
            var planet2: String
            do {
                planet1 = planets[random.nextInt(planets.size - 2)] // 排除上升点和中天点
                planet2 = planets[random.nextInt(planets.size - 2)]
                val pair = if (planet1 < planet2) Pair(planet1, planet2) else Pair(planet2, planet1)
                
                // 确保行星对不重复且两个行星不相同
                if (planet1 != planet2 && !usedPairs.contains(pair)) {
                    usedPairs.add(pair)
                    break
                }
            } while (true)
            
            // 随机选择相位类型
            val aspectType = aspectTypes[random.nextInt(aspectTypes.size)]
            val aspectDegree = aspectDegrees[aspectType] ?: 0
            
            // 使用标准格式：* 行星1和行星2形成相位类型(度数度)
            sb.append("* ${planet1}和${planet2}形成${aspectType}(${aspectDegree}度)\n")
        }
        
        // 添加总论部分
        sb.append("\n【总论】\n")
        if (fullName != null) {
            sb.append("${fullName}，")
        }
        
        if (birthDate != null) {
            sb.append("根据您${birthDate}")
            if (birthTime != null) {
                sb.append(" ${birthTime}")
            }
            sb.append("的出生数据，")
        }
        
        sb.append("您的星盘显示：")
        
        // 随机选择主宫位
        val houses = listOf("第一宫(上升宫)", "第四宫(天底宫)", "第七宫(下降宫)", "第十宫(中天宫)")
        val selectedHouse = houses[random.nextInt(houses.size)]
        
        // 随机选择强势星座
        val strongZodiac = zodiacSigns[random.nextInt(zodiacSigns.size)]
        
        sb.append("在您的星盘中，${selectedHouse}位于${strongZodiac}座，")
        
        // 随机选择一个行星作为主导
        val leadingPlanet = planets[random.nextInt(planets.size - 2)]  // 排除上升点和中天点
        sb.append("${leadingPlanet}是您星盘的主导行星，")
        
        // 添加一些性格特质描述
        val traits = listOf("创造力", "逻辑思维", "情感敏感度", "行动力", "沟通能力", "直觉", "稳定性", "变通性")
        val selectedTraits = mutableListOf<String>()
        val traitCount = 2 + random.nextInt(2)  // 2-3个特质
        
        for (i in 0 until traitCount) {
            val trait = traits[random.nextInt(traits.size)]
            if (!selectedTraits.contains(trait)) {
                selectedTraits.add(trait)
            }
        }
        
        sb.append("这表明您的${selectedTraits.joinToString("和")}较为突出。\n\n")
        
        // 添加元素平衡分析
        sb.append("【元素平衡】\n")
        sb.append("在您的星盘中：\n")
        
        val elements = listOf("火元素", "土元素", "风元素", "水元素")
        val elementStrengths = mutableMapOf<String, Int>()
        
        // 为每种元素随机生成强度值（1-5）
        for (element in elements) {
            elementStrengths[element] = 1 + random.nextInt(5)
        }
        
        // 确定最强和最弱的元素
        val strongestElement = elementStrengths.maxByOrNull { it.value }?.key ?: "火元素"
        val weakestElement = elementStrengths.minByOrNull { it.value }?.key ?: "水元素"
        
        for (element in elements) {
            val strength = elementStrengths[element] ?: 3
            val description = when (strength) {
                1 -> "非常弱"
                2 -> "较弱"
                3 -> "中等"
                4 -> "较强"
                else -> "非常强"
            }
            sb.append("- ${element}：${description}，")
            
            when (element) {
                "火元素" -> sb.append("影响您的热情、动力和创造力\n")
                "土元素" -> sb.append("影响您的稳定性、实用性和耐心\n")
                "风元素" -> sb.append("影响您的思考能力、沟通和适应性\n")
                "水元素" -> sb.append("影响您的情感、直觉和敏感度\n")
            }
        }
        
        sb.append("\n${strongestElement}最为突出，这使您在")
        when (strongestElement) {
            "火元素" -> sb.append("行动力、热情和自信心方面表现出色")
            "土元素" -> sb.append("踏实、稳重和实用主义方面有优势")
            "风元素" -> sb.append("思考、表达和社交方面较为出色")
            "水元素" -> sb.append("情感理解、同理心和直觉方面有特长")
        }
        sb.append("。\n\n")
        
        // 行星组合分析
        sb.append("【关键行星组合】\n")
        
        // 选择2-3个行星组合进行分析
        val combinations = mutableListOf<String>()
        val combinationCount = 2 + random.nextInt(2)
        
        val planetCombinations = listOf(
            "太阳与月亮", "太阳与水星", "太阳与金星", "月亮与火星", 
            "水星与金星", "金星与火星", "木星与土星", "天王星与海王星"
        )
        
        for (i in 0 until combinationCount) {
            val combination = planetCombinations[random.nextInt(planetCombinations.size)]
            if (!combinations.contains(combination)) {
                combinations.add(combination)
            }
        }
        
        for (combination in combinations) {
            sb.append("${combination}的相位关系显示：")
            
            when (combination) {
                "太阳与月亮" -> sb.append("您的意识与潜意识之间的互动较为${if (random.nextBoolean()) "和谐" else "紧张"}，这影响了您处理情感和理性问题的方式。")
                "太阳与水星" -> sb.append("您的思维表达方式与自我认同感有着${if (random.nextBoolean()) "一致的" else "有时冲突的"}关系。")
                "太阳与金星" -> sb.append("您在爱情与自我价值观方面的态度表现出${if (random.nextBoolean()) "自信和谐的" else "某种程度的矛盾"}特点。")
                "月亮与火星" -> sb.append("您的情感反应与行动力之间存在${if (random.nextBoolean()) "互相促进的" else "需要平衡的"}动态关系。")
                "水星与金星" -> sb.append("您的沟通方式和审美价值观${if (random.nextBoolean()) "相互补充" else "有时不一致"}，这会影响人际互动。")
                "金星与火星" -> sb.append("您在爱情和欲望方面的能量${if (random.nextBoolean()) "协调流动" else "有时候会出现冲突"}，体现在关系互动中。")
                "木星与土星" -> sb.append("您的扩张愿望与实际限制之间的平衡${if (random.nextBoolean()) "良好" else "需要更多注意"}，影响职业发展。")
                "天王星与海王星" -> sb.append("您的变革意识与理想主义之间有着${if (random.nextBoolean()) "创造性的张力" else "和谐的共鸣"}，影响您的长期目标。")
                else -> sb.append("这显示了您性格中一些独特而复杂的方面。")
            }
            
            sb.append("\n")
        }
        
        sb.append("\n")
        
        // 生成事业部分
        sb.append("【事业】\n")
        val careerPlanets = listOf("太阳", "土星", "木星", "火星", "中天点")
        val careerPlanet = careerPlanets[random.nextInt(careerPlanets.size)]
        
        sb.append("您的事业宫受到${careerPlanet}的主要影响，")
        
        val careerAreas = listOf(
            "创意领域", "科技行业", "财务金融", "医疗健康", 
            "教育培训", "咨询服务", "公共关系", "研究分析"
        )
        
        val selectedAreas = careerAreas.shuffled(random).take(2)
        sb.append("适合从事${selectedAreas.joinToString("或")}。")
        
        // 添加事业周期
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val nextYear = currentYear + 1
        val futureYear = currentYear + 2 + random.nextInt(3)
        
        sb.append("从${currentYear}年到${nextYear}年是事业转型期，")
        sb.append("而${futureYear}年将是您事业的重要突破点。\n\n")
        
        // 添加感情部分
        sb.append("【感情】\n")
        val lovePlanets = listOf("金星", "火星", "月亮", "木星")
        val lovePlanet = lovePlanets[random.nextInt(lovePlanets.size)]
        
        sb.append("在感情方面，您的星盘显示${lovePlanet}对您的影响较大，")
        
        val loveTraits = listOf(
            "这意味着您注重关系中的稳定和安全",
            "这表明您在关系中非常重视沟通和理解",
            "这意味着您往往以情感和直觉指导关系",
            "这暗示您在感情中寻求自由和独立",
            "这表明您对伴侣有较高的标准和期望"
        )
        
        sb.append(loveTraits[random.nextInt(loveTraits.size)] + "。")
        
        // 感情状态预测
        val loveYear = currentYear + random.nextInt(2)
        sb.append("${loveYear}年对您的感情生活有特殊意义，可能会有重要的关系转变或进展。\n\n")
        
        // 添加建议部分
        sb.append("【建议】\n")
        
        // 如果有具体问题，添加针对性建议
        if (question != null) {
            sb.append("关于\"${question}\"的问题，根据您的星盘分析：\n\n")
            
            val specificAdvices = listOf(
                "目前星象对您有利，可以积极行动。最佳时机在月亮进入${zodiacSigns[random.nextInt(zodiacSigns.size)]}座时。",
                "需要等待更有利的星象对齐，建议在${zodiacSigns[random.nextInt(zodiacSigns.size)]}季节行动更佳。",
                "本阶段应专注于收集信息和规划，待土星离开逆行后再做决定。",
                "星盘显示这是一个转变期，适合在新月或满月期间做出改变。",
                "当前星相表明，问题的解决需要您结合理性分析与直觉感受。"
            )
            
            sb.append(specificAdvices[random.nextInt(specificAdvices.size)] + "\n\n")
        }
        
        // 添加一般性建议
        val generalAdvices = listOf(
            "利用您的${selectedTraits.firstOrNull() ?: "创造力"}优势，发挥潜能",
            "注意观察天象变化对您情绪的影响",
            "寻找平衡各个生活领域的方法",
            "重视自我反思和内在成长",
            "关注星象周期变化，把握有利时机",
            "记录星象变化与个人体验，增进自我了解",
            "尝试与您星座相合的活动和环境",
            "培养与您主导行星能量协调的习惯"
        )
        
        // 添加3-5条建议
        val adviceCount = 3 + random.nextInt(3)
        val selectedAdvices = generalAdvices.shuffled(random).take(adviceCount)
        
        for (i in selectedAdvices.indices) {
            sb.append("${i + 1}. ${selectedAdvices[i]}")
            if (i < selectedAdvices.size - 1) {
                sb.append("\n")
            }
        }
        
        // 添加一个特别总结
        sb.append("\n\n【总结】\n")
        sb.append("您的星盘显示了丰富而独特的能量组合，关键是如何平衡这些能量并发挥其最大潜能。")
        sb.append("特别注意${strongestElement}的影响，同时关注${weakestElement}相关领域的发展。")
        sb.append("在${currentYear}至${nextYear}年间，关注${careerPlanets[random.nextInt(careerPlanets.size)]}")
        sb.append("和${lovePlanets[random.nextInt(lovePlanets.size)]}的行运轨迹，这将对您的事业和关系发展带来重要影响。")
        
        return sb.toString()
    }
    
    /**
     * 模拟塔罗牌响应（专门为塔罗牌方法设计）
     */
    private fun simulateTarotResponse(prompt: String): String {
        // 使用固定的种子使每次结果不同但可预测
        val currentTime = System.currentTimeMillis()
        val random = Random(currentTime)
        
        // 提取用户输入的关键信息
        val questionMatch = "预测问题：([^\\n]+)|咨询问题：([^\\n]+)".toRegex().find(prompt)
        val fullNameMatch = "全名：([^\\n]+)".toRegex().find(prompt)
        
        val question = questionMatch?.groupValues?.get(1)?.takeIf { it.isNotEmpty() } 
            ?: questionMatch?.groupValues?.get(2)?.trim()
        val fullName = fullNameMatch?.groupValues?.get(1)?.trim()
        
        val sb = StringBuilder()
        
        // 生成牌阵概述
        sb.append("【概览】\n")
        
        if (fullName != null) {
            sb.append("${fullName}，")
        }
        
        if (question != null) {
            sb.append("关于\"${question}\"的问题，")
        } else {
            sb.append("根据您的提问，")
        }
        
        // 随机选择牌阵类型
        val spreadTypes = listOf("三张牌展开", "凯尔特十字牌阵", "过去-现在-未来牌阵", "五张牌展开", "关系牌阵")
        val spreadType = spreadTypes[random.nextInt(spreadTypes.size)]
        
        sb.append("我为您进行了${spreadType}塔罗牌分析。")
        
        val cardCount = when (spreadType) {
            "三张牌展开", "过去-现在-未来牌阵" -> 3
            "五张牌展开" -> 5
            "凯尔特十字牌阵" -> 10
            "关系牌阵" -> 6
            else -> 3
        }
        
        // 塔罗牌名称列表
        val majorArcana = listOf(
            "愚者", "魔术师", "女祭司", "女皇", "皇帝", "教皇", "恋人", "战车", 
            "力量", "隐士", "命运之轮", "正义", "倒吊人", "死神", "节制", 
            "恶魔", "塔", "星星", "月亮", "太阳", "审判", "世界"
        )
        
        val minorSuits = listOf("圣杯", "星币", "宝剑", "权杖")
        val minorRanks = listOf("Ace", "二", "三", "四", "五", "六", "七", "八", "九", "十", "侍从", "骑士", "王后", "国王")
        
        val allCards = mutableListOf<String>()
        allCards.addAll(majorArcana)
        
        for (suit in minorSuits) {
            for (rank in minorRanks) {
                allCards.add("$suit$rank")
            }
        }
        
        // 选择不重复的牌
        val selectedCards = allCards.shuffled(random).take(cardCount)
        val cardPositions = listOf("正位", "逆位")
        
        sb.append("本次抽取的牌为：")
        for (i in 0 until cardCount) {
            val position = cardPositions[random.nextInt(2)]
            sb.append("\n${i + 1}. ${selectedCards[i]}（${position}）")
        }
        sb.append("\n\n")
        
        // 生成牌面解读
        sb.append("【牌面解读】\n")
        
        // 根据牌阵类型生成不同的位置标题
        val positionTitles = when (spreadType) {
            "过去-现在-未来牌阵" -> listOf("过去", "现在", "未来")
            "凯尔特十字牌阵" -> listOf("核心问题", "障碍", "潜意识", "过去", "现状", "未来", "自我态度", "环境影响", "希望或恐惧", "最终结果")
            "关系牌阵" -> listOf("您自己", "对方", "关系现状", "关系基础", "过去影响", "未来发展")
            else -> (1..cardCount).map { "第${it}张牌" }
        }
        
        for (i in 0 until cardCount) {
            if (i < positionTitles.size) {
                sb.append("${positionTitles[i]}：${selectedCards[i]}\n")
            } else {
                sb.append("位置${i + 1}：${selectedCards[i]}\n")
            }
            
            // 生成牌面解释
            val meanings = listOf(
                "象征着内在的力量与潜能",
                "代表着变革与转机",
                "显示出挑战与机遇并存",
                "暗示着需要放下过去，迎接新的开始",
                "建议保持耐心与坚持",
                "提醒注意内心的真实需求",
                "表明当前处于选择的关键时刻",
                "体现了成长与进步的可能",
                "昭示着即将到来的变化"
            )
            
            sb.append("${meanings[random.nextInt(meanings.size)]}。")
            
            // 为每个牌添加一些细节解读
            val details = listOf(
                "牌面中的${listOf("光明", "黑暗", "水流", "山脉", "天空", "大地").random()}元素，暗示着${listOf("希望", "挑战", "流动性", "稳定性", "开阔视野", "根基").random()}。",
                "牌中人物的${listOf("姿态", "表情", "服饰", "手势").random()}透露出${listOf("内在力量", "犹豫不决", "自信满满", "深思熟虑").random()}的状态。",
                "牌的${listOf("色彩", "构图", "象征物").random()}与您的问题形成有趣的呼应。"
            )
            
            sb.append("${details[random.nextInt(details.size)]}\n\n")
        }
        
        // 生成综合分析
        sb.append("【深度分析】\n")
        
        // 根据所选牌的组合生成综合分析
        val hasPositiveCards = selectedCards.any { it in listOf("魔术师", "太阳", "星星", "世界", "力量", "女皇", "正义") }
        val hasNegativeCards = selectedCards.any { it in listOf("死神", "塔", "恶魔", "月亮", "倒吊人") }
        val hasNeutralCards = selectedCards.any { it in listOf("隐士", "命运之轮", "审判", "节制", "教皇") }
        
        if (hasPositiveCards && !hasNegativeCards) {
            sb.append("整体牌面呈现积极向上的能量。")
        } else if (hasNegativeCards && !hasPositiveCards) {
            sb.append("牌面显示当前您可能面临一些挑战。")
        } else {
            sb.append("牌面展示了复杂而多元的能量场域。")
        }
        
        // 添加更多综合分析
        val analysisPoints = listOf(
            "从整体牌阵的能量分布来看，${listOf("水元素", "火元素", "土元素", "风元素").random()}的强势表明您在${listOf("情感", "行动", "实务", "思考").random()}方面有特殊的优势。",
            
            "值得注意的是，牌阵中${if (random.nextBoolean()) "大阿尔卡纳" else "小阿尔卡纳"}牌的比例较高，这通常意味着${
                listOf(
                    "命运力量正在发挥主导作用", 
                    "您自身的选择将影响结果", 
                    "外部环境因素影响显著", 
                    "您处于生命的重要转折点"
                ).random()
            }。",
            
            "牌阵中的${selectedCards[random.nextInt(cardCount)]}与${selectedCards[random.nextInt(cardCount)]}形成有趣的互动，这可能提示${
                listOf(
                    "内在冲突需要调和", 
                    "矛盾的力量将带来创造性突破", 
                    "不同领域的经验可以相互借鉴", 
                    "当前的选择将影响多个生活方面"
                ).random()
            }。"
        )
        
        // 添加2-3点分析
        val analysisCount = 2 + random.nextInt(2)
        for (i in 0 until analysisCount) {
            sb.append("\n\n${analysisPoints[i % analysisPoints.size]}")
        }
        
        sb.append("\n\n")
        
        // 生成建议
        sb.append("【建议】\n")
        
        // 根据牌面情况给出建议
        val adviceCount = 3 + random.nextInt(2) // 3-4条建议
        val advices = listOf(
            "留意内心的直觉，它往往比逻辑思考更能引领您找到答案。",
            "现在是审视自身价值观和优先事项的好时机。",
            "保持开放心态，接纳变化带来的新可能性。",
            "与信任的人分享您的想法，外部视角可能带来意想不到的启示。",
            "适当的休息和反思将帮助您找到正确的方向。",
            "注意力集中在您能够控制的事物上，放下对不确定因素的担忧。",
            "信任时间的力量，一些事情需要自然发展。",
            "勇敢面对挑战，每个困难都蕴含成长的机会。",
            "重视细节，但不要忘记全局视角。",
            "寻找平衡点，避免走向任何极端。"
        )
        
        // 如果有特定问题，添加一条针对性建议
        if (question != null) {
            val specificAdvices = listOf(
                "关于\"${question}\"，牌面显示此时最好${if (random.nextBoolean()) "保持耐心" else "果断行动"}。",
                "在\"${question}\"这个问题上，塔罗提示您可能需要重新审视自己的${if (random.nextBoolean()) "期望" else "方法"}。",
                "对于\"${question}\"，牌面建议您寻求${if (random.nextBoolean()) "平衡与和谐" else "突破与创新"}。"
            )
            sb.append("${specificAdvices[random.nextInt(specificAdvices.size)]}\n\n")
        }
        
        // 添加一般性建议
        val selectedAdvices = advices.shuffled(random).take(adviceCount)
        for (i in selectedAdvices.indices) {
            sb.append("${i + 1}. ${selectedAdvices[i]}")
            if (i < selectedAdvices.size - 1) {
                sb.append("\n")
            }
        }
        
        return sb.toString()
    }
    
    /**
     * 模拟周公解梦响应（专门为解梦方法设计）
     */
    private fun simulateDreamResponse(prompt: String): String {
        // 使用固定的种子使每次结果不同但可预测
        val currentTime = System.currentTimeMillis()
        val random = Random(currentTime)
        
        // 提取用户输入的关键信息
        val dreamContentMatch = "梦境内容：([^\\n]+)|梦见：([^\\n]+)|梦到：([^\\n]+)".toRegex().find(prompt)
        val fullNameMatch = "全名：([^\\n]+)".toRegex().find(prompt)
        
        val dreamContent = dreamContentMatch?.groupValues?.let { 
            it[1].takeIf { it.isNotEmpty() } ?: it[2].takeIf { it.isNotEmpty() } ?: it[3].takeIf { it.isNotEmpty() }
        }?.trim()
        
        val fullName = fullNameMatch?.groupValues?.get(1)?.trim()
        
        // 尝试从提示词中提取更多梦境信息
        val promptLowerCase = prompt.lowercase()
        val commonDreams = listOf(
            "飞", "坠落", "追逐", "考试", "迟到", "裸体", "死亡", "失去牙齿", "怀孕", "蛇", 
            "水", "火", "动物", "亲人", "钱", "房子", "车", "婚礼", "工作", "旅行",
            "鬼", "怪物", "名人", "前任", "陌生人", "食物", "学校", "医院", "战争"
        )
        
        // 尝试识别梦境类型
        var detectedDream = ""
        for (dream in commonDreams) {
            if (promptLowerCase.contains("梦见$dream") || 
                promptLowerCase.contains("梦到$dream") || 
                promptLowerCase.contains("梦中$dream") ||
                promptLowerCase.contains("梦境中$dream")) {
                detectedDream = dream
                break
            }
        }
        
        // 如果没有提取到明确的梦境内容，但发现了常见梦境关键词
        val effectiveDreamContent = dreamContent ?: if (detectedDream.isNotEmpty()) {
            "梦见$detectedDream"
        } else {
            "梦境内容不明确"
        }
        
        val sb = StringBuilder()
        
        // 生成梦境概述部分
        sb.append("【梦境概述】\n")
        
        if (fullName != null) {
            sb.append("${fullName}，")
        }
        
        if (effectiveDreamContent != "梦境内容不明确") {
            sb.append("关于您「${effectiveDreamContent}」的梦境，")
        } else {
            sb.append("关于您所描述的梦境，")
        }
        
        // 梦境类型
        val dreamTypes = listOf("预示梦", "反映梦", "创造性梦境", "日常反射梦", "潜意识表达梦", "清醒梦", "重复梦")
        val selectedDreamType = dreamTypes[random.nextInt(dreamTypes.size)]
        
        sb.append("这是一种典型的${selectedDreamType}。")
        
        // 添加梦境形成原因
        val dreamCauses = listOf(
            "这类梦境通常源于您近期的心理状态和情绪变化",
            "这种梦可能反映了您潜意识中的某些担忧或期待",
            "这一梦境可能是由您最近的经历或周围环境变化引起的",
            "这种梦境往往与您内心的欲望或恐惧有关",
            "这类梦境通常是大脑整合记忆和处理情绪的方式"
        )
        
        sb.append(dreamCauses[random.nextInt(dreamCauses.size)] + "。\n\n")
        
        // 添加传统解梦部分
        sb.append("【传统解读】\n")
        
        // 根据不同梦境内容提供不同解读
        val traditionalInterpretations = mapOf(
            "飞" to "梦见飞翔通常象征着自由、超越和成功的渴望。在传统周公解梦中，这被视为吉兆，预示着事业上会有提升或突破。",
            "坠落" to "梦见坠落反映了不安全感和对失控的恐惧。传统解梦认为，这可能预示着近期会遇到一些挑战，需要更加谨慎地处理事务。",
            "追逐" to "被追逐的梦境通常代表着逃避某种压力或责任。古籍中认为，这种梦可能暗示您需要面对而非逃避当前的问题。",
            "考试" to "考试梦反映了对评判和失败的焦虑。传统解梦认为，这预示着近期可能面临重要选择或考验，需要充分准备。",
            "迟到" to "梦见迟到通常表示对错失机会的担忧。周公解梦认为这提醒您把握当前机遇，不要拖延重要事项。",
            "裸体" to "裸体梦通常反映脆弱感或对暴露真实自我的恐惧。传统解释认为这可能暗示您在某些社交场合感到不自在或不安全。",
            "死亡" to "死亡梦象征着结束和新生。在传统解梦中，这通常不是不祥之兆，而是代表一个阶段的结束和新阶段的开始。",
            "失去牙齿" to "梦见牙齿掉落在传统解梦中与家人健康和沟通有关。这可能反映了对失去能力或控制的恐惧。",
            "怀孕" to "怀孕梦象征着新想法、项目或关系的发展。传统上认为这是创造力和新开始的预兆。",
            "蛇" to "梦见蛇在传统解梦中有双重含义，可代表智慧和转变，也可能象征潜在的威胁或欺骗。具体解释需结合梦境细节。",
            "水" to "水的梦境与情感状态密切相关。清澈平静的水代表内心平和，而湍急或浑浊的水则暗示情绪波动或不安。",
            "火" to "火象征转变、净化或激情。在周公解梦中，梦见火可能预示生活中的重大变化或情感上的强烈体验。",
            "动物" to "动物梦通常反映我们的本能和特质。不同动物有不同象征，传统解梦认为这与您内在品质或当前面临的情况有关。",
            "亲人" to "梦见亲人通常反映您与他们的关系状态或对他们的思念。传统上认为，已故亲人出现在梦中可能是在传递信息或提供指引。",
            "钱" to "梦见钱财在传统解释中通常与机遇和资源有关。赚钱预示好运，而丢钱则可能暗示需要注意资源管理。",
            "房子" to "房子在梦中代表自我和安全感。传统解梦认为，不同状态的房子反映了您对自身生活空间和安全感的认知。",
            "车" to "车辆象征人生旅程和控制感。传统解释认为，梦见驾车反映了您如何掌控自己的人生方向。",
            "婚礼" to "婚礼梦不仅与爱情有关，也代表着人生中的承诺和转变。在传统解梦中，这通常被视为新阶段开始的象征。",
            "工作" to "工作相关的梦通常反映职业压力或成就感。传统解释认为，这类梦境反映了您的职业抱负和担忧。",
            "旅行" to "旅行梦象征人生旅程和探索。在周公解梦中，这通常被视为积极的变化和新机遇的征兆。",
            "鬼" to "梦见鬼怪在传统解梦中通常与未解决的恐惧或过去的问题有关。这提醒您可能需要面对过去或内心的阴暗面。",
            "怪物" to "怪物象征内心的恐惧或被压抑的情绪。传统解释认为，这类梦境提醒您需要勇敢面对困难和挑战。",
            "名人" to "梦见名人可能反映您对某些品质或成就的向往。传统解梦认为，这与您的抱负或自我认同有关。",
            "前任" to "前任出现在梦中通常与未解决的感情或过去的经验有关。这可能暗示您需要释放过去，才能更好地前行。",
            "陌生人" to "陌生人在梦中常代表自我的未知面向或新的可能性。传统解释认为，这可能预示着新关系或机会的到来。",
            "食物" to "食物梦与滋养和满足需求有关。传统解梦认为，不同的食物象征不同的精神或情感需求。",
            "学校" to "学校梦常反映对表现和评价的焦虑。传统上认为，这类梦境提醒您重视学习和个人成长。",
            "医院" to "医院梦境与健康忧虑或疗愈需求有关。传统解释认为，这提醒注意身心健康，或暗示需要休息和调养。",
            "战争" to "战争梦象征内心或外部的冲突。在传统解梦中，这类梦境反映了内心的矛盾或人际关系中的紧张。"
        )
        
        var interpretationProvided = false
        
        // 如果能识别特定梦境元素，提供相应解读
        if (detectedDream.isNotEmpty() && traditionalInterpretations.containsKey(detectedDream)) {
            sb.append(traditionalInterpretations[detectedDream] ?: "")
            interpretationProvided = true
        } else if (dreamContent != null) {
            // 尝试从梦境内容中识别关键词
            for ((keyword, interpretation) in traditionalInterpretations) {
                if (dreamContent.contains(keyword)) {
                    sb.append(interpretation)
                    interpretationProvided = true
                    break
                }
            }
        }
        
        // 如果没有找到特定解读，提供通用解读
        if (!interpretationProvided) {
            val generalInterpretations = listOf(
                "根据周公解梦的传统理论，您的梦境反映了潜意识中的变化与调整。梦中的情景和情绪是现实生活的投射，提示您关注内心需求。",
                "传统解梦学认为，这类梦境常与人生转变期有关，象征着旧阶段的结束和新阶段的开始。梦中的象征元素反映了您对这一转变的感受。",
                "周公解梦中认为，此类梦境通常与内心渴望或隐忧有关。梦境通过象征性图像表达了您可能尚未完全意识到的感受。",
                "古籍中解释，这种梦境往往是大脑整合记忆和处理情绪的自然过程。梦中出现的场景和人物可能是过去经历的重组。"
            )
            
            sb.append(generalInterpretations[random.nextInt(generalInterpretations.size)])
        }
        
        // 添加传统吉凶判断
        val fortunes = listOf("吉", "中吉", "小吉", "凶中带吉", "吉中有凶", "小凶", "大凶")
        var fortuneWeight = listOf(3, 2, 2, 1, 1, 1, 0)  // 权重倾向于吉利的结果
        
        // 如果梦到一些特别内容，调整权重
        if (detectedDream in listOf("死亡", "坠落", "追逐", "鬼", "战争")) {
            fortuneWeight = listOf(1, 1, 2, 2, 2, 1, 1)  // 更平衡的权重
        }
        
        var fortune = ""
        val fortuneRoll = random.nextInt(fortuneWeight.sum())
        var accumulatedWeight = 0
        
        for (i in fortunes.indices) {
            accumulatedWeight += fortuneWeight[i]
            if (fortuneRoll < accumulatedWeight) {
                fortune = fortunes[i]
                break
            }
        }
        
        sb.append("\n\n根据周公解梦古籍记载，此梦「${fortune}」。\n\n")
        
        // 添加心理学解读部分
        sb.append("【现代解析】\n")
        
        val psychologicalInterpretations = listOf(
            "从现代心理学角度看，这类梦境常反映潜意识中的情绪处理过程。弗洛伊德认为，梦是被压抑欲望的表达；而荣格则视梦为通往集体无意识的窗口。",
            "现代梦境分析认为，梦是大脑处理和整合日常经历的方式。您的梦可能是在帮助您处理最近经历的事件或情绪变化。",
            "心理学家解释，这种梦境类型通常与自我认同和成长有关。梦境内容反映了您内心正在发生的变化和调整过程。",
            "当代研究表明，此类梦境常与压力和情绪调节相关。梦可能是您处理压力或适应变化的一种方式。",
            "从神经科学角度看，这种梦境模式可能与记忆巩固和情绪处理有关。大脑在梦中重新组织和整合经验，形成新的认知联系。"
        )
        
        sb.append(psychologicalInterpretations[random.nextInt(psychologicalInterpretations.size)] + " ")
        
        // 添加个人情境相关解析
        val personalContexts = listOf(
            "您的梦境可能反映了当前生活中的某些变化或挑战。思考一下，梦中的元素如何与您的现实经历相呼应？",
            "这个梦可能是提醒您关注被忽视的情感或需求。梦中的象征常常代表我们在清醒状态下容易忽略的思绪。",
            "您的梦境可能在提示您重新评估某些关系或生活方向。梦中出现的人物和情境可能象征着现实中需要关注的方面。",
            "这类梦常与自我成长和转变有关。思考梦中的挑战可能代表您人生旅程中的哪些成长机会？",
            "您的梦可能是在处理未解决的情感或冲突。梦境提供了一个安全的空间，使潜意识能够探索和处理复杂感受。"
        )
        
        sb.append(personalContexts[random.nextInt(personalContexts.size)] + "\n\n")
        
        // 添加建议部分
        sb.append("【启示与建议】\n")
        
        // 根据梦境内容提供针对性建议
        var adviceProvided = false
        
        val dreamAdvices = mapOf(
            "飞" to "这个梦鼓励您释放创造力，追求自由和突破界限。在现实生活中，可以尝试拓展舒适区，探索新的可能性。",
            "坠落" to "这个梦提醒您关注生活中的稳定和安全感。建议审视当前压力来源，建立更坚实的支持系统，适当放慢步伐，确保自己站稳脚跟。",
            "追逐" to "这个梦境鼓励您直面而非逃避挑战。建议列出当前困扰您的问题，制定具体应对计划，必要时寻求支持，转被动为主动。",
            "考试" to "这类梦境建议您正视对评判的焦虑。可以通过充分准备、设定合理期望、学习接受不完美来缓解压力，记住，成长比完美更重要。",
            "迟到" to "这个梦提醒您关注时间管理和对重要事项的规划。建议评估当前优先级，制定更合理的时间表，避免拖延重要决定。",
            "裸体" to "这个梦鼓励您接纳真实的自己，建议反思在哪些场合或关系中感到脆弱，学习在保持真实的同时建立健康界限。",
            "死亡" to "这个梦象征转变和新生，鼓励您接纳生活的变化。建议反思哪些方面需要结束，哪些新的可能性正在开启，允许自己告别过去，拥抱未来。",
            "失去牙齿" to "这类梦境通常与沟通和表达有关。建议关注人际交往中的沟通方式，确保有效表达自己的观点和需求，同时保持适当自信。",
            "怀孕" to "这个梦鼓励您关注正在发展的新想法或项目。给自己足够的时间和空间来培育这些'种子'，相信创造过程，耐心等待成果。",
            "蛇" to "蛇的梦境提示您关注转变和智慧。建议思考生活中哪些方面需要蜕变，警惕潜在的欺骗或隐患，同时拥抱自我更新的过程。",
            "水" to "水的梦境建议您关注情绪健康。可以通过冥想、写日记或与信任的人分享感受来探索和处理情绪，让情感如水般流动而不淤积。",
            "火" to "火的梦境鼓励您拥抱变化和净化。审视哪些过时的思维模式或习惯需要放下，允许激情引导您，但注意控制其强度，避免'燃尽'。",
            "动物" to "动物梦鼓励您关注本能和直觉。反思梦中动物代表的品质在您生活中如何体现，学习平衡理性思考和直觉反应。",
            "亲人" to "这类梦境提醒您关注家庭关系。可以主动联系亲人，表达关爱，解决可能存在的隔阂，珍视这些重要联系。",
            "钱" to "关于金钱的梦境建议您审视资源管理和价值观。评估当前财务状况，确保资源分配符合真正的优先事项，记住财富不仅限于物质。",
            "房子" to "房子梦提醒您关注安全感和自我认同。思考如何创造更舒适的生活环境，无论物理空间还是心理空间，都应反映真实的自我。",
            "车" to "车辆梦鼓励您思考人生方向和控制感。评估当前路径是否符合真实目标，学习在保持方向的同时，也能灵活应对路上的变化。",
            "婚礼" to "婚礼梦提示您关注承诺和人生转变。思考当前的承诺是否符合内心期望，准备好迎接新阶段，同时确保决定基于真实意愿而非外界压力。",
            "工作" to "工作梦境建议您平衡职业抱负和个人满足感。评估工作压力来源，明确职业目标，确保职业道路与个人价值观一致。",
            "旅行" to "旅行梦鼓励您拥抱变化和新体验。可以计划实际旅行，或在日常生活中寻找小冒险，保持好奇心，拓宽视野。",
            "鬼" to "鬼怪梦境提醒您面对内心恐惧。勇敢审视那些被忽视或压抑的情绪，允许自己感受不适，然后寻找健康方式释放和转化。",
            "怪物" to "怪物梦境鼓励您正视内心阴暗面。接纳这些'阴影'作为自我的一部分，理解它们的根源，寻找健康方式整合这些能量。",
            "名人" to "名人梦提示您反思自我认同和抱负。思考梦中名人代表的品质如何反映您的渴望，寻找机会在现实中培养这些特质。",
            "前任" to "前任梦境建议您处理未解决的感情。允许自己感受和接纳这些情绪，从过往关系中学习，然后有意识地释放，为未来腾出空间。",
            "陌生人" to "陌生人梦鼓励您探索未知可能性。对新关系和体验保持开放，同时认识自我的不同面向，拥抱成长和变化。",
            "食物" to "食物梦提醒您关注身心滋养。思考哪些方面需要'喂养'——身体、心灵、关系或创造力，确保各方面需求得到平衡满足。",
            "学校" to "学校梦鼓励终身学习和成长。反思哪些领域您希望提升，制定学习计划，同时记住真正的成长不仅关乎成绩，也关乎全面发展。",
            "医院" to "医院梦提醒您关注健康和自我修复。审视身心需求，寻找适当方式放松和恢复，记住休息也是生产力的一部分。",
            "战争" to "战争梦境建议您关注内心和外部冲突。识别冲突根源，学习健康的冲突解决方式，平衡坚持原则与灵活妥协。"
        )
        
        if (detectedDream.isNotEmpty() && dreamAdvices.containsKey(detectedDream)) {
            sb.append(dreamAdvices[detectedDream] ?: "")
            adviceProvided = true
        } else if (dreamContent != null) {
            // 尝试从梦境内容中识别关键词
            for ((keyword, advice) in dreamAdvices) {
                if (dreamContent.contains(keyword)) {
                    sb.append(advice)
                    adviceProvided = true
                    break
                }
            }
        }
        
        // 如果没有找到特定建议，提供通用建议
        if (!adviceProvided) {
            val generalAdvices = listOf(
                "建议您记录这个梦及其引发的感受，看看它如何与您的现实生活产生共鸣。梦境日记可以帮助识别模式和主题，加深自我了解。\n\n尝试在睡前进行短暂冥想，创造平静的心境有助于更有意义的梦境体验。记住，梦是潜意识的表达，倾听它们可能带来意想不到的洞见。",
                
                "这个梦可能在提示您关注生活平衡。评估工作、关系、健康和个人成长各方面，看是否有被忽视的领域需要更多关注。\n\n在日常生活中留出时间进行反思，无论是通过冥想、散步还是创造性活动，都能帮助整合梦境带来的启示。",
                
                "建议您思考梦境中的情绪如何与现实生活中的感受相连。情绪常常是重要信息的载体，值得深入探索。\n\n尝试使用创造性方式（如绘画、写作或音乐）表达梦境体验，这可以帮助处理和理解潜意识内容，促进自我成长。",
                
                "这个梦提醒您关注内在需求和真实愿望。在做决定时，多问问自己'这真的是我想要的吗？'，确保选择符合内心价值观。\n\n修习正念可以增强对当下的觉察，帮助您更清晰地感知内在指引，无论是来自梦境还是直觉。"
            )
            
            sb.append(generalAdvices[random.nextInt(generalAdvices.size)])
        }
        
        // 引用经典解梦理论
        val classicQuotes = listOf(
            "正如《周公解梦》中所说：\"知梦则知命，解梦则解人。\"梦境是自我认识的重要窗口。",
            "古人云：\"日有所思，夜有所梦。\"梦常反映我们清醒时的思虑和关注。",
            "弗洛伊德认为：\"梦是通往无意识的皇家大道。\"通过解读梦境，我们能更好地理解自己。",
            "荣格指出：\"梦是潜意识自发而公正的自我表达。\"梦境常包含我们意识所忽略的智慧。",
            "东方传统认为：\"吉梦主吉，凶梦主凶，吉中有凶，凶中有吉。\"梦境解读应全面而辩证。"
        )
        
        sb.append("\n\n" + classicQuotes[random.nextInt(classicQuotes.size)])
        
        return sb.toString()
    }
    
    /**
     * 模拟周易预测响应
     */
    private fun simulateZhouYiResponse(question: String): String {
        try {
            val random = Random()
            val sb = StringBuilder()
            
            // 随机选择一个六十四卦
            val hexagrams = arrayOf("乾", "坤", "屯", "蒙", "需", "讼", "师", "比", "小畜", "履", "泰", "否",
                "同人", "大有", "谦", "豫", "随", "蛊", "临", "观", "噬嗑", "贲", "剥", "复",
                "无妄", "大畜", "颐", "大过", "坎", "离", "咸", "恒", "遁", "大壮", "晋", "明夷",
                "家人", "睽", "蹇", "解", "损", "益", "夬", "姤", "萃", "升", "困", "井",
                "革", "鼎", "震", "艮", "渐", "归妹", "丰", "旅", "巽", "兑", "涣", "节",
                "中孚", "小过", "既济", "未济")
            val hexagram = hexagrams[random.nextInt(hexagrams.size)]
            
            // 随机生成变爻
            val changedLines = mutableListOf<Int>()
            val changeCount = random.nextInt(3) // 0-2个变爻
            for (i in 0 until changeCount) {
                val line = random.nextInt(6) + 1 // 1-6爻
                if (!changedLines.contains(line)) {
                    changedLines.add(line)
                }
            }
            
            // 添加卦象部分
            sb.append("【卦象】\n")
            sb.append("您所求问的\"${question}\"，得到${hexagram}卦")
            if (changedLines.isNotEmpty()) {
                sb.append("，变爻在第${changedLines.joinToString("、")}爻")
            }
            sb.append("。\n\n")
            
            sb.append("${hexagram}卦象征着")
            // 为每个卦象添加一个简单描述
            when (hexagram) {
                "乾" -> sb.append("纯阳刚健，代表强大的创造力和领导力")
                "坤" -> sb.append("纯阴柔顺，代表包容和顺从")
                "屯" -> sb.append("初始阶段的困难和阻碍")
                "蒙" -> sb.append("蒙昧未开，需要启蒙和教育")
                // 添加更多卦象的描述...
                else -> sb.append("一种特定的能量状态，反映了您当前的处境和未来趋势")
            }
            sb.append("。\n\n")
            
            // 添加卦辞解析部分
            sb.append("【卦辞解析】\n")
            sb.append("《周易》中${hexagram}卦的卦辞是：\"")
            // 为常见卦象添加卦辞
            when (hexagram) {
                "乾" -> sb.append("元亨利贞")
                "坤" -> sb.append("元亨利牝马之贞")
                "屯" -> sb.append("元亨利贞，勿用有攸往，利建侯")
                "蒙" -> sb.append("亨。匪我求童蒙，童蒙求我。初筮告，再三渎，渎则不告。利贞")
                // 添加更多卦象的卦辞...
                else -> sb.append("此卦有其特定卦辞，表达了特定的哲理和指导")
            }
            sb.append("\"。\n\n")
            
            sb.append("这个卦辞的现代解释是：")
            when (hexagram) {
                "乾" -> sb.append("象征着宏图大展、事业兴旺的征兆。建议保持坚韧不拔的态度，遵循正道，自然能获得成功。")
                "坤" -> sb.append("象征着顺应自然、谦和包容的态度。像温顺的母马一样，稳健前行，不急不躁，自然会达到目的。")
                "屯" -> sb.append("表示虽然面临初始的困难，但最终会有所成就。建议暂时不要贸然行动，而是先打好基础，建立制度和规则。")
                "蒙" -> sb.append("象征蒙昧状态下需要启蒙和引导。如童蒙般纯真，需要适当的教育和指导。保持正道，不可反复无常。")
                // 添加更多卦象的解释...
                else -> sb.append("这个卦象揭示了您当前处境中的机遇和挑战，指导您如何应对当前局面以获得最佳结果。")
            }
            sb.append("\n\n")
            
            // 添加爻辞分析
            if (changedLines.isNotEmpty()) {
                sb.append("您的变爻是：")
                changedLines.forEach { line ->
                    sb.append("\n第${line}爻：")
                    // 随机生成爻辞解释
                    val lineDescriptions = arrayOf(
                        "这表示当前处于转变期，需要保持谨慎和耐心。",
                        "此爻象征即将到来的机遇，但需要有足够的准备才能把握。",
                        "此爻暗示您可能面临一些挑战，但通过坚持和努力能够克服。",
                        "这预示着一个积极的变化正在发生，保持开放心态。",
                        "此爻提醒您注意细节，不要忽视小事，它们可能带来重要影响。"
                    )
                    sb.append(lineDescriptions[random.nextInt(lineDescriptions.size)])
                }
                sb.append("\n\n")
            }
            
            // 添加情境解读部分
            sb.append("【情境解读】\n")
            sb.append("针对您的问题\"${question}\"，从${hexagram}卦的角度分析：\n\n")
            
            // 基于问题类型生成相关解读
            if (question.contains("事业") || question.contains("工作") || question.contains("职业")) {
                sb.append("在事业方面，${hexagram}卦预示")
                val careerReadings = arrayOf(
                    "您当前可能正面临职业转型的关键期。这个卦象建议您根据自身优势做出选择，不要盲目跟随他人的建议。现阶段适合沉淀自己，积累专业知识和技能，为未来的发展打下坚实基础。",
                    "工作上可能会遇到一些挑战，但这些挑战正是您成长的机会。这个卦象提醒您要保持坚韧的态度，不畏困难，积极寻求解决问题的方法。您的努力终将获得认可和回报。",
                    "职业发展处于上升期，但需要稳扎稳打。不要急于求成或跳槽，而是应该在现有岗位上不断提升自己的能力和价值。时机成熟时，晋升和发展自然会到来。",
                    "可能有新的工作机会即将出现，但需要您做好充分准备并保持警觉。这个卦象建议您提前完善自己的专业技能，扩展人际网络，以便在机会来临时能够把握住。"
                )
                sb.append(careerReadings[random.nextInt(careerReadings.size)])
            } else if (question.contains("感情") || question.contains("婚姻") || question.contains("爱情")) {
                sb.append("在感情方面，${hexagram}卦显示")
                val loveReadings = arrayOf(
                    "您的感情生活可能正处于转变期。这个卦象提醒您审视自己真正的情感需求，不要因为外界压力或者习惯而勉强维持不健康的关系。真正的感情需要双方的真诚付出和有效沟通。",
                    "感情中需要更多的耐心和理解。您可能期望立即看到改变，但感情的发展需要时间和双方的共同努力。这个卦象建议您放下急躁的心态，给彼此更多成长的空间。",
                    "良好的感情关系正在形成或增强。这个卦象预示着和谐与稳定，但仍需要您保持真诚的态度和有效的沟通。避免将小问题积累成大矛盾，及时表达自己的想法和感受。",
                    "可能需要面对一些感情上的困难或抉择。这个卦象提醒您内心的声音往往是最可靠的指引。无论做出什么决定，确保它符合您的真实意愿和长远幸福。"
                )
                sb.append(loveReadings[random.nextInt(loveReadings.size)])
            } else if (question.contains("财富") || question.contains("投资") || question.contains("钱财")) {
                sb.append("在财富方面，${hexagram}卦建议")
                val wealthReadings = arrayOf(
                    "近期的财务状况可能会有所波动，但不必过度担忧。这个卦象建议您建立合理的财务规划，增加收入来源的多样性，避免将所有资金投入单一项目。保持稳健的投资策略将帮助您渡过可能的不稳定期。",
                    "财务上应该更加谨慎和保守。这个卦象警示可能的风险和陷阱，建议您不要被短期利益诱惑而做出冲动决策。现阶段适合巩固已有成果，减少不必要的支出，积累财富。",
                    "有望获得财务上的突破或增长。但这个卦象提醒您，真正的财富来自于长期积累和明智管理，而非投机取巧。现在是制定长远财务目标和投资战略的好时机。",
                    "财务状况将随着您的努力逐步改善。这个卦象显示，踏实工作和理性消费是提升经济状况的关键。同时，保持开放心态，可能有意外的财富机会出现。"
                )
                sb.append(wealthReadings[random.nextInt(wealthReadings.size)])
            } else {
                sb.append("对于您的问题，${hexagram}卦给出的指引是：")
                val generalReadings = arrayOf(
                    "当前局面看似复杂，但实际上正在朝着有利的方向发展。这个卦象建议您保持耐心和信心，不要被表面的困难所干扰。坚持正确的方向，最终会看到积极的结果。",
                    "您可能正处于一个需要做出决策的关键时刻。这个卦象提醒您，明智的选择不仅基于眼前利益，更要考虑长远影响。倾听内心的声音，它通常能给出最适合您的指引。",
                    "当前局面存在一些不确定性，但这也意味着有新的可能性。这个卦象建议您保持灵活的心态，准备适应可能的变化。同时，不要忽视细节，它们可能包含重要信息。",
                    "面对的挑战虽然艰巨，但您具备克服它们的能力。这个卦象鼓励您发挥自身优势，寻求必要的帮助，勇敢面对困难。每一次挑战都是成长的机会。"
                )
                sb.append(generalReadings[random.nextInt(generalReadings.size)])
            }
            sb.append("\n\n")
            
            // 添加传统智慧引用
            sb.append("《易经》有云：\"")
            val wisdomQuotes = arrayOf(
                "天行健，君子以自强不息",
                "地势坤，君子以厚德载物",
                "善不积不足以成名，恶不积不足以灭身",
                "君子藏器于身，待时而动",
                "知几其神乎，君子上交不谄，下交不渎",
                "与时偕行，随时处变",
                "天地之大德曰生",
                "君子以俭德辟难"
            )
            val selectedQuote = wisdomQuotes[random.nextInt(wisdomQuotes.size)]
            sb.append(selectedQuote)
            sb.append("\"，这一古老智慧在您当前的情况中尤为适用。\n\n")
            
            // 添加建议部分
            sb.append("【建议】\n")
            sb.append("基于${hexagram}卦的启示，给您的具体建议是：\n\n")
            
            // 随机生成几点建议
            val adviceCount = 3 + random.nextInt(3) // 3-5点建议
            val advicePool = arrayOf(
                "保持内心的平静和耐心，不要被短期的波动所干扰。",
                "审慎决策，避免冲动行事，确保每一步都经过深思熟虑。",
                "寻求值得信任的人的建议，但最终的决定应该由自己做出。",
                "增强自身能力和知识，为未来的机会做好准备。",
                "避免过度担忧未知的结果，专注于当下能够掌控的事情。",
                "保持灵活的心态，准备好适应可能的变化和调整。",
                "在合适的时机表达自己的想法和感受，避免积累负面情绪。",
                "建立合理的计划和目标，逐步推进，不要期望一蹴而就。",
                "在做出重要决定前，确保考虑了所有可能的后果和影响。",
                "平衡工作与休息，保持身心健康，这是成功的基础。",
                "寻找能够激发您热情和创造力的活动，它们可以带来新的视角和灵感。",
                "不要害怕改变和挑战，它们往往是成长和进步的催化剂。"
            )
            
            for (i in 1..adviceCount) {
                var advice = advicePool[random.nextInt(advicePool.size)]
                while (sb.toString().contains(advice)) { // 避免重复建议
                    advice = advicePool[random.nextInt(advicePool.size)]
                }
                sb.append("${i}. $advice\n")
            }
            sb.append("\n")
            
            // 添加总结部分
            sb.append("【总结】\n")
            sb.append("简单来说，")
            
            // 根据卦象生成总结
            val summaryByHexagram = mapOf(
                "乾" to "您现在正处于充满活力和潜力的阶段，要保持坚定不移的毅力和自信心。只要按照正确的方向努力，一定能取得成功。说白了，就是坚持做对的事，不要轻易放弃，好运气自然会来敲门。",
                "坤" to "现在需要您保持谦逊和包容的态度，顺应形势发展。就像大地一样，不声不响地承载万物，却成就了巨大的功业。简单说，先别着急表现自己，默默积累实力，时机成熟时再出手。",
                "屯" to "您目前面临的困难是暂时的，就像春天的种子刚刚破土，看似弱小却充满生机。关键是不要急着求成，而是要打好基础。用大白话说，眼下可能有点艰难，但挺过去就是一片光明，关键是别放弃。"
            )
            
            val defaultSummaries = arrayOf(
                "这个周易卦象告诉我们，您现在所处的局面虽然有一定挑战，但只要按照建议行事，问题会迎刃而解。换句话说，别太担心，按部就班地做事，结果自然会好。",
                "简单总结一下，您现在需要的是耐心和毅力，而不是立竿见影的方法。踏实走好每一步，不急不躁，自然能看到好结果。",
                "说白了，这个卦象是在提醒您，现在最重要的是保持清醒的头脑，不要被情绪左右。冷静分析，理性决策，事情就会向好的方向发展。",
                "总的来说，周易提醒您现在是蓄势待发的时候，不要急于求成。做好准备工作，等待合适的时机，成功就在不远处等着您。"
            )
            
            sb.append(summaryByHexagram.getOrDefault(hexagram, defaultSummaries[random.nextInt(defaultSummaries.size)]))
            
            return sb.toString()
        } catch (e: Exception) {
            Log.e(TAG, "生成周易模拟响应时出错", e)
            return """
                【卦象】
                模拟周易卦象生成过程中出现错误。
                
                【卦辞解析】
                由于技术原因，无法提供完整解析。这可能是由于系统临时故障或数据处理问题。
                
                【建议】
                1. 请重新尝试占卜
                2. 如果问题持续，可以尝试使用其他占卜方式
                3. 确保输入的问题清晰明确
                
                【总结】
                简单说，系统现在遇到了点小问题，建议您稍后再试，或者换个算命方式。
            """
        }
    }
    
    /**
     * 智能分割文本内容为有意义的小节
     */
    private fun subdivideSection(title: String, contentLines: List<String>): List<ResultSection> {
        val result = mutableListOf<ResultSection>()
        
        try {
            // 如果内容行数很少，直接返回原始内容
            if (contentLines.size <= 5) {
                result.add(ResultSection(title, contentLines.joinToString("\n")))
                return result
            }
            
            val lines = contentLines.filter { it.trim().isNotEmpty() }
            val totalLines = lines.size
            
            // 尝试基于内容特征分组（空行、标题特征等）
            val groups = mutableListOf<MutableList<String>>()
            var currentGroup = mutableListOf<String>()
            groups.add(currentGroup)
            
            // 可能的标题模式：数字开头、短横线开头、冒号结尾等
            val titlePattern = Regex("^(\\d+\\.|\\*|•|-|【|\\[).*|.*[:：]\\s*$")
            
            for (line in lines) {
                if (line.trim().isEmpty()) continue
                
                if (titlePattern.matches(line.trim()) || 
                    (currentGroup.isNotEmpty() && line.length < 30 && line.trim().endsWith("："))) {
                    // 如果当前行看起来像标题且当前组不为空，创建新组
                    if (currentGroup.isNotEmpty()) {
                        currentGroup = mutableListOf<String>()
                        groups.add(currentGroup)
                    }
                }
                
                currentGroup.add(line)
            }
            
            // 如果只有一个组或组过多，使用简单分段
            if (groups.size == 1 || groups.size > 5) {
                // 简单地按长度分为2-3段
                val segmentCount = if (totalLines > 15) 3 else 2
                val segmentSize = totalLines / segmentCount
                
                for (i in 0 until segmentCount) {
                    val start = i * segmentSize
                    val end = if (i == segmentCount - 1) totalLines else (i + 1) * segmentSize
                    val title = if (segmentCount > 1) "$title ${toRomanNumeral(i + 1)}" else title
                    result.add(ResultSection(
                        title,
                        lines.subList(start, end).joinToString("\n")
                    ))
                }
            } else {
                // 使用分组结果
                for ((i, group) in groups.withIndex()) {
                    if (group.isEmpty()) continue
                    
                    // 尝试识别组标题
                    val groupTitle = if (i == 0) {
                        title
                    } else if (group[0].length < 20 && (group[0].endsWith("：") || group[0].endsWith(":") ||
                        group[0].startsWith("-") || group[0].startsWith("*"))) {
                        group[0].replace(Regex("^[-*\\s]+"), "").trim()
                    } else {
                        "$title ${toRomanNumeral(i + 1)}"
                    }
                    
                    result.add(ResultSection(groupTitle, group.joinToString("\n")))
                }
            }
            
            return result
        } catch (e: Exception) {
            // 出现任何错误，返回原内容
            Log.e(TAG, "细分内容时出错", e)
            result.add(ResultSection(title, contentLines.joinToString("\n")))
            return result
        }
    }
    
    /**
     * 为周公解梦内容创建合理的分段
     */
    private fun splitDreamContent(content: String): List<ResultSection> {
        val sections = mutableListOf<ResultSection>()
        
        try {
            // 周公解梦解析的几个典型部分
            val lines = content.split("\n").filter { it.trim().isNotEmpty() }
            
            if (lines.isEmpty()) {
                // 如果内容为空，返回一个默认部分
                sections.add(ResultSection("解梦分析", "无法解析内容，请重新尝试"))
                return sections
            }
            
            if (lines.size <= 1) {
                // 如果只有一行内容，直接作为"解梦分析"
                sections.add(ResultSection("解梦分析", content.trim()))
                return sections
            }
            
            // 检查内容中是否已经有明确的标题段落
            val titlePattern = Regex("(【|\\[|\\(|「|『)(.*?)(】|\\]|\\)|」|』)")
            val hasTitles = lines.any { titlePattern.find(it) != null }
            
            if (hasTitles) {
                // 如果已经有标题格式，按现有标题分段
                var currentTitle = "梦境概要"
                val tempSections = mutableMapOf<String, MutableList<String>>()
                tempSections[currentTitle] = mutableListOf()
                
                for (line in lines) {
                    val titleMatch = titlePattern.find(line)
                    if (titleMatch != null) {
                        // 找到新标题
                        currentTitle = titleMatch.groupValues[2].trim()
                        if (!tempSections.containsKey(currentTitle)) {
                            tempSections[currentTitle] = mutableListOf()
                        }
                        // 添加不包含标题符号的内容
                        val remainingContent = line.replaceFirst(titleMatch.value, "").trim()
                        if (remainingContent.isNotEmpty()) {
                            tempSections[currentTitle]?.add(remainingContent)
                        }
                    } else {
                        // 继续添加到当前标题
                        tempSections[currentTitle]?.add(line)
                    }
                }
                
                // 将临时段落转换为最终结果
                for ((title, contentLines) in tempSections) {
                    if (contentLines.isNotEmpty()) {
                        sections.add(ResultSection(title, contentLines.joinToString("\n")))
                    }
                }
            } else {
                // 如果没有明确标题，使用关键词来分段
                val overviewLines = mutableListOf<String>()
                val interpretationLines = mutableListOf<String>()
                val symbolLines = mutableListOf<String>()
                val psychoLines = mutableListOf<String>()
                val adviceLines = mutableListOf<String>()
                
                var currentSection = overviewLines
                
                // 基于关键词对内容进行分类
                for (line in lines) {
                    when {
                        line.contains("梦境概要") || line.contains("梦境简述") || line.contains("梦境描述") || 
                        line.contains("总体") || line.contains("概览") || line.contains("简述") -> {
                            currentSection = overviewLines
                        }
                        line.contains("象征") || line.contains("象征意义") || line.contains("象征物") -> {
                            currentSection = symbolLines
                        }
                        line.contains("心理") || line.contains("心理分析") || line.contains("心理学解释") -> {
                            currentSection = psychoLines
                        }
                        line.contains("解析") || line.contains("解梦") || line.contains("解释") || 
                        line.contains("传统") || line.contains("传统解释") || line.contains("民间") -> {
                            currentSection = interpretationLines
                        }
                        line.contains("建议") || line.contains("忠告") || line.contains("指引") || 
                        line.contains("提示") || line.contains("行动") -> {
                            currentSection = adviceLines
                        }
                    }
                    
                    currentSection.add(line)
                }
                
                // 根据内容分配段落
                if (overviewLines.isNotEmpty()) {
                    sections.add(ResultSection("梦境概要", overviewLines.joinToString("\n")))
                }
                
                if (symbolLines.isNotEmpty()) {
                    sections.add(ResultSection("象征意义", symbolLines.joinToString("\n")))
                }
                
                if (interpretationLines.isNotEmpty()) {
                    sections.add(ResultSection("梦境解析", interpretationLines.joinToString("\n")))
                }
                
                if (psychoLines.isNotEmpty()) {
                    sections.add(ResultSection("心理分析", psychoLines.joinToString("\n")))
                }
                
                if (adviceLines.isNotEmpty()) {
                    sections.add(ResultSection("建议", adviceLines.joinToString("\n")))
                }
            }
            
            // 如果没有找到任何分段，尝试智能分段
            if (sections.isEmpty()) {
                // 尝试智能分段：按段落长度切分
                if (lines.size >= 10) {
                    // 基本的三部分分段
                    val totalLines = lines.size
                    val firstPartEnd = (totalLines * 0.3).toInt().coerceAtLeast(3)
                    val secondPartEnd = (totalLines * 0.7).toInt().coerceAtMost(totalLines - 3)
                    
                    sections.add(ResultSection(
                        "梦境概述", 
                        lines.subList(0, firstPartEnd).joinToString("\n")
                    ))
                    
                    sections.add(ResultSection(
                        "解析", 
                        lines.subList(firstPartEnd, secondPartEnd).joinToString("\n")
                    ))
                    
                    sections.add(ResultSection(
                        "建议", 
                        lines.subList(secondPartEnd, totalLines).joinToString("\n")
                    ))
                } else {
                    // 内容较少，直接添加为一个部分
                    sections.add(ResultSection("梦境解析", content.trim()))
                }
            }
            
        } catch (e: Exception) {
            // 如果解析出现异常，返回原文本作为一个分段
            Log.e(TAG, "解梦分段处理异常", e)
            sections.add(ResultSection("梦境解析", content.trim()))
        }
        
        return sections
    }
    
    /**
     * 将数字转换为罗马数字表示
     */
    private fun toRomanNumeral(number: Int): String {
        return when (number) {
            1 -> "I"
            2 -> "II"
            3 -> "III"
            4 -> "IV"
            5 -> "V"
            6 -> "VI"
            7 -> "VII"
            8 -> "VIII"
            9 -> "IX"
            10 -> "X"
            else -> number.toString()
        }
    }
    
    /**
     * 使用协程发送邮件
     * 
     * @param subject 邮件主题
     * @param body 邮件内容
     * @param toEmail 收件人邮箱，默认为FEEDBACK_EMAIL
     * @return 发送成功返回true，否则返回false
     */
    suspend fun sendEmail(
        subject: String, 
        body: String, 
        toEmail: String = AppConfig.EmailConfig.FEEDBACK_EMAIL
    ): Boolean {
        // 使用EmailSender发送邮件
        return EmailSender.sendEmail(subject, body, toEmail)
    }
    
    /**
     * 从内容中提取可能的标题
     */
    private fun extractTitleFromContent(content: String): String {
        // 尝试从内容中提取标题
        val lines = content.split("\n")
        if (lines.isNotEmpty()) {
            val firstLine = lines[0].trim()
            if (firstLine.length < 30) {
                return firstLine
            }
        }
        return "解析"
    }
    
    /**
     * 将响应文本解析为多个部分
     * 将响应文本拆分为多个部分
     * 可以从外部调用，用于处理模拟函数返回的结果
     * 添加全面的异常处理，确保不会因解析错误导致应用崩溃
     */
    fun parseResponseToSections(content: String): List<ResultSection> {
        val sections = mutableListOf<ResultSection>()
        
        try {
            // 检查内容是否为空
            if (content.isBlank()) {
                sections.add(ResultSection("结果", "无法获取有效内容"))
                return sections
            }
            
            // 尝试根据标题格式分段
            val titlePattern = "【([^】]+)】|\\[([^\\]]+)\\]|^(.*?)[：:]".toRegex(RegexOption.MULTILINE)
            val titleMatches = titlePattern.findAll(content)
            
            // 如果找到标题格式的分隔符
            if (titleMatches.count() > 0) {
                var currentTitle = "概述"
                var currentContent = StringBuilder()
                var lastIndex = 0
                
                for (match in titleMatches) {
                    // 如果不是第一个标题，保存上一部分内容
                    if (match.range.first > lastIndex && currentContent.isNotEmpty()) {
                        sections.add(ResultSection(currentTitle, currentContent.toString().trim()))
                        currentContent = StringBuilder()
                    }
                    
                    // 提取新标题
                    currentTitle = match.groupValues[1].ifEmpty { 
                        match.groupValues[2].ifEmpty { match.groupValues[3] }
                    }
                    
                    // 提取此标题下的内容
                    val titleEnd = match.range.last + 1
                    val nextMatchStart = titleMatches.find { it.range.first > titleEnd }?.range?.first ?: content.length
                    
                    if (nextMatchStart > titleEnd) {
                        currentContent.append(content.substring(titleEnd, nextMatchStart))
                    }
                    
                    lastIndex = nextMatchStart
                }
                
                // 添加最后一部分
                if (currentContent.isNotEmpty()) {
                    sections.add(ResultSection(currentTitle, currentContent.toString().trim()))
                }
            } else {
                // 没有找到标准格式的标题，尝试智能分段
                val lines = content.split("\n").filter { it.trim().isNotEmpty() }
                
                // 如果内容很短，直接添加为一个部分
                if (lines.size <= 5) {
                    sections.add(ResultSection("结果", content.trim()))
                } else {
                    // 根据内容长度，尝试将其分为2-3个部分
                    val partCount = if (lines.size > 15) 3 else 2
                    val partSize = lines.size / partCount
                    
                    for (i in 0 until partCount) {
                        val start = i * partSize
                        val end = if (i == partCount - 1) lines.size else (i + 1) * partSize
                        
                        // 尝试为每个部分找一个合适的标题
                        val partTitle = when (i) {
                            0 -> "综合分析"
                            partCount - 1 -> "建议"
                            else -> "详细解读"
                        }
                        
                        val partContent = lines.subList(start, end).joinToString("\n")
                        sections.add(ResultSection(partTitle, partContent))
                    }
                }
            }
            
            // 检查是否生成了有效的部分
            if (sections.isEmpty()) {
                sections.add(ResultSection("结果", content.trim()))
            }
            
            return sections
        } catch (e: Exception) {
            // 出现任何异常，记录错误并返回简单的结果
            Log.e(TAG, "解析响应文本出错", e)
            sections.add(ResultSection("结果", "解析内容时出现错误，原始内容如下：\n\n${content.take(1000)}"))
            return sections
        }
    }
    
    /**
     * 分割周易内容为多个部分
     */
    private fun splitZhouYiContent(content: String): List<ResultSection> {
        val sections = mutableListOf<ResultSection>()
        
        try {
            // 增加日志记录，便于调试
            Log.d(TAG, "开始解析周易内容，长度: ${content.length}")
            
            // 首先检查内容是否为空
            if (content.isBlank()) {
                Log.e(TAG, "周易内容为空!")
                sections.add(ResultSection("提示", "未能生成有效的周易卦象解析"))
                return sections
            }
            
            // 尝试使用标准格式进行分段
            val sectionPattern = "【([^】]+)】([\\s\\S]*?)(?=【[^】]+】|$)"
            val matcher = Pattern.compile(sectionPattern).matcher(content)
            
            var found = false
            
            // 查找所有使用【】格式的部分
            while (matcher.find()) {
                found = true
                val title = matcher.group(1)?.trim() ?: "周易解析"
                val sectionContent = matcher.group(2)?.trim() ?: ""
                
                // 只有当标题和内容都不为空时才添加到结果中
                if (title.isNotBlank() && sectionContent.isNotBlank()) {
                    sections.add(ResultSection(title, sectionContent))
                }
            }
            
            // 如果没有找到标准格式，尝试其他方式
            if (!found) {
                // 尝试使用默认解析方法
                Log.w(TAG, "周易内容未使用标准【】格式，尝试其他解析方式")
                return parseResponseToSections(content)
            }
            
            // 确保至少返回一个部分
            if (sections.isEmpty()) {
                Log.w(TAG, "解析结果为空，使用简化解析")
                
                // 尝试简单分段
                val lines = content.split("\n")
                val plainText = lines.joinToString("\n")
                
                // 至少返回一个基本部分
                sections.add(ResultSection("卦象解析", plainText))
            }
            
            // 记录日志
            Log.d(TAG, "周易内容解析完成，生成 ${sections.size} 个部分")
            
            return sections
        } catch (e: Exception) {
            // 捕获所有可能的异常
            Log.e(TAG, "分割周易内容时出错", e)
            
            // 确保返回非空结果
            if (sections.isEmpty()) {
                try {
                    // 尝试使用最简单的文本分割
                    val plainText = content.replace("【", "===").replace("】", "===")
                    val parts = plainText.split("===")
                    
                    if (parts.size > 2) {
                        // 有可能分离出标题和内容
                        var i = 0
                        while (i < parts.size - 1) {
                            val title = parts[i].trim()
                            val content = parts[i + 1].trim()
                            
                            if (title.isNotBlank() && content.isNotBlank()) {
                                sections.add(ResultSection(title, content))
                            }
                            
                            i += 2
                        }
                    }
                    
                    // 如果还是没有有效部分，添加整个内容
            if (sections.isEmpty()) {
                        sections.add(ResultSection("周易解析", content))
                    }
                } catch (ex: Exception) {
                    // 真的出错了，返回错误信息
                    Log.e(TAG, "无法解析周易内容：${ex.message}")
                    sections.add(ResultSection("解析错误", "无法解析卦象内容，请重试"))
                }
            }
            
            return sections
        }
    }
    
    /**
     * 解析API响应，生成占卜结果对象
     * 增加全面的异常处理以防应用崩溃
     */
    private fun parseResponse(method: DivinationMethod, inputData: Map<String, String>, response: String): DivinationResult {
        try {
            // 解析JSON响应
            val jsonResponse = JSONObject(response)
            
            var responseContent = ""
            
            // 获取内容
            if (jsonResponse.has("choices")) {
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() > 0) {
                    val choice = choices.getJSONObject(0)
                    if (choice.has("message")) {
                        responseContent = choice.getJSONObject("message").getString("content")
                    }
                }
            }
            
            // 如果无法获取内容，使用备用方法尝试解析
            if (responseContent.isBlank()) {
                Log.w(TAG, "无法从标准格式获取响应内容，尝试备用解析方法")
                responseContent = extractTextFromJson(response)
            }
            
            // 如果仍然为空，使用模拟响应
            if (responseContent.isBlank()) {
                Log.w(TAG, "响应内容为空，使用模拟响应")
                responseContent = simulateResponse(method.name)
            }
            
            // 将文本切分为多个部分并处理
            val sections = parseResponseToSections(responseContent)
            
            // 如果是中国传统算命方法，在结果前面添加卦诗
            val resultSections = mutableListOf<ResultSection>()
            
            // 检查是否为中国传统算命方法（type=1）
            if (method.type == 1) {
                // 生成卦诗并添加到结果最前面
                val poem = generateDivinationPoem(method, inputData, responseContent)
                resultSections.add(poem)
            }
            
            // 添加原始解析的结果部分
            resultSections.addAll(sections)
            
            // 创建结果对象
            return DivinationResult(
                id = UUID.randomUUID().toString(),
                methodId = method.id,
                createTime = Date(),
                inputData = inputData,
                resultSections = resultSections
            )
        } catch (e: Exception) {
            Log.e(TAG, "解析响应失败", e)
            
            // 返回简化的错误结果
            val errorSections = mutableListOf<ResultSection>()
            
            // 即使出错，也尝试为中国传统算命方法添加卦诗（使用简化内容）
            if (method.type == 1) {
                val poem = generateDivinationPoem(method, inputData, "错误解析")
                errorSections.add(poem)
            }
            
            errorSections.add(ResultSection(
                title = "解析错误",
                content = "解析AI响应时出现错误: ${e.message}\n\n原始响应:\n${response.take(200)}..."
            ))
            
            // 添加模拟结果作为备用
            if (method.id == "zhouyi") {
                // 对周易使用特殊的模拟
                val simulatedResponse = simulateZhouYiResponse(method.name)
                val sections = parseResponseToSections(simulatedResponse)
                errorSections.addAll(sections)
            } else {
                // 对其他方法使用通用模拟
                val simulatedResponse = simulateResponse(method.name)
                val sections = parseResponseToSections(simulatedResponse)
                errorSections.addAll(sections)
            }
            
            return DivinationResult(
                id = UUID.randomUUID().toString(),
                methodId = method.id,
                createTime = Date(),
                inputData = inputData,
                resultSections = errorSections
            )
        }
    }
    
    /**
     * 尝试从各种可能的JSON格式中提取文本内容
     */
    private fun extractTextFromJson(jsonStr: String): String {
        try {
            val jsonObject = JSONObject(jsonStr)
            
            // 尝试多种可能的路径
            val possiblePaths = listOf(
                arrayOf("choices", 0, "message", "content"),
                arrayOf("choices", 0, "text"),
                arrayOf("output", "text"),
                arrayOf("result", "text"),
                arrayOf("content"),
                arrayOf("text")
            )
            
            for (path in possiblePaths) {
                try {
                    var current: Any = jsonObject
                    for (key in path) {
                        current = if (key is Int) {
                            (current as JSONArray).get(key)
                        } else {
                            (current as JSONObject).get(key.toString())
                        }
                    }
                    if (current is String && current.isNotBlank()) {
                        return current
                    }
                } catch (e: Exception) {
                    // 继续尝试下一个路径
                    continue
                }
            }
            
            // 如果上述方法都失败，尝试直接返回整个JSON字符串
            return jsonStr
        } catch (e: Exception) {
            // 如果不是有效JSON，直接返回原字符串
            return jsonStr
        }
    }
    
    /**
     * 为塔罗牌内容创建适当的分段
     */
    private fun subdivideTarotSection(title: String, contentLines: List<String>): List<ResultSection> {
        val result = mutableListOf<ResultSection>()
        
        try {
            // 如果内容行数很少，直接返回原始内容
            if (contentLines.size <= 5) {
                result.add(ResultSection(title, contentLines.joinToString("\n")))
                return result
            }
            
            // 尝试识别塔罗牌内容中的小标题
            val groups = mutableListOf<MutableList<String>>()
            var currentGroup = mutableListOf<String>()
            groups.add(currentGroup)
            
            // 小标题模式：数字编号、短横线开头、冒号结尾等
            val titlePattern = Regex("^(\\d+\\.|\\*|•|-|【|\\[).*|.*[:：]\\s*$")
            
            for (line in contentLines) {
                if (line.trim().isEmpty()) continue
                
                if (titlePattern.matches(line.trim()) || 
                    (currentGroup.isNotEmpty() && line.length < 30 && line.trim().endsWith("："))) {
                    // 如果当前行看起来像标题且当前组不为空，创建新组
                    if (currentGroup.isNotEmpty()) {
                        currentGroup = mutableListOf<String>()
                        groups.add(currentGroup)
                    }
                }
                
                currentGroup.add(line)
            }
            
            // 处理分组，生成结果
            if (groups.size == 1) {
                // 没有找到明显的小标题，使用通用方法分段
                return subdivideSection(title, contentLines)
            }
            
            // 从组中提取标题
            for (group in groups) {
                if (group.isEmpty()) continue
                
                val groupTitle: String
                val groupContent: List<String>
                
                // 判断第一行是否是标题
                if (titlePattern.matches(group[0].trim()) || group[0].length < 30) {
                    // 第一行作为标题
                    groupTitle = group[0].replace(Regex("^[\\d\\*•\\-【\\[]+\\.?\\s*"), "")
                                         .replace(Regex("[:：]\\s*$"), "")
                                         .trim()
                    groupContent = if (group.size > 1) group.subList(1, group.size) else listOf("无详细内容")
                } else {
                    // 使用原标题
                    groupTitle = title
                    groupContent = group
                }
                
                result.add(ResultSection(groupTitle, groupContent.joinToString("\n")))
            }
            
            return result
        } catch (e: Exception) {
            // 出现任何错误，返回原内容
            Log.e(TAG, "细分塔罗内容时出错", e)
            result.add(ResultSection(title, contentLines.joinToString("\n")))
            return result
        }
    }
    
    /**
     * 解析API错误消息，返回用户友好的错误说明
     */
    private fun getApiErrorMessage(errorJson: String): String {
        try {
            val jsonObject = JSONObject(errorJson)
            
            // 尝试提取错误信息
            if (jsonObject.has("error")) {
                val errorObj = jsonObject.getJSONObject("error")
                if (errorObj.has("message")) {
                    return errorObj.getString("message")
                }
                // 有些API返回type字段
                if (errorObj.has("type")) {
                    return "错误类型: " + errorObj.getString("type")
                }
            }
            
            // 返回完整错误，但限制长度
            return if (errorJson.length > 100) errorJson.substring(0, 100) + "..." else errorJson
        } catch (e: Exception) {
            Log.w(TAG, "解析API错误信息失败", e)
            return "无法解析错误详情"
        }
    }
    
    /**
     * 为中国传统算命结果生成卦诗
     * 根据算命方法和输入数据生成一首与结果相符的卦诗
     */
    private fun generateDivinationPoem(method: DivinationMethod, inputData: Map<String, String>, content: String): ResultSection {
        // 从内容中提取主题关键词来生成更相关的卦诗
        var theme = ""
        try {
            // 提取内容中的关键词
            val keywords = extractKeywords(content)
            theme = keywords.take(3).joinToString("、")
        } catch (e: Exception) {
            Log.e(TAG, "提取关键词失败", e)
        }
        
        // 根据算命方法选择合适的卦诗风格
        val poemTitle = when (method.id) {
            "zhouyi" -> "卦诗"
            "bazi" -> "命诗"
            "ziwei" -> "星诗"
            "almanac" -> "日诗"
            "dream" -> "梦诗"
            else -> "卦诗"
        }
        
        // 生成卦诗内容
        val poem = StringBuilder()
        
        // 如果有主题，先添加主题标题行并加上空行分隔
        if (theme.isNotEmpty()) {
            poem.append("《").append(theme).append("》\n\n")
        }
        
        // 对于不同的算命方法，生成不同风格的卦诗
        poem.append(when (method.id) {
            "zhouyi" -> {
                try {
                    // 从内容中提取卦象
                    var gua = "卦象未知"
                    val guaPattern = Regex("【卦象】[\\s\\S]*?得到(.)卦")
                    val match = guaPattern.find(content)
                    if (match != null && match.groupValues.size > 1) {
                        gua = match.groupValues[1]
                    } else {
                        // 尝试其他模式匹配
                        val altPattern = Regex("卦象是([乾坤震艮坎离巽兑])卦")
                        val altMatch = altPattern.find(content)
                        if (altMatch != null && altMatch.groupValues.size > 1) {
                            gua = altMatch.groupValues[1]
                        } else {
                            // 如果无法提取，随机选择一个
                            gua = listOf("乾", "坤", "震", "艮", "坎", "离", "巽", "兑").random()
                        }
                    }
                    
                    // 记录卦象信息以便日志追踪
                    Log.d(TAG, "为周易卦象'$gua'生成卦诗")
                    
                    generatePoemViaAPI(gua, "周易", theme)
                } catch (e: Exception) {
                    // 所有方法都失败时，使用最简单的备用诗
                    Log.e(TAG, "生成周易卦诗过程中发生错误", e)
                    val gua = listOf("乾", "坤", "震", "艮", "坎", "离", "巽", "兑").random()
                    "${gua}卦示幽微，天机暗中来。\n一言能解惑，万象自安排。\n运势如流水，前程似锦绣。\n莫问前程事，心静自然明。"
                }
            }
            "bazi" -> {
                try {
                    // 尝试从内容中提取用户的命主用神或五行属性
                    var element = ""
                    val patternList = listOf(
                        Regex("喜用神[：:](.*?)[\n,。]"),
                        Regex("日主[：:](.*?)[\n,。]"),
                        Regex("五行属性[：:](.*?)[\n,。]")
                    )
                    
                    for (pattern in patternList) {
                        val match = pattern.find(content)
                        if (match != null && match.groupValues.size > 1) {
                            element = match.groupValues[1].trim()
                            if (element.isNotEmpty()) break
                        }
                    }
                    
                    // 如果没有找到，使用五行元素之一
                    if (element.isEmpty()) {
                        element = listOf("金", "木", "水", "火", "土").random()
                    }
                    
                    Log.d(TAG, "为八字命理元素'$element'生成命诗")
                    
                    generatePoemViaAPI(element, "八字命理", theme)
                } catch (e: Exception) {
                    Log.e(TAG, "生成八字命诗过程中发生错误", e)
                    """
                    命局藏玄机，八字定乾坤。
                    福禄随缘至，祸福自己求。
                    顺势而为进，逆境亦成长。
                    心怀善念行，福报自然来。
                    """.trimIndent()
                }
            }
            "ziwei" -> {
                try {
                    // 尝试从内容中提取紫微命盘主星
                    var star = ""
                    val patternList = listOf(
                        Regex("命宫主星[：:](.*?)[\n,。]"),
                        Regex("命宫[：:](.*?)[\n,。]"),
                        Regex("主星[：:](.*?)[\n,。]")
                    )
                    
                    for (pattern in patternList) {
                        val match = pattern.find(content)
                        if (match != null && match.groupValues.size > 1) {
                            star = match.groupValues[1].trim()
                            if (star.isNotEmpty()) break
                        }
                    }
                    
                    // 如果没有找到，使用紫微主星之一
                    if (star.isEmpty()) {
                        star = listOf("紫微", "天机", "太阳", "武曲", "天同", "廉贞", "天府", "太阴", "贪狼", "巨门", "天相", "天梁", "七杀", "破军").random()
                    }
                    
                    Log.d(TAG, "为紫微斗数主星'$star'生成星诗")
                    
                    generatePoemViaAPI(star, "紫微斗数", theme)
                } catch (e: Exception) {
                    Log.e(TAG, "生成紫微星诗过程中发生错误", e)
                    """
                    紫微垂象示，星辰暗中明。
                    一生多坎坷，终得见光明。
                    命途虽已定，变数在人为。
                    善修身与心，自有福相随。
                    """.trimIndent()
                }
            }
            // ... 为其他算命方法生成诗
            else -> {
                try {
                    // 为其他方法提供通用的诗歌生成
                    val methodName = method.name
                    
                    Log.d(TAG, "为${methodName}生成通用诗")
                    
                    generatePoemViaAPI("", methodName, theme)
                } catch (e: Exception) {
                    Log.e(TAG, "生成通用诗过程中发生错误", e)
                    """
                    天机玄妙不可测，一言难尽古今情。
                    顺应自然心安泰，勿忘初心向前行。
                    福祸无常难预料，心存善念福自来。
                    明心见性修身行，自有天道酬勤心。
                    """.trimIndent()
                }
            }
        })
        
        return ResultSection(poemTitle, poem.toString())
    }
    
    /**
     * 通过API生成诗歌的通用方法
     * @param keyword 主要关键词（卦象/元素/星宿等）
     * @param methodName 算命方法名称
     * @param theme 可选的主题
     * @return 生成的诗歌内容
     */
    private fun generatePoemViaAPI(keyword: String, methodName: String, theme: String = ""): String {
        // 构建诗歌生成提示词
        val poemPrompt = """
        请为${methodName}${if (keyword.isNotEmpty()) "中的「$keyword」" else ""}创作一首优美、富有哲理的诗。
        ${if (theme.isNotEmpty()) "诗的主题是关于「$theme」。" else ""}
        要求：
        1. 八句诗，每句五言或七言，遵循中国古典诗词格律
        2. ${if (keyword.isNotEmpty()) "诗中必须包含「$keyword」字" else "诗中必须体现${methodName}的特点"}
        3. 反映${if (keyword.isNotEmpty()) "$keyword" else methodName}的特质和哲学意义
        4. 风格庄重典雅，有传统韵味
        5. 内容要富有启示性，适合作为占卜结果的引导
        6. 直接输出诗歌内容，不要有任何前言、解释或标题
        """

        // 尝试调用API生成诗歌
        try {
            // 简化的API调用，仅用于诗歌生成
            val requestBody = JSONObject().apply {
                put("model", "deepseek-chat")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", poemPrompt)
                    })
                })
                put("max_tokens", 500)
                put("temperature", 0.8)  // 稍微提高创造性
            }

            // 创建连接并发送请求
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpsURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            // 尝试从应用上下文获取API密钥
            val appContext = getApplicationContext()
            val apiKey = if (appContext != null) {
                getApiKey(appContext)
            } else {
                ""  // 如果无法获取上下文，使用空字符串
            }
            
            Log.d(TAG, "尝试为${methodName}${if (keyword.isNotEmpty()) "中的「$keyword」" else ""}生成诗")
            
            if (apiKey.isNotEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.connectTimeout = 15000  // 15秒连接超时
                connection.readTimeout = 30000     // 30秒读取超时
                connection.doOutput = true

                // 写入请求
                DataOutputStream(connection.outputStream).use { 
                    it.write(requestBody.toString().toByteArray()) 
                }

                // 处理响应
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(
                        InputStreamReader(connection.inputStream)
                    ).use { it.readText() }

                    // 解析JSON响应
                    val jsonResponse = JSONObject(response)
                    val choices = jsonResponse.getJSONArray("choices")
                    if (choices.length() > 0) {
                        val messageObj = choices.getJSONObject(0).getJSONObject("message")
                        val poemContent = messageObj.getString("content").trim()
                        
                        // 记录成功
                        Log.d(TAG, "成功从API获取诗：${poemContent.take(20)}...")
                        return poemContent  // 返回API生成的诗
                    } else {
                        throw Exception("API响应中没有找到诗歌内容")
                    }
                } else {
                    // 读取错误信息
                    val errorResponse = BufferedReader(
                        InputStreamReader(connection.errorStream)
                    ).use { it.readText() }
                    
                    Log.e(TAG, "获取诗歌API错误: ${connection.responseCode}, $errorResponse")
                    throw Exception("API调用失败: ${connection.responseCode}")
                }
            } else {
                Log.w(TAG, "未配置API密钥，无法生成诗")
                throw Exception("未配置API密钥")
            }
        } catch (e: Exception) {
            // 如果API调用失败，使用备用诗
            Log.e(TAG, "诗歌API调用失败: ${e.message}", e)
            
            // 基于关键词和当前时间，生成确定性随机种子
            val seed = ((keyword + methodName).hashCode() + System.currentTimeMillis() / (24 * 60 * 60 * 1000)).toLong()
            val random = Random(seed)
            
            // 基于关键词和算命方法选择备用诗
            val backupPoems = when (methodName) {
                "周易" -> {
                    when (keyword) {
                        "乾" -> listOf(
                            "乾坤大父德，万物仰生辉。\n刚健君子志，自强不息时。\n厚积方能发，谦和处世宜。\n天行道常在，福祉自天随。",
                            "乾元始太极，一气化阴阳。\n刚健君子德，自强志弥坚。\n天行何穷已，大道本无疆。\n顺应天地意，万事皆可昌。",
                            "乾天运行健，日月星辰明。\n君子修厚德，自强永不停。\n九五居尊位，万物仰高明。\n顺天而行事，功成名自成。"
                        )
                        "坤" -> listOf(
                            "坤厚载万物，大地育群生。\n柔顺行君道，容忍养和平。\n善积多福报，恶聚必自倾。\n大道无言说，默默化有形。",
                            "坤土承天泽，厚德载物行。\n母仪天下范，柔德育群生。\n包容万物象，助长众生情。\n谦逊随时节，功成不自矜。",
                            "坤宁静和顺，大地蕴生机。\n厚德方载物，柔道可包容。\n万物皆化育，众生尽安宁。\n君子遵坤德，利在永守贞。"
                        )
                        else -> listOf(
                            "${keyword}卦藏深意，天机暗中来。\n一言能解惑，万象自安排。\n运势如流水，前程似锦绣。\n莫问前程事，心静自然明。",
                            "观${keyword}悟天机，玄妙通古今。\n一念即千里，万象归一心。\n福祸皆有数，吉凶在人为。\n顺应时与势，心安即是真。",
                            "${keyword}象示天机，妙理蕴其中。\n阴阳相辅合，刚柔互相融。\n君子观变化，思索致广大。\n得失皆由己，修身自安康。"
                        )
                    }
                }
                "八字命理" -> listOf(
                    "${keyword}为用养元气，命理显通明。\n先天由父母，后天在修行。\n知命不惧命，顺势而为进。\n天机藏玄妙，静水见本心。",
                    "八字${keyword}为引，命理定乾坤。\n阴阳为道本，五行为命门。\n福禄皆有数，祸福自己求。\n顺势而为进，逆境亦成长。",
                    "命由${keyword}定性，八字藏天机。\n一生多曲折，终将得安宁。\n知命不惧命，立德可改运。\n谋事在人为，成事由天定。"
                )
                "紫微斗数" -> listOf(
                    "${keyword}耀命宫，星辰照人生。\n一世荣枯事，皆在命盘中。\n善修身与德，自有天佑助。\n逆境见真章，静心得妙用。",
                    "紫微${keyword}光，命宫照分明。\n星辰演命数，福祸皆有因。\n知命不迷命，修德可改运。\n天机虽难测，心正自安然。",
                    "${keyword}星垂象，紫微命盘明。\n一生多跌宕，终得见光明。\n命途虽已定，变数在人为。\n修身养浩然，自有福相随。"
                )
                else -> listOf(
                    "天机玄妙不可测，一言难尽古今情。\n顺应自然心安泰，勿忘初心向前行。\n福祸无常难预料，心存善念福自来。\n明心见性修身行，自有天道酬勤心。",
                    "命理${methodName}妙，天机藏微秒。\n万象皆有因，一念可改天。\n顺势而为进，逆境见真情。\n修身正己志，心安福自临。",
                    "${methodName}显天机，玄妙蕴其中。\n一世荣与枯，皆在命中求。\n心正行亦端，处世无愧怍。\n福祸皆有因，修身得安宁。"
                )
            }
            
            // 随机选择一首备用诗
            return backupPoems[random.nextInt(backupPoems.size)]
        }
    }
    
    /**
     * 从文本中提取关键词
     */
    private fun extractKeywords(text: String): List<String> {
        // 简单实现：提取内容中出现次数最多的词语作为关键词
        // 使用更简单的分隔方式避免复杂正则表达式
        val pattern = "[\\s,.。，、；;!！?？:：()（）]+"
        val regex = Regex(pattern)
        val words = text.split(regex)
        val wordCount = mutableMapOf<String, Int>()
        
        // 排除常见虚词和短词
        val stopWords = setOf("的", "了", "是", "在", "我", "你", "他", "她", "它", "有", "和", "与", "这", "那", "为", "以")
        
        for (word in words) {
            val trimmedWord = word.trim()
            if (trimmedWord.length >= 2 && trimmedWord !in stopWords) {
                wordCount[trimmedWord] = (wordCount[trimmedWord] ?: 0) + 1
            }
        }
        
        // 按出现次数排序并返回前10个词
        return wordCount.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key }
    }

    /**
     * 模拟八字命理响应
     */
    private fun simulateBaziResponse(prompt: String): String {
        // 使用固定的种子使每次结果不同但可预测
        val currentTime = System.currentTimeMillis()
        val random = Random(currentTime)
        
        // 提取用户输入的关键信息
        val birthDateMatch = "出生日期：([^\\n]+)".toRegex().find(prompt)
        val birthTimeMatch = "出生时间：([^\\n]+)|出生时辰：([^\\n]+)".toRegex().find(prompt)
        val genderMatch = "性别：([^\\n]+)".toRegex().find(prompt)
        
        val birthDate = birthDateMatch?.groupValues?.get(1)?.trim()
        val birthTime = birthTimeMatch?.groupValues?.get(1)?.takeIf { it.isNotEmpty() } 
            ?: birthTimeMatch?.groupValues?.get(2)?.trim()
        val gender = genderMatch?.groupValues?.get(1)?.trim()
        
        val baziResponse = StringBuilder()
        
        // 八字命盘
        baziResponse.append("【八字命盘】\n")
        
        // 生成五行分析
        val wuxing = arrayOf("金", "木", "水", "火", "土")
        val wuxingCount = IntArray(5)
        
        // 如果有出生日期，生成更个性化的内容
        if (birthDate != null) {
            // 简单解析出年月日
            val dateComponents = birthDate.replace(Regex("[年月日-]"), " ").trim().split(Regex("\\s+"))
            val year = dateComponents.getOrNull(0)?.toIntOrNull() ?: 1990
            val month = dateComponents.getOrNull(1)?.toIntOrNull() ?: 1
            val day = dateComponents.getOrNull(2)?.toIntOrNull() ?: 1
            
            // 简单的天干地支转换
            val tianGan = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
            val diZhi = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
            
            // 年干支（简化计算）
            val yearGan = tianGan[(year - 4) % 10]
            val yearZhi = diZhi[(year - 4) % 12]
            
            // 月干支（简化计算）
            val monthOffset = (year % 10) * 2 + month
            val monthGan = tianGan[monthOffset % 10]
            val monthZhi = diZhi[(month + 2 - 1) % 12]
            
            // 日干支（非常简化的计算，实际应该使用精确算法）
            val dayOffset = ((year * 5 + month * 7 + day) % 10)
            val dayGan = tianGan[dayOffset]
            val dayZhi = diZhi[(month * 2 + day) % 12]
            
            // 时干支（如果有时辰信息）
            var hourGan = "未知"
            var hourZhi = "未知"
            if (birthTime != null) {
                val hour = try {
                    if (birthTime.contains(":")) {
                        birthTime.split(":")[0].toInt()
                    } else {
                        // 假设是中文时辰
                        val timeZhi = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
                        val timeIndex = timeZhi.indexOf(birthTime.replace("时", ""))
                        if (timeIndex >= 0) (timeIndex * 2) + 23 % 24 else 12
                    }
                } catch (e: Exception) {
                    12 // 默认中午
                }
                
                val hourOffset = (dayOffset * 2 + hour / 2) % 10
                hourGan = tianGan[hourOffset]
                hourZhi = diZhi[(hour + 1) / 2 % 12]
            }
            
            // 构建八字命盘
            baziResponse.append("您的八字：$yearGan$yearZhi $monthGan$monthZhi $dayGan$dayZhi")
            if (hourGan != "未知") {
                baziResponse.append(" $hourGan$hourZhi")
            }
            baziResponse.append("\n\n")
            
            // 简单计算五行数量（实际应参考天干地支对应的五行）
            wuxingCount[yearGan.hashCode() % 5]++
            wuxingCount[yearZhi.hashCode() % 5]++
            wuxingCount[monthGan.hashCode() % 5]++
            wuxingCount[monthZhi.hashCode() % 5]++
            wuxingCount[dayGan.hashCode() % 5]++
            wuxingCount[dayZhi.hashCode() % 5]++
            if (hourGan != "未知") {
                wuxingCount[hourGan.hashCode() % 5]++
                wuxingCount[hourZhi.hashCode() % 5]++
            }
            
            // 找出最强和最弱的五行
            val maxIndex = wuxingCount.indices.maxByOrNull { wuxingCount[it] } ?: 0
            val minIndex = wuxingCount.indices.minByOrNull { wuxingCount[it] } ?: 0
            val strongestElement = wuxing[maxIndex]
            val weakestElement = wuxing[minIndex]
            
            baziResponse.append("五行分析：\n")
            for (i in wuxing.indices) {
                baziResponse.append("${wuxing[i]}：")
                repeat(wuxingCount[i]) { baziResponse.append("●") }
                repeat(5 - wuxingCount[i]) { baziResponse.append("○") }
                baziResponse.append("\n")
            }
            baziResponse.append("\n")
            
            // 日主分析
            baziResponse.append("日主：$dayGan$dayZhi\n")
            val dayMaster = when (dayGan) {
                "甲", "乙" -> "木"
                "丙", "丁" -> "火"
                "戊", "己" -> "土"
                "庚", "辛" -> "金"
                "壬", "癸" -> "水"
                else -> "未知"
            }
            
            // 喜用神分析（简化）
            baziResponse.append("喜用神：")
            val likeElement = when (dayMaster) {
                "木" -> "水、木" + if (wuxingCount[2] < 2) "、火" else ""
                "火" -> "木、火" + if (wuxingCount[0] < 2) "、土" else ""
                "土" -> "火、土" + if (wuxingCount[3] < 2) "、金" else ""
                "金" -> "土、金" + if (wuxingCount[1] < 2) "、水" else ""
                "水" -> "金、水" + if (wuxingCount[4] < 2) "、木" else ""
                else -> "未知"
            }
            baziResponse.append("$likeElement\n")
            
            // 忌神分析（简化）
            baziResponse.append("忌神：")
            val dislikeElement = when (dayMaster) {
                "木" -> "金" + if (wuxingCount[0] > 3) "、土" else ""
                "火" -> "水" + if (wuxingCount[3] > 3) "、金" else ""
                "土" -> "木" + if (wuxingCount[1] > 3) "、水" else ""
                "金" -> "火" + if (wuxingCount[4] > 3) "、木" else ""
                "水" -> "土" + if (wuxingCount[2] > 3) "、火" else ""
                else -> "未知"
            }
            baziResponse.append("$dislikeElement\n\n")
        } else {
            // 默认命盘（没有生日信息）
            baziResponse.append("由于缺少出生日期信息，无法生成完整八字命盘。以下是基于一般性原则的分析。\n\n")
            
            // 生成随机五行分布
            for (i in wuxing.indices) {
                wuxingCount[i] = random.nextInt(5) + 1
            }
            
            // 定义随机的最强和最弱五行
            val maxIndex = wuxingCount.indices.maxByOrNull { wuxingCount[it] } ?: 0
            val minIndex = wuxingCount.indices.minByOrNull { wuxingCount[it] } ?: 0
            val strongestElement = wuxing[maxIndex]
            val weakestElement = wuxing[minIndex]
        }

        // 获取最强和最弱五行作为字段存储，避免修改总结部分的代码
        val maxIndex = wuxingCount.indices.maxByOrNull { wuxingCount[it] } ?: 0
        val minIndex = wuxingCount.indices.minByOrNull { wuxingCount[it] } ?: 0
        val strongest = wuxing[maxIndex]
        val weakest = wuxing[minIndex]
        
        // 性格分析
        baziResponse.append("【性格分析】\n")
        val personalityTraits = listOf(
            "您性格中兼具理性与感性，能够在不同场合展现不同的一面。",
            "您处事稳健，不急不躁，能够沉着应对各种挑战。",
            "您具有较强的适应能力，在变化的环境中能够迅速调整自己。",
            "您思维灵活，善于从多角度分析问题，寻找解决方案。",
            "您有较强的自我意识，明确自己的目标和方向。",
            "您重视人际关系，善于与人沟通，建立良好的人际网络。",
            "您处事谨慎，喜欢深思熟虑后再行动。",
            "您有一定的创新精神，不满足于常规，喜欢尝试新事物。"
        )
        
        // 根据性别选择不同的性格描述（如果有性别信息）
        val genderSpecificTraits = if (gender == "男") {
            listOf(
                "作为男性，您展现出果断和坚毅的特质，决策力较强。",
                "您有保护他人的本能，在关键时刻能够挺身而出。",
                "您较为理性，追求事业上的成就和社会认可。"
            )
        } else if (gender == "女") {
            listOf(
                "作为女性，您展现出细腻和洞察力，善于感知他人情绪。",
                "您平衡了柔和与坚韧的特质，面对困难不轻易退缩。",
                "您有较强的情感表达能力，善于维系人际关系。"
            )
        } else {
            listOf(
                "您平衡了阴阳特质，既有决断力又有细腻的一面。",
                "您在不同场合能够展现不同的性格特点，适应性强。",
                "您的处事风格灵活多变，能够根据情况调整自己的行为模式。"
            )
        }
        
        // 随机选择3-5个性格特质
        val selectedTraits = (personalityTraits.shuffled(random).take(3) + 
                             genderSpecificTraits.shuffled(random).take(1)).shuffled()
        selectedTraits.forEach { 
            baziResponse.append("$it\n") 
        }
        baziResponse.append("\n")
        
        // 事业分析
        baziResponse.append("【事业分析】\n")
        val careerAnalysis = listOf(
            "您适合在需要创造力和独立思考的领域发展，如设计、研发或创意行业。",
            "您具有较强的分析能力，适合从事需要数据分析和逻辑思考的工作，如金融、研究或IT领域。",
            "您的沟通能力和人际关系处理能力较强，适合需要团队合作和客户沟通的工作，如销售、市场或公关。",
            "您做事细致耐心，适合需要精确和专注的工作，如技术类或专业服务类工作。",
            "您有领导天赋，适合管理岗位或创业，能够带领团队实现目标。",
            "您对细节有敏锐洞察力，适合质量控制或审计类工作。",
            "您求知欲强，适合持续学习和研究的领域，如学术研究或教育行业。"
        )
        
        // 随机选择2-3个事业特点
        careerAnalysis.shuffled(random).take(random.nextInt(2) + 2).forEach {
            baziResponse.append("$it\n")
        }
        
        // 添加事业发展周期
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val careerPeriods = listOf(
            "近期（${currentYear}-${currentYear+2}年）是您事业发展的关键期，可能面临重要选择或转折。",
            "未来3-5年（${currentYear}-${currentYear+4}年）是您事业稳步上升的阶段，适合稳扎稳打，积累经验和资源。",
            "${currentYear+1}年将是您事业变动较大的一年，可能有职位变动或工作性质的改变。",
            "${currentYear+2}-${currentYear+5}年期间，您的事业将进入一个新的发展周期，有望获得突破性进展。"
        )
        
        // 随机选择一个事业发展周期描述
        baziResponse.append("\n" + careerPeriods[random.nextInt(careerPeriods.size)] + "\n\n")
        
        // 财运分析
        baziResponse.append("【财运分析】\n")
        val wealthAnalysis = listOf(
            "您的财运与事业发展密切相关，事业有成，财运自然随之而来。",
            "您的财运起伏较大，需要注重稳健理财和风险控制。",
            "您有一定的理财天赋，但需要克制冲动消费的倾向。",
            "您适合长期稳健的投资策略，不适合短期投机行为。",
            "您的偏财运较好，可能通过副业或投资获得额外收入。",
            "您的正财运稳定，通过努力工作能获得稳定的收入增长。",
            "您需要注意财务规划，建立完善的储蓄和投资体系。"
        )
        
        // 随机选择2-3个财运特点
        wealthAnalysis.shuffled(random).take(random.nextInt(2) + 2).forEach {
            baziResponse.append("$it\n")
        }
        
        // 财运时期
        val wealthPeriods = listOf(
            "${currentYear}年下半年至${currentYear+1}年初是您财运较好的时期，适合适度投资或理财。",
            "${currentYear+1}年中期可能出现财务波动，建议提前做好准备，避免大额开支。",
            "${currentYear+2}-${currentYear+3}年期间财运呈上升趋势，可能有意外收获或投资回报。",
            "近期到${currentYear+1}年是积累财富的重要阶段，建议增加储蓄比例，减少不必要开支。"
        )
        
        // 随机选择一个财运时期描述
        baziResponse.append("\n" + wealthPeriods[random.nextInt(wealthPeriods.size)] + "\n\n")
        
        // 感情分析
        baziResponse.append("【感情分析】\n")
        val loveAnalysis = if (gender == "男") {
            listOf(
                "您在感情中较为理性，希望建立稳定和谐的关系。",
                "您重视忠诚和信任，寻找能够相互理解和支持的伴侣。",
                "您在感情中可能有一定的保守倾向，不轻易表达内心情感。",
                "您需要在感情中学习更多地表达关心和情感，增进与伴侣的交流。",
                "您适合与性格温和、理解力强的伴侣相处。",
                "您的感情观较为传统，重视家庭和责任。"
            )
        } else if (gender == "女") {
            listOf(
                "您在感情中细腻敏感，期待被理解和尊重。",
                "您寻找能够给予安全感并尊重您独立性的伴侣。",
                "您的情感表达丰富，但有时可能过于敏感。",
                "您在感情中需要建立良好的沟通机制，避免误解。",
                "您适合与稳重可靠、情感表达能力强的伴侣相处。",
                "您在感情中既渴望亲密又保持一定的独立空间。"
            )
        } else {
            listOf(
                "您在感情中寻求平等和相互尊重的关系。",
                "您重视情感交流和共同成长，期待与伴侣共同进步。",
                "您的感情观较为成熟，能够平衡情感需求和现实考量。",
                "您适合与性格互补、能够理解您内心需求的伴侣相处。",
                "您在感情中需要建立有效的沟通方式，表达真实需求。",
                "您渴望稳定而有深度的感情关系，不喜欢肤浅的交往。"
            )
        }
        
        // 随机选择2-3个感情特点
        loveAnalysis.shuffled(random).take(random.nextInt(2) + 2).forEach {
            baziResponse.append("$it\n")
        }
        
        // 感情发展
        val lovePeriods = listOf(
            "${currentYear}-${currentYear+1}年是您感情发展的重要时期，可能遇到重要的人或关系出现转折。",
            "未来两年内，您的感情生活可能经历一些考验，需要耐心和沟通来维系关系。",
            "${currentYear+1}年下半年至${currentYear+2}年是您感情状况改善的时期，单身者有望遇到心仪对象。",
            "近期感情状况较为稳定，适合深化现有关系或认真思考未来规划。"
        )
        
        // 随机选择一个感情时期描述
        baziResponse.append("\n" + lovePeriods[random.nextInt(lovePeriods.size)] + "\n\n")
        
        // 健康分析
        baziResponse.append("【健康分析】\n")
        val healthAnalysis = listOf(
            "您的体质基本健康，但需要注意劳逸结合，避免过度疲劳。",
            "您可能对季节变化较为敏感，季节交替时应特别注意身体调适。",
            "您的消化系统可能是健康的薄弱环节，应注意饮食规律和质量。",
            "您的呼吸系统需要关注，建议定期进行有氧运动增强肺活量。",
            "您可能有情绪波动引起的身体不适，建议学习情绪管理和放松技巧。",
            "您的免疫力总体良好，但压力过大时可能出现免疫功能下降。",
            "您应特别注意心血管健康，定期检查并保持适度运动。"
        )
        
        // 随机选择2个健康提示
        healthAnalysis.shuffled(random).take(2).forEach {
            baziResponse.append("$it\n")
        }
        
        // 健康建议
        baziResponse.append("\n建议：\n")
        val healthAdvice = listOf(
            "保持规律作息，确保充足睡眠。",
            "均衡饮食，增加蔬果摄入，减少高油高糖食品。",
            "坚持适度运动，每周至少3次30分钟以上的有氧活动。",
            "定期体检，做到疾病早发现早治疗。",
            "学习压力管理技巧，如冥想、深呼吸或正念练习。",
            "保持良好心态，避免长期情绪低落或过度焦虑。",
            "根据季节变化调整生活习惯，增强身体适应能力。"
        )
        
        // 随机选择3个健康建议
        healthAdvice.shuffled(random).take(3).forEach {
            baziResponse.append("- $it\n")
        }
        baziResponse.append("\n")
        
        // 总结和建议
        baziResponse.append("【总结和建议】\n")
        val summaryAdvice = if (birthDate != null) {
            """
            综合八字分析，您的命盘显示您具有${if (random.nextBoolean()) "较为平衡" else "略有起伏"}的人生轨迹。
            
            优势在于${if (strongest == "金") "果断决策和执行力" 
                    else if (strongest == "木") "创新思维和适应能力"
                    else if (strongest == "水") "智慧和洞察力"
                    else if (strongest == "火") "激情和表达能力"
                    else "稳重和耐心"}，
                    
            需要补足的是${if (weakest == "金") "坚持和规划能力"
                    else if (weakest == "木") "灵活性和开放思维"
                    else if (weakest == "水") "情感表达和直觉"
                    else if (weakest == "火") "主动性和热情"
                    else "踏实和耐心"}。
            
            未来${currentYear}-${currentYear+3}年是您人生的关键期，建议:
            
            1. 发挥${strongest}的优势，在事业上寻求突破和发展。
            2. 注意弥补${weakest}的不足，通过学习和实践提升相关能力。
            3. 在人际关系中保持真诚和开放态度，建立有益的社交网络。
            4. 健康方面保持警惕，建立良好的生活习惯和定期检查。
            5. 财务上做好规划，平衡收入与支出，适度投资未来。
            
            记住，八字只是参考，您的努力和选择才是决定命运的关键因素。
            """
        } else {
            """
            虽然缺少完整的出生信息，但基于一般命理原则，每个人都有自己的优势和挑战。
            
            建议您:
            
            1. 寻找并发挥自身优势，在适合的领域深耕发展。
            2. 认识自身不足，通过学习和实践不断提升和完善。
            3. 在人际关系中保持真诚和开放态度，建立有益的社交网络。
            4. 关注身心健康，建立良好的生活习惯和积极的心态。
            5. 做好财务规划，平衡当下享受与未来投资。
            
            命运掌握在自己手中，积极的态度和持续的努力才是改变命运的关键。
            """
        }
        
        baziResponse.append(summaryAdvice.trimIndent())
        
        return baziResponse.toString()
    }

    /**
     * 模拟紫微斗数响应
     */
    private fun simulateZiweiResponse(prompt: String): String {
        // 简单的紫微斗数响应实现
        return """
            【命盘分析】
            您的紫微命盘显示命宫有紫微星坐守，代表您具有较强的领导力和责任感。
            三方四正有吉星拱照，暗示您一生中能得到贵人相助。
            
            【宫位解析】
            命宫：紫微星主照，性格刚毅，具有领导才能。
            财帛宫：武曲星坐守，财运稳定，善于理财。
            官禄宫：天府星照耀，事业发展平稳，有升迁机会。
            福德宫：左辅星入驻，内心善良，乐于助人。
            
            【流年运势】
            今年大运与流年相合，整体运势较为顺遂。
            事业上有发展机会，适合稳扎稳打。
            财运平稳，宜保守投资，避免冒险。
            感情方面可能有波折，需要耐心沟通。
            
            【建议】
            1. 发挥领导才能，主动承担责任
            2. 注重人际关系的维护，广结善缘
            3. 保持稳健的理财习惯，避免投机
            4. 关注健康，注意劳逸结合
            5. 在感情中增进沟通，避免固执己见
            
            【总结】
            您的紫微命盘显示，您是个有责任感、能力强的人，通过自身努力能够获得稳定的发展。只要充分发挥自己的优势，并且注意调整不足，一定能获得成功。简单说，保持踏实努力的态度，好运自然会来。
        """.trimIndent()
    }
    
    /**
     * 模拟手相占卜响应
     */
    private fun simulatePalmistryResponse(prompt: String): String {
        // 简单的手相响应实现
        return """
            【掌纹分析】
            您的生命线较长且清晰，表示健康状况良好，生命力旺盛。
            智慧线平直且深，显示您思维清晰，逻辑性强。
            感情线丰满有力，代表情感丰富，重视感情生活。
            
            【手型解读】
            您的手型属于混合型，兼具实用性和灵活性。
            指节较长，显示您做事细致，有条理性。
            手掌弹性适中，暗示您性格平和，适应能力强。
            
            【综合分析】
            从您的手相来看，您是一个理性与感性平衡的人。
            工作中能够条理清晰地处理问题，有组织能力。
            情感生活丰富多彩，重视与他人的情感联系。
            健康状况总体良好，但需注意定期休息，避免过度疲劳。
            
            【建议】
            1. 充分发挥您的组织能力，可在团队中担任协调角色
            2. 平衡工作与生活，避免过度劳累
            3. 在情感关系中保持开放态度，加强沟通
            4. 定期进行身体检查，保持健康生活习惯
            
            【总结】
            简单来说，您的手相显示您是个能力全面、情感丰富的人，只要注意劳逸结合，未来的发展前景非常光明。记住，手相只是参考，最终的命运掌握在自己手中。
        """.trimIndent()
    }
    
    /**
     * 模拟面相占卜响应
     */
    private fun simulateFaceReadingResponse(prompt: String): String {
        // 简单的面相响应实现
        return """
            【面相总论】
            您的面相端正匀称，五官协调，属于福相。
            额头饱满宽阔，显示智慧充足，思维活跃。
            眉毛清秀有力，代表决策能力强，具有领导气质。
            鼻梁高挺，鼻翼适中，财运良好，事业有成。
            
            【性格分析】
            从面相来看，您性格坚毅，有较强的责任感。
            做事认真细致，不轻言放弃，具有毅力。
            善于交际，人缘较好，能够获得他人支持。
            有一定的进取心，追求事业上的成功。
            
            【运势解读】
            事业运：面相显示您适合管理工作，具有领导才能。
            财运：财库饱满，理财能力强，收入稳定。
            感情运：感情生活较为顺利，能够吸引异性。
            健康运：体质较好，但需注意消化系统。
            
            【建议】
            1. 发挥领导才能，积极承担责任
            2. 保持良好的人际关系，广结善缘
            3. 注意饮食规律，保护消化系统
            4. 适度锻炼，增强体质
            
            【总结】
            总体而言，您的面相显示您是个有能力、有福气的人。只要保持积极的心态和良好的习惯，一定能够获得成功和幸福。面相虽然重要，但后天的努力同样能改变命运，希望您珍惜自身优势，不断进步。
        """.trimIndent()
    }
    
    /**
     * 模拟解梦响应
     */
    private fun simulateDreamInterpretationResponse(prompt: String): String {
        // 从提示中提取梦境内容
        val dreamMatch = Regex("梦见([^，。？！\n]+)").find(prompt)
        val dreamContent = dreamMatch?.groupValues?.get(1) ?: "未知梦境"
        
        // 简单的解梦响应实现
        return """
            【梦境解析】
            您梦见"$dreamContent"，这个梦境在心理学上有着特殊的象征意义。
            梦是潜意识的表达，反映了您内心深处的想法和情感。
            
            【象征意义】
            从象征学角度看，这个梦可能代表着您内心的期望或忧虑。
            梦中的元素与您当前生活中的某些事件或情感有关。
            这不应该被视为预兆，而是您内心想法的投射。
            
            【心理分析】
            从心理分析角度看，这个梦可能反映了您近期的心理状态。
            梦中的情绪和场景与您的现实生活有着微妙的联系。
            了解这些联系有助于您更好地理解自己的内心世界。
            
            【建议】
            1. 记录梦境，寻找与现实生活的联系
            2. 关注梦中的情绪，理解内心真实感受
            3. 不必过度解读梦境，保持平和心态
            4. 如果梦境反复出现，可能需要关注现实中的相关问题
            
            【总结】
            简单来说，您的梦反映了内心的某些想法和情感，它既不是预兆也不是警示，只是潜意识的自然表达。通过理解梦境，您可以更好地了解自己，但不必过度担忧。大多数梦境只是大脑处理信息的方式，保持积极心态才是最重要的。
        """.trimIndent()
    }
    
    /**
     * 模拟黄历/择日响应
     */
    private fun simulateAlmanacResponse(prompt: String): String {
        // 获取当前日期
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        
        // 使用确定性算法生成宜忌
        // 结合年月日以及当年的第几天，确保每天都不同
        val seed = (year * 10000 + month * 100 + day) * dayOfYear
        val random = Random(seed.toLong())
        
        // 扩大宜忌活动的范围，增加变化性
        val goodActivities = listOf(
            "祭祀", "出行", "开业", "安床", "搬家", "结婚", "装修", "开工", "签约", "入宅",
            "祈福", "求嗣", "纳财", "开张", "交易", "立券", "会友", "谢土", "纳畜", "牧养",
            "起基", "修造", "动土", "上梁", "栽种", "纳采", "冠笄", "订婚", "嫁娶", "移徙"
        )
        val badActivities = listOf(
            "动土", "安葬", "修造", "开张", "入学", "求医", "交易", "诉讼", "远行", "开仓",
            "出行", "赴任", "修坟", "破土", "伐木", "架马", "安门", "行船", "掘井", "乘船",
            "拆卸", "破屋", "开渠", "筑堤", "穿井", "造桥", "造船", "针灸", "放水", "纳采"
        )
        
        // 使用不同的随机种子选择不同数量的项目
        val goodRandom = Random((seed + 1).toLong())
        val badRandom = Random((seed + 2).toLong())
        val todayGood = goodActivities.shuffled(goodRandom).take(goodRandom.nextInt(4) + 2) // 2-5项
        val todayBad = badActivities.shuffled(badRandom).take(badRandom.nextInt(4) + 2) // 2-5项
        
        // 生成不同的吉凶方位
        // 为每个方位使用不同的种子
        val directions = listOf("东", "南", "西", "北", "东南", "西南", "东北", "西北")
        val directionLuck = mutableMapOf<String, String>()
        
        for ((index, direction) in directions.withIndex()) {
            // 使用不同的公式计算每个方位的吉凶
            val dirSeed = seed + index * 17 // 使用质数17增加变化性
            val isLucky = when {
                (dirSeed % 7 == 0) -> true  // 用7作为除数
                (dirSeed % 5 == 1) -> true  // 用5作为除数，余1判定
                (dirSeed % 3 == 2) -> true  // 用3作为除数，余2判定
                else -> false
            }
            directionLuck[direction] = if (isLucky) "吉" else "凶"
        }
        
        // 生成吉日与凶日的判断
        // 使用年月日的组合，确保每天结果不同
        val isLuckyDay = when {
            (month * day) % 6 == 0 -> "黄道吉日"   // 月日乘积被6整除
            (month + day) % 5 == 0 -> "黄道吉日"   // 月日之和被5整除
            (dayOfYear % 7) <= 2 -> "黄道吉日"     // 一年中每7天有3天是吉日
            else -> "普通日子"
        }
        
        // 添加不同的日建和日禄
        val tiangan = arrayOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
        val dizhi = arrayOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
        
        // 基于日期计算干支索引
        val tianganIndex = (dayOfYear + year % 10) % 10
        val dizhiIndex = (dayOfYear + year % 12) % 12
        
        val dayGanzhi = "${tiangan[tianganIndex]}${dizhi[dizhiIndex]}日"
        
        // 基于日期生成每日宜出行方向
        val travelRandom = Random((seed + 5).toLong())
        val travelDirections = directions.shuffled(travelRandom).take(2)
        
        // 基于日期生成运势评级
        val luckLevel = when {
            (seed % 15 == 0) -> "极好"   // 大约1/15的概率
            (seed % 10 == 1) -> "很好"   // 大约1/10的概率
            (seed % 7 <= 2) -> "不错"    // 大约3/7的概率
            (seed % 5 <= 2) -> "一般"    // 大约3/5的概率
            else -> "有起伏"              // 其余情况
        }
        
        // 使用更多变化的格式和内容生成回复
        return """
            【今日运势 - ${year}年${month}月${day}日】
            ${dayGanzhi} ${isLuckyDay}
            
            【吉凶提示】
            宜：${todayGood.joinToString("、")}
            忌：${todayBad.joinToString("、")}
            
            【方位吉凶】
            东方：${directionLuck["东"]} | 南方：${directionLuck["南"]}
            西方：${directionLuck["西"]} | 北方：${directionLuck["北"]}
            东南：${directionLuck["东南"]} | 西南：${directionLuck["西南"]}
            东北：${directionLuck["东北"]} | 西北：${directionLuck["西北"]}
            
            【今日宜出行方向】
            ${travelDirections.joinToString("、")}方向吉利，适合出行。
            
            【吉时】
            ${dizhi[random.nextInt(dizhi.size)]}时、${dizhi[random.nextInt(dizhi.size)]}时、${dizhi[random.nextInt(dizhi.size)]}时
            
            【今日财位】
            ${directions.shuffled(Random((seed + 3).toLong())).first()}方位为财位，可摆放招财物品。
            
            【每日建议】
            今天适合${if (todayGood.isNotEmpty()) todayGood.shuffled().first() else "休息"}，
            不宜${if (todayBad.isNotEmpty()) todayBad.shuffled().first() else "冒险"}。
            ${if (random.nextBoolean())
                "早起有利于提升今日运势。"
            else
                "做事保持谨慎态度会更有利。"
            }
            
            【金钱运】
            ${listOf("有意外收获的可能", "财运平平，量入为出", "注意避免冲动消费", "适合投资理财", "宜守不宜进").shuffled(Random((seed + 4).toLong())).first()}
            
            【事业运】
            ${listOf("工作进展顺利", "可能有新机遇出现", "适合开展新项目", "维持现状为宜", "暂避锋芒，稳守为主").shuffled(Random((seed + 5).toLong())).first()}
            
            【桃花运】
            ${listOf("桃花运旺盛", "异性缘佳", "平淡无奇", "注意避免情感纠纷", "适合增进与伴侣感情").shuffled(Random((seed + 6).toLong())).first()}
            
            【健康指数】
            ${60 + random.nextInt(41)}% - ${listOf("注意饮食规律", "适当运动有益健康", "避免过度劳累", "注意保暖", "保持良好心态").shuffled(Random((seed + 7).toLong())).first()}
            
            【幸运颜色】
            ${listOf("红色", "蓝色", "黄色", "绿色", "紫色", "橙色", "粉色", "白色", "黑色", "金色").shuffled(Random((seed + 8).toLong())).take(2).joinToString("、")}
            
            【总结】
            今天整体运势${luckLevel}，
            ${listOf(
                "建议保持平和心态，按计划行事。",
                "宜静不宜动，适合思考和规划。",
                "可大胆行动，把握机会。",
                "中规中矩，不宜冒险。",
                "谨慎行事，避免重大决策。"
            ).shuffled(Random((seed + 9).toLong())).first()}
            记住，黄历只是参考，真正决定一天好坏的是您的态度和行动。
        """.trimIndent()
    }
    
    /**
     * 模拟数字命理响应
     */
    private fun simulateNumerologyResponse(prompt: String): String {
        // 简单的数字命理响应实现
        return """
            【数字能量】
            根据您提供的信息，您的生命数字为7。
            数字7象征着分析、智慧、洞察力和内省。
            拥有生命数字7的人通常具有强大的分析能力和直觉。
            
            【性格特质】
            您天生具有探索精神，热爱研究和学习。
            思维深刻，善于思考人生哲理和深层次问题。
            独立性强，喜欢独处，享受精神世界的充实。
            直觉敏锐，常常能感知到他人无法察觉的细节。
            
            【人生使命】
            您的使命在于探索真相，追求智慧和知识。
            通过深入思考和研究，为世界提供独特见解。
            帮助他人看清事物本质，传递智慧和洞察力。
            
            【优势与挑战】
            优势：分析能力强，思维深刻，直觉敏锐。
            挑战：可能过于沉浸在自己的世界，与现实脱节。
            有时显得疏远和神秘，不易与他人建立亲密关系。
            
            【建议】
            1. 保持好奇心和探索精神，持续学习和成长
            2. 适当走出内心世界，增进与他人的交流和联系
            3. 将您的洞察力和智慧应用于实际生活中
            4. 寻找平衡点，兼顾理想与现实
            
            【总结】
            作为生命数字7的人，您具有深刻的思想和敏锐的洞察力。虽然有时可能显得疏远，但您的智慧和独特视角是宝贵的礼物。简单来说，您是个思想家，通过探索和思考为这个世界带来独特价值。接受自己的与众不同，同时不忘与他人建立联系，您的人生将更加充实和平衡。
        """.trimIndent()
    }
    
    /**
     * 模拟奇门遁甲响应
     */
    private fun simulateQimenResponse(prompt: String): String {
        // 简单的奇门遁甲响应实现
        return """
            【局势分析】
            当前奇门局为休门伏吟，坎宫值符。
            天盘与地盘呈现天蓬临玄武之象。
            局中天心回向，显示变动之机。
            
            【门户解析】
            休门值符，主吉门吉神相会，利于休养生息。
            死门值使，暗示阻滞之象，宜静不宜动。
            惊门入离，示警醒之意，需保持警觉。
            
            【九星解读】
            天蓬星入首，主智慧谋略，利于思考和计划。
            天芮星伏宫，暗示稳固基础，固本培元。
            天冲星化气，示变动迅速，需把握时机。
            
            【问题解答】
            对于您所咨询的事项，当前局势显示：
            时机尚未成熟，需再观察一段时间。
            暂时不宜大动作，可做小范围尝试。
            有贵人相助迹象，可寻求智者建议。
            
            【趋吉避凶】
            宜：思考计划、修身养性、寻求指导
            忌：冒然行动、贸然投资、激进决策
            
            【总结】
            此局显示当前形势暂未明朗，需保持观望态度。
            休门值符，宜休养生息；死门值使，不宜妄动。
            简言之，目前是思考和准备的时期，而非行动的时刻。
            待局势明朗后再做决断，将获得更好结果。
        """.trimIndent()
    }
    
    /**
     * 模拟通用响应
     */
    private fun simulateGenericResponse(prompt: String): String {
        // 通用响应实现
        return """
            【综合分析】
            根据您提供的信息，以下是相关分析和建议。
            请注意，这是基于一般原则的分析，仅供参考。
            
            【运势解读】
            近期运势平稳，有小幅波动但总体向好。
            工作或学习方面可能面临一些挑战，需要保持耐心。
            人际关系整体和谐，适合巩固现有关系网络。
            健康状况良好，但需注意劳逸结合，避免过度疲劳。
            
            【发展方向】
            当前阶段适合稳扎稳打，巩固基础，积累经验。
            可以尝试拓展新的领域，但不宜冒进或过度冒险。
            注重自我提升和学习，为未来发展打下坚实基础。
            
            【建议】
            1. 保持积极心态，面对挑战时冷静分析
            2. 制定合理的短期和长期目标，循序渐进
            3. 注重身心平衡，适当放松和休息
            4. 加强人际交往，建立有价值的人脉网络
            5. 学会取舍，专注于最重要的事情
            
            【总结】
            简单来说，当前是稳定发展的时期，不急不躁，踏实前行将获得良好结果。记住，命运掌握在自己手中，积极的态度和持续的努力才是成功的关键。
        """.trimIndent()
    }

    /**
     * 获取应用上下文
     * 注意：这个方法可能返回null，调用时需要处理
     */
    private fun getApplicationContext(): Context? {
        try {
            // 尝试获取应用上下文
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread")
            currentActivityThreadMethod.isAccessible = true
            val currentActivityThread = currentActivityThreadMethod.invoke(null)
            
            val getApplicationMethod = activityThreadClass.getDeclaredMethod("getApplication")
            getApplicationMethod.isAccessible = true
            return getApplicationMethod.invoke(currentActivityThread) as? Context
        } catch (e: Exception) {
            Log.e(TAG, "无法获取应用上下文: ${e.message}", e)
            return null
        }
    }
}