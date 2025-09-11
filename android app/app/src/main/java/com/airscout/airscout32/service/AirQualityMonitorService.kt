package com.airscout.airscout32.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.airscout.airscout32.MainActivity
import com.airscout.airscout32.R
import com.airscout.airscout32.data.AppDatabase
import com.airscout.airscout32.data.Measurement
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AirQualityMonitorService : Service() {

    companion object {
        const val ACTION_DATA = "com.airscout.airscout32.ACTION_DATA"
        const val EXTRA_JSON = "json"
        private const val CHANNEL_ID = "AirScout32Channel"
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
        db = AppDatabase.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        // Starte hier deine Bluetooth-Logik im Hintergrund-Thread
        // und sende Daten mit sendDataToActivity(jsonString)
        return START_STICKY
    }

    private fun sendDataToActivity(json: String) {
        val intent = Intent(ACTION_DATA)
        intent.putExtra(EXTRA_JSON, json)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "AirScout32", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AirScout32 läuft")
            .setContentText("Luftqualität wird überwacht")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AirScout32::Wakelock")
        wakeLock?.acquire()
    }

    private fun handleNewJson(json: String) {
        // Parse JSON und speichern
        val measurement = parseMeasurement(json)
        if (measurement != null) {
            CoroutineScope(Dispatchers.IO).launch {
                db.measurementDao().insert(measurement)
            }
        }
        sendDataToActivity(json)
    }

    private fun parseMeasurement(json: String): Measurement? {
        return try {
            Gson().fromJson(json, Measurement::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroy() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
