package com.airscout.airscout32.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.airscout.airscout32.data.AirSensorData

@Database(
    entities = [AirSensorData::class],
    version = 2, // Version von 1 auf 2 erhöht
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun airDataDao(): AirDataDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "air_sensor_database"
                )
                .fallbackToDestructiveMigration() // Löscht alte DB bei Schema-Änderungen
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
