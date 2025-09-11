package com.airscout.airscout32.utils

import android.content.Context
import android.content.SharedPreferences

class ChartSettingsConfig(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("chart_settings", Context.MODE_PRIVATE)
    
    fun saveChartDataLimit(limit: Int) {
        prefs.edit().apply {
            putInt("chart_data_limit", limit)
            apply()
        }
    }
    
    fun getChartDataLimit(): Int {
        return prefs.getInt("chart_data_limit", 100) // Default: 100 data points
    }
}