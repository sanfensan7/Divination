package com.example.divination.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.divination.ui.theme.IOSColor
import com.example.divination.ui.theme.IOSSpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun TarotAnimationOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onCompleted: (List<TarotRevealedCard>) -> Unit
) {
    if (!visible) return

    val selectedCards = remember { mutableStateListOf<TarotDrawResult>() }
    val revealedCards = remember {
        mutableStateListOf<TarotRevealedCard?>(null, null, null)
    }
    val flippedState = remember { mutableStateListOf(false, false, false) }
    var phase by remember { mutableStateOf(TarotPhase.IDLE) }
    var isShuffling by remember { mutableStateOf(false) }
    var isDealing by remember { mutableStateOf(false) }
    var canFlip by remember { mutableStateOf(false) }
    var completionGuard by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun resetState() {
        completionGuard = false
        selectedCards.clear()
        flippedState.indices.forEach { flippedState[it] = false }
        revealedCards.indices.forEach { revealedCards[it] = null }
        phase = TarotPhase.IDLE
        isShuffling = false
        isDealing = false
        canFlip = false
    }

    LaunchedEffect(visible) {
        if (visible) {
            resetState()
        }
    }

    fun startSequence() {
        if (isShuffling || isDealing || canFlip) return
        isShuffling = true
        phase = TarotPhase.SHUFFLE
        selectedCards.clear()
        flippedState.indices.forEach { flippedState[it] = false }

        scope.launch {
            delay(1500)
            isShuffling = false
            isDealing = true
            phase = TarotPhase.DRAWING
            val drawn = TarotCardLibrary.draw(3)
            selectedCards.addAll(drawn)
            delay(1200)
            isDealing = false
            canFlip = true
            phase = TarotPhase.REVEAL
        }
    }

    fun handleCardFlip(index: Int) {
        if (!canFlip) return
        if (flippedState.getOrNull(index) == true) return
        flippedState[index] = true
        val drawResult = selectedCards.getOrNull(index) ?: return
        revealedCards[index] = TarotCardLibrary.reveal(drawResult)
        if (flippedState.all { it }) {
            canFlip = false
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xEC000000)
    ) {
        Box(Modifier.fillMaxSize()) {
            CosmicBackdrop()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 56.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                OverlayHeader(
                    phase = phase,
                    isShuffling = isShuffling,
                    onStart = { startSequence() }
                )

                TarotCardStage(
                    phase = phase,
                    cards = selectedCards,
                    flippedState = flippedState,
                    canFlip = canFlip,
                    onCardFlip = { handleCardFlip(it) },
                    onStart = { startSequence() }
                )

                OverlayFooter(phase, revealedCards)

                val allRevealed = revealedCards.all { it != null }
                if (allRevealed && phase == TarotPhase.REVEAL) {
                    Spacer(modifier = Modifier.height(12.dp))
                    IOSButton(
                        text = "开始占卜",
                        onClick = {
                            if (!completionGuard) {
                                completionGuard = true
                                onCompleted(revealedCards.filterNotNull())
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color(0x33000000), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭塔罗动画",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun OverlayHeader(
    phase: TarotPhase,
    isShuffling: Boolean,
    onStart: () -> Unit
) {
    val title = when (phase) {
        TarotPhase.IDLE -> "请在心中默念问题"
        TarotPhase.SHUFFLE -> "星光正在洗牌"
        TarotPhase.DRAWING -> "命运之牌浮现"
        TarotPhase.REVEAL -> "天启已经到来"
    }
    val subtitle = when (phase) {
        TarotPhase.IDLE -> "点按牌堆，让宇宙开始应答"
        TarotPhase.SHUFFLE -> "让念头与宇宙呼吸保持一致"
        TarotPhase.DRAWING -> "三张能量最强的牌正向你靠近"
        TarotPhase.REVEAL -> "接住来自星海的讯息"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            color = Color(0xFFF5E8FF),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = Color(0xFFE5D3FF),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        if (phase == TarotPhase.IDLE && !isShuffling) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "点按牌堆开始洗牌",
                color = Color(0xFFEDCCFF),
                fontSize = 14.sp,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onStart() }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun TarotCardStage(
    phase: TarotPhase,
    cards: List<TarotDrawResult>,
    flippedState: List<Boolean>,
    canFlip: Boolean,
    onCardFlip: (Int) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "tarot-card-sway")
    val sway by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    if (phase == TarotPhase.IDLE || (phase == TarotPhase.SHUFFLE && cards.isEmpty())) {
        TarotDeckStack(
            isShuffling = phase == TarotPhase.SHUFFLE,
            onClick = onStart
        )
        return
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val card = cards.getOrNull(index)
            TarotAnimatedCard(
                index = index,
                phase = phase,
                sway = sway,
                card = card,
                flippedState = flippedState,
                canFlip = canFlip,
                onCardFlip = onCardFlip
            )
        }
    }
}

@Composable
private fun TarotDeckStack(
    isShuffling: Boolean,
    onClick: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "deck-breathe")
    val breathe by infinite.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isShuffling) 500 else 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "deck-breathe-value"
    )

    Box(
        modifier = Modifier
            .size(width = 140.dp, height = 200.dp)
            .graphicsLayer {
                scaleX = breathe
                scaleY = breathe
            }
            .clip(CardShape)
            .background(Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        repeat(3) { layer ->
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = (layer * -4).dp, y = (layer * 4).dp)
                    .graphicsLayer { alpha = 0.75f - layer * 0.15f }
            ) {
                CardBack()
            }
        }
        Text(
            text = if (isShuffling) "洗牌中…" else "点按牌堆",
            color = Color(0xFFEEDCFF),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TarotAnimatedCard(
    index: Int,
    phase: TarotPhase,
    sway: Float,
    card: TarotDrawResult?,
    flippedState: List<Boolean>,
    canFlip: Boolean,
    onCardFlip: (Int) -> Unit
) {
    val density = LocalDensity.current
    val baseRotation = -12f + index * 12f
    val targetRotation = when (phase) {
        TarotPhase.SHUFFLE -> baseRotation
        TarotPhase.DRAWING -> baseRotation / 2
        TarotPhase.REVEAL -> 0f
        TarotPhase.IDLE -> baseRotation
    }
    val rotation by animateFloatAsState(
        targetValue = if (phase == TarotPhase.SHUFFLE) targetRotation + sin((sway + index) * PI).toFloat() * 8 else targetRotation,
        animationSpec = tween(600),
        label = "card-rotation-$index"
    )

    val targetOffset = when (phase) {
        TarotPhase.SHUFFLE -> (sin((sway + index) * PI * 2) * 18).dp
        TarotPhase.DRAWING -> (-8).dp
        TarotPhase.REVEAL -> 0.dp
        TarotPhase.IDLE -> 0.dp
    }
    val offset by animateFloatAsState(
        targetValue = targetOffset.value,
        animationSpec = tween(600),
        label = "card-offset-$index"
    )

    val scale by animateFloatAsState(
        targetValue = when (phase) {
            TarotPhase.REVEAL -> 1.05f
            TarotPhase.DRAWING -> 0.98f
            TarotPhase.SHUFFLE -> 0.95f
            TarotPhase.IDLE -> 0.95f
        },
        animationSpec = tween(500),
        label = "card-scale-$index"
    )

    val isFlipped = flippedState.getOrNull(index) == true
    val flipTarget = when {
        phase != TarotPhase.REVEAL -> 180f
        isFlipped -> 0f
        else -> 180f
    }
    val flipRotation by animateFloatAsState(
        targetValue = flipTarget,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "card-flip-$index"
    )

    val cardClickable = phase == TarotPhase.REVEAL && canFlip && card != null && !isFlipped

    Box(
        modifier = Modifier
            .size(width = 110.dp, height = 170.dp)
            .graphicsLayerCompat(
                rotationZ = rotation,
                scaleX = scale,
                scaleY = scale,
                translationY = offset
            )
            .graphicsLayer {
                rotationY = flipRotation
                cameraDistance = 32 * density.density
            }
            .clip(CardShape)
            .background(Color.Transparent)
            .clickable(
                enabled = cardClickable,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCardFlip(index) },
        contentAlignment = Alignment.Center
    ) {
        val showFront = phase == TarotPhase.REVEAL && card != null && isFlipped && flipRotation <= 90f
        if (showFront) {
            CardFront(card!!)
        } else {
            CardBack()
        }
    }
}

@Composable
private fun CardBack() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF36204D), Color(0xFF110C1C))
                )
            )
            .drawBehind {
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(Color(0x33F5E8FF), radius = size.minDimension * 0.45f, center = center)
                drawCircle(Color(0x55C8A6FF), radius = size.minDimension * 0.25f, center = center)
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(48.dp)) {
            drawCircle(color = Color(0x88FFD479))
            drawIntoCanvas {
                val center = Offset(size.width / 2f, size.height / 2f)
                for (i in 0 until 8) {
                    val angle = PI * 2 / 8 * i
                    val radius = size.minDimension / 2.8f
                    val x = center.x + radius * kotlin.math.cos(angle).toFloat()
                    val y = center.y + radius * kotlin.math.sin(angle).toFloat()
                    drawCircle(Color(0xFFFFE9A5), center = Offset(x, y), radius = 4f)
                }
            }
        }
    }
}

