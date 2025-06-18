package com.airscout.airscout32.fragments

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
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
    private val pm25Entries = mutableListOf<Entry>()
    
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
        
        // PM2.5 Chart
        binding.pm25Chart.apply {
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
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                1
            )
            return
        }
        
        val devices = viewModel.getPairedDevices()
        if (devices.isNullOrEmpty()) {
            return
        }
        
        val deviceNames = devices.map { "${it.name} (${it.address})" }.toTypedArray()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Bluetooth Device")
            .setItems(deviceNames) { _, which ->
                val selectedDevice = devices.elementAt(which)
                viewModel.connectToDevice(selectedDevice)
            }
            .show()
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
        binding.currentTemperature.text = "${data.temperature.toInt()}°C"
        binding.currentHumidity.text = "${data.humidity.toInt()}%"
        binding.currentPm25.text = "${data.pm25.toInt()}μg/m³"
        binding.currentCo2.text = "${data.co2.toInt()}ppm"
    }
    
    private fun updateCharts(dataList: List<com.airscout.airscout32.data.AirSensorData>) {
        temperatureEntries.clear()
        humidityEntries.clear()
        pm25Entries.clear()
        
        dataList.forEachIndexed { index, data ->
            temperatureEntries.add(Entry(index.toFloat(), data.temperature.toFloat()))
            humidityEntries.add(Entry(index.toFloat(), data.humidity.toFloat()))
            pm25Entries.add(Entry(index.toFloat(), data.pm25.toFloat()))
        }
        
        updateChart(binding.temperatureChart, temperatureEntries, "Temperature", Color.RED)
        updateChart(binding.humidityChart, humidityEntries, "Humidity", Color.BLUE)
        updateChart(binding.pm25Chart, pm25Entries, "PM2.5", Color.GREEN)
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
