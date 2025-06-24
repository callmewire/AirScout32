package com.airscout.airscout32.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "air_sensor_data")
data class AirSensorData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val timestamp: Long = System.currentTimeMillis(),
    
    val temperature: Double,
    val humidity: Double,
    val gas1: Double,
    val gas2: Double,
    val battery: Double
) {
    fun toCsvRow(): String {
        val date = Date(timestamp)
        return "${date},$temperature,$humidity,$gas1,$gas2,$battery"
    }
    
    companion object {
        fun csvHeader(): String {
            return "Timestamp,Temperature,Humidity,Gas1,Gas2,Battery"
        }
    }
}