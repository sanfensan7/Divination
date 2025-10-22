package com.example.divination.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.divination.R
import com.example.divination.model.MBTIAnswer
import com.example.divination.model.MBTIQuestion
import com.example.divination.model.MBTIResult
import com.example.divination.utils.MBTICalculator
import com.example.divination.utils.MBTIQuestionProvider
import com.example.divination.utils.MBTIStorageService
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * MBTI测试界面
 * 提供60道题目的答题流程
 */
class MBTITestFragment : Fragment() {

    private lateinit var tvProgress: TextView
    private lateinit var tvQuestionNumber: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnPrevious: MaterialButton
    private lateinit var btnNext: MaterialButton
    
    private val optionButtons = mutableListOf<MaterialButton>()
    
    private lateinit var questionProvider: MBTIQuestionProvider
    private lateinit var calculator: MBTICalculator
    private lateinit var storageService: MBTIStorageService
    
    private var questions: List<MBTIQuestion> = emptyList()
    private var answers: MutableMap<Int, MBTIAnswer> = mutableMapOf()
    private var currentQuestionIndex = 0
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mbti_test, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        initServices()
        loadQuestions()
        checkUnfinishedTest()
        displayCurrentQuestion()
    }
    
    private fun initViews(view: View) {
        tvProgress = view.findViewById(R.id.tvProgress)
        tvQuestionNumber = view.findViewById(R.id.tvQuestionNumber)
        tvQuestion = view.findViewById(R.id.tvQuestion)
        progressBar = view.findViewById(R.id.progressBar)
        btnPrevious = view.findViewById(R.id.btnPrevious)
        btnNext = view.findViewById(R.id.btnNext)
        
        // 初始化选项按钮
        optionButtons.apply {
            add(view.findViewById(R.id.btnStronglyAgree))
            add(view.findViewById(R.id.btnAgree))
            add(view.findViewById(R.id.btnSlightlyAgree))
            add(view.findViewById(R.id.btnNeutral))
            add(view.findViewById(R.id.btnSlightlyDisagree))
            add(view.findViewById(R.id.btnDisagree))
            add(view.findViewById(R.id.btnStronglyDisagree))
        }
        
        // 设置选项按钮点击事件
        optionButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                onOptionSelected(3 - index) // 转换为 3, 2, 1, 0, -1, -2, -3
            }
        }
        
        btnPrevious.setOnClickListener { previousQuestion() }
        btnNext.setOnClickListener { nextQuestion() }
    }
    
    private fun initServices() {
        questionProvider = MBTIQuestionProvider.getInstance(requireContext())
        calculator = MBTICalculator.getInstance()
        storageService = MBTIStorageService.getInstance(requireContext())
    }
    
    private fun loadQuestions() {
        questions = questionProvider.getAllQuestions()
        if (questions.isEmpty()) {
            Toast.makeText(context, "加载题库失败", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun checkUnfinishedTest() {
        if (storageService.hasUnfinishedTest()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("继续测试")
                .setMessage("检测到未完成的测试，是否继续？")
                .setPositiveButton("继续") { _, _ ->
                    loadProgress()
                }
                .setNegativeButton("重新开始") { _, _ ->
                    storageService.clearProgress()
                }
                .show()
        }
    }
    
    private fun loadProgress() {
        val (questionId, answersJson) = storageService.getProgress()
        answersJson?.let {
            try {
                val answerList = MBTIAnswer.fromJsonString(it)
                answerList.forEach { answer ->
                    answers[answer.questionId] = answer
                }
                currentQuestionIndex = questionId - 1
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun displayCurrentQuestion() {
        if (currentQuestionIndex !in questions.indices) return
        
        val question = questions[currentQuestionIndex]
        val questionNumber = currentQuestionIndex + 1
        
        // 更新UI
        tvProgress.text = "第 $questionNumber / ${questions.size} 题"
        tvQuestionNumber.text = "题目 $questionNumber"
        tvQuestion.text = question.text
        
        // 更新进度条
        val progress = (questionNumber * 100) / questions.size
        progressBar.progress = progress
        
        // 更新导航按钮状态
        btnPrevious.isEnabled = currentQuestionIndex > 0
        btnNext.text = if (currentQuestionIndex == questions.size - 1) {
            "提交"
        } else {
            "下一题"
        }
        
        // 高亮已选择的选项
        highlightSelectedOption()
    }
    
    private fun highlightSelectedOption() {
        val question = questions[currentQuestionIndex]
        val answer = answers[question.id]
        
        optionButtons.forEachIndexed { index, button ->
            val value = 3 - index
            if (answer?.selectedOption == value) {
                button.strokeWidth = 4
                button.strokeColor = android.content.res.ColorStateList.valueOf(
                    resources.getColor(android.R.color.black, null)
                )
            } else {
                button.strokeWidth = 0
            }
        }
    }
    
    private fun onOptionSelected(value: Int) {
        val question = questions[currentQuestionIndex]
        answers[question.id] = MBTIAnswer(question.id, value)
        
        // 保存进度
        saveProgress()
        
        // 显示选中效果
        highlightSelectedOption()
        
        // 自动跳转到下一题（延迟300ms）
        view?.postDelayed({
            if (currentQuestionIndex < questions.size - 1) {
                nextQuestion()
            } else {
                // 最后一题，显示提交提示
                Toast.makeText(context, "已完成所有题目，请点击提交", Toast.LENGTH_SHORT).show()
            }
        }, 300)
    }
    
    private fun previousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--
            displayCurrentQuestion()
        }
    }
    
    private fun nextQuestion() {
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            displayCurrentQuestion()
        } else {
            // 最后一题，提交答案
            submitTest()
        }
    }
    
    private fun submitTest() {
        // 检查是否所有题目都已回答
        val unansweredCount = questions.size - answers.size
        if (unansweredCount > 0) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("提示")
                .setMessage("还有 $unansweredCount 道题未作答，是否继续提交？")
                .setPositiveButton("继续提交") { _, _ ->
                    calculateAndShowResult()
                }
                .setNegativeButton("返回作答", null)
                .show()
        } else {
            calculateAndShowResult()
        }
    }
    
    private fun calculateAndShowResult() {
        try {
            // 计算结果
            val answerList = answers.values.toList()
            val result = calculator.calculateResult(
                answerList,
                questions,
                questionProvider.getVersion()
            )
            
            // 保存结果
            storageService.saveResult(result)
            storageService.clearProgress()
            
            // 跳转到结果页面
            navigateToResult(result)
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "计算结果失败：${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun navigateToResult(result: MBTIResult) {
        val fragment = MBTIResultFragment.newInstance(result)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    private fun saveProgress() {
        val answerList = answers.values.toList()
        val answersJson = MBTIAnswer.toJsonString(answerList)
        storageService.saveProgress(currentQuestionIndex + 1, answersJson)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // 保存当前进度
        if (answers.isNotEmpty() && answers.size < questions.size) {
            saveProgress()
        }
    }
    
    companion object {
        fun newInstance() = MBTITestFragment()
    }
}

