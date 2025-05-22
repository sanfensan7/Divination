package com.example.divination.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divination.databinding.ItemResultHistoryBinding
import com.example.divination.model.DivinationResult
import com.example.divination.utils.DivinationMethodProvider
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 历史结果适配器
 */
class ResultHistoryAdapter(
    private val onItemClick: (DivinationResult) -> Unit,
    private val onDeleteClick: (DivinationResult) -> Unit
) : ListAdapter<DivinationResult, ResultHistoryAdapter.ViewHolder>(DiffCallback()) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResultHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = getItem(position)
        holder.bind(result)
    }
    
    inner class ViewHolder(
        private val binding: ItemResultHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            
            binding.btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }
        
        fun bind(result: DivinationResult) {
            // 获取算命方法
            val method = DivinationMethodProvider.getMethodById(result.methodId)
            
            binding.tvMethodName.text = method?.name ?: "未知算命方法"
            binding.tvDate.text = dateFormat.format(result.createTime)
            
            // 设置第一个结果部分作为摘要
            val summary = result.resultSections.firstOrNull()?.content ?: ""
            binding.tvSummary.text = summary.take(100) + if (summary.length > 100) "..." else ""
        }
    }
    
    private class DiffCallback : DiffUtil.ItemCallback<DivinationResult>() {
        override fun areItemsTheSame(oldItem: DivinationResult, newItem: DivinationResult): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: DivinationResult, newItem: DivinationResult): Boolean {
            return oldItem == newItem
        }
    }
} 