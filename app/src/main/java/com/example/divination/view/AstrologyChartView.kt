package com.example.divination.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 占星星盘视图
 * 用于根据文字描述绘制占星学星盘
 */
class AstrologyChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 绘制画笔
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }
    
    private val planetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }
    
    private val zodiacPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        alpha = 80 // 半透明
    }
    
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 16f
        textAlign = Paint.Align.LEFT
    }
    
    // 日期和时间
    private var chartDate: String = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())
    
    // 星座符号
    private val zodiacSymbols = arrayOf(
        "白羊", "金牛", "双子", "巨蟹", "狮子", "处女", "天秤", "天蝎", "射手", "摩羯", "水瓶", "双鱼"
    )
    
    // 行星符号
    private val planetSymbols = mapOf(
        "太阳" to "☉",
        "月亮" to "☽",
        "水星" to "☿",
        "金星" to "♀",
        "火星" to "♂",
        "木星" to "♃",
        "土星" to "♄",
        "天王星" to "♅",
        "海王星" to "♆",
        "冥王星" to "♇",
        "上升点" to "上升",
        "中天点" to "中天",
        "北交点" to "☊",
        "南交点" to "☋"
    )
    
    // 行星位置数据
    private var planets: MutableMap<String, PlanetPosition> = mutableMapOf()
    
    // 相位数据
    private var aspects: MutableList<Aspect> = mutableListOf()
    
    // 宫位数据
    private var houses: MutableList<House> = mutableListOf()
    
    // 上升点和中天点
    private var ascendant: Int = 0  // 上升点所在的度数（0-359）
    private var midheaven: Int = 0  // 中天点所在的度数（0-359）
    
    /**
     * 设置星盘数据
     * @param description 星盘描述文本
     */
    fun setChartData(description: String) {
        parseChartDescription(description)
        invalidate()
    }
    
    /**
     * 解析星盘描述
     */
    private fun parseChartDescription(description: String) {
        // 重置数据
        planets.clear()
        aspects.clear()
        houses.clear()
        
        // 尝试提取日期时间信息
        val dateRegex = "(\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}\\s*\\d{1,2}:\\d{1,2})".toRegex()
        val dateMatch = dateRegex.find(description)
        if (dateMatch != null) {
            chartDate = dateMatch.value
        }
        
        // 增强行星位置解析正则，支持更多表达格式
        val planetRegex = "(太阳|月亮|水星|金星|火星|木星|土星|天王星|海王星|冥王星|上升点|中天点|北交点|南交点)\\s*(位于|在|落在|处于)?\\s*([♈♉♊♋♌♍♎♏♐♑♒♓]|白羊|金牛|双子|巨蟹|狮子|处女|天秤|天蝎|射手|摩羯|水瓶|双鱼)(座)?\\s*(\\d+)?\\s*(°|度)?".toRegex()
        val planetMatches = planetRegex.findAll(description)
        
        for (match in planetMatches) {
            val planetName = match.groupValues[1]
            val zodiacSign = match.groupValues[3]
            // 如果没有度数信息，使用默认值15度（星座中点）
            val degree = match.groupValues[5].toIntOrNull() ?: 15
            
            val zodiacIndex = when {
                zodiacSign == "♈" || zodiacSign.contains("白羊") -> 0
                zodiacSign == "♉" || zodiacSign.contains("金牛") -> 1
                zodiacSign == "♊" || zodiacSign.contains("双子") -> 2
                zodiacSign == "♋" || zodiacSign.contains("巨蟹") -> 3
                zodiacSign == "♌" || zodiacSign.contains("狮子") -> 4
                zodiacSign == "♍" || zodiacSign.contains("处女") -> 5
                zodiacSign == "♎" || zodiacSign.contains("天秤") -> 6
                zodiacSign == "♏" || zodiacSign.contains("天蝎") -> 7
                zodiacSign == "♐" || zodiacSign.contains("射手") -> 8
                zodiacSign == "♑" || zodiacSign.contains("摩羯") -> 9
                zodiacSign == "♒" || zodiacSign.contains("水瓶") -> 10
                zodiacSign == "♓" || zodiacSign.contains("双鱼") -> 11
                else -> 0
            }
            
            val totalDegree = zodiacIndex * 30 + degree
            planets[planetName] = PlanetPosition(planetName, totalDegree, 0)  // 简化版本不考虑宫位
            
            // 特殊处理上升点和中天点
            if (planetName == "上升点") {
                ascendant = totalDegree
            } else if (planetName == "中天点") {
                midheaven = totalDegree
            }
        }
        
        // 增强相位解析正则，支持更多表达格式
        val aspectRegex = "(太阳|月亮|水星|金星|火星|木星|土星|天王星|海王星|冥王星)\\s*(和|与)\\s*(太阳|月亮|水星|金星|火星|木星|土星|天王星|海王星|冥王星)\\s*(形成|构成|呈现)?\\s*(合相|六分相|四分相|三分相|二分相|反对相)".toRegex()
        val aspectMatches = aspectRegex.findAll(description)
        
        for (match in aspectMatches) {
            val planet1 = match.groupValues[1]
            val planet2 = match.groupValues[3]
            val aspectType = when (match.groupValues[5]) {
                "合相" -> 0
                "六分相" -> 60
                "四分相" -> 90
                "三分相" -> 120
                "二分相" -> 150
                "反对相" -> 180
                else -> 0
            }
            
            aspects.add(Aspect(planet1, planet2, aspectType))
        }
        
        // 简单解析十二宫
        for (i in 0 until 12) {
            // 这里简化处理，假设十二宫均匀分布
            houses.add(House(i, (ascendant + i * 30) % 360, (ascendant + (i + 1) * 30) % 360))
        }
        
        // 如果没有行星数据，添加示例数据（确保星盘不为空）
        if (planets.isEmpty()) {
            // 添加默认的行星数据，基于模板图中的位置
            planets["太阳"] = PlanetPosition("太阳", 45, 0)  // 金牛座15度
            planets["月亮"] = PlanetPosition("月亮", 300, 0)  // 摩羯座0度
            planets["水星"] = PlanetPosition("水星", 60, 0)  // 双子座0度
            planets["金星"] = PlanetPosition("金星", 150, 0)  // 处女座0度
            planets["火星"] = PlanetPosition("火星", 195, 0)  // 天秤座15度
            planets["木星"] = PlanetPosition("木星", 240, 0)  // 天蝎座30度
            planets["土星"] = PlanetPosition("土星", 30, 0)  // 金牛座0度
            planets["天王星"] = PlanetPosition("天王星", 75, 0)  // 双子座15度
            planets["海王星"] = PlanetPosition("海王星", 330, 0)  // 双鱼座0度
            planets["冥王星"] = PlanetPosition("冥王星", 270, 0)  // 摩羯座0度
            planets["上升点"] = PlanetPosition("上升点", 0, 0)  // 白羊座0度
            planets["中天点"] = PlanetPosition("中天点", 270, 0)  // 摩羯座0度
            ascendant = 0
            midheaven = 270

            // 添加相位关系
            aspects.add(Aspect("太阳", "木星", 120))  // 三分相
            aspects.add(Aspect("太阳", "土星", 180))  // 反对相
            aspects.add(Aspect("火星", "冥王星", 90))  // 四分相
            aspects.add(Aspect("金星", "海王星", 60))  // 六分相
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // 获取视图的中心点和半径
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(width, height) / 2f - 40f
        
        // 绘制日期和时间
        labelPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(chartDate, 10f, 30f, labelPaint)
        
        // 绘制十二星座背景区域
        drawZodiacSectors(canvas, centerX, centerY, radius)
        
        // 绘制外圆（黄道）
        canvas.drawCircle(centerX, centerY, radius, circlePaint)
        
        // 绘制内圆（表示星盘内部）
        canvas.drawCircle(centerX, centerY, radius * 0.8f, circlePaint)
        canvas.drawCircle(centerX, centerY, radius * 0.6f, circlePaint)
        
        // 绘制十二宫的分隔线
        drawHouseLines(canvas, centerX, centerY, radius)
        
        // 绘制十二星座符号
        drawZodiacSymbols(canvas, centerX, centerY, radius)
        
        // 绘制刻度
        drawDegreeMarks(canvas, centerX, centerY, radius)
        
        // 绘制相位线
        drawAspectLines(canvas, centerX, centerY, radius * 0.5f)
        
        // 绘制行星位置
        drawPlanets(canvas, centerX, centerY, radius)
        
        // 绘制相位线标签
        drawAspectLegend(canvas, 10f, height - 40f)
    }
    
    private fun drawZodiacSectors(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        val outerRadius = radius
        val innerRadius = radius * 0.8f
        
        for (i in 0 until 12) {
            val startAngle = i * 30f
            val sweepAngle = 30f
            
            val path = Path()
            path.moveTo(centerX, centerY)
            path.addArc(centerX - outerRadius, centerY - outerRadius, 
                         centerX + outerRadius, centerY + outerRadius, 
                         startAngle, sweepAngle)
            path.lineTo(centerX + innerRadius * cos(Math.toRadians((i+1) * 30.0)).toFloat(), 
                         centerY + innerRadius * sin(Math.toRadians((i+1) * 30.0)).toFloat())
            path.addArc(centerX - innerRadius, centerY - innerRadius, 
                         centerX + innerRadius, centerY + innerRadius, 
                         startAngle + sweepAngle, -sweepAngle)
            path.close()
            
            // 设置不同星座的颜色
            zodiacPaint.color = getZodiacBackgroundColor(i)
            canvas.drawPath(path, zodiacPaint)
        }
    }
    
    private fun drawHouseLines(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        // 如果没有解析到宫位数据，使用默认的均匀分布
        val houseCount = if (houses.isEmpty()) 12 else houses.size
        
        for (i in 0 until houseCount) {
            val angle = if (houses.isEmpty()) {
                Math.toRadians((i * 30).toDouble())
            } else {
                Math.toRadians(houses[i].startDegree.toDouble())
            }
            
            val startX = centerX
            val startY = centerY
            val endX = centerX + radius * cos(angle).toFloat()
            val endY = centerY + radius * sin(angle).toFloat()
            
            canvas.drawLine(startX, startY, endX, endY, linePaint)
            
            // 显示宫位号码
            val textX = centerX + radius * 0.7f * cos(angle).toFloat()
            val textY = centerY + radius * 0.7f * sin(angle).toFloat()
            
            textPaint.textSize = 16f
            canvas.drawText((i + 1).toString(), textX, textY, textPaint)
        }
    }
    
    private fun drawZodiacSymbols(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        textPaint.textSize = 18f
        
        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30 + 15).toDouble())  // 每个星座的中点
            val x = centerX + (radius + 25f) * cos(angle).toFloat()
            val y = centerY + (radius + 25f) * sin(angle).toFloat()
            
            textPaint.color = getZodiacColor(i)
            canvas.drawText(zodiacSymbols[i], x, y, textPaint)
        }
    }
    
    private fun drawDegreeMarks(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        for (i in 0 until 360 step 5) {
            val angle = Math.toRadians(i.toDouble())
            val startX = centerX + radius * 0.8f * cos(angle).toFloat()
            val startY = centerY + radius * 0.8f * sin(angle).toFloat()
            
            val length = if (i % 30 == 0) 10f else if (i % 10 == 0) 8f else 5f
            val endX = centerX + (radius * 0.8f - length) * cos(angle).toFloat()
            val endY = centerY + (radius * 0.8f - length) * sin(angle).toFloat()
            
            linePaint.color = Color.GRAY
            canvas.drawLine(startX, startY, endX, endY, linePaint)
        }
    }
    
    private fun drawPlanets(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        val innerRadius = radius * 0.5f
        planetPaint.textSize = 22f
        
        // 首先计算行星位置，以便避免重叠
        val planetCoordinates = mutableMapOf<String, Pair<Float, Float>>()
        planets.forEach { (name, position) ->
            val angle = Math.toRadians(position.degree.toDouble())
            val distance = innerRadius * (0.5f + position.house.toFloat() / 24f)
            val x = centerX + distance * cos(angle).toFloat()
            val y = centerY + distance * sin(angle).toFloat()
            
            planetCoordinates[name] = Pair(x, y)
            
            // 绘制行星符号
            val symbol = planetSymbols[name] ?: name
            planetPaint.color = getPlanetColor(name)
            canvas.drawText(symbol, x, y, planetPaint)
            
            // 绘制连接线
            linePaint.color = Color.LTGRAY
            canvas.drawLine(centerX, centerY, x, y, linePaint)
        }
        
        // 绘制特殊标记，如ASC和MC
        if (planets.containsKey("上升点")) {
            val ascAngle = Math.toRadians(ascendant.toDouble())
            val ascX = centerX + radius * cos(ascAngle).toFloat()
            val ascY = centerY + radius * sin(ascAngle).toFloat()
            
            linePaint.color = Color.BLACK
            linePaint.strokeWidth = 2f
            canvas.drawLine(centerX, centerY, ascX, ascY, linePaint)
            
            // 绘制ASC标记
            val labelX = centerX + (radius + 15f) * cos(ascAngle).toFloat()
            val labelY = centerY + (radius + 15f) * sin(ascAngle).toFloat()
            labelPaint.color = Color.BLACK
            labelPaint.textSize = 18f
            labelPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("上升", labelX, labelY, labelPaint)
            linePaint.strokeWidth = 1f
        }
        
        if (planets.containsKey("中天点")) {
            val mcAngle = Math.toRadians(midheaven.toDouble())
            val mcX = centerX + radius * cos(mcAngle).toFloat()
            val mcY = centerY + radius * sin(mcAngle).toFloat()
            
            linePaint.color = Color.BLACK
            linePaint.strokeWidth = 2f
            canvas.drawLine(centerX, centerY, mcX, mcY, linePaint)
            
            // 绘制MC标记
            val labelX = centerX + (radius + 15f) * cos(mcAngle).toFloat()
            val labelY = centerY + (radius + 15f) * sin(mcAngle).toFloat()
            labelPaint.color = Color.BLACK
            labelPaint.textSize = 18f
            labelPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("中天", labelX, labelY, labelPaint)
            linePaint.strokeWidth = 1f
        }
    }
    
    private fun drawAspectLines(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        aspects.forEach { aspect ->
            val planet1 = planets[aspect.planet1] ?: return@forEach
            val planet2 = planets[aspect.planet2] ?: return@forEach
            
            val angle1 = Math.toRadians(planet1.degree.toDouble())
            val angle2 = Math.toRadians(planet2.degree.toDouble())
            
            val x1 = centerX + radius * cos(angle1).toFloat()
            val y1 = centerY + radius * sin(angle1).toFloat()
            val x2 = centerX + radius * cos(angle2).toFloat()
            val y2 = centerY + radius * sin(angle2).toFloat()
            
            // 根据相位类型设置线条样式
            when (aspect.aspectType) {
                0 -> { // 合相
                    linePaint.color = Color.RED
                    linePaint.strokeWidth = 2f
                }
                60 -> { // 六分相
                    linePaint.color = Color.CYAN
                    linePaint.strokeWidth = 1f
                }
                90 -> { // 四分相
                    linePaint.color = Color.RED
                    linePaint.strokeWidth = 1.5f
                }
                120 -> { // 三分相
                    linePaint.color = Color.GREEN
                    linePaint.strokeWidth = 1.5f
                }
                180 -> { // 反对相
                    linePaint.color = Color.BLUE
                    linePaint.strokeWidth = 2f
                }
                else -> {
                    linePaint.color = Color.GRAY
                    linePaint.strokeWidth = 1f
                }
            }
            
            canvas.drawLine(x1, y1, x2, y2, linePaint)
        }
    }
    
    private fun drawAspectLegend(canvas: Canvas, x: Float, y: Float) {
        labelPaint.textSize = 16f
        labelPaint.textAlign = Paint.Align.LEFT
        
        // 绘制相位线图例
        linePaint.strokeWidth = 2f
        
        // 三分相 (120°)
        linePaint.color = Color.GREEN
        canvas.drawLine(x, y, x + 30f, y, linePaint)
        canvas.drawText("三分相", x + 40f, y + 5f, labelPaint)
        
        // 六分相 (60°)
        linePaint.color = Color.CYAN
        canvas.drawLine(x + 80f, y, x + 110f, y, linePaint)
        canvas.drawText("六分相", x + 120f, y + 5f, labelPaint)
        
        // 反对相 (180°)
        linePaint.color = Color.BLUE
        canvas.drawLine(x + 160f, y, x + 190f, y, linePaint)
        canvas.drawText("反对相", x + 200f, y + 5f, labelPaint)
        
        // 四分相 (90°)
        linePaint.color = Color.RED
        canvas.drawLine(x + 240f, y, x + 270f, y, linePaint)
        canvas.drawText("四分相", x + 280f, y + 5f, labelPaint)
    }
    
    private fun getZodiacBackgroundColor(index: Int): Int {
        return when (index % 4) {
            0 -> Color.rgb(255, 240, 240)  // 火象星座 - 浅红
            1 -> Color.rgb(240, 255, 240)  // 土象星座 - 浅绿
            2 -> Color.rgb(240, 240, 255)  // 风象星座 - 浅蓝
            3 -> Color.rgb(255, 255, 240)  // 水象星座 - 浅黄
            else -> Color.WHITE
        }
    }
    
    private fun getZodiacColor(index: Int): Int {
        return when (index % 4) {
            0 -> Color.RED     // 火象星座
            1 -> Color.rgb(0, 128, 0)  // 土象星座 - 绿色
            2 -> Color.rgb(255, 165, 0)  // 风象星座 - 橙色
            3 -> Color.BLUE    // 水象星座
            else -> Color.BLACK
        }
    }
    
    private fun getPlanetColor(planetName: String): Int {
        return when (planetName) {
            "太阳" -> Color.rgb(255, 140, 0)  // 橙色
            "月亮" -> Color.rgb(192, 192, 192)  // 银色
            "水星" -> Color.rgb(173, 216, 230)  // 浅蓝色
            "金星" -> Color.rgb(0, 128, 0)  // 绿色
            "火星" -> Color.RED
            "木星" -> Color.rgb(128, 0, 128)  // 紫色
            "土星" -> Color.rgb(139, 69, 19)  // 棕色
            "天王星" -> Color.rgb(0, 191, 255)  // 深蓝色
            "海王星" -> Color.BLUE
            "冥王星" -> Color.BLACK
            "上升点" -> Color.RED
            "中天点" -> Color.BLUE
            "北交点" -> Color.DKGRAY
            "南交点" -> Color.DKGRAY
            else -> Color.BLACK
        }
    }
    
    // 数据类
    data class PlanetPosition(
        val name: String,
        val degree: Int,  // 0-359度
        val house: Int    // 0-11表示第1-12宫
    )
    
    data class Aspect(
        val planet1: String,
        val planet2: String,
        val aspectType: Int  // 合相(0)、六分相(60)、四分相(90)、三分相(120)、二分相(150)、反对相(180)等
    )
    
    data class House(
        val index: Int,    // 0-11表示第1-12宫
        val startDegree: Int,  // 起始度数
        val endDegree: Int     // 结束度数
    )
} 