package com.example.divination.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import kotlin.math.min
import android.widget.Toast
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.doOnLayout
import com.example.divination.utils.safePerformDivination

class TarotAnimationFragment : Fragment() {

    private var _binding: FragmentTarotAnimationBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding is null, Fragment可能已被销毁")
    
    private lateinit var methodId: String
    private lateinit var question: String
    private lateinit var spread: String
    private lateinit var cardBackDrawable: Drawable
    private var method: DivinationMethod? = null
    private var cardViews = mutableListOf<View>()
    private var cardNames = mutableListOf<String>()
    private var selectedCards = mutableListOf<Int>()
    private var isActive = true // 跟踪Fragment是否处于活跃状态
    
    companion object {
        private const val ARG_METHOD_ID = "method_id"
        private const val ARG_QUESTION = "question"
        
        fun newInstance(methodId: String, inputData: Map<String, String>): TarotAnimationFragment {
            val fragment = TarotAnimationFragment()
            val args = Bundle()
            args.putString(ARG_METHOD_ID, methodId)
            args.putString(ARG_QUESTION, inputData["question"] ?: "")
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        methodId = arguments?.getString(ARG_METHOD_ID) ?: ""
        question = arguments?.getString(ARG_QUESTION) ?: ""
        spread = "三张牌阵" // 默认使用三张牌阵
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
        
        // 只使用三张牌阵
        setupThreeCardSpread()
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
    
    private fun animateShuffling(onComplete: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        val randomCards = cardViews.indices.toList().shuffled()
        
        // 设置初始透明度和缩放
        cardViews.forEach { cardView ->
            cardView.alpha = 0.9f
            cardView.scaleX = 0.95f
            cardView.scaleY = 0.95f
            
            // 禁用点击事件，直到洗牌完成
            cardView.isClickable = false
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
                                    // 启用卡片点击事件
                                    for (cardView in cardViews) {
                                        cardView.isClickable = true
                                    }
                                    
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
        try {
            // 防止重复选择
            if (selectedCards.contains(position)) return
            
            // 确保位置有效
            if (position < 0 || position >= cardViews.size) {
                Log.e("TarotAnimation", "无效的卡片位置: $position")
                return
            }
            
            val cardView = cardViews[position]
            
            // 添加选中前的小动画效果
            val pulseAnim = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 1.1f, 1f)
            val pulseAnimY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 1.1f, 1f)
            pulseAnim.duration = 200
            pulseAnimY.duration = 200
            
            val animSet = AnimatorSet()
            animSet.playTogether(pulseAnim, pulseAnimY)
            animSet.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    // 动画结束后禁用点击
                    cardView.isClickable = false
                    selectedCards.add(position)
                    
                    // 翻转动画
                    flipCard(cardView) {
                        // 查看是否选择了足够的卡片
                        checkCompletedSelection()
                    }
                }
            })
            animSet.start()
        } catch (e: Exception) {
            Log.e("TarotAnimation", "卡片选择异常", e)
            Toast.makeText(requireContext(), "卡片选择出错", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun flipCard(cardView: View, onComplete: () -> Unit) {
        try {
            // 获取随机卡片
            val randomCardIndex = Random().nextInt(cardNames.size)
            val cardName = cardNames[randomCardIndex]
            val isReversed = false // 始终为正位，不再使用随机逻辑
            
            // 随机选择牌面含义
            val meaningIndex = min(Random().nextInt(CARD_MEANINGS.size), CARD_MEANINGS.size - 1)
            val cardMeaning = CARD_MEANINGS[meaningIndex]
            
            // 设置卡片标记
            cardView.tag = cardName
            
            // 如果是TarotCardView，使用其翻转方法
            if (cardView is TarotCardView) {
                // 设置卡片信息
                cardView.setCardInfo(cardName, cardMeaning, null, isReversed)
                
                // 先禁用卡片的点击
                cardView.isClickable = false
                
                // 执行翻转动画
                val flipAnim = ObjectAnimator.ofFloat(cardView, "rotationY", 0f, 90f)
                flipAnim.duration = 300
                flipAnim.interpolator = AccelerateDecelerateInterpolator()
                
                flipAnim.addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        // 翻转到90度时，卡片已经看不见了，这时翻转卡片的正反面
                        cardView.setBackFacing(false) // 直接设置为正面，不使用flip()
                        
                        // 继续翻转到360度而不是180度，这样卡片方向就是正确的
                        val flipBackAnim = ObjectAnimator.ofFloat(cardView, "rotationY", 90f, 360f)
                        flipBackAnim.duration = 300
                        flipBackAnim.interpolator = DecelerateInterpolator()
                        
                        flipBackAnim.addListener(object : android.animation.AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: android.animation.Animator) {
                                // 显示卡片名称
                                showCardName(cardView, cardName)
                                
                                // 动画完成后重置rotationY为0以避免累积旋转
                                cardView.rotationY = 0f
                                
                                // 确保还是显示正面
                                cardView.setBackFacing(false)
                                
                                // 完成回调
                                onComplete()
                            }
                        })
                        
                        flipBackAnim.start()
                    }
                })
                
                flipAnim.start()
            } else {
                // 对于非TarotCardView，使用旋转动画模拟翻转
                val rotation = ObjectAnimator.ofFloat(cardView, "rotationY", 0f, 90f)
                rotation.duration = 300
                rotation.interpolator = AccelerateDecelerateInterpolator()
                
                rotation.addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        // 在旋转到90度时更新卡面
                        if (cardView is ImageView) {
                            cardView.setImageResource(getRandomCardResourceId(cardName))
                        }
                        
                        // 继续旋转到360度而不是180度，保证卡片正面朝上
                        val rotation2 = ObjectAnimator.ofFloat(cardView, "rotationY", 90f, 360f)
                        rotation2.duration = 300
                        rotation2.interpolator = DecelerateInterpolator()
                        
                        rotation2.addListener(object : android.animation.AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: android.animation.Animator) {
                                // 显示卡片名称
                                showCardName(cardView, cardName)
                                
                                // 动画完成后重置rotationY为0以避免累积旋转
                                cardView.rotationY = 0f
                                
                                // 完成回调
                                onComplete()
                            }
                        })
                        
                        rotation2.start()
                    }
                })
                
                rotation.start()
            }
        } catch (e: Exception) {
            Log.e("TarotAnimation", "卡片翻转异常", e)
            // 确保回调被调用，避免流程中断
            onComplete()
        }
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
        try {
            // 显示卡片名称
            val position = cardViews.indexOf(cardView)
            
            // 获取牌位标题
            val title = getCardPositionTitle(position)
            
            // 更新底部状态栏显示最近翻开的牌
            binding.tvCardDescription.text = "抽到的牌: $cardName ($title)"
            
            // 更新已抽到的牌的列表显示
            updateCardListDisplay()
        } catch (e: Exception) {
            Log.e("TarotAnimation", "显示卡片名称异常", e)
        }
    }
    
    private fun getCardPositionTitle(position: Int): String {
        // 只保留三张牌阵的位置标题
                val titles = listOf("过去", "现在", "未来")
        return if (position < titles.size) titles[position] else ""
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
        try {
            // 三张牌阵需要3张卡片
            val requiredCards = 3
            
            if (selectedCards.size >= requiredCards) {
                // 禁用所有卡片点击
                for (cardView in cardViews) {
                    cardView.isClickable = false
                }
                
                // 显示完成消息
                binding.tvInstructions.text = "塔罗牌解读完成，正在生成详细分析..."
                binding.tvInstructions.text = getString(R.string.ai_thinking_time, 80)
                
                // 延迟后，请求AI解读
                Handler(Looper.getMainLooper()).postDelayed({
                    generateTarotReading()
                }, 1500)
            } else {
                // 更新提示信息
                binding.tvInstructions.text = "请继续选择剩余的卡片...(${selectedCards.size}/$requiredCards)"
            }
        } catch (e: Exception) {
            Log.e("TarotAnimation", "检查卡片选择完成异常", e)
            // 如果出错，尝试直接生成解读
            binding.tvInstructions.text = "处理卡片时出错，尝试生成解读..."
            generateTarotReading()
        }
    }
    
    private fun generateTarotReading() {
        try {
            // 准备输入数据
            val inputData = mapOf(
                "question" to question,
                "spread" to spread,
                "cards" to getSelectedCardNames()
            )
            
            // 显示加载状态
            showLoadingAnimation(true)
            
            // 调用AI服务获取解读
            safePerformDivination(
                requireContext(),
                method!!,
                inputData
            ) { result, error ->
                // 首先检查Fragment是否仍然处于活跃状态
                if (!isActive || !isAdded || _binding == null) return@safePerformDivination
                
                try {
                    showLoadingAnimation(false)
                    
                    if (error != null) {
                        safeSetText(binding.tvInstructions, "解读失败：${error.message?.take(50) ?: "未知错误"}")
                    } else if (result != null) {
                        // 保存结果并显示
                        LocalStorageService.saveResult(requireContext(), result)
                        showResult(result)
                    } else {
                        safeSetText(binding.tvInstructions, "解读失败：未知错误")
                    }
                } catch (e: Exception) {
                    Log.e("TarotAnimation", "解读回调处理异常", e)
                    showLoadingAnimation(false)
                    safeSetText(binding.tvInstructions, "解读处理出错：${e.message?.take(50) ?: "未知错误"}")
                }
            }
        } catch (e: Exception) {
            Log.e("TarotAnimation", "生成解读异常", e)
            showLoadingAnimation(false)
            safeSetText(binding.tvInstructions, "生成解读出错：${e.message?.take(50) ?: "未知错误"}")
        }
    }
    
    // 显示/隐藏加载动画
    private fun showLoadingAnimation(show: Boolean) {
        if (!isActive || !isAdded || _binding == null) return
        try {
            if (show) {
                // 显示加载卡片
                binding.cardLoading.visibility = View.VISIBLE
                binding.tvLoadingHint.text = getRandomTarotLoadingHint()
                startTarotLoadingAnimation()
            } else {
                // 隐藏加载卡片
                binding.cardLoading.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e("TarotAnimation", "显示加载动画异常", e)
        }
    }
    
    // 获取随机的塔罗牌加载提示
    private fun getRandomTarotLoadingHint(): String {
        val hints = listOf(
            "🔮 塔罗牌正在为您占卜...",
            "🃏 解读牌面能量中...",
            "✨ 分析牌阵组合...",
            "🌙 感应宇宙讯息...",
            "💫 揭示命运真相...",
            "⭐ 连接塔罗智慧...",
            "🌟 探索未知领域..."
        )
        return hints.random()
    }
    
    // 启动塔罗牌加载动画
    private fun startTarotLoadingAnimation() {
        if (!isActive || !isAdded || _binding == null) return
        try {
            // 为整个卡片添加淡入动画
            val fadeInAnimation = AnimationUtils.loadAnimation(
                requireContext(), 
                R.anim.fade_in
            )
            binding.cardLoading.startAnimation(fadeInAnimation)
            
            // 为进度条外圈添加旋转动画
            val rotateAnimation = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.rotate_animation
            )
            binding.progressBar.parent?.let { parent ->
                if (parent is View) {
                    parent.startAnimation(rotateAnimation)
                }
            }
            
            // 为提示文字添加脉动效果
            val pulseAnimation = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.pulse_animation
            )
            binding.tvLoadingHint.startAnimation(pulseAnimation)
            
            // 为三个小圆点添加错开的脉动动画
            animateTarotLoadingDots()
        } catch (e: Exception) {
            Log.e("TarotAnimation", "启动加载动画异常", e)
        }
    }
    
    // 为加载指示点添加错开的动画效果
    private fun animateTarotLoadingDots() {
        if (!isActive || !isAdded || _binding == null) return
        try {
            val dot1 = binding.root.findViewById<View>(R.id.loadingDot1)
            val dot2 = binding.root.findViewById<View>(R.id.loadingDot2)
            val dot3 = binding.root.findViewById<View>(R.id.loadingDot3)
            
            // 为每个点创建脉动动画，但添加不同的延迟
            dot1?.let { animateTarotDot(it, 0) }
            dot2?.let { animateTarotDot(it, 250) }
            dot3?.let { animateTarotDot(it, 500) }
        } catch (e: Exception) {
            Log.e("TarotAnimation", "添加点动画异常", e)
        }
    }
    
    // 为单个圆点添加动画
    private fun animateTarotDot(dot: View, startDelay: Long) {
        dot.postDelayed({
            if (isActive && isAdded && _binding != null) {
                dot.animate()
                    .scaleX(1.6f)
                    .scaleY(1.6f)
                    .alpha(0.2f)
                    .setDuration(700)
                    .withEndAction {
                        if (isActive && isAdded && _binding != null) {
                            dot.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(1f)
                                .setDuration(700)
                                .withEndAction {
                                    // 循环动画
                                    if (isActive && isAdded && _binding != null && binding.cardLoading.visibility == View.VISIBLE) {
                                        animateTarotDot(dot, 0)
                                    }
                                }
                                .start()
                        }
                    }
                    .start()
            }
        }, startDelay)
    }
    
    // 添加安全的UI更新方法
    private fun safeSetViewVisibility(view: View?, visibility: Int) {
        if (!isActive || !isAdded || _binding == null) return
        try {
            view?.visibility = visibility
        } catch (e: Exception) {
            Log.e("TarotAnimation", "设置视图可见性异常", e)
        }
    }
    
    private fun safeSetText(view: TextView?, text: CharSequence) {
        if (!isActive || !isAdded || _binding == null) return
        try {
            view?.text = text
        } catch (e: Exception) {
            Log.e("TarotAnimation", "设置文本异常", e)
        }
    }
    
    private fun getSelectedCardNames(): String {
        // 获取已选择的卡片名称
        val selectedCardNames = mutableListOf<String>()
        
        try {
            for (i in selectedCards.indices) {
                val position = selectedCards[i]
                if (position >= 0 && position < cardViews.size) {
                    val cardView = cardViews[position]
                    val cardName = cardView.tag as? String ?: "未知牌"
                    selectedCardNames.add(cardName)
                } else {
                    selectedCardNames.add("未知牌")
                }
            }
        } catch (e: Exception) {
            Log.e("TarotAnimation", "获取卡片名称异常", e)
            // 添加一个默认值避免返回空字符串
            if (selectedCardNames.isEmpty()) {
                selectedCardNames.add("塔罗牌")
            }
        }
        
        return selectedCardNames.joinToString(", ")
    }
    
    private fun showResult(result: DivinationResult) {
        if (!isActive || !isAdded) return
        
        try {
            // 跳转到结果页面
            val resultFragment = DivinationResultFragment.newInstance(result.id)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, resultFragment)
                .addToBackStack(null)
                .commit()
        } catch (e: Exception) {
            Log.e("TarotAnimation", "显示结果异常", e)
            safeShowToast("显示结果出错：${e.message?.take(50) ?: "未知错误"}")
        }
    }
    
    private fun safeShowToast(message: String) {
        if (!isActive || !isAdded) return
        try {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("TarotAnimation", "显示Toast异常", e)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isActive = false
    }
} 