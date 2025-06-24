package com.airscout.airscout32.utils

import android.content.Context
import android.content.SharedPreferences
import com.airscout.airscout32.data.AirSensorData
import com.google.gson.JsonObject
import com.google.gson.JsonParser

data class JsonKeyMapping(
    val temperatureKey: String = "tmp",
    val humidityKey: String = "hum",
    val gas1Key: String = "gas1",
    val gas2Key: String = "gas2",
    val batteryKey: String = "akku"
)

class JsonMappingConfig(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("json_mapping", Context.MODE_PRIVATE)
    
    fun saveMapping(mapping: JsonKeyMapping) {
        prefs.edit().apply {
            putString("temp_key", mapping.temperatureKey)
            putString("humidity_key", mapping.humidityKey)
            putString("gas1_key", mapping.gas1Key)
            putString("gas2_key", mapping.gas2Key)
            putString("battery_key", mapping.batteryKey)
            apply()
        }
    }
    
    fun loadMapping(): JsonKeyMapping {
        return JsonKeyMapping(
            temperatureKey = prefs.getString("temp_key", "tmp") ?: "tmp",
            humidityKey = prefs.getString("humidity_key", "hum") ?: "hum",
            gas1Key = prefs.getString("gas1_key", "gas1") ?: "gas1",
            gas2Key = prefs.getString("gas2_key", "gas2") ?: "gas2",
            batteryKey = prefs.getString("battery_key", "akku") ?: "akku"
        )
    }
    
    fun parseJsonWithMapping(jsonString: String): AirSensorData? {
        return try {
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject
            val mapping = loadMapping()
            
            val temperature = getDoubleValue(jsonObject, mapping.temperatureKey)
            val humidity = getDoubleValue(jsonObject, mapping.humidityKey)
            val gas1 = getDoubleValue(jsonObject, mapping.gas1Key)
            val gas2 = getDoubleValue(jsonObject, mapping.gas2Key)
            val battery = getDoubleValue(jsonObject, mapping.batteryKey)
            
            AirSensorData(
                temperature = temperature,
                humidity = humidity,
                gas1 = gas1,
                gas2 = gas2,
                battery = battery
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getDoubleValue(jsonObject: JsonObject, key: String): Double {
        return when {
            jsonObject.has(key) && jsonObject.get(key).isJsonPrimitive -> {
                jsonObject.get(key).asDouble
            }
            else -> 0.0
        }
    }
}
