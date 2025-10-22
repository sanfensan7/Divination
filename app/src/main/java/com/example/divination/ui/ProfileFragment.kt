package com.example.divination.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.divination.R
import com.example.divination.databinding.FragmentProfileBinding
import com.example.divination.model.DivinationResult
import com.example.divination.model.MBTIResult
import com.example.divination.ui.adapter.ResultHistoryAdapter
import com.example.divination.utils.DivinationMethodProvider
import com.example.divination.utils.LocalStorageService
import com.example.divination.utils.MBTIStorageService
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding is null, Fragment可能已被销毁")
    
    private lateinit var resultAdapter: ResultHistoryAdapter
    private lateinit var mbtiStorageService: MBTIStorageService
    private var isActive = true // 跟踪Fragment是否处于活跃状态

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        mbtiStorageService = MBTIStorageService.getInstance(requireContext())
        setupRecyclerView()
        loadResults()
        displayMBTIInfo()
    }
    
    override fun onResume() {
        super.onResume()
        // 每次返回到此页面时更新结果列表
        loadResults()
    }
    
    private fun setupRecyclerView() {
        resultAdapter = ResultHistoryAdapter(
            onItemClick = { result ->
                // 处理结果点击
                navigateToResultDetail(result)
            },
            onDeleteClick = { result ->
                // 处理删除点击
                showDeleteConfirmDialog(result)
            }
        )
        
        binding.rvResultHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resultAdapter
        }
    }
    
    private fun loadResults() {
        try {
            val results = LocalStorageService.getAllResults(requireContext())
            if (results.isEmpty()) {
                safeShowEmptyState(true)
            } else {
                safeShowEmptyState(false)
                resultAdapter.submitList(results)
            }
        } catch (e: Exception) {
            safeShowEmptyState(true)
            safeShowError("加载结果失败：${e.message}")
        }
    }
    
    /**
     * 显示MBTI测试信息
     */
    private fun displayMBTIInfo() {
        try {
            val latestResult = mbtiStorageService.getLatestResult()
            val testCount = mbtiStorageService.getResultCount()
            
            // 这里可以在UI中显示MBTI相关信息
            // 例如：最近的人格类型、测试次数等
            // 如果你的布局中有专门的MBTI信息区域，可以在这里更新
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun safeShowEmptyState(isEmpty: Boolean) {
        if (!isActive || !isAdded || _binding == null) return
        try {
            binding.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvResultHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
        } catch (e: Exception) {
            // 忽略异常
        }
    }
    
    private fun safeShowError(message: String) {
        if (!isActive || !isAdded) return
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // 忽略异常
        }
    }
    
    private fun navigateToResultDetail(result: DivinationResult) {
        if (!isActive || !isAdded) return
        try {
            val resultFragment = DivinationResultFragment.newInstance(result.id)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, resultFragment)
                .addToBackStack(null)
                .commit()
        } catch (e: Exception) {
            safeShowError("无法显示结果详情：${e.message}")
        }
    }
    
    private fun showClearHistoryConfirmation() {
        if (!isActive || !isAdded) return
        try {
            AlertDialog.Builder(requireContext())
                .setTitle("清除历史记录")
                .setMessage("确定要清除所有历史记录吗？此操作不可撤销。")
                .setPositiveButton("确定") { _, _ ->
                    clearHistory()
                }
                .setNegativeButton("取消", null)
                .show()
        } catch (e: Exception) {
            safeShowError("无法显示确认对话框：${e.message}")
        }
    }
    
    private fun clearHistory() {
        try {
            val allResults = LocalStorageService.getAllResults(requireContext())
            for (result in allResults) {
                LocalStorageService.deleteResult(requireContext(), result.id)
            }
            loadResults() // 重新加载结果（应该显示空状态）
            safeShowError("历史记录已清除")
        } catch (e: Exception) {
            safeShowError("清除历史记录失败：${e.message}")
        }
    }
    
    private fun showDeleteConfirmDialog(result: DivinationResult) {
        // 查找算命方法名称
        val methodName = DivinationMethodProvider.getMethodById(result.methodId)?.name ?: "未知算命"
        
        AlertDialog.Builder(requireContext())
            .setTitle("删除历史记录")
            .setMessage("确定要删除这条${methodName}的历史记录吗？")
            .setPositiveButton("删除") { _, _ ->
                deleteResult(result)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun deleteResult(result: DivinationResult) {
        // 从本地存储中删除结果
        val success = LocalStorageService.deleteResult(requireContext(), result.id)
        
        if (success) {
            // 重新加载历史记录列表
            loadResults()
            Toast.makeText(requireContext(), "历史记录已删除", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "删除失败，请重试", Toast.LENGTH_SHORT).show()
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