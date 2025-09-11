package com.airscout.airscout32

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.airscout.airscout32.databinding.ActivityMainBinding
import com.airscout.airscout32.fragments.HistoricalFragment
import com.airscout.airscout32.fragments.RealtimeFragment
import com.airscout.airscout32.fragments.SettingsFragment
import com.airscout.airscout32.service.AirQualityMonitorService

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    // BroadcastReceiver für Daten vom Service
    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AirQualityMonitorService.ACTION_DATA) {
                val json = intent.getStringExtra(AirQualityMonitorService.EXTRA_JSON)
                // An das aktuelle Fragment weiterleiten, falls benötigt
                val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                if (fragment is RealtimeFragment && json != null) {
                    fragment.onNewData(json)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupBottomNavigation()
        
        // Service starten
        val serviceIntent = Intent(this, AirQualityMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        
        // Load initial fragment
        if (savedInstanceState == null) {
            loadFragment(RealtimeFragment())
        }
    }
    
    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            dataReceiver, IntentFilter(AirQualityMonitorService.ACTION_DATA)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataReceiver)
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
