package com.example.divination.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * 用户信息页面适配器
 */
class UserProfilePagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    
    private val fragments = listOf<Pair<String, () -> Fragment>>(
        "个人信息" to { com.example.divination.ui.UserProfileEditFragment() },
        "用户画像" to { com.example.divination.ui.UserPortraitFragment() }
    )
    
    override fun getItemCount(): Int = fragments.size
    
    override fun createFragment(position: Int): Fragment {
        return fragments[position].second()
    }
    
    fun getPageTitle(position: Int): String {
        return fragments[position].first
    }
}
