package com.airscout.airscout32.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airscout.airscout32.databinding.FragmentSettingsBinding
import com.airscout.airscout32.utils.ChartSettingsConfig
import com.airscout.airscout32.utils.JsonKeyMapping
import com.airscout.airscout32.utils.JsonMappingConfig
import com.airscout.airscout32.viewmodel.AirDataViewModel

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AirDataViewModel
    private lateinit var jsonMappingConfig: JsonMappingConfig
    private lateinit var chartSettingsConfig: ChartSettingsConfig

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[AirDataViewModel::class.java]
        jsonMappingConfig = JsonMappingConfig(requireContext())
        chartSettingsConfig = ChartSettingsConfig(requireContext())
        
        loadCurrentMapping()
        loadChartSettings()
        setupButtons()
    }
    
    private fun loadCurrentMapping() {
        val mapping = jsonMappingConfig.loadMapping()
        binding.etTempKey.setText(mapping.temperatureKey)
        binding.etHumidityKey.setText(mapping.humidityKey)
        binding.etGas1Key.setText(mapping.gas1Key)
        binding.etGas2Key.setText(mapping.gas2Key)
        binding.etBatteryKey.setText(mapping.batteryKey)
    }
    
    private fun loadChartSettings() {
        val chartLimit = chartSettingsConfig.getChartDataLimit()
        binding.etChartLimit.setText(chartLimit.toString())
    }
    
    private fun setupButtons() {
        binding.btnSaveMapping.setOnClickListener {
            saveMapping()
        }
        
        binding.btnResetMapping.setOnClickListener {
            resetMapping()
        }
        
        binding.btnSaveChartSettings.setOnClickListener {
            saveChartSettings()
        }
        
        binding.btnResetCurrent.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Aktuelle Werte zurücksetzen")
                .setMessage("Möchten Sie die aktuell angezeigten Werte wirklich zurücksetzen?")
                .setPositiveButton("Zurücksetzen") { _, _ ->
                    viewModel.resetCurrentValues()
                    Toast.makeText(requireContext(), "Aktuelle Werte zurückgesetzt", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Abbrechen", null)
                .show()
        }
    }
    
    private fun saveMapping() {
        val mapping = JsonKeyMapping(
            temperatureKey = binding.etTempKey.text.toString().trim(),
            humidityKey = binding.etHumidityKey.text.toString().trim(),
            gas1Key = binding.etGas1Key.text.toString().trim(),
            gas2Key = binding.etGas2Key.text.toString().trim(),
            batteryKey = binding.etBatteryKey.text.toString().trim()
        )
        
        // Validate that no field is empty
        if (mapping.temperatureKey.isEmpty() || mapping.humidityKey.isEmpty() || 
            mapping.gas1Key.isEmpty() || mapping.gas2Key.isEmpty() || 
            mapping.batteryKey.isEmpty()) {
            Toast.makeText(requireContext(), "Alle Felder müssen ausgefüllt sein", Toast.LENGTH_SHORT).show()
            return
        }
        
        jsonMappingConfig.saveMapping(mapping)
        viewModel.updateJsonMapping()
        
        Toast.makeText(requireContext(), "JSON Mapping gespeichert", Toast.LENGTH_SHORT).show()
    }
    
    private fun resetMapping() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Mapping zurücksetzen")
            .setMessage("Möchten Sie die JSON-Mapping-Konfiguration auf die Standardwerte zurücksetzen?")
            .setPositiveButton("Zurücksetzen") { _, _ ->
                val defaultMapping = JsonKeyMapping()
                jsonMappingConfig.saveMapping(defaultMapping)
                loadCurrentMapping()
                viewModel.updateJsonMapping()
                Toast.makeText(requireContext(), "Mapping zurückgesetzt", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }
    
    private fun saveChartSettings() {
        val limitText = binding.etChartLimit.text.toString().trim()
        
        if (limitText.isEmpty()) {
            Toast.makeText(requireContext(), "Bitte eine Zahl eingeben", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val limit = limitText.toInt()
            if (limit < 10) {
                Toast.makeText(requireContext(), "Minimum 10 Datenpunkte erforderlich", Toast.LENGTH_SHORT).show()
                return
            }
            if (limit > 1000) {
                Toast.makeText(requireContext(), "Maximum 1000 Datenpunkte erlaubt", Toast.LENGTH_SHORT).show()
                return
            }
            
            chartSettingsConfig.saveChartDataLimit(limit)
            viewModel.updateChartDataLimit(limit)
            
            Toast.makeText(requireContext(), "Diagramm Einstellungen gespeichert", Toast.LENGTH_SHORT).show()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Ungültige Zahl", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
