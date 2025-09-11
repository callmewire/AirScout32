package com.airscout.airscout32.utils

import android.content.Context
import android.content.SharedPreferences
import com.airscout.airscout32.data.AirSensorData
import com.google.gson.JsonObject
import com.google.gson.JsonParser

data class JsonKeyMapping(
    val temperatureKey: String = "tmp",
    val humidityKey: String = "hum",
    val co2Key: String = "CO2",
    val vocKey: String = "VOC+CO",
    val batteryKey: String = "Akku"
)

class JsonMappingConfig(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("json_mapping", Context.MODE_PRIVATE)
    
    fun saveMapping(mapping: JsonKeyMapping) {
        prefs.edit().apply {
            putString("temp_key", mapping.temperatureKey)
            putString("humidity_key", mapping.humidityKey)
            putString("co2_key", mapping.co2Key)
            putString("voc_key", mapping.vocKey)
            putString("battery_key", mapping.batteryKey)
            apply()
        }
    }
    
    fun loadMapping(): JsonKeyMapping {
        return JsonKeyMapping(
            temperatureKey = prefs.getString("temp_key", "tmp") ?: "tmp",
            humidityKey = prefs.getString("humidity_key", "hum") ?: "hum",
            co2Key = prefs.getString("co2_key", "CO2") ?: "CO2",
            vocKey = prefs.getString("voc_key", "VOC+CO") ?: "VOC+CO",
            batteryKey = prefs.getString("battery_key", "Akku") ?: "Akku"
        )
    }
    
    fun parseJsonWithMapping(jsonString: String): AirSensorData? {
        return try {
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject
            val mapping = loadMapping()
            
            val temperature = getDoubleValue(jsonObject, mapping.temperatureKey)
            val humidity = getDoubleValue(jsonObject, mapping.humidityKey)
            val co2 = getDoubleValue(jsonObject, mapping.co2Key)
            val voc = getDoubleValue(jsonObject, mapping.vocKey)
            val battery = getDoubleValue(jsonObject, mapping.batteryKey)
            
            AirSensorData(
                temperature = temperature,
                humidity = humidity,
                gas1 = co2,
                gas2 = voc,
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
