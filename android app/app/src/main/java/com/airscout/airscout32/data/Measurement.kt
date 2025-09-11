package com.airscout.airscout32.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Measurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val co2: Int,
    val voc: Int,
    val co: Int,
    val battery: Int
)
