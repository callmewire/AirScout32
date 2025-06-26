package com.airscout.airscout32.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.airscout.airscout32.data.SessionData

@Dao
interface SessionDao {
    
    @Query("SELECT * FROM session_data ORDER BY startTime DESC")
    fun getAllSessions(): LiveData<List<SessionData>>
    
    @Insert
    suspend fun insertSession(session: SessionData)
    
    @Delete
    suspend fun deleteSession(session: SessionData)
    
    @Query("DELETE FROM session_data")
    suspend fun deleteAllSessions()
}