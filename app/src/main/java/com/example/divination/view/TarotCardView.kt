package com.example.divination.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.divination.R

/**
 * 自定义塔罗牌视图
 * 提供更美观的塔罗牌展示效果
 */
class TarotCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        setShadowLayer(12f, 0f, 4f, Color.parseColor("#40000000"))
    }
    
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B8860B") // 暗金色
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 30f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    
    private val symbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700") // 金色
        style = Paint.Style.FILL
    }
    
    private var cardName: String = ""
    private var cardMeaning: String = ""
    private var cardIcon: Drawable? = null
    private var isReversed: Boolean = false
    private var isBackFacing: Boolean = true
    // 添加一个标记，记录卡片是否已经被翻转为正面
    private var hasBeenFlipped: Boolean = false
    
    // 卡片背面图案
    private var cardBackDrawable: Drawable? = null
    
    init {
        // 初始化卡片背面图案
        cardBackDrawable = ContextCompat.getDrawable(context, R.drawable.ic_tarot_card_back)
        
        // 启用硬件加速以支持阴影绘制
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }
    
    /**
     * 设置卡片信息
     */
    fun setCardInfo(name: String, meaning: String = "", icon: Drawable? = null, reversed: Boolean = false) {
        this.cardName = name
        this.cardMeaning = meaning
        this.cardIcon = icon
        this.isReversed = reversed
        this.isBackFacing = false
        this.hasBeenFlipped = true
        invalidate()
    }
    
    /**
     * 设置是否显示卡片背面
     */
    fun setBackFacing(backFacing: Boolean) {
        // 如果卡片已经翻转为正面，并且试图将其设置回背面，则忽略此操作
        if (hasBeenFlipped && backFacing) {
            return
        }
        
        // 否则设置新状态
        this.isBackFacing = backFacing
        
        // 如果设置为正面，标记为已翻转
        if (!backFacing) {
            this.hasBeenFlipped = true
        }
        
        invalidate()
    }
    
    /**
     * 翻转卡片（正反面切换）
     */
    fun flip() {
        // 如果卡片已经翻转为正面，不再允许翻回背面
        if (hasBeenFlipped && !isBackFacing) {
            return
        }
        
        isBackFacing = !isBackFacing
        
        // 如果翻转到正面，标记为已翻转
        if (!isBackFacing) {
            hasBeenFlipped = true
        }
        
        // 确保先更新camera位置，实现3D效果
        val centerX = width / 2f
        val centerY = height / 2f
        
        // 设置相机距离，提供更好的3D透视效果
        setCameraDistance(8000f)
        
        // 添加翻转动画时的阴影变化
        if (isBackFacing) {
            cardPaint.setShadowLayer(12f, 0f, 4f, Color.parseColor("#40000000"))
        } else {
            cardPaint.setShadowLayer(16f, 0f, 6f, Color.parseColor("#60000000"))
        }
        
        invalidate()
    }
    
    /**
     * 设置3D视图的相机距离
     */
    override fun setCameraDistance(distance: Float) {
        // 调用View的setCameraDistance方法
        super.setCameraDistance(distance)
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 保持卡片宽高比为2:3
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = (width * 1.5).toInt()
        setMeasuredDimension(width, height)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val height = height.toFloat()
        val cornerRadius = width / 10
        
        // 绘制卡片背景
        canvas.drawRoundRect(0f, 0f, width, height, cornerRadius, cornerRadius, cardPaint)
        
        if (isBackFacing) {
            // 绘制卡片背面
            drawCardBack(canvas, width, height)
        } else {
            // 绘制卡片正面
            drawCardFront(canvas, width, height)
        }
        
        // 绘制卡片边框
        canvas.drawRoundRect(6f, 6f, width - 6f, height - 6f, cornerRadius, cornerRadius, borderPaint)
    }
    
    private fun drawCardBack(canvas: Canvas, width: Float, height: Float) {
        cardBackDrawable?.let { drawable ->
            drawable.setBounds(0, 0, width.toInt(), height.toInt())
            drawable.draw(canvas)
        } ?: run {
            // 如果没有背面图案，绘制默认花纹
            val centerX = width / 2
            val centerY = height / 2
            val starRadius = width / 6
            
            // 绘制星星图案
            for (i in 0 until 8) {
                val angle = Math.PI * i / 4
                val x = centerX + starRadius * Math.cos(angle).toFloat()
                val y = centerY + starRadius * Math.sin(angle).toFloat()
                
                canvas.drawCircle(x, y, 10f, symbolPaint)
            }
            
            // 绘制中心图案
            canvas.drawCircle(centerX, centerY, starRadius / 2, symbolPaint)
        }
    }
    
    private fun drawCardFront(canvas: Canvas, width: Float, height: Float) {
        val centerX = width / 2
        val paddingX = width / 10
        val paddingY = height / 10
        
        // 如果卡片被反向，旋转画布
        if (isReversed) {
            canvas.save()
            canvas.rotate(180f, centerX, height / 2)
        }
        
        // 绘制卡片名称
        textPaint.textSize = width / 10
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(cardName, centerX, paddingY * 2, textPaint)
        
        // 绘制图标或符号
        val iconSize = width / 2
        val iconTop = height / 3 - iconSize / 2
        
        cardIcon?.let { icon ->
            icon.setBounds(
                (centerX - iconSize / 2).toInt(),
                iconTop.toInt(),
                (centerX + iconSize / 2).toInt(),
                (iconTop + iconSize).toInt()
            )
            icon.draw(canvas)
        } ?: run {
            // 如果没有图标，绘制一个默认符号
            canvas.drawCircle(centerX, height / 3, iconSize / 3, symbolPaint)
        }
        
        // 绘制卡片含义（如果有）
        if (cardMeaning.isNotEmpty()) {
            textPaint.textSize = width / 18
            textPaint.textAlign = Paint.Align.LEFT
            
            val textWidth = width - 2 * paddingX
            val staticLayout = StaticLayout.Builder.obtain(
                cardMeaning, 0, cardMeaning.length, textPaint, textWidth.toInt()
            ).build()
            
            // 保存当前画布状态
            canvas.save()
            // 移动画布位置到文本起始点
            canvas.translate(paddingX, height * 2 / 3)
            // 绘制文本
            staticLayout.draw(canvas)
            // 恢复画布状态
            canvas.restore()
        }
        
        // 如果卡片被反向，恢复画布
        if (isReversed) {
            canvas.restore()
            
            // 添加"逆位"标记
            textPaint.textSize = width / 15
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.color = Color.RED
            canvas.drawText("逆位", centerX, height - paddingY / 2, textPaint)
            textPaint.color = Color.BLACK
        }
    }
} 