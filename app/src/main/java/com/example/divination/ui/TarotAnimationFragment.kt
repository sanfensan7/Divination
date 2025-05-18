package com.example.divination.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.divination.R
import com.example.divination.databinding.FragmentTarotAnimationBinding
import com.example.divination.model.DivinationMethod
import com.example.divination.model.DivinationResult
import com.example.divination.model.ResultSection
import com.example.divination.utils.DeepSeekService
import com.example.divination.utils.DivinationMethodProvider
import com.example.divination.utils.LocalStorageService
import com.example.divination.view.TarotCardView
import java.util.*

class TarotAnimationFragment : Fragment() {

    private var _binding: FragmentTarotAnimationBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var methodId: String
    private lateinit var question: String
    private lateinit var spread: String
    private lateinit var cardBackDrawable: Drawable
    private var method: DivinationMethod? = null
    private var cardViews = mutableListOf<View>()
    private var cardNames = mutableListOf<String>()
    private var selectedCards = mutableListOf<Int>()
    
    companion object {
        private const val ARG_METHOD_ID = "method_id"
        private const val ARG_QUESTION = "question"
        private const val ARG_SPREAD = "spread"
        
        fun newInstance(methodId: String, inputData: Map<String, String>): TarotAnimationFragment {
            val fragment = TarotAnimationFragment()
            val args = Bundle()
            args.putString(ARG_METHOD_ID, methodId)
            args.putString(ARG_QUESTION, inputData["question"] ?: "")
            args.putString(ARG_SPREAD, inputData["spread"] ?: "三张牌阵")
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        methodId = arguments?.getString(ARG_METHOD_ID) ?: ""
        question = arguments?.getString(ARG_QUESTION) ?: ""
        spread = arguments?.getString(ARG_SPREAD) ?: "三张牌阵"
        method = DivinationMethodProvider.getMethodById(methodId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTarotAnimationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeResources()
        setupUI()
        startCardSelection()
    }
    
    private fun initializeResources() {
        // 初始化塔罗牌资源
        cardBackDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_tarot_card_back)!!
        
        // 初始化塔罗牌名称列表
        initTarotCardNames()
    }
    
    private fun initTarotCardNames() {
        // 22张大阿卡纳牌
        cardNames.addAll(listOf(
            "愚者", "魔术师", "女祭司", "女皇", "皇帝", "教皇", "恋人", 
            "战车", "力量", "隐者", "命运之轮", "正义", "倒吊人", "死神", 
            "节制", "恶魔", "高塔", "星星", "月亮", "太阳", "审判", "世界"
        ))
        
        // 56张小阿卡纳牌 (简化为四种花色)
        val suits = listOf("权杖", "圣杯", "宝剑", "星币")
        for (suit in suits) {
            for (i in 1..10) {
                cardNames.add("$i $suit")
            }
            // 宫廷牌
            cardNames.addAll(listOf("侍从 $suit", "骑士 $suit", "王后 $suit", "国王 $suit"))
        }
    }
    
    private fun setupUI() {
        // 显示用户问题
        binding.tvQuestion.text = "问题：$question"
        binding.tvSpreadName.text = "牌阵：$spread"
        
        // 设置布局和卡片数量
        setupCardLayout()
    }
    
    private fun setupCardLayout() {
        // 根据不同的牌阵设置不同的卡片布局
        binding.cardsContainer.removeAllViews()
        cardViews.clear()
        
        when (spread) {
            "三张牌阵" -> setupThreeCardSpread()
            "凯尔特十字阵" -> setupCelticCrossSpread()
            "生命之树阵" -> setupTreeOfLifeSpread()
            else -> setupThreeCardSpread() // 默认使用三张牌阵
        }
    }
    
    private fun setupThreeCardSpread() {
        // 创建三张卡片视图并水平排列
        val container = LinearLayout(requireContext())
        container.orientation = LinearLayout.HORIZONTAL
        container.gravity = android.view.Gravity.CENTER
        container.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        // 创建三张卡片视图
        for (i in 0 until 3) {
            val cardView = createCardView()
            
            // 在LinearLayout中设置边距
            val params = LinearLayout.LayoutParams(
                cardView.layoutParams.width,
                cardView.layoutParams.height
            )
            params.marginStart = 8.dpToPx()  // 设置较小的水平间距
            params.marginEnd = 8.dpToPx()
            cardView.layoutParams = params
            
            container.addView(cardView)
            cardViews.add(cardView)
        }
        
        binding.cardsContainer.addView(container)
    }
    
    private fun setupCelticCrossSpread() {
        // 创建10张卡片的凯尔特十字牌阵
        val crossLayout = createCelticCrossLayout()
        binding.cardsContainer.addView(crossLayout)
    }
    
    private fun createCelticCrossLayout(): View {
        // 创建复杂的凯尔特十字牌阵布局
        val layout = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_celtic_cross, binding.cardsContainer, false)
        
        // 获取所有卡片视图并替换为TarotCardView
        for (i in 1..10) {
            val cardId = resources.getIdentifier("card$i", "id", requireContext().packageName)
            val cardContainer = layout.findViewById<FrameLayout>(cardId)
            
            // 创建TarotCardView并添加到容器中
            val cardView = com.example.divination.view.TarotCardView(requireContext())
            cardView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            cardView.setBackFacing(true)
            
            // 清除容器中的ImageView并添加TarotCardView
            cardContainer.removeAllViews()
            cardContainer.addView(cardView)
            
            cardViews.add(cardView)
        }
        
        return layout
    }
    
