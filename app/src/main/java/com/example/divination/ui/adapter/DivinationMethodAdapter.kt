package com.example.divination.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divination.databinding.ItemDivinationMethodBinding
import com.example.divination.model.DivinationMethod
import com.example.divination.util.IconHelper

/**
 * 算命方法适配器
 */
class DivinationMethodAdapter(
    private val onItemClick: (DivinationMethod) -> Unit
) : ListAdapter<DivinationMethod, DivinationMethodAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDivinationMethodBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val method = getItem(position)
        holder.bind(method)
    }
    
    inner class ViewHolder(
        private val binding: ItemDivinationMethodBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }
        
        fun bind(method: DivinationMethod) {
            binding.tvMethodName.text = method.name
            binding.tvMethodDescription.text = method.description
            
            // 使用IconHelper设置图标和颜色
            IconHelper.setMethodIcon(
                method.name, 
                binding.ivMethodIcon,
                binding.iconBackgroundOuter,
                binding.iconBackgroundInner,
                binding.root.context
            )
        }
    }
    
    private class DiffCallback : DiffUtil.ItemCallback<DivinationMethod>() {
        override fun areItemsTheSame(oldItem: DivinationMethod, newItem: DivinationMethod): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: DivinationMethod, newItem: DivinationMethod): Boolean {
            return oldItem == newItem
        }
    }
} 