package com.example.divination.model

/**
 * MBTI 16种人格类型详细信息
 * 
 * @property code 人格类型代码 (如: INTJ, ENFP)
 * @property name 人格类型名称 (如: 建筑师, 竞选者)
 * @property role 角色类型 (分析师/外交官/守护者/探险家)
 * @property description 类型概述
 * @property strengths 优势列表
 * @property weaknesses 劣势列表
 * @property careers 适合职业
 * @property relationships 人际关系特点
 * @property growth 成长建议
 */
data class PersonalityType(
    val code: String,
    val name: String,
    val role: Role,
    val description: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val careers: List<String>,
    val relationships: String,
    val growth: String
) {
    /**
     * 四大角色分类
     */
    enum class Role(val displayName: String, val color: String) {
        ANALYST("分析师", "#9B59B6"),      // 紫色
        DIPLOMAT("外交官", "#3498DB"),     // 蓝色
        SENTINEL("守护者", "#1ABC9C"),     // 青色
        EXPLORER("探险家", "#F39C12");     // 橙色
        
        companion object {
            fun fromCode(code: String): Role {
                return when {
                    code in listOf("INTJ", "INTP", "ENTJ", "ENTP") -> ANALYST
                    code in listOf("INFJ", "INFP", "ENFJ", "ENFP") -> DIPLOMAT
                    code in listOf("ISTJ", "ISFJ", "ESTJ", "ESFJ") -> SENTINEL
                    code in listOf("ISTP", "ISFP", "ESTP", "ESFP") -> EXPLORER
                    else -> throw IllegalArgumentException("Unknown personality type: $code")
                }
            }
        }
    }
    
    companion object {
        /**
         * 根据四字母代码获取人格类型详情
         */
        fun getByCode(code: String): PersonalityType {
            return ALL_TYPES.find { it.code == code } 
                ?: throw IllegalArgumentException("Unknown personality type code: $code")
        }
        
        /**
         * 16种人格类型完整数据库
         */
        val ALL_TYPES = listOf(
            // ========== 分析师型 (Analyst) ==========
            PersonalityType(
                code = "INTJ",
                name = "建筑师",
                role = Role.ANALYST,
                description = "富有想象力和战略性的思想家，一切皆在计划之中。INTJ是最具独立性的人格类型，他们善于系统性思考，追求知识和能力的提升，对未来有清晰的愿景。",
                strengths = listOf(
                    "理性思维，逻辑清晰",
                    "战略眼光，善于规划",
                    "独立自主，意志坚定",
                    "求知欲强，学习能力出色",
                    "对系统和流程有深刻理解"
                ),
                weaknesses = listOf(
                    "过于理性，忽视情感",
                    "完美主义，标准过高",
                    "不善社交，显得冷漠",
                    "固执己见，难以妥协",
                    "对细节和执行缺乏耐心"
                ),
                careers = listOf(
                    "战略规划师", "系统架构师", "科学研究员",
                    "投资分析师", "企业顾问", "软件工程师",
                    "大学教授", "律师", "医学专家"
                ),
                relationships = "INTJ在人际关系中重视深度而非广度，喜欢与智识相当的人交流。他们不善表达情感，但对真正在意的人会展现忠诚和承诺。需要伴侣理解其独立性和对个人空间的需求。",
                growth = "学会倾听他人情感需求，不要过于追求完美而忽视现实的可行性。尝试在决策时考虑人的因素，培养同理心。适当放松对自己和他人的高标准，享受过程而非只关注结果。"
            ),
            
            PersonalityType(
                code = "INTP",
                name = "逻辑学家",
                role = Role.ANALYST,
                description = "具有创新精神的发明家，对知识有着止不住的渴望。INTP是天生的问题解决者，他们喜欢探索理论和抽象概念，追求理解事物的本质。",
                strengths = listOf(
                    "分析能力极强，逻辑严密",
                    "创造力丰富，善于创新",
                    "客观公正，不受情绪影响",
                    "开放思维，乐于探索",
                    "学习能力强，知识面广"
                ),
                weaknesses = listOf(
                    "过度分析，难以决断",
                    "缺乏耐心处理日常琐事",
                    "不善表达情感",
                    "容易沉浸理论而脱离实际",
                    "对他人感受不够敏感"
                ),
                careers = listOf(
                    "研究科学家", "程序员", "数学家",
                    "哲学家", "经济学家", "数据分析师",
                    "技术作家", "系统分析师", "大学讲师"
                ),
                relationships = "INTP珍视智识上的连接，喜欢与能激发思考的人交往。他们在亲密关系中可能显得疏离，但实际上很在意伴侣。需要足够的独处时间来思考和充电。",
                growth = "学会将理论转化为实践，不要过度沉迷于分析。培养情商，关注他人的情感需求。设定明确的目标和截止日期，避免无休止的探索而不行动。"
            ),
            
            PersonalityType(
                code = "ENTJ",
                name = "指挥官",
                role = Role.ANALYST,
                description = "大胆、富有想象力且意志强大的领导者，总能找到或创造解决方法。ENTJ天生具有领导才能，善于制定战略并推动执行。",
                strengths = listOf(
                    "天生的领导者，决断力强",
                    "战略思维，目标导向",
                    "高效执行，组织能力出色",
                    "自信果敢，不畏挑战",
                    "善于激励他人，推动变革"
                ),
                weaknesses = listOf(
                    "过于强势，忽视他人感受",
                    "缺乏耐心，急于求成",
                    "难以接受批评",
                    "可能显得傲慢和支配欲强",
                    "工作狂倾向，忽视生活平衡"
                ),
                careers = listOf(
                    "CEO/高管", "企业家", "管理顾问",
                    "投资银行家", "律师", "政治家",
                    "商业战略师", "项目经理", "军官"
                ),
                relationships = "ENTJ在关系中同样展现领导力，喜欢与有能力、有抱负的人交往。他们重视效率和成长，期待伴侣能独立且有自己的目标。需要学会放慢节奏，享受情感交流。",
                growth = "培养同理心，学会倾听而非总是主导。认识到不是所有问题都需要立即解决，给他人成长的空间。平衡工作与生活，重视人际关系的情感层面。"
            ),
            
            PersonalityType(
                code = "ENTP",
                name = "辩论家",
                role = Role.ANALYST,
                description = "聪明好奇的思想家，无法抗拒智力上的挑战。ENTP善于创新和头脑风暴，喜欢探索各种可能性，是天生的辩手。",
                strengths = listOf(
                    "思维敏捷，反应迅速",
                    "创新能力强，点子多",
                    "口才出众，辩论高手",
                    "适应力强，灵活变通",
                    "热情洋溢，富有感染力"
                ),
                weaknesses = listOf(
                    "容易分心，难以坚持",
                    "不喜欢处理细节和后续工作",
                    "好辩论，可能冒犯他人",
                    "缺乏耐心，急于求新",
                    "对常规和规则感到厌倦"
                ),
                careers = listOf(
                    "创业家", "营销策划", "发明家",
                    "律师", "顾问", "政治分析师",
                    "软件开发", "记者", "演讲家"
                ),
                relationships = "ENTP在关系中寻求智力刺激和新鲜感，喜欢与思想开放的人交往。他们善于交流但可能忽视情感深度。需要伴侣能接受其多变和探索欲。",
                growth = "学会坚持完成项目而非总是追逐新想法。培养耐心，重视细节执行。意识到辩论的界限，不要为了辩而辩。发展情感智慧，理解感性思维的价值。"
            ),
            
            // ========== 外交官型 (Diplomat) ==========
            PersonalityType(
                code = "INFJ",
                name = "提倡者",
                role = Role.DIPLOMAT,
                description = "安静而神秘，同时鼓舞人心且不知疲倦的理想主义者。INFJ是最稀有的人格类型，他们富有洞察力，追求意义和深度连接。",
                strengths = listOf(
                    "洞察力强，理解他人",
                    "理想主义，追求意义",
                    "富有创造力和想象力",
                    "坚定的价值观和原则",
                    "善于激励和帮助他人成长"
                ),
                weaknesses = listOf(
                    "过于理想化，易失望",
                    "完美主义，对自己要求过高",
                    "容易情绪疲惫",
                    "难以应对冲突",
                    "过度关注他人而忽视自己"
                ),
                careers = listOf(
                    "心理咨询师", "作家", "教师",
                    "社会工作者", "人力资源", "艺术家",
                    "非营利组织负责人", "编辑", "宗教工作者"
                ),
                relationships = "INFJ在关系中寻求深层连接和真实性，他们是忠诚的伴侣和朋友。善于理解他人需求但可能忽视自己。需要能尊重其价值观和独处时间的伴侣。",
                growth = "学会设定界限，不要过度投入他人问题。接受现实的不完美，降低对自己和他人的期待。重视自我关怀，避免情感透支。勇于表达需求，不要总是妥协。"
            ),
            
            PersonalityType(
                code = "INFP",
                name = "调停者",
                role = Role.DIPLOMAT,
                description = "诗意、善良的利他主义者，总是热切地为正义和和谐而努力。INFP内心丰富，追求真实和意义，是理想主义的梦想家。",
                strengths = listOf(
                    "富有同理心和compassion",
                    "创造力丰富，艺术天赋",
                    "坚守核心价值观",
                    "开放包容，不评判他人",
                    "善于发现事物的深层意义"
                ),
                weaknesses = listOf(
                    "过于敏感，易受伤害",
                    "难以应对批评",
                    "逃避冲突，不善拒绝",
                    "理想化倾向强，脱离现实",
                    "拖延症，难以做决定"
                ),
                careers = listOf(
                    "作家", "艺术家", "心理咨询师",
                    "教师", "社会工作者", "设计师",
                    "音乐家", "翻译", "图书管理员"
                ),
                relationships = "INFP在关系中寻求深度和真实，他们是体贴温暖的伴侣。重视情感连接和共同价值观。需要理解其敏感性和对独处的需求，以及对理想爱情的向往。",
                growth = "培养实用技能，将理想转化为行动。学会面对冲突而非逃避，建立健康界限。接受现实的复杂性，不要过度理想化。制定具体计划，克服拖延倾向。"
            ),
            
            PersonalityType(
                code = "ENFJ",
                name = "主人公",
                role = Role.DIPLOMAT,
                description = "富有魅力鼓舞人心的领导者，有能力使听众着迷。ENFJ天生的导师和激励者，善于理解和引导他人实现潜能。",
                strengths = listOf(
                    "出色的沟通和人际技能",
                    "天生的领导魅力",
                    "高度同理心，理解他人",
                    "热情洋溢，激励他人",
                    "组织协调能力强"
                ),
                weaknesses = listOf(
                    "过度在意他人看法",
                    "难以做出艰难决定",
                    "容易忽视自己需求",
                    "理想主义，期望过高",
                    "可能过度干预他人生活"
                ),
                careers = listOf(
                    "教师", "培训师", "人力资源经理",
                    "公关专家", "政治家", "心理咨询师",
                    "非营利组织领导", "销售经理", "活动策划"
                ),
                relationships = "ENFJ是温暖支持的伴侣，善于营造和谐氛围。他们重视关系的成长和深度交流。可能过度关注对方需求而忽视自己，需要学会平衡付出与接受。",
                growth = "学会说不，设定健康界限。认识到不能解决所有人的问题。关注自己的需求和幸福。接受他人的选择，不要过度干预。培养客观性，平衡情感与理性。"
            ),
            
            PersonalityType(
                code = "ENFP",
                name = "竞选者",
                role = Role.DIPLOMAT,
                description = "热情、有创造力、社交能力强的自由精神，总能找到理由微笑。ENFP充满活力和可能性，是天生的激励者和探索者。",
                strengths = listOf(
                    "热情洋溢，感染力强",
                    "创造力丰富，想象力无限",
                    "善于社交，人缘好",
                    "适应力强，灵活应变",
                    "善于发现新机会和可能性"
                ),
                weaknesses = listOf(
                    "容易分心，缺乏专注",
                    "过度乐观，忽视现实困难",
                    "难以坚持完成项目",
                    "情绪化，容易焦虑",
                    "不善处理细节和常规工作"
                ),
                careers = listOf(
                    "创意总监", "市场营销", "记者",
                    "演员", "心理咨询师", "活动策划",
                    "教师", "创业家", "公关专家"
                ),
                relationships = "ENFP在关系中充满热情和浪漫，善于表达情感。他们寻求深度连接和成长。可能过度理想化关系，需要接受现实的平淡时刻。重视自由和探索。",
                growth = "培养专注力，学会完成而非只是开始。发展实用技能和时间管理能力。接受生活的常规面，不要总是追求新鲜刺激。学会深入而非广泛，质量重于数量。"
            ),
            
            // ========== 守护者型 (Sentinel) ==========
            PersonalityType(
                code = "ISTJ",
                name = "物流师",
                role = Role.SENTINEL,
                description = "实际且注重事实的个人，可靠性不容置疑。ISTJ是最负责任的人格类型，重视传统、秩序和忠诚。",
                strengths = listOf(
                    "高度责任心，值得信赖",
                    "务实高效，执行力强",
                    "组织能力出色，注重细节",
                    "诚实正直，遵守承诺",
                    "冷静客观，逻辑清晰"
                ),
                weaknesses = listOf(
                    "过于刻板，缺乏灵活性",
                    "抗拒改变和新方法",
                    "不善表达情感",
                    "可能过于挑剔和批判",
                    "工作狂倾向，忽视休闲"
                ),
                careers = listOf(
                    "会计师", "审计师", "项目经理",
                    "律师", "军官", "工程师",
                    "数据分析师", "行政管理", "质量控制"
                ),
                relationships = "ISTJ是忠诚可靠的伴侣，重视承诺和稳定。他们通过行动而非言语表达爱。可能显得保守和缺乏浪漫，但实际上非常在意家庭和关系。",
                growth = "学会接纳变化和不确定性。培养情感表达能力，不要过于克制。尝试新事物，走出舒适区。平衡工作与生活，允许自己放松。对他人多一些包容和理解。"
            ),
            
            PersonalityType(
                code = "ISFJ",
                name = "守卫者",
                role = Role.SENTINEL,
                description = "非常专注且温暖的守护者，时刻准备保护所爱之人。ISFJ是最体贴的人格类型，善于照顾他人，重视和谐。",
                strengths = listOf(
                    "可靠负责，值得信赖",
                    "善良体贴，关心他人",
                    "观察力强，注重细节",
                    "勤奋努力，任劳任怨",
                    "实用主义，脚踏实地"
                ),
                weaknesses = listOf(
                    "过度付出，忽视自己",
                    "难以拒绝他人",
                    "抗拒改变，固守传统",
                    "容易被批评伤害",
                    "不善于表达需求"
                ),
                careers = listOf(
                    "护士", "教师", "行政助理",
                    "社会工作者", "图书管理员", "人力资源",
                    "内科医生", "营养师", "客服经理"
                ),
                relationships = "ISFJ是温暖支持的伴侣，善于照顾对方需求。他们重视稳定和传统，是家庭的支柱。可能过度付出而忽视自己，需要学会表达需求。",
                growth = "学会设定界限，关注自己的需求。勇于表达不满，不要总是忍让。接受变化，尝试新方法。培养自信，认可自己的价值。学会说不，避免过度承担。"
            ),
            
            PersonalityType(
                code = "ESTJ",
                name = "总经理",
                role = Role.SENTINEL,
                description = "出色的管理者，在管理事务或人员方面无与伦比。ESTJ是天生的组织者，重视秩序、效率和传统。",
                strengths = listOf(
                    "组织管理能力强",
                    "决断力强，执行高效",
                    "正直诚实，原则性强",
                    "务实可靠，注重结果",
                    "善于领导和协调团队"
                ),
                weaknesses = listOf(
                    "过于强硬，缺乏灵活性",
                    "不善处理情感问题",
                    "可能显得专制和固执",
                    "难以接受批评",
                    "过于关注规则而忽视人情"
                ),
                careers = listOf(
                    "高级管理", "运营经理", "警察",
                    "军官", "法官", "财务总监",
                    "银行经理", "房地产经纪", "项目主管"
                ),
                relationships = "ESTJ在关系中重视稳定和传统，是可靠的伴侣。他们通过承担责任表达爱。可能显得缺乏浪漫和情感表达，需要学会温柔和倾听。",
                growth = "培养同理心，理解他人情感。学会灵活变通，不要过于死板。倾听不同意见，接受多元观点。平衡工作与家庭，重视情感交流。放松控制欲，给他人空间。"
            ),
            
            PersonalityType(
                code = "ESFJ",
                name = "执政官",
                role = Role.SENTINEL,
                description = "极有同情心、受欢迎的人，总是热心地帮助他人。ESFJ是天生的照顾者，重视和谐、传统和人际关系。",
                strengths = listOf(
                    "善于社交，人际技能强",
                    "热心助人，体贴周到",
                    "组织协调能力出色",
                    "忠诚可靠，值得信赖",
                    "实用主义，注重细节"
                ),
                weaknesses = listOf(
                    "过度在意他人看法",
                    "难以应对批评",
                    "可能过于关注社会规范",
                    "不善处理冲突",
                    "容易忽视自己需求"
                ),
                careers = listOf(
                    "活动策划", "护士", "教师",
                    "人力资源", "公关专员", "社会工作者",
                    "客服经理", "办公室经理", "销售代表"
                ),
                relationships = "ESFJ是温暖支持的伴侣，善于营造和谐家庭氛围。他们重视传统和稳定，乐于照顾家人。可能过度依赖他人认可，需要培养自我价值感。",
                growth = "学会接受批评，不要过于敏感。关注自己的需求，不要总是迎合他人。接受多元价值观，不要过于保守。培养独立性，减少对认可的依赖。学会面对冲突。"
            ),
            
            // ========== 探险家型 (Explorer) ==========
            PersonalityType(
                code = "ISTP",
                name = "鉴赏家",
                role = Role.EXPLORER,
                description = "大胆而实际的实验家，擅长使用各种工具。ISTP是天生的问题解决者，喜欢动手实践和探索机制。",
                strengths = listOf(
                    "动手能力强，技术高超",
                    "冷静理性，应变能力强",
                    "独立自主，不依赖他人",
                    "实用主义，注重效率",
                    "勇于冒险，敢于尝试"
                ),
                weaknesses = listOf(
                    "不善表达情感",
                    "容易感到无聊和不耐烦",
                    "可能过于冒险和鲁莽",
                    "难以做长期承诺",
                    "不喜欢规则和约束"
                ),
                careers = listOf(
                    "机械工程师", "飞行员", "程序员",
                    "消防员", "运动员", "技师",
                    "摄影师", "法医", "建筑师"
                ),
                relationships = "ISTP在关系中重视自由和独立，不喜欢过多情感需求。他们通过行动而非言语表达关心。需要理解其需要空间和新鲜刺激的特点。",
                growth = "培养情感表达能力，学会沟通需求。考虑长期后果，不要过于冲动。发展人际技能，关注他人感受。学会坚持，不要总是寻求新刺激。建立稳定的生活结构。"
            ),
            
            PersonalityType(
                code = "ISFP",
                name = "探险家",
                role = Role.EXPLORER,
                description = "灵活有魅力的艺术家，时刻准备探索和体验新事物。ISFP是温和的艺术家，活在当下，追求美和和谐。",
                strengths = listOf(
                    "艺术天赋，审美能力强",
                    "善良温和，体贴他人",
                    "灵活适应，随遇而安",
                    "观察力敏锐，感知细腻",
                    "热爱自由，追求真实"
                ),
                weaknesses = listOf(
                    "过于敏感，易受伤害",
                    "逃避冲突，不善竞争",
                    "缺乏长期规划",
                    "难以应对压力",
                    "可能过于理想化"
                ),
                careers = listOf(
                    "艺术家", "设计师", "摄影师",
                    "音乐家", "厨师", "兽医",
                    "美容师", "时尚设计", "园艺师"
                ),
                relationships = "ISFP是温柔体贴的伴侣，重视和谐和真实连接。他们通过行动表达爱，善于营造浪漫氛围。需要足够的个人空间和自由，不喜欢被束缚。",
                growth = "培养应对冲突的能力，不要总是逃避。发展长期规划能力，考虑未来。学会表达需求，不要总是妥协。建立应对压力的机制。平衡理想与现实。"
            ),
            
            PersonalityType(
                code = "ESTP",
                name = "企业家",
                role = Role.EXPLORER,
                description = "聪明、精力充沛善于感知的人，真心享受生活在边缘。ESTP是天生的冒险家，活力四射，善于把握机会。",
                strengths = listOf(
                    "行动力强，雷厉风行",
                    "适应力强，灵活应变",
                    "社交能力出色，魅力十足",
                    "实用主义，注重结果",
                    "勇于冒险，敢于挑战"
                ),
                weaknesses = listOf(
                    "冲动鲁莽，缺乏耐心",
                    "不喜欢规则和约束",
                    "难以关注长远",
                    "可能不够敏感",
                    "容易感到无聊"
                ),
                careers = listOf(
                    "创业家", "销售经理", "运动员",
                    "急救人员", "警察", "股票交易员",
                    "活动策划", "房地产经纪", "摄影师"
                ),
                relationships = "ESTP在关系中充满活力和激情，善于制造乐趣。他们活在当下，不喜欢过多情感分析。需要伴侣能接受其冒险精神和对自由的需求。",
                growth = "培养耐心，学会深思熟虑。考虑长期后果，不要只关注眼前。发展情感敏感度，理解他人需求。学会坚持，完成开始的项目。接受必要的规则和结构。"
            ),
            
            PersonalityType(
                code = "ESFP",
                name = "表演者",
                role = Role.EXPLORER,
                description = "自发的、精力充沛和热情的表演者，生活在他们周围永远不会无聊。ESFP是天生的entertainer，热爱生活，散发快乐。",
                strengths = listOf(
                    "热情洋溢，感染力强",
                    "善于社交，人缘极佳",
                    "乐观积极，享受当下",
                    "实用主义，善于观察",
                    "适应力强，灵活应变"
                ),
                weaknesses = listOf(
                    "缺乏长期规划",
                    "容易分心，不够专注",
                    "过于敏感他人看法",
                    "逃避冲突和困难",
                    "可能过度追求刺激"
                ),
                careers = listOf(
                    "演员", "活动策划", "销售",
                    "导游", "儿童教师", "设计师",
                    "摄影师", "公关", "美容顾问"
                ),
                relationships = "ESFP是温暖有趣的伴侣，善于营造快乐氛围。他们慷慨大方，乐于取悦对方。活在当下，可能忽视长期规划。需要欣赏和认可。",
                growth = "培养长期规划能力，不要只活在当下。学会面对困难而非逃避。发展财务管理能力，避免冲动消费。减少对他人认可的依赖。培养深度思考能力。"
            )
        )
    }
}

