package com.example.divination

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.divination.databinding.ActivityMainBinding
import com.example.divination.ui.DivinationMethodsFragment
import com.example.divination.ui.HomeFragment
import com.example.divination.ui.ProfileFragment
import com.example.divination.ui.SettingsFragment

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupBottomNavigation()
        
        // 默认显示首页
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_divination -> loadFragment(DivinationMethodsFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
                R.id.nav_settings -> loadFragment(SettingsFragment())
                else -> false
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }
} 