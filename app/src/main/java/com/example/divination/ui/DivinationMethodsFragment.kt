package com.example.divination.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.divination.R
import com.example.divination.databinding.FragmentDivinationMethodsBinding
import com.example.divination.model.DivinationMethod
import com.example.divination.ui.adapter.DivinationMethodAdapter
import com.example.divination.utils.DivinationMethodProvider

class DivinationMethodsFragment : Fragment() {

    private var _binding: FragmentDivinationMethodsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var methodAdapter: DivinationMethodAdapter
    private var currentMethodType = 0 // 0: 全部, 1: 中国传统, 2: 西方传统, 3: 心理测评

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDivinationMethodsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupTabLayout()
    }
    
    private fun setupRecyclerView() {
        methodAdapter = DivinationMethodAdapter { method ->
            // 处理算命方法点击
            // MBTI测试特殊处理，直接进入测试界面
            val fragment = if (method.id == "mbti") {
                MBTITestFragment.newInstance()
            } else {
                DivinationDetailFragment.newInstance(method.id)
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.rvDivinationMethods.adapter = methodAdapter
        loadMethods(0) // 默认加载全部算命方法
    }
    
    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadMethods(0) // 全部
                    1 -> loadMethods(1) // 中国传统
                    2 -> loadMethods(2) // 西方传统
                    3 -> loadMethods(3) // 心理测评
                }
            }
            
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }
    
    private fun loadMethods(type: Int) {
        currentMethodType = type
        val methods = when (type) {
            1 -> DivinationMethodProvider.getChineseMethods()
            2 -> DivinationMethodProvider.getWesternMethods()
            3 -> DivinationMethodProvider.getPsychologicalMethods()
            else -> DivinationMethodProvider.getAllMethods()
        }
        methodAdapter.submitList(methods)
    }
    
    // 供外部调用，直接选择中国传统算命方法
    fun selectChineseMethods() {
        binding.tabLayout.getTabAt(1)?.select()
    }
    
    // 供外部调用，直接选择西方传统算命方法
    fun selectWesternMethods() {
        binding.tabLayout.getTabAt(2)?.select()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 