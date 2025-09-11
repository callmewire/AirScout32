package com.airscout.airscout32.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.airscout.airscout32.data.AirSensorData

@Dao
interface AirDataDao {
    
    @Query("SELECT * FROM air_sensor_data ORDER BY timestamp DESC")
    fun getAllData(): LiveData<List<AirSensorData>>
    
    @Query("SELECT * FROM air_sensor_data ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentData(limit: Int): List<AirSensorData>
    
    @Query("SELECT * FROM air_sensor_data WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    suspend fun getDataInRange(startTime: Long, endTime: Long): List<AirSensorData>
    
    @Insert
    suspend fun insertData(data: AirSensorData)
    
    @Delete
    suspend fun deleteData(data: AirSensorData)
    
    @Query("DELETE FROM air_sensor_data")
    suspend fun deleteAllData()
}
