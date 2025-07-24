package com.airscout.airscout32.fragments

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.airscout.airscout32.R
import com.airscout.airscout32.databinding.FragmentRealtimeBinding
import com.airscout.airscout32.viewmodel.AirDataViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RealtimeFragment : Fragment() {
    
    private var _binding: FragmentRealtimeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AirDataViewModel
    private val temperatureEntries = mutableListOf<Entry>()
    private val humidityEntries = mutableListOf<Entry>()
    private val co2Entries = mutableListOf<Entry>()
    private val vocEntries = mutableListOf<Entry>()
    private var sessionStartTime: Long = 0
    
    // Battery notification
    private var lastBatteryLevel = 100f
    private var lowBatteryNotificationShown = false
    private val CHANNEL_ID = "battery_alerts"
    private val NOTIFICATION_ID = 1001
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRealtimeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[AirDataViewModel::class.java]
        
        createNotificationChannel()
        setupCharts()
        setupBluetoothButton()
        setupActionButtons()
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
            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            legend.textColor = Color.WHITE
        }
        
        // Humidity Chart
        binding.humidityChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            legend.textColor = Color.WHITE
        }
        
        // Gas1 Chart (CO2)
        binding.co2Chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            legend.textColor = Color.WHITE
        }
        
        // VOC Chart
        binding.vocChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            legend.textColor = Color.WHITE
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
    
    private fun setupActionButtons() {
        binding.btnSaveSession.setOnClickListener {
            showSaveSessionDialog()
        }
        
        binding.btnClearData.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Clear Data")
                .setMessage("Clear current session data? This will not affect saved sessions.")
                .setPositiveButton("Clear") { _, _ ->
                    viewModel.clearCurrentSession()
                    sessionStartTime = System.currentTimeMillis()
                }
                .setNegativeButton("Cancel", null)
                .show()
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
    
    private fun showSaveSessionDialog() {
        val sessionDataCount = viewModel.getCurrentSessionDataCount()
        if (sessionDataCount == 0) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("No Data")
                .setMessage("No data to save. Start collecting data first.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        
        val input = android.widget.EditText(requireContext())
        input.hint = "Session name"
        val defaultName = "Session ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())}"
        input.setText(defaultName)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Save Session")
            .setMessage("Save $sessionDataCount data points as session:")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val sessionName = input.text.toString().trim()
                if (sessionName.isNotEmpty()) {
                    viewModel.saveCurrentSession(sessionName)
                    sessionStartTime = System.currentTimeMillis()
                    Toast.makeText(requireContext(), "Session saved: $sessionName ($sessionDataCount points)", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
            val chartLimit = viewModel.getChartDataLimit()
            val sessionDataCount = viewModel.getCurrentSessionDataCount()
            
            // Show both chart data count and full session data count
            binding.tvDataCount.text = "Chart: ${data.size}/$chartLimit | Session: $sessionDataCount data points"
            
            if (data.isNotEmpty()) {
                if (sessionStartTime == 0L) {
                    sessionStartTime = data.first().timestamp
                }
                val latest = data.last()
                updateCurrentValues(latest)
                updateCharts(data)
            }
        }
    }
    
    private fun updateCurrentValues(data: com.airscout.airscout32.data.AirSensorData) {
        binding.currentTemperature.text = "${String.format("%.1f", data.temperature)}°C"
        binding.currentHumidity.text = "${String.format("%.1f", data.humidity)}%"
        binding.currentGas1.text = "${String.format("%.0f", data.gas1)} ppm"
        binding.currentGas2.text = "${String.format("%.0f", data.gas2)} ppm"
        
        // Update battery indicator and check for low battery
        val batteryLevel = data.battery.toFloat()
        binding.batteryIndicator.setBatteryLevel(batteryLevel)
        checkBatteryLevel(batteryLevel)
    }
    
    private fun updateCharts(dataList: List<com.airscout.airscout32.data.AirSensorData>) {
        temperatureEntries.clear()
        humidityEntries.clear()
        co2Entries.clear()
        vocEntries.clear()
        
        dataList.forEachIndexed { index, data ->
            temperatureEntries.add(Entry(index.toFloat(), data.temperature.toFloat()))
            humidityEntries.add(Entry(index.toFloat(), data.humidity.toFloat()))
            co2Entries.add(Entry(index.toFloat(), data.gas1.toFloat()))
            vocEntries.add(Entry(index.toFloat(), data.gas2.toFloat()))
        }
        
        updateChart(binding.temperatureChart, temperatureEntries, "Temperature (°C)", Color.RED)
        updateChart(binding.humidityChart, humidityEntries, "Humidity (%)", Color.BLUE)
        updateChart(binding.co2Chart, co2Entries, "CO2 (ppm)", Color.GREEN)
        updateChart(binding.vocChart, vocEntries, "VOC (ppm)", Color.YELLOW)
    }
    
    private fun updateChart(chart: com.github.mikephil.charting.charts.LineChart, entries: List<Entry>, label: String, color: Int) {
        val dataSet = LineDataSet(entries, label).apply {
            this.color = color
            setCircleColor(color)
            lineWidth = 2f
            circleRadius = 3f
            setDrawCircleHole(false)
            valueTextSize = 9f
            valueTextColor = Color.WHITE
        }
        
        chart.data = LineData(dataSet)
        chart.invalidate()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Battery Alerts"
            val descriptionText = "Notifications for low battery levels on measurement device"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
                lightColor = Color.RED
                setSound(null, null) // Use default notification sound
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun checkBatteryLevel(currentLevel: Float) {
        // Check if battery dropped to 20% or below
        if (currentLevel <= 20f && lastBatteryLevel > 20f && !lowBatteryNotificationShown) {
            Log.w("RealtimeFragment", "Battery level critical: ${currentLevel}% (was ${lastBatteryLevel}%)")
            showLowBatteryAlert(currentLevel)
            showLowBatteryNotification(currentLevel)
            lowBatteryNotificationShown = true
        }
        
        // Reset notification flag if battery goes above 25% (hysteresis)
        if (currentLevel > 25f && lowBatteryNotificationShown) {
            Log.i("RealtimeFragment", "Battery level recovered: ${currentLevel}% - notifications re-enabled")
            lowBatteryNotificationShown = false
        }
        
        lastBatteryLevel = currentLevel
    }
    
    private fun showLowBatteryAlert(batteryLevel: Float) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Low Battery Warning")
            .setMessage("Warning! The measurement device battery has dropped to ${batteryLevel.toInt()}%.\n\nPlease charge the device soon to avoid data loss.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Disable Notifications") { dialog, _ ->
                lowBatteryNotificationShown = true // Disable further notifications for this session
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }
    
    private fun showLowBatteryNotification(batteryLevel: Float) {
        try {
            val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_error) // Default Android warning icon
                .setContentTitle("AirScout32 - Low Battery Alert")
                .setContentText("Device Battery: ${batteryLevel.toInt()}% - Please charge!")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Measurement device battery is critically low at ${batteryLevel.toInt()}%. Please charge the device to avoid interruption of data collection."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibration, lights
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500, 250, 500)) // Custom vibration pattern
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show on lock screen
                .setOngoing(false) // Allow dismissal
                .setTimeoutAfter(30000) // Auto-dismiss after 30 seconds if not interacted with
            
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(requireContext()).notify(NOTIFICATION_ID, builder.build())
                Log.d("RealtimeFragment", "Low battery notification sent: ${batteryLevel.toInt()}%")
            } else {
                Log.w("RealtimeFragment", "Notification permission not granted")
            }
        } catch (e: Exception) {
            Log.e("RealtimeFragment", "Error showing notification", e)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
