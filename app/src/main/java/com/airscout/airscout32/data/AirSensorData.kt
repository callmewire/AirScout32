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

    val pm25: Double,

    val pm10: Double,

    val co2: Double
) {
    fun toCsvRow(): String {
        val date = Date(timestamp)
        return "${date},$temperature,$humidity,$pm25,$pm10,$co2"
    }

    companion object {
        fun csvHeader(): String {
            return "Timestamp,Temperature,Humidity,PM2.5,PM10,CO2"
        }
    }
}