@Composable
private fun CardFront(card: TarotDrawResult) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFF1C2), Color(0xFFFDD9FF)),
                    start = androidx.compose.ui.geometry.Offset.Zero,
                    end = androidx.compose.ui.geometry.Offset.Infinite
                )
            )
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = card.position.displayName,
            color = Color(0xFF612534),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = buildString {
                append(card.definition.name)
                if (card.isReversed) append(" · 逆位")
            },
            color = Color(0xFF31152E),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = TarotCardLibrary.meaningFor(card),
            color = Color(0xFF5E3253),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = TarotCardLibrary.positionMeaning(card.position),
            color = IOSColor.TextSecondary,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OverlayFooter(phase: TarotPhase, cards: List<TarotRevealedCard?>) {
    val revealed = cards.filterNotNull()
    AnimatedVisibility(visible = revealed.isNotEmpty() && phase == TarotPhase.REVEAL) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "抽到的牌",
                color = Color(0xFFE7D6FF),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            revealed.forEachIndexed { index, card ->
                Text(
                    text = buildString {
                        append("${index + 1}. ")
                        append(card.position)
                        append(" · ")
                        append(card.name)
                        if (card.isReversed) append("（逆位）")
                    },
                    color = Color(0xFFD9C7FF),
                    fontSize = 14.sp
                )
                Text(
                    text = card.meaning,
                    color = Color(0xFFBDAFE3),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = IOSSpacing.XSmall)
                )
            }
        }
    }
}