    private fun setupTreeOfLifeSpread() {
        // 创建10张卡片的生命之树牌阵
        val treeLayout = createTreeOfLifeLayout()
        binding.cardsContainer.addView(treeLayout)
    }
    
    private fun createTreeOfLifeLayout(): View {
        // 创建生命之树牌阵布局
        val layout = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_tree_of_life, binding.cardsContainer, false)
        
        // 获取所有卡片视图并替换为TarotCardView
        for (i in 1..10) {
            val cardId = resources.getIdentifier("card$i", "id", requireContext().packageName)
            val cardContainer = layout.findViewById<FrameLayout>(cardId)
            
            // 创建TarotCardView并添加到容器中
            val cardView = com.example.divination.view.TarotCardView(requireContext())
            cardView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            cardView.setBackFacing(true)
            
            // 清除容器中的ImageView并添加TarotCardView
            cardContainer.removeAllViews()
            cardContainer.addView(cardView)
            
            cardViews.add(cardView)
        }
        
        return layout
    }
    
    private fun createCardView(): View {
        // 使用自定义TarotCardView替代ImageView
        val cardView = com.example.divination.view.TarotCardView(requireContext())
        cardView.layoutParams = ViewGroup.MarginLayoutParams(
            100.dpToPx(),  // 从150改为100，减小卡片宽度
            150.dpToPx()   // 从220改为150，减小卡片高度
        )
        // TarotCardView默认显示背面
        cardView.setBackFacing(true)
        return cardView
    }
    
    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }
    
    private fun startCardSelection() {
        // 根据牌阵类型确定是否显示动画
        when (spread) {
            "三张牌阵" -> {
                // 对三张牌阵保持原有的动画效果
                binding.tvInstructions.text = "请稍候，正在洗牌..."
                
                // 洗牌动画
                animateShuffling {
                    // 洗牌完成后，开始抽牌
                    binding.tvInstructions.text = "请在心中默念问题，然后点击任意一张牌..."
                    
                    // 设置卡片的点击事件
                    for (i in cardViews.indices) {
                        val cardView = cardViews[i]
                        cardView.tag = i
                        cardView.setOnClickListener { view ->
                            onCardSelected(view.tag as Int)
                        }
                    }
                }
            }
            "凯尔特十字阵", "生命之树阵" -> {
                // 凯尔特十字阵和生命之树阵直接显示结果，不展示动画
                binding.tvInstructions.text = "塔罗牌解读完成，正在生成详细分析..."
                
                // 自动为所有卡片生成随机牌面
                for (i in cardViews.indices) {
                    // 获取随机卡片
                    val randomCardIndex = Random().nextInt(cardNames.size)
                    val cardName = cardNames[randomCardIndex]
                    val meaningIndex = Random().nextInt(CARD_MEANINGS.size)
                    val cardMeaning = CARD_MEANINGS[meaningIndex]
                    
                    // 设置牌面
                    val cardView = cardViews[i]
                    if (cardView is TarotCardView) {
                        cardView.setCardInfo(
                            name = cardName,
                            meaning = cardMeaning,
                            reversed = false  // 正位显示
                        )
                    }
                    
                    // 保存卡片信息
                    cardView.tag = cardName
                    selectedCards.add(i)
                    
                    // 更新底部状态显示
                    showCardName(cardView, cardName)
                }
                
                // 延迟后，请求AI解读
                Handler(Looper.getMainLooper()).postDelayed({
                    generateTarotReading()
                }, 1500)
            }
            else -> {
                // 默认使用动画
                binding.tvInstructions.text = "请稍候，正在洗牌..."
                
                animateShuffling {
                    binding.tvInstructions.text = "请在心中默念问题，然后点击任意一张牌..."
                    
                    for (i in cardViews.indices) {
                        val cardView = cardViews[i]
                        cardView.tag = i
                        cardView.setOnClickListener { view ->
                            onCardSelected(view.tag as Int)
                        }
                    }
                }
            }
        }
    }
    
    private fun animateShuffling(onComplete: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        val randomCards = cardViews.indices.toList().shuffled()
        
        // 设置初始透明度和缩放
        cardViews.forEach { cardView ->
            cardView.alpha = 0.9f
            cardView.scaleX = 0.95f
            cardView.scaleY = 0.95f
        }
        
        // 创建洗牌开始的集体动画
        val initialAnimations = mutableListOf<Animator>()
        cardViews.forEach { cardView ->
            val shake = ObjectAnimator.ofFloat(cardView, "rotation", -5f, 5f, 0f)
            shake.duration = 500
            shake.interpolator = AccelerateDecelerateInterpolator()
            
            val fadeIn = ObjectAnimator.ofFloat(cardView, "alpha", 0.9f, 1f)
            fadeIn.duration = 500
            
            val scaleUpX = ObjectAnimator.ofFloat(cardView, "scaleX", 0.95f, 1f)
            val scaleUpY = ObjectAnimator.ofFloat(cardView, "scaleY", 0.95f, 1f)
            scaleUpX.duration = 500
            scaleUpY.duration = 500
            
            initialAnimations.add(shake)
            initialAnimations.add(fadeIn)
            initialAnimations.add(scaleUpX)
            initialAnimations.add(scaleUpY)
        }
        
        val initialAnimSet = AnimatorSet()
        initialAnimSet.playTogether(initialAnimations)
        initialAnimSet.start()
        
        // 洗牌开始后的动画延迟
        handler.postDelayed({
            // 洗牌动画序列
            val totalShuffles = 3 // 洗牌次数
            var shuffleCount = 0
            
            fun doShuffleAnimation() {
                if (shuffleCount >= totalShuffles) {
                    // 洗牌结束，开始最终整理动画
                    val finalAnimations = mutableListOf<Animator>()
                    cardViews.forEach { cardView ->
                        val finalRotation = ObjectAnimator.ofFloat(cardView, "rotation", cardView.rotation, 0f)
                        finalRotation.duration = 400
                        finalRotation.interpolator = DecelerateInterpolator()
                        
                        val finalScaleX = ObjectAnimator.ofFloat(cardView, "scaleX", cardView.scaleX, 1f)
                        val finalScaleY = ObjectAnimator.ofFloat(cardView, "scaleY", cardView.scaleY, 1f)
                        finalScaleX.duration = 400
                        finalScaleY.duration = 400
                        
                        finalAnimations.add(finalRotation)
                        finalAnimations.add(finalScaleX)
                        finalAnimations.add(finalScaleY)
                    }
                    
                    val finalAnimSet = AnimatorSet()
                    finalAnimSet.playTogether(finalAnimations)
                    finalAnimSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            // 添加一个视觉反馈，表示洗牌完成
                            val pulseAnimations = mutableListOf<Animator>()
                            cardViews.forEach { cardView ->
                                val pulseX = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 1.05f, 1f)
                                val pulseY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 1.05f, 1f)
                                pulseX.duration = 300
                                pulseY.duration = 300
                                
                                pulseAnimations.add(pulseX)
                                pulseAnimations.add(pulseY)
                            }
                            
                            val pulseAnimSet = AnimatorSet()
                            pulseAnimSet.playTogether(pulseAnimations)
                            pulseAnimSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: android.animation.Animator) {
                                    // 最终完成回调
                                    handler.postDelayed({
                                        onComplete()
                                    }, 300)
                                }
                            })
                            pulseAnimSet.start()
                        }
                    })
                    finalAnimSet.start()
                    return
                }
                
                // 执行一次洗牌
                for (i in randomCards.indices) {
                    handler.postDelayed({
                        val cardView = cardViews[randomCards[i]]
                        
                        // 创建洗牌动画
                        val scaleX = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 0.95f, 1f)
                        val scaleY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 0.98f, 1f)
                        val rotation = ObjectAnimator.ofFloat(cardView, "rotation", 
                            cardView.rotation, cardView.rotation + (Math.random() * 8 - 4).toFloat())
                        val translationX = ObjectAnimator.ofFloat(cardView, "translationX", 
                            0f, (Math.random() * 20 - 10).toFloat(), 0f)
                        
                        scaleX.duration = 250
                        scaleY.duration = 250
                        rotation.duration = 250
                        translationX.duration = 250
                        
                        val animSet = AnimatorSet()
                        animSet.playTogether(scaleX, scaleY, rotation, translationX)
                        animSet.interpolator = AccelerateDecelerateInterpolator()
                        animSet.start()
                        
                        // 所有卡片动画完成后进行下一次洗牌
                        if (i == randomCards.size - 1) {
                            shuffleCount++
                            handler.postDelayed({
                                doShuffleAnimation()
                            }, 300)
                        }
                    }, i * 100L) // 洗牌速度比原来快一些
                }
            }
            
            // 开始洗牌动画序列
            doShuffleAnimation()
            
        }, 600) // 等待初始动画完成
    }
    
    private fun onCardSelected(position: Int) {
        // 防止重复选择
        if (selectedCards.contains(position)) return
        
        val cardView = cardViews[position]
        cardView.isClickable = false
        selectedCards.add(position)
        
        // 翻转动画
        flipCard(cardView) {
            // 查看是否选择了足够的卡片
            checkCompletedSelection()
        }
    }
    
    private fun flipCard(cardView: View, onComplete: () -> Unit) {
        // 获取随机卡片
        val randomCardIndex = Random().nextInt(cardNames.size)
        val cardName = cardNames[randomCardIndex]
        val isReversed = false // 始终为正位，不再使用随机逻辑
        
        // 随机选择牌面含义
        val meaningIndex = Random().nextInt(CARD_MEANINGS.size)
        val cardMeaning = CARD_MEANINGS[meaningIndex]
        
        // 获取卡片在屏幕中的位置
        val cardLocation = IntArray(2)
        cardView.getLocationOnScreen(cardLocation)
        
        // 获取牌堆位置（假设在屏幕中心）
        val centerX = binding.root.width / 2
        val centerY = binding.root.height / 2
        
        // 计算移动距离
        val moveX = cardLocation[0] - centerX + cardView.width / 2
        val moveY = cardLocation[1] - centerY + cardView.height / 2
        
        // 创建移动动画（从牌堆到目标位置）
        val translateX = ObjectAnimator.ofFloat(cardView, "translationX", -moveX.toFloat(), 0f)
        val translateY = ObjectAnimator.ofFloat(cardView, "translationY", -moveY.toFloat(), 0f)
        val translateAnimSet = AnimatorSet()
        translateAnimSet.playTogether(translateX, translateY)
        translateAnimSet.duration = 500
        translateAnimSet.interpolator = DecelerateInterpolator()
        
        // 创建3D翻转动画
        val flipOutAnimator = ObjectAnimator.ofFloat(cardView, "rotationY", 0f, 90f)
        flipOutAnimator.duration = 300
        flipOutAnimator.interpolator = DecelerateInterpolator()
        
        val flipInAnimator = ObjectAnimator.ofFloat(cardView, "rotationY", -90f, 0f)
        flipInAnimator.duration = 300
        flipInAnimator.interpolator = DecelerateInterpolator()
        
        // 添加视觉效果（缩放和亮度变化）
        val scaleXOut = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 1.05f)
        val scaleYOut = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 1.05f)
        scaleXOut.duration = 300
        scaleYOut.duration = 300
        
        val scaleXIn = ObjectAnimator.ofFloat(cardView, "scaleX", 1.05f, 1f)
        val scaleYIn = ObjectAnimator.ofFloat(cardView, "scaleY", 1.05f, 1f)
        scaleXIn.duration = 300
        scaleYIn.duration = 300
        
        // 设置卡片选中效果
        val selectedAlpha = ObjectAnimator.ofFloat(cardView, "alpha", 1f, 0.9f, 1f)
        selectedAlpha.duration = 600
        
        // 创建完整动画序列
        val fullAnimSet = AnimatorSet()
        
        // 首先执行移动动画
        translateAnimSet.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                // 移动结束后执行翻转动画
                flipOutAnimator.start()
            }
        })
        
        // 在翻转一半时切换卡面
        flipOutAnimator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                // 翻到一半时，切换卡面
                if (cardView is TarotCardView) {
                    // 如果是TarotCardView，直接设置卡片信息
                    cardView.setCardInfo(
                        name = cardName,
                        meaning = cardMeaning,
                        reversed = isReversed
                    )
                } else {
                    // 对于普通View，设置背景图片
                    val resourceId = getRandomCardResourceId(cardName)
                    if (cardView is ImageView) {
                        cardView.setImageResource(resourceId)
                    }
                }
                
                // 显示卡片名称
                showCardName(cardView, cardName)
                
                // 开始翻转回来的动画
                flipInAnimator.start()
            }
        })
        
        // 翻转完成后执行后续动作
        flipInAnimator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                // 添加卡片放大缩小的视觉效果
                val finalAnimation = AnimatorSet()
                finalAnimation.playTogether(scaleXOut, scaleYOut, selectedAlpha)
                
                finalAnimation.addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        // 恢复原始大小
                        val scaleNormalizer = AnimatorSet()
                        scaleNormalizer.playTogether(scaleXIn, scaleYIn)
                        scaleNormalizer.start()
                        
                        // 完成回调
                        onComplete()
                    }
                })
                
                finalAnimation.start()
            }
        })
        
        // 启动整个动画序列
        translateAnimSet.start()
    }
    
    // 塔罗牌牌面含义示例
    private val CARD_MEANINGS = arrayOf(
        "象征新的开始和无限可能",
        "代表权力、意志和创造力",
        "暗示直觉、智慧和内在知识",
        "象征富足、创造和滋养",
        "代表掌控、权威和稳定",
        "暗示精神指引和传统",
        "象征爱情、和谐与选择",
        "代表决心、意志力与成功",
        "暗示勇气、力量和信心",
        "象征反思、寻求真理和孤独",
        "代表命运、变化与机遇",
        "暗示平衡、公正与真理",
        "象征牺牲、让步和新视角",
        "代表结束、变革和重生",
        "暗示中庸、平衡与和谐",
        "象征诱惑、执着与束缚",
        "代表突然变化、混乱与释放",
        "暗示希望、启示与灵感",
        "象征幻觉、直觉与潜意识",
        "代表成功、喜悦与活力",
        "暗示重生、更新与决定",
        "象征完成、成就与圆满"
    )

    private fun addTextToCardView(cardView: View, cardName: String) {
        // 如果父视图是FrameLayout，则可以添加TextView到卡片上
        val parent = cardView.parent as? ViewGroup ?: return
        
        // 检查是否已有文本标签
        val existingTextView = parent.findViewWithTag<TextView>("${cardView.id}_text")
        if (existingTextView != null) {
            // 更新现有标签文本
            existingTextView.text = cardName
            return
        }
        
        // 创建文本标签
        val textView = TextView(requireContext()).apply {
            text = cardName
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            textSize = 12f
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            alpha = 0.85f
            
            // 设置唯一标签以便查找
            tag = "${cardView.id}_text"
        }
        
        // 将标签相对位置与卡片关联
        val cardIndex = cardViews.indexOf(cardView)
        if (parent is FrameLayout) {
            // 添加文本视图到框架布局
            textView.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.BOTTOM
            }
            parent.addView(textView)
        } else if (parent is ConstraintLayout && cardIndex != -1) {
            // 在ConstraintLayout中，需要设置约束关系
            textView.layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                // 设置约束，使TextView位于卡片底部
                bottomToBottom = cardView.id
                startToStart = cardView.id
                endToEnd = cardView.id
                // 将TextView的底部与卡片底部对齐，但稍微偏移
                setMargins(0, 0, 0, 0)
            }
            parent.addView(textView)
        } else {
            // 对于其他类型的父布局，使用基本的边距布局
            textView.layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                // 设置位置为卡片底部
                val cardPosition = IntArray(2)
                cardView.getLocationOnScreen(cardPosition)
                val cardWidth = cardView.width
                val cardHeight = cardView.height
                
                // 将TextView的位置设置在卡片下方
                width = cardWidth
                setMargins(cardPosition[0], cardPosition[1] + cardHeight - 30, 0, 0)
            }
            parent.addView(textView)
        }
    }
    
    private fun getRandomCardResourceId(cardName: String): Int {
        // 根据卡片名称获取对应的资源ID
        // 目前使用默认资源，实际项目中应该有对应的牌面资源
        return when {
            cardName.contains("愚者") -> R.drawable.ic_tarot_card_front
            cardName.contains("魔术师") -> R.drawable.ic_tarot_card_front
            cardName.contains("女祭司") -> R.drawable.ic_tarot_card_front
            // 其他卡牌同理，实际项目中应该为每个牌面创建资源
            else -> R.drawable.ic_tarot_card_front
        }
    }
    
    private fun showCardName(cardView: View, cardName: String) {
        // 显示卡片名称
        val position = cardViews.indexOf(cardView)
        
        // 获取牌位标题
        val title = getCardPositionTitle(position)
        
        // 更新底部状态栏显示最近翻开的牌
        binding.tvCardDescription.text = "抽到的牌: $cardName ($title)"
        
        // 更新已抽到的牌的列表显示
        updateCardListDisplay()
    }
    
    private fun getCardPositionTitle(position: Int): String {
        return when (spread) {
            "三张牌阵" -> {
                val titles = listOf("过去", "现在", "未来")
                if (position < titles.size) titles[position] else ""
            }
            "凯尔特十字阵" -> {
                val titles = listOf("现状", "挑战", "过去", "未来", "上方", "下方", "建议", "外在影响", "希望与恐惧", "最终结果")
                if (position < titles.size) titles[position] else ""
            }
            "生命之树阵" -> {
                val titles = listOf("精神", "智慧", "理解", "慈悲", "力量", "美", "胜利", "荣耀", "基础", "物质")
                if (position < titles.size) titles[position] else ""
            }
            else -> ""
        }
    }
    
    private fun updateCardListDisplay() {
        // 显示所有已选择的牌列表
        if (selectedCards.isEmpty()) return
        
        val cardInfoBuilder = StringBuilder()
        cardInfoBuilder.append("已抽到的牌：\n")
        
        for (i in selectedCards.indices) {
            val position = selectedCards[i]
            val cardView = cardViews[position]
            val cardName = cardView.tag as? String ?: "未知"
            val title = getCardPositionTitle(position)
            
            cardInfoBuilder.append("${i+1}. $cardName ($title)\n")
        }
        
        // 更新描述区域，显示所有卡片
        binding.tvCardsInfo.text = cardInfoBuilder.toString()
        binding.tvCardsInfo.visibility = View.VISIBLE
    }
    
    private fun checkCompletedSelection() {
        // 检查是否已选择足够的卡片
        val requiredCards = when (spread) {
            "三张牌阵" -> 3
            "凯尔特十字阵" -> 10
            "生命之树阵" -> 10
            else -> 3
        }
        
        if (selectedCards.size >= requiredCards) {
            // 禁用所有卡片点击
            for (cardView in cardViews) {
                cardView.isClickable = false
            }
            
            // 显示完成消息
            binding.tvInstructions.text = "塔罗牌解读完成，正在生成详细分析..."
            
            // 延迟后，请求AI解读
            Handler(Looper.getMainLooper()).postDelayed({
                generateTarotReading()
            }, 1500)
        } else {
            // 更新提示信息
            binding.tvInstructions.text = "请继续选择剩余的卡片...(${selectedCards.size}/$requiredCards)"
        }
    }
    
    private fun generateTarotReading() {
        // 准备输入数据
        val inputData = mapOf(
            "question" to question,
            "spread" to spread,
            "cards" to getSelectedCardNames()
        )
        
        // 显示加载状态
        binding.progressBar.visibility = View.VISIBLE
        
        // 调用AI服务获取解读
        DeepSeekService.performDivination(
            requireContext(),
            method!!,
            inputData
        ) { result, error ->
            binding.progressBar.visibility = View.GONE
            
            if (error != null) {
                binding.tvInstructions.text = "解读失败：${error.message}"
            } else if (result != null) {
                // 保存结果并显示
                LocalStorageService.saveResult(requireContext(), result)
                showResult(result)
            }
        }
    }
    
    private fun getSelectedCardNames(): String {
        // 获取已选择的卡片名称
        val selectedCardNames = mutableListOf<String>()
        
        for (i in selectedCards.indices) {
            val position = selectedCards[i]
            val cardView = cardViews[position]
            val cardName = cardView.tag as? String ?: "未知"
            selectedCardNames.add(cardName)
        }
        
        return selectedCardNames.joinToString(", ")
    }
    
    private fun showResult(result: DivinationResult) {
        // 跳转到结果页面
        val resultFragment = DivinationResultFragment.newInstance(result.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, resultFragment)
            .addToBackStack(null)
            .commit()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 