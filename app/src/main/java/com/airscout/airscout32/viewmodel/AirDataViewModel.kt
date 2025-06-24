package com.airscout.airscout32.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.airscout.airscout32.bluetooth.BluetoothService
import com.airscout.airscout32.data.AirSensorData
import com.airscout.airscout32.data.SessionData
import com.airscout.airscout32.database.AppDatabase
import com.airscout.airscout32.utils.ChartSettingsConfig
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AirDataViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val bluetoothService = BluetoothService(application)
    private val gson = Gson()
    private val chartSettingsConfig = ChartSettingsConfig(application)
    
    private val _realtimeData = MutableLiveData<List<AirSensorData>>()
    val realtimeData: LiveData<List<AirSensorData>> = _realtimeData
    
    private val _connectionState = MutableLiveData<Boolean>()
    val connectionState: LiveData<Boolean> = _connectionState
    
    val historicalData = database.airDataDao().getAllData()
    val savedSessions = database.sessionDao().getAllSessions()
    
    private val realtimeList = mutableListOf<AirSensorData>()
    private var chartDataLimit = 100
    
    init {
        observeBluetoothData()
        observeConnectionState()
        chartDataLimit = chartSettingsConfig.getChartDataLimit()
    }
    
    private fun observeBluetoothData() {
        viewModelScope.launch {
            bluetoothService.dataFlow.collect { data ->
                // Save to database
                database.airDataDao().insertData(data)
                
                // Add to realtime list with limit
                realtimeList.add(data)
                while (realtimeList.size > chartDataLimit) {
                    realtimeList.removeAt(0)
                }
                
                _realtimeData.postValue(realtimeList.toList())
            }
        }
    }
    
    private fun observeConnectionState() {
        viewModelScope.launch {
            bluetoothService.connectionStateFlow.collect { isConnected ->
                _connectionState.postValue(isConnected)
            }
        }
    }
    
    fun getPairedDevices(): Set<BluetoothDevice>? {
        return bluetoothService.getPairedDevices()
    }
    
    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            try {
                val success = bluetoothService.connectToDevice(device)
                Log.d("AirDataViewModel", "Connection result: $success")
            } catch (e: Exception) {
                Log.e("AirDataViewModel", "Connection error: ${e.message}", e)
            }
        }
    }
    
    fun disconnect() {
        bluetoothService.disconnect()
    }
    
    fun isConnected(): Boolean {
        return bluetoothService.isConnected()
    }
    
    fun deleteAllData() {
        viewModelScope.launch {
            database.airDataDao().deleteAllData()
            realtimeList.clear()
            _realtimeData.postValue(emptyList())
        }
    }
    
    fun resetCurrentValues() {
        realtimeList.clear()
        _realtimeData.postValue(emptyList())
    }
    
    fun getStoredDataHistory() = bluetoothService.getDataHistory()
    
    fun clearStoredHistory() = bluetoothService.clearHistory()
    
    fun exportStoredDataToCsv() = bluetoothService.exportToCSV()
    
    fun exportToCsv(): File? {
        return try {
            val data = historicalData.value ?: return null
            if (data.isEmpty()) return null
            
            val file = File(getApplication<Application>().getExternalFilesDir(null), "air_sensor_data.csv")
            val writer = FileWriter(file)
            
            writer.append(AirSensorData.csvHeader())
            writer.append("\n")
            
            data.forEach { item ->
                writer.append(item.toCsvRow())
                writer.append("\n")
            }
            
            writer.close()
            file
        } catch (e: Exception) {
            null
        }
    }
    
    fun updateJsonMapping() {
        // The BluetoothService will use the updated mapping automatically
        // on the next received data since it loads the config each time
        Log.d("AirDataViewModel", "JSON mapping configuration updated")
    }
    
    fun saveCurrentSession(sessionName: String) {
        viewModelScope.launch {
            val currentData = realtimeList.toList()
            if (currentData.isNotEmpty()) {
                val startTime = currentData.first().timestamp
                val endTime = currentData.last().timestamp
                val dataJson = gson.toJson(currentData)
                
                val session = SessionData(
                    sessionName = sessionName,
                    startTime = startTime,
                    endTime = endTime,
                    dataCount = currentData.size,
                    sessionDataJson = dataJson
                )
                
                database.sessionDao().insertSession(session)
                clearCurrentSession()
            }
        }
    }
    
    fun clearCurrentSession() {
        realtimeList.clear()
        _realtimeData.postValue(emptyList())
    }
    
    fun exportSessionToCsv(session: SessionData): File? {
        return try {
            val data = gson.fromJson(session.sessionDataJson, Array<AirSensorData>::class.java).toList()
            val fileName = "session_${session.sessionName.replace(" ", "_")}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(session.startTime))}.csv"
            val file = File(getApplication<Application>().getExternalFilesDir(null), fileName)
            val writer = FileWriter(file)
            
            writer.append("Session: ${session.sessionName}\n")
            writer.append("Start: ${SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date(session.startTime))}\n")
            writer.append("End: ${SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date(session.endTime))}\n")
            writer.append("Duration: ${session.getDurationMinutes()} minutes\n")
            writer.append("Data Points: ${session.dataCount}\n\n")
            
            writer.append("Timestamp,Date,Time,Temperature,Humidity,Gas1,Gas2,Battery\n")
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            
            data.forEach { sensorData ->
                val date = Date(sensorData.timestamp)
                writer.append("${sensorData.timestamp},")
                writer.append("${dateFormat.format(date)},")
                writer.append("${timeFormat.format(date)},")
                writer.append("${sensorData.temperature},")
                writer.append("${sensorData.humidity},")
                writer.append("${sensorData.gas1},")
                writer.append("${sensorData.gas2},")
                writer.append("${sensorData.battery}\n")
            }
            
            writer.close()
            file
        } catch (e: Exception) {
            Log.e("AirDataViewModel", "CSV export error: ${e.message}", e)
            null
        }
    }
    
    fun deleteSession(session: SessionData) {
        viewModelScope.launch {
            database.sessionDao().deleteSession(session)
        }
    }
    
    fun clearAllSessions() {
        viewModelScope.launch {
            database.sessionDao().deleteAllSessions()
        }
    }
    
    fun updateChartDataLimit(limit: Int) {
        chartDataLimit = limit
        
        // Apply new limit to existing data
        while (realtimeList.size > chartDataLimit) {
            realtimeList.removeAt(0)
        }
        
        _realtimeData.postValue(realtimeList.toList())
        Log.d("AirDataViewModel", "Chart data limit updated to: $limit")
    }
    
    fun getChartDataLimit(): Int {
        return chartDataLimit
    }
}
