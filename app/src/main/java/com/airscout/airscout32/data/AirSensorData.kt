package com.airscout.airscout32.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(tableName = "air_sensor_data")
data class AirSensorData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val timestamp: Long = System.currentTimeMillis(),
    
    @SerializedName("tmp")
    val temperature: Double,
    
    @SerializedName("hum")
    val humidity: Double,
    
    @SerializedName("gas1")
    val gas1: Double,
    
    @SerializedName("gas2")
    val gas2: Double,
    
    @SerializedName("akku")
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

