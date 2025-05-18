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
import com.example.divination.ui.adapter.ResultHistoryAdapter
import com.example.divination.utils.DivinationMethodProvider
import com.example.divination.utils.LocalStorageService
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var resultAdapter: ResultHistoryAdapter

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
        
        setupRecyclerView()
        loadHistoryResults()
    }
    
    override fun onResume() {
        super.onResume()
        // 每次返回到此页面时更新结果列表
        loadHistoryResults()
    }
    
    private fun setupRecyclerView() {
        resultAdapter = ResultHistoryAdapter(
            onItemClick = { result ->
                // 处理结果点击
                val resultFragment = DivinationResultFragment.newInstance(result.id)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, resultFragment)
                    .addToBackStack(null)
                    .commit()
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
            loadHistoryResults()
            Toast.makeText(requireContext(), "历史记录已删除", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "删除失败，请重试", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadHistoryResults() {
        // 从本地存储加载所有历史结果
        val results = LocalStorageService.getAllResults(requireContext())
        
        // 按时间倒序排序
        val sortedResults = results.sortedByDescending { it.createTime }
        
        // 更新适配器
        resultAdapter.submitList(sortedResults)
        
        // 更新UI显示
        updateEmptyView(sortedResults.isEmpty())
        updateResultCount(sortedResults.size)
    }
    
    private fun updateEmptyView(isEmpty: Boolean) {
        binding.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvResultHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun updateResultCount(count: Int) {
        binding.tvResultCount.text = getString(R.string.history_count, count)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 