package com.airscout.airscout32.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import com.airscout.airscout32.data.AirSensorData
import com.airscout.airscout32.utils.JsonMappingConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

data class TimestampedSensorData(
    val timestamp: Long,
    val data: AirSensorData
)

class BluetoothService(private val context: Context) {
    
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var isConnected = false
    private val gson = Gson()
    
    // Data storage
    private val sensorDataHistory = mutableListOf<TimestampedSensorData>()
    private val historyFile = File(context.filesDir, "sensor_data_history.json")
    
    private val _dataFlow = MutableSharedFlow<AirSensorData>()
    val dataFlow: SharedFlow<AirSensorData> = _dataFlow
    
    private val _connectionStateFlow = MutableSharedFlow<Boolean>()
    val connectionStateFlow: SharedFlow<Boolean> = _connectionStateFlow
    
    private val jsonMappingConfig = JsonMappingConfig(context)
    
    init {
        loadHistoryFromFile()
    }
    
    fun getPairedDevices(): Set<BluetoothDevice>? {
        return bluetoothAdapter?.bondedDevices
    }
    
    fun connectToDevice(device: BluetoothDevice): Boolean {
        return try {
            Log.d("BluetoothService", "Attempting to connect to ${device.address}")
            disconnect() // Schließe vorherige Verbindung
            
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            isConnected = true
            
            Log.d("BluetoothService", "Connected successfully")
            startListening()
            
            CoroutineScope(Dispatchers.Main).launch {
                _connectionStateFlow.emit(true)
            }
            
            true
        } catch (e: IOException) {
            Log.e("BluetoothService", "Connection failed: ${e.message}", e)
            isConnected = false
            CoroutineScope(Dispatchers.Main).launch {
                _connectionStateFlow.emit(false)
            }
            false
        } catch (e: SecurityException) {
            Log.e("BluetoothService", "Permission denied: ${e.message}", e)
            false
        } catch (e: Exception) {
            Log.e("BluetoothService", "Unexpected error: ${e.message}", e)
            false
        }
    }
    
    private fun startListening() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = bluetoothSocket?.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                
                Log.d("BluetoothService", "Started listening for data")
                
                while (isConnected && bluetoothSocket?.isConnected == true) {
                    val jsonString = reader.readLine()
                    if (jsonString != null && jsonString.isNotEmpty()) {
                        try {
                            Log.d("BluetoothService", "Received: $jsonString")
                            
                            // Use dynamic JSON parsing
                            val sensorData = jsonMappingConfig.parseJsonWithMapping(jsonString)
                            
                            if (sensorData != null) {
                                // Store data with timestamp
                                val timestampedData = TimestampedSensorData(
                                    timestamp = System.currentTimeMillis(),
                                    data = sensorData
                                )
                                sensorDataHistory.add(timestampedData)
                                saveHistoryToFile()
                                
                                _dataFlow.emit(sensorData)
                            } else {
                                Log.w("BluetoothService", "Could not parse JSON with current mapping: $jsonString")
                            }
                        } catch (e: Exception) {
                            Log.e("BluetoothService", "JSON parsing error: ${e.message}", e)
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("BluetoothService", "Reading error: ${e.message}", e)
                disconnect()
            } catch (e: Exception) {
                Log.e("BluetoothService", "Unexpected listening error: ${e.message}", e)
                disconnect()
            }
        }
    }
    
    fun disconnect() {
        isConnected = false
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null
        } catch (e: IOException) {
            Log.e("BluetoothService", "Disconnect error: ${e.message}", e)
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            _connectionStateFlow.emit(false)
        }
    }
    
    fun isConnected(): Boolean = isConnected
    
    // Storage and history functions
    fun getDataHistory(): List<TimestampedSensorData> {
        return sensorDataHistory.toList()
    }
    
    fun clearHistory() {
        sensorDataHistory.clear()
        saveHistoryToFile()
        Log.d("BluetoothService", "Data history cleared")
    }
    
    fun exportToCSV(): File? {
        return try {
            val csvFile = File(context.getExternalFilesDir(null), "airscout_data_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv")
            val writer = FileWriter(csvFile)
            
            // CSV Header
            writer.append("Timestamp,Date,Time,Temperature,Humidity,Gas1,Gas2,Battery\n")
            
            // Data rows
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            
            sensorDataHistory.forEach { timestampedData ->
                val date = Date(timestampedData.timestamp)
                writer.append("${timestampedData.timestamp},")
                writer.append("${dateFormat.format(date)},")
                writer.append("${timeFormat.format(date)},")
                writer.append("${timestampedData.data.temperature},")
                writer.append("${timestampedData.data.humidity},")
                writer.append("${timestampedData.data.gas1},")
                writer.append("${timestampedData.data.gas2},")
                writer.append("${timestampedData.data.battery}\n")
            }
            
            writer.close()
            csvFile
        } catch (e: Exception) {
            Log.e("BluetoothService", "CSV export error: ${e.message}", e)
            null
        }
    }
    
    private fun saveHistoryToFile() {
        try {
            val json = gson.toJson(sensorDataHistory)
            historyFile.writeText(json)
        } catch (e: Exception) {
            Log.e("BluetoothService", "Error saving history: ${e.message}", e)
        }
    }
    
    private fun loadHistoryFromFile() {
        try {
            if (historyFile.exists()) {
                val json = historyFile.readText()
                val type = object : TypeToken<MutableList<TimestampedSensorData>>() {}.type
                val loadedHistory = gson.fromJson<MutableList<TimestampedSensorData>>(json, type)
                sensorDataHistory.clear()
                sensorDataHistory.addAll(loadedHistory)
                Log.d("BluetoothService", "Loaded ${sensorDataHistory.size} history entries")
            }
        } catch (e: Exception) {
            Log.e("BluetoothService", "Error loading history: ${e.message}", e)
        }
    }
}
