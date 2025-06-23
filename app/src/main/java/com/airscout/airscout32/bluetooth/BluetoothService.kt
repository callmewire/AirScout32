package com.airscout.airscout32.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.airscout.airscout32.data.AirSensorData
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class BluetoothService {
    
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var isConnected = false
    private val gson = Gson()
    
    private val _dataFlow = MutableSharedFlow<AirSensorData>()
    val dataFlow: SharedFlow<AirSensorData> = _dataFlow
    
    private val _connectionStateFlow = MutableSharedFlow<Boolean>()
    val connectionStateFlow: SharedFlow<Boolean> = _connectionStateFlow
    
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
                            val sensorData = gson.fromJson(jsonString, AirSensorData::class.java)
                            _dataFlow.emit(sensorData)
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
}