@Composable
private fun CosmicBackdrop() {
    val starField = remember { StarField.generate(32) }
    val infinite = rememberInfiniteTransition(label = "cosmic")
    val shimmer by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 4800, easing = LinearEasing)
        ),
        label = "cosmic-move"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF0B0A23), Color(0xFF04010B)),
                center = center,
                radius = size.maxDimension * 0.9f
            )
        )

        val sweep = Brush.sweepGradient(
            colors = listOf(
                Color(0x332B88FF),
                Color.Transparent,
                Color(0x332B88FF)
            )
        )
        drawCircle(brush = sweep, radius = size.maxDimension * 0.65f)

        starField.forEach { star ->
            val blink = 0.4f + 0.6f * kotlin.math.abs(sin((shimmer + star.phaseOffset) * PI)).toFloat()
            drawCircle(
                color = Color.White.copy(alpha = blink),
                radius = star.radius,
                center = androidx.compose.ui.geometry.Offset(
                    x = star.x * size.width,
                    y = star.y * size.height
                )
            )
        }
    }
}

private object StarField {
    data class Star(val x: Float, val y: Float, val radius: Float, val phaseOffset: Float)

    fun generate(count: Int): List<Star> = buildList {
        repeat(count) {
            add(
                Star(
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    radius = Random.nextFloat() * 3f + 1f,
                    phaseOffset = Random.nextFloat()
                )
            )
        }
    }
}

private enum class TarotPhase { IDLE, SHUFFLE, DRAWING, REVEAL }

enum class TarotCardPosition(val displayName: String, val description: String) {
    PAST("过去", "揭示早先经历如何影响当下"),
    PRESENT("现在", "呈现此刻能量与可行动的焦点"),
    FUTURE("未来", "预示下一阶段的发展方向")
}

data class TarotCardDefinition(
    val name: String,
    val uprightMeaning: String,
    val reversedMeaning: String
)

data class TarotDrawResult(
    val definition: TarotCardDefinition,
    val position: TarotCardPosition,
    val isReversed: Boolean
)

data class TarotRevealedCard(
    val name: String,
    val position: String,
    val isReversed: Boolean,
    val meaning: String,
    val positionHint: String
)

