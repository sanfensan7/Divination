package com.example.divination.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.divination.R
import com.example.divination.model.MBTIResult
import com.example.divination.model.PersonalityType
import com.example.divination.utils.MBTIStorageService
import com.google.android.material.button.MaterialButton

/**
 * MBTI测试结果展示界面
 */
class MBTIResultFragment : Fragment() {

    private lateinit var tvPersonalityType: TextView
    private lateinit var tvPersonalityName: TextView
    private lateinit var tvPersonalityRole: TextView
    private lateinit var tvTestDate: TextView
    
    private lateinit var tvEILabel: TextView
    private lateinit var tvSNLabel: TextView
    private lateinit var tvTFLabel: TextView
    private lateinit var tvJPLabel: TextView
    
    private lateinit var pbEI: ProgressBar
    private lateinit var pbSN: ProgressBar
    private lateinit var pbTF: ProgressBar
    private lateinit var pbJP: ProgressBar
    
    private lateinit var tvDescription: TextView
    private lateinit var tvStrengths: TextView
    private lateinit var tvWeaknesses: TextView
    private lateinit var tvCareers: TextView
    private lateinit var tvRelationships: TextView
    private lateinit var tvGrowth: TextView
    
    private lateinit var btnRetake: MaterialButton
    private lateinit var btnFinish: MaterialButton
    
    private lateinit var result: MBTIResult
    private lateinit var storageService: MBTIStorageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        result = arguments?.getSerializable(ARG_RESULT) as? MBTIResult
            ?: throw IllegalArgumentException("Result is required")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mbti_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        storageService = MBTIStorageService.getInstance(requireContext())
        displayResult()
    }

    private fun initViews(view: View) {
        tvPersonalityType = view.findViewById(R.id.tvPersonalityType)
        tvPersonalityName = view.findViewById(R.id.tvPersonalityName)
        tvPersonalityRole = view.findViewById(R.id.tvPersonalityRole)
        tvTestDate = view.findViewById(R.id.tvTestDate)
        
        tvEILabel = view.findViewById(R.id.tvEILabel)
        tvSNLabel = view.findViewById(R.id.tvSNLabel)
        tvTFLabel = view.findViewById(R.id.tvTFLabel)
        tvJPLabel = view.findViewById(R.id.tvJPLabel)
        
        pbEI = view.findViewById(R.id.pbEI)
        pbSN = view.findViewById(R.id.pbSN)
        pbTF = view.findViewById(R.id.pbTF)
        pbJP = view.findViewById(R.id.pbJP)
        
        tvDescription = view.findViewById(R.id.tvDescription)
        tvStrengths = view.findViewById(R.id.tvStrengths)
        tvWeaknesses = view.findViewById(R.id.tvWeaknesses)
        tvCareers = view.findViewById(R.id.tvCareers)
        tvRelationships = view.findViewById(R.id.tvRelationships)
        tvGrowth = view.findViewById(R.id.tvGrowth)
        
        btnRetake = view.findViewById(R.id.btnRetake)
        btnFinish = view.findViewById(R.id.btnFinish)
        
        btnRetake.setOnClickListener { retakeTest() }
        btnFinish.setOnClickListener { finish() }
    }

    private fun displayResult() {
        try {
            // 获取人格类型详情
            val personalityType = PersonalityType.getByCode(result.personalityType)
            
            // 显示人格类型信息
            tvPersonalityType.text = result.personalityType
            tvPersonalityName.text = personalityType.name
            tvPersonalityRole.text = personalityType.role.displayName
            tvTestDate.text = "测试日期：${storageService.formatDate(result.testDate)}"
            
            // 显示维度得分
            tvEILabel.text = result.getEILabel()
            tvSNLabel.text = result.getSNLabel()
            tvTFLabel.text = result.getTFLabel()
            tvJPLabel.text = result.getJPLabel()
            
            pbEI.progress = result.getEIPercentage()
            pbSN.progress = result.getSNPercentage()
            pbTF.progress = result.getTFPercentage()
            pbJP.progress = result.getJPPercentage()
            
            // 显示详细描述
            tvDescription.text = personalityType.description
            
            // 显示优势
            tvStrengths.text = personalityType.strengths.joinToString("\n") { "• $it" }
            
            // 显示劣势
            tvWeaknesses.text = personalityType.weaknesses.joinToString("\n") { "• $it" }
            
            // 显示职业建议
            tvCareers.text = personalityType.careers.joinToString("、")
            
            // 显示人际关系
            tvRelationships.text = personalityType.relationships
            
            // 显示成长建议
            tvGrowth.text = personalityType.growth
            
        } catch (e: Exception) {
            e.printStackTrace()
            tvDescription.text = "加载人格类型信息失败：${e.message}"
        }
    }

    private fun retakeTest() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, MBTITestFragment.newInstance())
            .commit()
    }

    private fun finish() {
        // 返回到主界面
        parentFragmentManager.popBackStack()
    }

    companion object {
        private const val ARG_RESULT = "result"
        
        fun newInstance(result: MBTIResult): MBTIResultFragment {
            return MBTIResultFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_RESULT, result)
                }
            }
        }
    }
}

