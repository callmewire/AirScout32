package com.airscout.airscout32

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.airscout.airscout32.databinding.ActivityMainBinding
import com.airscout.airscout32.fragments.HistoricalFragment
import com.airscout.airscout32.fragments.RealtimeFragment
import com.airscout.airscout32.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupBottomNavigation()
        
        // Load initial fragment
        if (savedInstanceState == null) {
            loadFragment(RealtimeFragment())
        }
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_realtime -> {
                    loadFragment(RealtimeFragment())
                    true
                }
                R.id.nav_historical -> {
                    loadFragment(HistoricalFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}