private object TarotCardLibrary {
    private val cardDefinitions = listOf(
        TarotCardDefinition(
            "愚者",
            uprightMeaning = "勇于开启旅程，保持纯真与自由。",
            reversedMeaning = "冲动鲁莽、迷失方向或缺乏承诺。"
        ),
        TarotCardDefinition(
            "魔术师",
            uprightMeaning = "掌控资源、付诸行动、聚焦意志。",
            reversedMeaning = "能量分散、操纵或自我怀疑。"
        ),
        TarotCardDefinition(
            "女祭司",
            uprightMeaning = "聆听直觉、洞见潜意识的答案。",
            reversedMeaning = "秘密被压抑、直觉受阻或困惑。"
        ),
        TarotCardDefinition(
            "女皇",
            uprightMeaning = "丰盛滋养、关系和谐与创造力。",
            reversedMeaning = "依赖、懒散或失去创作火花。"
        ),
        TarotCardDefinition(
            "皇帝",
            uprightMeaning = "秩序、责任、策略与决断。",
            reversedMeaning = "僵化、控制欲或权威失衡。"
        ),
        TarotCardDefinition(
            "教皇",
            uprightMeaning = "传统智慧、导师指引与精神价值。",
            reversedMeaning = "质疑规则、价值冲突或虚伪信念。"
        ),
        TarotCardDefinition(
            "恋人",
            uprightMeaning = "真诚连接、共鸣与重要抉择。",
            reversedMeaning = "犹豫不决、失衡的关系或合作。"
        ),
        TarotCardDefinition(
            "战车",
            uprightMeaning = "自律前行、掌控方向、突破阻碍。",
            reversedMeaning = "失控、拖延或意志被削弱。"
        ),
        TarotCardDefinition(
            "力量",
            uprightMeaning = "温柔的勇气、疗愈与自信。",
            reversedMeaning = "自我怀疑、情绪失衡或压抑怒气。"
        ),
        TarotCardDefinition(
            "隐者",
            uprightMeaning = "内省、独处、寻找智慧之光。",
            reversedMeaning = "孤立、逃避现实或迷失目标。"
        ),
        TarotCardDefinition(
            "命运之轮",
            uprightMeaning = "周期转换、机缘与命运之门。",
            reversedMeaning = "停滞、重复旧循环、错失转机。"
        ),
        TarotCardDefinition(
            "正义",
            uprightMeaning = "公平、真相显现与因果平衡。",
            reversedMeaning = "偏颇、不公或需要诚实面对。"
        ),
        TarotCardDefinition(
            "倒吊人",
            uprightMeaning = "暂停、换位思考、以牺牲换突破。",
            reversedMeaning = "抗拒改变、被动停滞或自怜。"
        ),
        TarotCardDefinition(
            "死神",
            uprightMeaning = "必要的告别与重生的门槛。",
            reversedMeaning = "迟迟不放手、害怕转型。"
        ),
        TarotCardDefinition(
            "节制",
            uprightMeaning = "协调、节奏、耐心与疗愈。",
            reversedMeaning = "失衡、过度或缺乏节制。"
        ),
        TarotCardDefinition(
            "恶魔",
            uprightMeaning = "看见执念、诱惑与束缚的根源。",
            reversedMeaning = "摆脱束缚、觉察不健康循环。"
        ),
        TarotCardDefinition(
            "高塔",
            uprightMeaning = "突发剧变、觉醒与结构坍塌。",
            reversedMeaning = "延迟崩溃、抗拒面对真相。"
        ),
        TarotCardDefinition(
            "星星",
            uprightMeaning = "希望、灵感、疗愈与新愿景。",
            reversedMeaning = "信心不足、灵感枯竭或自我怀疑。"
        ),
        TarotCardDefinition(
            "月亮",
            uprightMeaning = "潜意识、梦境、敏感与直觉。",
            reversedMeaning = "迷雾渐散、直觉开始清晰。"
        ),
        TarotCardDefinition(
            "太阳",
            uprightMeaning = "喜悦、成功、生命力与光明。",
            reversedMeaning = "暂时失去热情或过度乐观。"
        ),
        TarotCardDefinition(
            "审判",
            uprightMeaning = "觉醒召唤、宽恕与阶段总结。",
            reversedMeaning = "犹豫、担心评判或迟迟不决定。"
        ),
        TarotCardDefinition(
            "世界",
            uprightMeaning = "圆满、整合、完成与自由。",
            reversedMeaning = "未竟之事、需要补完循环。"
        )
    )

    private val positionOrder = listOf(
        TarotCardPosition.PAST,
        TarotCardPosition.PRESENT,
        TarotCardPosition.FUTURE
    )

    fun draw(count: Int): List<TarotDrawResult> {
        val random = Random(System.currentTimeMillis())
        val available = cardDefinitions.shuffled(random)
        val actual = minOf(count, available.size, positionOrder.size)
        return List(actual) { index ->
            val definition = available[index]
            val position = positionOrder[index]
            val isReversed = random.nextBoolean()
            TarotDrawResult(definition, position, isReversed)
        }
    }

    fun meaningFor(card: TarotDrawResult): String {
        return if (card.isReversed) card.definition.reversedMeaning else card.definition.uprightMeaning
    }

    fun positionMeaning(position: TarotCardPosition): String = position.description

    fun reveal(result: TarotDrawResult): TarotRevealedCard {
        return TarotRevealedCard(
            name = result.definition.name,
            position = result.position.displayName,
            isReversed = result.isReversed,
            meaning = meaningFor(result),
            positionHint = positionMeaning(result.position)
        )
    }
}

private val CardShape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)

private fun Modifier.graphicsLayerCompat(
    rotationZ: Float,
    scaleX: Float,
    scaleY: Float,
    translationY: Float
): Modifier = this.then(
    Modifier.graphicsLayer {
        this.rotationZ = rotationZ
        this.scaleX = scaleX
        this.scaleY = scaleY
        this.translationY = translationY
    }
)
