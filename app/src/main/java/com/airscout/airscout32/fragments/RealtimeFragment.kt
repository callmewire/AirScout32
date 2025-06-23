package com.airscout.airscout32.fragments

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.airscout.airscout32.databinding.FragmentRealtimeBinding
import com.airscout.airscout32.viewmodel.AirDataViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch

class RealtimeFragment : Fragment() {
    
    private var _binding: FragmentRealtimeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AirDataViewModel
    private val temperatureEntries = mutableListOf<Entry>()
    private val humidityEntries = mutableListOf<Entry>()
    private val gas1Entries = mutableListOf<Entry>()
    private val batteryEntries = mutableListOf<Entry>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRealtimeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[AirDataViewModel::class.java]
        
        setupCharts()
        setupBluetoothButton()
        observeData()
    }
    
    private fun setupCharts() {
        // Temperature Chart
        binding.temperatureChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
        }
        
        // Humidity Chart
        binding.humidityChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
        }
        
        // Gas1 Chart
        binding.gas1Chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
        }
        
        // Battery Chart
        binding.batteryChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
        }
    }
    
    private fun setupBluetoothButton() {
        binding.bluetoothButton.setOnClickListener {
            if (viewModel.isConnected()) {
                viewModel.disconnect()
            } else {
                showDeviceSelectionDialog()
            }
        }
        
        viewModel.connectionState.observe(viewLifecycleOwner) { isConnected ->
            binding.bluetoothButton.text = if (isConnected) "Disconnect" else "Connect"
            binding.connectionStatus.text = if (isConnected) "Connected" else "Disconnected"
        }
    }
    
    private fun showDeviceSelectionDialog() {
        // Prüfe Permissions
        val missingPermissions = mutableListOf<String>()
        
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        
        if (missingPermissions.isNotEmpty()) {
            requestPermissions(missingPermissions.toTypedArray(), 1001)
            return
        }
        
        try {
            val devices = viewModel.getPairedDevices()
            if (devices.isNullOrEmpty()) {
                // Zeige Nachricht, dass keine Geräte gefunden wurden
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("No Devices")
                    .setMessage("No paired Bluetooth devices found. Please pair your sensor first in Android Settings.")
                    .setPositiveButton("OK", null)
                    .show()
                return
            }
            
            val deviceNames = devices.map { 
                val name = if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    it.name ?: "Unknown"
                } else {
                    "Unknown"
                }
                "$name (${it.address})"
            }.toTypedArray()
            
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Bluetooth Device")
                .setItems(deviceNames) { _, which ->
                    val selectedDevice = devices.elementAt(which)
                    try {
                        Log.d("RealtimeFragment", "Connecting to device: ${selectedDevice.address}")
                        viewModel.connectToDevice(selectedDevice)
                    } catch (e: Exception) {
                        Log.e("RealtimeFragment", "Error connecting: ${e.message}", e)
                        androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Connection Error")
                            .setMessage("Error connecting to device: ${e.message}")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && 
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            showDeviceSelectionDialog()
        }
    }
    
    private fun observeData() {
        viewModel.realtimeData.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                val latest = data.last()
                updateCurrentValues(latest)
                updateCharts(data)
            }
        }
    }
    
    private fun updateCurrentValues(data: com.airscout.airscout32.data.AirSensorData) {
        binding.currentTemperature.text = "${String.format("%.1f", data.temperature)}°C"
        binding.currentHumidity.text = "${String.format("%.1f", data.humidity)}%"
        binding.currentGas1.text = String.format("%.0f", data.gas1)
        binding.currentBattery.text = "${String.format("%.2f", data.battery)}V"
    }
    
    private fun updateCharts(dataList: List<com.airscout.airscout32.data.AirSensorData>) {
        temperatureEntries.clear()
        humidityEntries.clear()
        gas1Entries.clear()
        batteryEntries.clear()
        
        dataList.forEachIndexed { index, data ->
            temperatureEntries.add(Entry(index.toFloat(), data.temperature.toFloat()))
            humidityEntries.add(Entry(index.toFloat(), data.humidity.toFloat()))
            gas1Entries.add(Entry(index.toFloat(), data.gas1.toFloat()))
            batteryEntries.add(Entry(index.toFloat(), data.battery.toFloat()))
        }
        
        updateChart(binding.temperatureChart, temperatureEntries, "Temperature", Color.RED)
        updateChart(binding.humidityChart, humidityEntries, "Humidity", Color.BLUE)
        updateChart(binding.gas1Chart, gas1Entries, "Gas1", Color.GREEN)
        updateChart(binding.batteryChart, batteryEntries, "Battery", Color.MAGENTA)
    }
    
    private fun updateChart(chart: com.github.mikephil.charting.charts.LineChart, entries: List<Entry>, label: String, color: Int) {
        val dataSet = LineDataSet(entries, label).apply {
            this.color = color
            setCircleColor(color)
            lineWidth = 2f
            circleRadius = 3f
            setDrawCircleHole(false)
            valueTextSize = 9f
        }
        
        chart.data = LineData(dataSet)
        chart.invalidate()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
