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
import com.airscout.airscout32.database.AppDatabase
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

class AirDataViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val bluetoothService = BluetoothService(application)
    
    private val _realtimeData = MutableLiveData<List<AirSensorData>>()
    val realtimeData: LiveData<List<AirSensorData>> = _realtimeData
    
    private val _connectionState = MutableLiveData<Boolean>()
    val connectionState: LiveData<Boolean> = _connectionState
    
    val historicalData = database.airDataDao().getAllData()
    
    private val realtimeList = mutableListOf<AirSensorData>()
    
    init {
        observeBluetoothData()
        observeConnectionState()
    }
    
    private fun observeBluetoothData() {
        viewModelScope.launch {
            bluetoothService.dataFlow.collect { data ->
                // Save to database
                database.airDataDao().insertData(data)
                
                // Add to realtime list
                realtimeList.add(data)
                if (realtimeList.size > 100) {
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
}
