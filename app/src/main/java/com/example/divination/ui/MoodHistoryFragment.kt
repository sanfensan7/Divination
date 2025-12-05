package com.example.divination.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.divination.databinding.FragmentMoodHistoryTabsBinding
import com.example.divination.ui.adapter.UserProfilePagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

/**
 * 用户信息和画像Tab容器Fragment
 */
class MoodHistoryFragment : Fragment() {
    
    private var _binding: FragmentMoodHistoryTabsBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodHistoryTabsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewPager()
    }
    
    /**
     * 设置ViewPager和TabLayout
     */
    private fun setupViewPager() {
        val adapter = UserProfilePagerAdapter(requireActivity())
        binding.viewPager.adapter = adapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
