package com.airscout.airscout32.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MeasurementDao {
    @Insert
    suspend fun insert(measurement: Measurement)

    @Query("SELECT * FROM Measurement ORDER BY timestamp DESC")
    suspend fun getAll(): List<Measurement>
}
