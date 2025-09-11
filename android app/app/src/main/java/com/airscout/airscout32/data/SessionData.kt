package com.airscout.airscout32.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "session_data")
data class SessionData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val sessionName: String,
    val startTime: Long,
    val endTime: Long,
    val dataCount: Int,
    val sessionDataJson: String
) {
    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(startTime))
    }
    
    fun getDurationMinutes(): Long {
        return (endTime - startTime) / (1000 * 60)
    }
}